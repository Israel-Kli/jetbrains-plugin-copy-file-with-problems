package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class CopyWithInlineIssues : BaseFileAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            return
        }
        
        val document = editor.document
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        
        val startLine = document.getLineNumber(startOffset)
        val endLine = document.getLineNumber(endOffset)
        
        val result = buildContentWithProblems(
            psiFile,
            document,
            startLine,
            endLine,
        ) { fileName -> fileName }
        
        copyToClipboard(result)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        
        val isEnabled = (project != null) && 
                       (editor != null) && 
                       (psiFile != null) && 
                       (editor.selectionModel.hasSelection())
        
        e.presentation.isEnabledAndVisible = isEnabled
        e.presentation.text = "Copy with Inline Issues"
    }
}