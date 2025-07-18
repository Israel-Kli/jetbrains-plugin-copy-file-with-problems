package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager

class CopyFileWithProblemsAction : BaseFileAction("Copy file with inline issues") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val virtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
        
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return
        
        val result = buildFileContentWithProblems(psiFile, document, project, virtualFile)
        
        copyToClipboard(result)
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