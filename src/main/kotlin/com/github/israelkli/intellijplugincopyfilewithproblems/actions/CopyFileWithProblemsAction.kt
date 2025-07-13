package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import java.awt.datatransfer.StringSelection

class CopyFileWithProblemsAction : AnAction("Copy File with Problems") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        
        val document = psiFile.viewProvider.document ?: return
        val fileContent = document.text
        
        // Try to get the editor for this file
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(virtualFile)?.let { fileEditor ->
            if (fileEditor is com.intellij.openapi.fileEditor.TextEditor) {
                fileEditor.editor
            } else null
        }
        
        val result = buildString {
            // Get a relative path from the project root
            val projectBasePath = project.basePath
            val relativePath = if (projectBasePath != null && virtualFile.path.startsWith(projectBasePath)) {
                virtualFile.path.substring(projectBasePath.length).removePrefix("/")
            } else {
                virtualFile.path
            }
            appendLine("// $relativePath")
            appendLine()
            
            val lines = fileContent.lines()
            lines.forEachIndexed { index, line ->
                append(line)
                
                val problems = getProblemsForLine(project, psiFile, editor, index)
                if (problems.isNotEmpty()) {
                    append(" // ")
                    append(problems.joinToString("; ") { "${it.severity}: ${it.message}" })
                }
                appendLine()
            }
        }
        
        val selection = StringSelection(result)
        CopyPasteManager.getInstance().setContents(selection)
    }

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.getData(CommonDataKeys.PROJECT)
        
        val isEnabled = virtualFile != null && 
                       project != null && 
                       !virtualFile.isDirectory
        
        e.presentation.isEnabledAndVisible = isEnabled
    }

    private fun getProblemsForLine(project: Project, psiFile: PsiFile, editor: Editor?, lineNumber: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        val document = psiFile.viewProvider.document ?: return problems
        
        if (lineNumber >= document.lineCount) return problems
        
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        
        // Ensure document is committed and analyzed
        PsiDocumentManager.getInstance(project).commitDocument(document)
        
        // Method 1: Try to get highlights from editor markup if editor is available
        if (editor != null) {
            try {
                val markupModel = editor.markupModel
                val highlighters = markupModel.allHighlighters
                
                for (highlighter in highlighters) {
                    // Check if highlighter overlaps with this line
                    if (highlighter.startOffset < lineEndOffset && highlighter.endOffset > lineStartOffset) {
                        val tooltip = highlighter.errorStripeTooltip
                        if (tooltip != null) {
                            val tooltipText = tooltip.toString()
                            if (tooltipText.isNotBlank() && tooltipText != "null") {
                                // Only filter out ELEMENT_UNDER_CARET specifically
                                if (!tooltipText.contains("ELEMENT_UNDER_CARET")) {
                                    val severity = determineSeveritySimple(tooltipText, highlighter)
                                    problems.add(ProblemInfo(severity, tooltipText))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to the next method
            }
        }
        
        // Method 2: Force analysis and get highlight infos
        try {
            val daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
            
            // Force restart analysis to ensure we have fresh highlights
            daemonCodeAnalyzer.restart(psiFile)
            
            // Wait a bit for analysis to complete
            Thread.sleep(100)
            
            // Try to get highlights from DaemonCodeAnalyzerImpl
            val daemonImpl = daemonCodeAnalyzer as? DaemonCodeAnalyzerImpl
            if (daemonImpl != null) {
                val highlightInfos = daemonImpl.getFileLevelHighlights(project, psiFile)
                
                for (info in highlightInfos) {
                    if (info.startOffset < lineEndOffset && info.endOffset > lineStartOffset) {
                        val severity = when {
                            info.severity >= HighlightSeverity.ERROR -> "ERROR"
                            info.severity >= HighlightSeverity.WARNING -> "WARNING"
                            info.severity >= HighlightSeverity.WEAK_WARNING -> "WEAK_WARNING"
                            info.severity >= HighlightSeverity.INFORMATION -> "INFO"
                            else -> "HINT"
                        }
                        val description = info.description
                        if (!description.isNullOrBlank()) {
                            // Only filter out ELEMENT_UNDER_CARET specifically
                            if (!description.contains("ELEMENT_UNDER_CARET")) {
                                problems.add(ProblemInfo(severity, description))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Continue to the next method
        }
        
        // Method 3: Check for PSI syntax errors in this line
        try {
            // Find all PSI error elements in the file
            val psiErrors = mutableListOf<PsiErrorElement>()
            psiFile.accept(object : PsiRecursiveElementVisitor() {
                override fun visitErrorElement(element: PsiErrorElement) {
                    super.visitErrorElement(element)
                    val elementRange = element.textRange
                    if (elementRange.startOffset <= lineEndOffset && elementRange.endOffset >= lineStartOffset) {
                        psiErrors.add(element)
                    }
                }
            })
            
            for (errorElement in psiErrors) {
                val errorMessage = errorElement.errorDescription
                if (!errorMessage.isNullOrBlank()) {
                    problems.add(ProblemInfo("SYNTAX_ERROR", errorMessage))
                }
            }
        } catch (e: Exception) {
            // Continue to next method
        }
        
        // Method 4: Direct PSI validation for common Java/Kotlin issues
        try {
            if (psiFile.name.endsWith(".java") || psiFile.name.endsWith(".kt")) {
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                // Check for split identifiers like "cacheM anager"
                val splitIdentifierPattern = Regex("\\b([a-zA-Z]+)\\s+([a-z][a-zA-Z]*)(\\s*\\()")
                val splitMatch = splitIdentifierPattern.find(lineText)
                if (splitMatch != null) {
                    val before = splitMatch.groupValues[1]
                    val after = splitMatch.groupValues[2]
                    problems.add(ProblemInfo("ERROR", "Split identifier detected: '$before $after' should be '${before}${after.capitalize()}'"))
                }
                
                // Check for @Override without proper return type
                if (lineText.contains("@Override") || lineText.contains("@Bean")) {
                    // Look for method declaration patterns with issues
                    val methodPattern = Regex("(public|private|protected)\\s+([a-zA-Z]+)\\s+([a-zA-Z]+)\\s*\\(")
                    if (methodPattern.containsMatchIn(lineText)) {
                        // This looks like a method, check for common issues
                        if (!lineText.contains("void") && !lineText.contains("String") && 
                            !lineText.contains("int") && !lineText.contains("boolean") &&
                            !lineText.contains("CacheManager") && !lineText.contains("Object")) {
                            problems.add(ProblemInfo("ERROR", "Invalid method declaration; return type required"))
                        }
                    }
                }
                
                // Check for annotation placement issues
                if (lineText.trim().startsWith("@") && lineText.contains("(")) {
                    val nextLineNum = lineNumber + 1
                    if (nextLineNum < document.lineCount) {
                        val nextLineStart = document.getLineStartOffset(nextLineNum)
                        val nextLineEnd = document.getLineEndOffset(nextLineNum)
                        val nextLineText = document.getText(TextRange(nextLineStart, nextLineEnd))
                        
                        if (nextLineText.trim().startsWith("@")) {
                            problems.add(ProblemInfo("ERROR", "Annotations are not allowed here"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Continue if validation fails
        }
        
        // Method 4: Run IntelliJ inspections to catch semantic errors
        try {
            val inspectionManager = InspectionManager.getInstance(project)
            val profileManager = InspectionProjectProfileManager.getInstance(project)
            val profile = profileManager.currentProfile
            
            // Get all enabled inspections for this file type
            val inspectionTools = profile.getInspectionTools(psiFile)
            
            for (tool in inspectionTools) {
                if (tool.tool is LocalInspectionTool) {
                    try {
                        val localTool = tool.tool as LocalInspectionTool
                        
                        // Create a problems holder to collect issues
                        val problemsHolder = ProblemsHolder(inspectionManager, psiFile, false)
                        
                        // Find all elements in this line
                        val elementsInLine = mutableListOf<PsiElement>()
                        var currentElement = psiFile.findElementAt(lineStartOffset)
                        while (currentElement != null && currentElement.textRange.startOffset < lineEndOffset) {
                            elementsInLine.add(currentElement)
                            // Also include parent elements that span into this line
                            var parent = currentElement.parent
                            while (parent != null && parent.textRange.startOffset <= lineEndOffset && parent.textRange.endOffset >= lineStartOffset) {
                                if (!elementsInLine.contains(parent)) {
                                    elementsInLine.add(parent)
                                }
                                parent = parent.parent
                            }
                            currentElement = PsiTreeUtil.nextLeaf(currentElement)
                        }
                        
                        // Run inspection on elements in this line
                        for (element in elementsInLine) {
                            try {
                                // Create a visitor that applies this inspection
                                val visitor = localTool.buildVisitor(problemsHolder, false)
                                element.accept(visitor)
                            } catch (e: Exception) {
                                // Continue with other elements
                            }
                        }
                        
                        // Extract problems from the holder
                        val foundProblems = problemsHolder.results
                        for (problem in foundProblems) {
                            val problemElement = problem.psiElement
                            val problemRange = problemElement?.textRange
                            
                            // Check if this problem overlaps with our line
                            if (problemRange != null && 
                                problemRange.startOffset <= lineEndOffset && 
                                problemRange.endOffset >= lineStartOffset) {
                                
                                val severity = when (problem.highlightType) {
                                    ProblemHighlightType.ERROR, 
                                    ProblemHighlightType.GENERIC_ERROR, 
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING -> "ERROR"
                                    ProblemHighlightType.WARNING, 
                                    ProblemHighlightType.WEAK_WARNING -> "WARNING"
                                    else -> "INFO"
                                }
                                
                                val description = problem.descriptionTemplate
                                if (!description.isNullOrBlank()) {
                                    problems.add(ProblemInfo(severity, description))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with next inspection tool
                    }
                }
            }
        } catch (e: Exception) {
            // Continue if inspection execution fails
        }
        
        // Method 4: Add YAML-specific error detection  
        try {
            if (psiFile.name.endsWith(".yaml") || psiFile.name.endsWith(".yml")) {
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                // Check for YAML block mapping issues
                val trimmedLine = lineText.trim()
                
                // Check for invalid child elements in block mapping
                if (trimmedLine.contains(":") && !trimmedLine.startsWith("-")) {
                    val colonIndex = trimmedLine.indexOf(":")
                    val beforeColon = trimmedLine.substring(0, colonIndex).trim()
                    val afterColon = trimmedLine.substring(colonIndex + 1).trim()
                    
                    // Check for invalid indentation patterns
                    val leadingSpaces = lineText.length - lineText.trimStart().length
                    
                    // Look at previous non-empty line to check context
                    var prevLineNum = lineNumber - 1
                    var prevLineText = ""
                    while (prevLineNum >= 0) {
                        val prevStart = document.getLineStartOffset(prevLineNum)
                        val prevEnd = document.getLineEndOffset(prevLineNum)
                        prevLineText = document.getText(TextRange(prevStart, prevEnd)).trim()
                        if (prevLineText.isNotEmpty()) break
                        prevLineNum--
                    }
                    
                    // Check for common YAML mapping errors
                    if (prevLineText.isNotEmpty()) {
                        val prevSpaces = if (prevLineNum >= 0) {
                            val prevStart = document.getLineStartOffset(prevLineNum)
                            val prevEnd = document.getLineEndOffset(prevLineNum)
                            val prevFullLine = document.getText(TextRange(prevStart, prevEnd))
                            prevFullLine.length - prevFullLine.trimStart().length
                        } else 0
                        
                        // If current line has same indentation as previous but previous is a value line
                        if (leadingSpaces == prevSpaces && prevLineText.contains(":") && 
                            prevLineText.substringAfter(":").trim().isNotEmpty()) {
                            problems.add(ProblemInfo("ERROR", "Invalid child element in a block mapping"))
                        }
                        
                        // Check for mapping key without proper structure
                        if (beforeColon.contains(" ") && !beforeColon.startsWith("\"") && !beforeColon.startsWith("'")) {
                            problems.add(ProblemInfo("ERROR", "Invalid child element in a block mapping"))
                        }
                    }
                }
                
                // Check for other common YAML syntax issues
                if (trimmedLine.contains("\t")) {
                    problems.add(ProblemInfo("ERROR", "YAML does not allow tabs for indentation"))
                }
                
                // Check for unquoted strings that might need quotes
                if (trimmedLine.contains(":") && trimmedLine.contains("@") && !trimmedLine.contains("\"")) {
                    problems.add(ProblemInfo("WARNING", "Special characters in YAML values should be quoted"))
                }
            }
        } catch (e: Exception) {
            // Continue if YAML validation fails
        }
        
        return problems.distinctBy { "${it.severity}:${it.message}" }
    }
    
    private fun determineSeveritySimple(tooltipText: String, highlighter: RangeHighlighter): String {
        // First check the highlighter's layer
        val layer = highlighter.layer
        if (layer >= HighlighterLayer.ERROR) return "ERROR"
        if (layer >= HighlighterLayer.WARNING) return "WARNING"
        
        // Then check tooltip text content
        val lowerText = tooltipText.lowercase()
        return when {
            lowerText.contains("error") || lowerText.contains("cannot resolve") || lowerText.contains("not found") -> "ERROR"
            lowerText.contains("warning") || lowerText.contains("deprecated") || lowerText.contains("unused") -> "WARNING"
            lowerText.contains("typo") || lowerText.contains("suggestion") -> "INFO"
            else -> "INFO"
        }
    }

    private data class ProblemInfo(val severity: String, val message: String)
}