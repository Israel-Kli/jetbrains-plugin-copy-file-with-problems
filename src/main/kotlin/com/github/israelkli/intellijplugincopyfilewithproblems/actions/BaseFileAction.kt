package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.github.israelkli.intellijplugincopyfilewithproblems.services.ProblemDetectionService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import java.awt.datatransfer.StringSelection

abstract class BaseFileAction(text: String) : AnAction(text) {
    
    protected val problemDetectionService = ProblemDetectionService()
    
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    
    protected fun copyToClipboard(content: String) {
        val selection = StringSelection(content)
        CopyPasteManager.getInstance().setContents(selection)
    }
    
    protected fun buildContentWithProblems(
        psiFile: PsiFile,
        document: com.intellij.openapi.editor.Document,
        lineStart: Int,
        lineEnd: Int,
        headerProvider: (String) -> String
    ): String {
        return buildString {
            val virtualFile = psiFile.virtualFile
            if (virtualFile != null) {
                appendLine(headerProvider(virtualFile.name))
                appendLine()
            }
            
            for (lineNumber in lineStart..lineEnd) {
                val lineStartOffset = document.getLineStartOffset(lineNumber)
                val lineEndOffset = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                append(lineText)
                
                val problems = problemDetectionService.findProblems(psiFile, lineStartOffset, lineEndOffset)
                for (problem in problems) {
                    appendLine()
                    val severityPrefix = when (problem.severity) {
                        "ERROR" -> "ERROR"
                        "WARNING" -> "WARNING"
                        "WEAK_WARNING" -> "WEAK_WARNING"
                        "INFO" -> "INFO"
                        "INSPECTION" -> "INSPECTION"
                        else -> problem.severity
                    }
                    append("// $severityPrefix: ${problem.message}")
                }
                appendLine()
            }
        }
    }
    
    protected fun buildFileContentWithProblems(
        psiFile: PsiFile,
        document: com.intellij.openapi.editor.Document,
        project: com.intellij.openapi.project.Project,
        virtualFile: com.intellij.openapi.vfs.VirtualFile
    ): String {
        return buildString {
            val projectBasePath = project.basePath
            val relativePath = if (projectBasePath != null && virtualFile.path.startsWith(projectBasePath)) {
                virtualFile.path.substring(projectBasePath.length).removePrefix("/")
            } else {
                virtualFile.path
            }
            appendLine("// $relativePath")
            appendLine()
            
            val fileContent = document.text
            val lines = fileContent.lines()
            lines.forEachIndexed { index, line ->
                append(line)
                
                val lineStartOffset = document.getLineStartOffset(index)
                val lineEndOffset = document.getLineEndOffset(index)
                val problems = problemDetectionService.findProblems(psiFile, lineStartOffset, lineEndOffset)
                for (problem in problems) {
                    appendLine()
                    val severityPrefix = when (problem.severity) {
                        "ERROR" -> "ERROR"
                        "WARNING" -> "WARNING"
                        "WEAK_WARNING" -> "WEAK_WARNING"
                        "INFO" -> "INFO"
                        "INSPECTION" -> "INSPECTION"
                        else -> problem.severity
                    }
                    append("// $severityPrefix: ${problem.message}")
                }
                appendLine()
            }
        }
    }
}