package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.awt.datatransfer.StringSelection

class CopyFileWithProblemsAction : AnAction("Copy File with Problems") {

    data class ProblemInfo(val severity: String, val message: String)
    
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val virtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
        
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return
        
        val fileContent = document.text
        
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
                
                // Find problems for this line (simplified)
                val lineStartOffset = document.getLineStartOffset(index)
                val lineEndOffset = document.getLineEndOffset(index)
                val problems = findSimpleProblems(psiFile, lineStartOffset, lineEndOffset)
                for (problem in problems) {
                    appendLine()
                    append("// ${problem.severity}: ${problem.message}")
                }
                appendLine()
            }
        }
        
        val selection = StringSelection(result)
        CopyPasteManager.getInstance().setContents(selection)
    }
    
    private fun findSimpleProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        
        try {
            // Method 1: Check for PSI errors at line start and nearby elements
            val elementAtStart = psiFile.findElementAt(lineStartOffset)
            if (elementAtStart is PsiErrorElement) {
                val errorMessage = elementAtStart.errorDescription
                if (errorMessage.isNotBlank()) {
                    problems.add(ProblemInfo("ERROR", errorMessage))
                }
            }
            
            // Check parent elements (up to 2 levels to avoid deep traversal)
            var currentElement = elementAtStart?.parent
            repeat(2) {
                if (currentElement is PsiErrorElement) {
                    val errorMessage = currentElement.errorDescription
                    if (errorMessage.isNotBlank()) {
                        problems.add(ProblemInfo("ERROR", errorMessage))
                    }
                    return@repeat
                }
                currentElement = currentElement?.parent
            }
            
            // Method 2: Check a few elements within the line range (limited to avoid hanging)
            var offset = lineStartOffset
            var elementCount = 0
            while (offset < lineEndOffset && elementCount < 5) { // Limit to 5 elements per line
                val element = psiFile.findElementAt(offset)
                if (element is PsiErrorElement) {
                    val errorMessage = element.errorDescription
                    if (errorMessage.isNotBlank()) {
                        problems.add(ProblemInfo("ERROR", errorMessage))
                    }
                }
                offset += maxOf(1, element?.textLength ?: 1)
                elementCount++
            }
            
            // Simple pattern checks
            val document = elementAtStart?.containingFile?.let { 
                PsiDocumentManager.getInstance(it.project).getDocument(it) 
            }
            if (document != null) {
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                // Check for split identifiers like "cacheM anager"
                if (psiFile.name.endsWith(".java") || psiFile.name.endsWith(".kt")) {
                    val splitPattern = Regex("\\b([a-zA-Z]+)\\s+([a-z][a-zA-Z]*)(\\s*\\()")
                    val match = splitPattern.find(lineText)
                    if (match != null) {
                        problems.add(ProblemInfo("ERROR", "Split identifier detected: '${match.groupValues[1]} ${match.groupValues[2]}'"))
                    }
                }
                
                // Check for YAML errors
                if (psiFile.name.endsWith(".yaml") || psiFile.name.endsWith(".yml")) {
                    if (lineText.contains("\t")) {
                        problems.add(ProblemInfo("ERROR", "YAML does not allow tabs for indentation"))
                    }
                }
            }
            
        } catch (e: Exception) {
            // Ignore errors to prevent hanging
        }
        
        return problems.distinctBy { it.message }
    }

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.getData(CommonDataKeys.PROJECT)
        
        val isEnabled = virtualFile != null && 
                       project != null && 
                       !virtualFile.isDirectory
        
        e.presentation.isEnabledAndVisible = isEnabled
    }
}