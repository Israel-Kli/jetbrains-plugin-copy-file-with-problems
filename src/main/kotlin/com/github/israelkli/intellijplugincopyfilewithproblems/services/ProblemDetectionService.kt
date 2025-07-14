package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile

class ProblemDetectionService {
    
    data class ProblemInfo(val severity: String, val message: String, val startOffset: Int, val endOffset: Int)
    
    fun findProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        
        try {
            // Method 1: Get highlights from DaemonCodeAnalyzer (native IDEA inspections)
            val highlights = getHighlightsForRange(psiFile, lineStartOffset, lineEndOffset)
            for (highlight in highlights) {
                if (highlight.description != null && highlight.description.isNotBlank()) {
                    val severity = when (highlight.severity) {
                        HighlightSeverity.ERROR -> "ERROR"
                        HighlightSeverity.WARNING -> "WARNING"
                        HighlightSeverity.WEAK_WARNING -> "WEAK_WARNING"
                        HighlightSeverity.INFORMATION -> "INFO"
                        else -> "INFO"
                    }
                    problems.add(ProblemInfo(
                        severity = severity,
                        message = highlight.description,
                        startOffset = highlight.startOffset,
                        endOffset = highlight.endOffset
                    ))
                }
            }
            
            // Method 2: Run active inspections programmatically
            val inspectionProblems = runInspectionsOnRange(psiFile, lineStartOffset, lineEndOffset)
            problems.addAll(inspectionProblems)
            
            // Method 3: Fallback to PSI-based error detection (for cases where daemon isn't available)
            if (problems.isEmpty()) {
                problems.addAll(findPsiProblems(psiFile, lineStartOffset, lineEndOffset))
            }
            
        } catch (_: Exception) {
            // Fallback to simple PSI-based detection if native methods fail
            problems.addAll(findPsiProblems(psiFile, lineStartOffset, lineEndOffset))
        }
        
        return problems.distinctBy { "${it.severity}:${it.message}" }
    }
    
    private fun getHighlightsForRange(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<HighlightInfo> {
        val project = psiFile.project
        val highlights = mutableListOf<HighlightInfo>()
        
        try {
            // Get a document for the PSI file
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return highlights
            
            // Get a markup model for the document
            val markupModel = DocumentMarkupModel.forDocument(document, project, true)
            val allHighlighters = markupModel.allHighlighters
            
            // Filter highlights that overlap with our range
            for (highlighter in allHighlighters) {
                if (highlighter.startOffset < endOffset && highlighter.endOffset > startOffset) {
                    val tooltip = highlighter.errorStripeTooltip
                    if (tooltip is HighlightInfo) {
                        highlights.add(tooltip)
                    }
                }
            }
        } catch (_: Exception) {
            // If getting highlights fails, return an empty list
        }
        
        return highlights
    }
    
    private fun runInspectionsOnRange(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        val project = psiFile.project
        
        try {
            // Get the inspection profile for the project
            val inspectionProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
            val enabledInspections = inspectionProfile.getInspectionTools(psiFile)
            
            // Run a subset of important inspections
            for (toolWrapper in enabledInspections.take(10)) { // Limit to avoid performance issues
                if (toolWrapper.tool is LocalInspectionTool) {
                    val inspectionTool = toolWrapper.tool as LocalInspectionTool
                    val inspectionManager = InspectionManager.getInstance(project)
                    
                    // Run inspection on the file
                    val descriptors = ReadAction.compute<Array<ProblemDescriptor>, RuntimeException> {
                        inspectionTool.checkFile(psiFile, inspectionManager, false) ?: emptyArray()
                    }
                    
                    // Filter descriptors that are within our range
                    for (descriptor in descriptors) {
                        val element = descriptor.psiElement
                        if (element != null) {
                            val elementStart = element.textRange.startOffset
                            val elementEnd = element.textRange.endOffset
                            
                            // Check if the problem overlaps with our range
                            if (elementStart < endOffset && elementEnd > startOffset) {
                                problems.add(ProblemInfo(
                                    severity = "INSPECTION",
                                    message = descriptor.descriptionTemplate,
                                    startOffset = elementStart,
                                    endOffset = elementEnd
                                ))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // If inspection running fails, continue with other methods
        }
        
        return problems
    }
    
    private fun findPsiProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        
        try {
            // Only check for native PSI errors - no custom pattern matching
            
            // Method 1: Check for PSI errors at line start
            val elementAtStart = psiFile.findElementAt(lineStartOffset)
            if (elementAtStart is PsiErrorElement) {
                val errorMessage = elementAtStart.errorDescription
                if (errorMessage.isNotBlank()) {
                    problems.add(ProblemInfo(
                        severity = "ERROR",
                        message = errorMessage,
                        startOffset = elementAtStart.textRange.startOffset,
                        endOffset = elementAtStart.textRange.endOffset
                    ))
                }
            }
            
            // Method 2: Check parent elements for errors (up to 2 levels to avoid deep traversal)
            var currentElement = elementAtStart?.parent
            repeat(2) {
                if (currentElement is PsiErrorElement) {
                    val errorMessage = currentElement.errorDescription
                    if (errorMessage.isNotBlank()) {
                        problems.add(ProblemInfo(
                            severity = "ERROR",
                            message = errorMessage,
                            startOffset = currentElement.textRange.startOffset,
                            endOffset = currentElement.textRange.endOffset
                        ))
                    }
                    return@repeat
                }
                currentElement = currentElement?.parent
            }
            
            // Method 3: Scan through elements within the line range for PSI errors
            var offset = lineStartOffset
            var elementCount = 0
            while (offset < lineEndOffset && elementCount < 10) { // Limit to 10 elements per line
                val element = psiFile.findElementAt(offset)
                if (element is PsiErrorElement) {
                    val errorMessage = element.errorDescription
                    if (errorMessage.isNotBlank()) {
                        problems.add(ProblemInfo(
                            severity = "ERROR",
                            message = errorMessage,
                            startOffset = element.textRange.startOffset,
                            endOffset = element.textRange.endOffset
                        ))
                    }
                }
                offset += maxOf(1, element?.textLength ?: 1)
                elementCount++
            }
            
        } catch (_: Exception) {
            // Ignore errors to prevent hanging
        }
        
        return problems
    }
}