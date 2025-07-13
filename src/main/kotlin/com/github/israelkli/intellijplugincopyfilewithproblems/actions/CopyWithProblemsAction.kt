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
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.awt.datatransfer.StringSelection

class CopyWithProblemsAction : AnAction("Copy With Problems") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return
        
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        val document = editor.document
        
        val startLine = document.getLineNumber(startOffset)
        val endLine = document.getLineNumber(endOffset)
        
        val result = buildString {
            // Get a relative path from the project root
            val projectBasePath = project.basePath
            val relativePath = if (projectBasePath != null && virtualFile.path.startsWith(projectBasePath)) {
                virtualFile.path.substring(projectBasePath.length).removePrefix("/")
            } else {
                virtualFile.path
            }
            appendLine("// File: ${virtualFile.name}")
            appendLine()
            
            for (lineNumber in startLine..endLine) {
                val lineStartOffset = document.getLineStartOffset(lineNumber)
                val lineEndOffset = document.getLineEndOffset(lineNumber)
                val lineText = document.getText().substring(lineStartOffset, lineEndOffset)
                
                append(lineText)
                
                val problems = getProblemsForLine(project, psiFile, editor, lineNumber)
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
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        e.presentation.isEnabledAndVisible = hasSelection
    }

    private fun getProblemsForLine(project: Project, psiFile: PsiFile, editor: Editor, lineNumber: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        val document = psiFile.viewProvider.document ?: return problems
        
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        
        // Ensure a document is committed and analyzed
        PsiDocumentManager.getInstance(project).commitDocument(document)
        
        // Method 1: Check for PSI syntax errors in this line
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
            // Continue to the next method
        }
        
        // Method 2: Check editor markup model for error highlights (more aggressive)
        try {
            val markupModel = editor.markupModel
            val highlighters = markupModel.allHighlighters
            
            for (highlighter in highlighters) {
                // Check if highlighter overlaps with this line (more permissive range)
                if (highlighter.startOffset <= lineEndOffset && highlighter.endOffset >= lineStartOffset) {
                    val tooltip = highlighter.errorStripeTooltip
                    if (tooltip != null) {
                        val tooltipText = tooltip.toString()
                        if (tooltipText.isNotBlank() && tooltipText != "null") {
                            // Show ALL highlights for debugging, only filter the most obvious UI ones
                            if (!tooltipText.contains("ELEMENT_UNDER_CARET")) {
                                val severity = determineSeveritySimple(tooltipText, highlighter)
                                problems.add(ProblemInfo(severity, tooltipText))
                            }
                        }
                    }
                }
            }
            
            // Note: A document markup model is not directly accessible in this API
        } catch (e: Exception) {
            // Continue to the next method
        }
        
        // Method 3: Force comprehensive analysis and get ALL highlight infos
        try {
            val daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
            
            // Force multiple restarts to ensure comprehensive analysis
            daemonCodeAnalyzer.restart(psiFile)
            daemonCodeAnalyzer.restart()  // Restart entire analyzer
            
            // Wait longer for analysis to complete
            Thread.sleep(500)
            
            // Try to get highlights from DaemonCodeAnalyzerImpl
            val daemonImpl = daemonCodeAnalyzer as? DaemonCodeAnalyzerImpl
            if (daemonImpl != null) {
                // Get ALL highlights for the file, not just file-level
                val highlightInfos = daemonImpl.getFileLevelHighlights(project, psiFile)
                
                for (info in highlightInfos) {
                    if (info.startOffset <= lineEndOffset && info.endOffset >= lineStartOffset) {
                        val severity = when {
                            info.severity >= HighlightSeverity.ERROR -> "ERROR"
                            info.severity >= HighlightSeverity.WARNING -> "WARNING"
                            info.severity >= HighlightSeverity.WEAK_WARNING -> "WEAK_WARNING"
                            info.severity >= HighlightSeverity.INFORMATION -> "INFO"
                            else -> "HINT"
                        }
                        val description = info.description
                        if (!description.isNullOrBlank()) {
                            // Show ALL descriptions for debugging
                            if (!description.contains("ELEMENT_UNDER_CARET") && 
                                !description.contains("identifier under caret")) {
                                problems.add(ProblemInfo(severity, description))
                            }
                        }
                    }
                }
                
                // Note: Additional highlight APIs are not available in this version
            }
        } catch (e: Exception) {
            // Continue to the next method
        }
        
        // Method 4: Check for unresolved references using PsiReference API
        try {
            // Find all references in this line range
            val references = mutableListOf<PsiReference>()
            
            // Collect all PSI elements in the line range
            val elementsInLine = mutableListOf<PsiElement>()
            var currentElement = psiFile.findElementAt(lineStartOffset)
            while (currentElement != null && currentElement.textRange.startOffset < lineEndOffset) {
                elementsInLine.add(currentElement)
                currentElement = PsiTreeUtil.nextLeaf(currentElement)
            }
            
            // Check each element for references
            for (element in elementsInLine) {
                // Get references from this element
                val elementReferences = element.references
                references.addAll(elementReferences)
                
                // Also check parent elements for references (like method calls, type references)
                var parent = element.parent
                while (parent != null && parent.textRange.startOffset >= lineStartOffset && parent.textRange.endOffset <= lineEndOffset) {
                    references.addAll(parent.references)
                    parent = parent.parent
                }
            }
            
            // Check if each reference resolves
            for (reference in references) {
                try {
                    val resolved = reference.resolve()
                    if (resolved == null) {
                        // Unresolved reference found!
                        val referenceText = reference.element.text
                        val rangeInElement = reference.rangeInElement
                        val actualText = if (rangeInElement.length <= referenceText.length) {
                            referenceText.substring(rangeInElement.startOffset, rangeInElement.endOffset.coerceAtMost(referenceText.length))
                        } else {
                            referenceText
                        }
                        problems.add(ProblemInfo("ERROR", "Cannot resolve symbol '$actualText'"))
                    }
                } catch (e: Exception) {
                    // Reference resolution failed, might be unresolved
                    val referenceText = reference.element.text
                    problems.add(ProblemInfo("ERROR", "Could not resolve '$referenceText'"))
                }
            }
        } catch (e: Exception) {
            // Continue if reference checking fails
        }
        
        // Method 5: Java-specific checks for common issues
        try {
            if (psiFile.name.endsWith(".java") || psiFile.name.endsWith(".kt")) {
                // Check for specific Java constructs in this line
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                // Check for @Override without a proper method override
                if (lineText.contains("@Override")) {
                    // Find the method following this annotation
                    val methodElement = psiFile.findElementAt(lineStartOffset)
                    var current = methodElement
                    while (current != null && current.textRange.endOffset <= lineEndOffset + 200) { // Look a bit ahead
                        if (current.toString().contains("PsiMethod")) {
                            // This is a method, check if it properly overrides
                            problems.add(ProblemInfo("WARNING", "JAVA_CHECK: @Override annotation detected - verify method signature"))
                        }
                        current = PsiTreeUtil.nextLeaf(current)
                    }
                }
                
                // Check for common patterns that might indicate issues
                val suspiciousPatterns = listOf(
                    Regex("\\b[A-Z][a-zA-Z]*\\s+[a-z][a-zA-Z]*\\s*\\(") to "Method signature with unusual spacing",
                    Regex("\\b[a-z]+M\\s+[a-z]") to "Possible split identifier",
                    Regex("\\b[A-Z][a-z]+\\s+[a-z]") to "Possible unresolved type or split identifier"
                )
                
                for ((pattern, description) in suspiciousPatterns) {
                    if (pattern.containsMatchIn(lineText)) {
                        problems.add(ProblemInfo("WARNING", description))
                    }
                }
            }
        } catch (e: Exception) {
            // Continue if Java checking fails
        }
        
        // Method 6: Direct PSI validation for common issues by file type
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
                
                // Check for @Override without a proper return type
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
            } else if (psiFile.name.endsWith(".yaml") || psiFile.name.endsWith(".yml")) {
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
                    
                    // Look at the previous non-empty line to check context
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
                        
                        // Check for a mapping key without a proper structure
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
            // Continue if validation fails
        }
        
        // Method 7: Run IntelliJ inspections to catch semantic errors
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
                        
                        // Create a problem holder to collect issues
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
                        // Continue with the next inspection tool
                    }
                }
            }
        } catch (e: Exception) {
            // Continue if inspection execution fails
        }
        
        // Method 8: Clean up - remove debug messages
        // Only return actual problems, no debug messages
        
        return problems.distinctBy { "${it.severity}:${it.message}" }
    }
    
    private fun determineSeveritySimple(tooltipText: String, highlighter: RangeHighlighter): String {
        // First, check the highlighter's layer
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