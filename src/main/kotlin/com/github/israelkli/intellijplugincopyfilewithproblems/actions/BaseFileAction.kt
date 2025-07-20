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
    
    fun getCommentPrefix(psiFile: PsiFile): String {
        val language = psiFile.language
        val languageId = language.id.lowercase()
        
        return when {
            languageId.contains("python") -> "# "
            languageId.contains("ruby") -> "# "
            languageId.contains("shell") -> "# "
            languageId.contains("bash") -> "# "
            languageId.contains("yaml") -> "# "
            languageId.contains("toml") -> "# "
            languageId.contains("dockerfile") -> "# "
            languageId.contains("makefile") -> "# "
            languageId.contains("properties") -> "# "
            languageId.contains("ini") -> "; "
            languageId.contains("sql") -> "-- "
            languageId.contains("lua") -> "-- "
            languageId.contains("haskell") -> "-- "
            languageId.contains("html") -> "<!-- "
            languageId.contains("xml") -> "<!-- "
            languageId.contains("css") -> "/* "
            languageId.contains("scss") -> "// "
            languageId.contains("sass") -> "// "
            languageId.contains("less") -> "// "
            else -> "// " // Default for Java, JavaScript, TypeScript, Kotlin, C, C++, PHP, etc.
        }
    }
    
    fun getCommentSuffix(psiFile: PsiFile): String {
        val language = psiFile.language
        val languageId = language.id.lowercase()
        
        return when {
            languageId.contains("html") -> " -->"
            languageId.contains("xml") -> " -->"
            languageId.contains("css") -> " */"
            else -> ""
        }
    }
    
    protected fun formatComment(psiFile: PsiFile, severityPrefix: String, message: String): String {
        val prefix = getCommentPrefix(psiFile)
        val suffix = getCommentSuffix(psiFile)
        return "$prefix$severityPrefix: $message$suffix"
    }
    
    private fun appendIssueComments(
        builder: StringBuilder,
        psiFile: PsiFile,
        issues: List<ProblemDetectionService.IssueInfo>
    ) {
        for (issue in issues) {
            builder.appendLine()
            val severityPrefix = when (issue.severity) {
                "ERROR" -> "ERROR"
                "WARNING" -> "WARNING"
                "WEAK_WARNING" -> "WEAK_WARNING"
                "INFO" -> "INFO"
                "INSPECTION" -> "INSPECTION"
                else -> issue.severity
            }
            builder.append(formatComment(psiFile, severityPrefix, issue.message))
        }
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
                val headerComment = formatComment(psiFile, "File", headerProvider(virtualFile.name))
                appendLine(headerComment)
                appendLine()
            }
            
            for (lineNumber in lineStart..lineEnd) {
                val lineStartOffset = document.getLineStartOffset(lineNumber)
                val lineEndOffset = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                
                append(lineText)
                
                val problems = problemDetectionService.findProblems(psiFile, lineStartOffset, lineEndOffset)
                appendIssueComments(this, psiFile, problems)
                appendLine()
            }
        }
    }
    
    protected fun buildFileContentWithInlineIssues(
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
            
            val headerComment = formatComment(psiFile, "File", relativePath)
            appendLine(headerComment)
            appendLine()
            
            val fileContent = document.text
            val lines = fileContent.lines()
            lines.forEachIndexed { index, line ->
                append(line)
                
                val lineStartOffset = document.getLineStartOffset(index)
                val lineEndOffset = document.getLineEndOffset(index)
                val issues = problemDetectionService.findProblems(psiFile, lineStartOffset, lineEndOffset)
                appendIssueComments(this, psiFile, issues)
                appendLine()
            }
        }
    }
}