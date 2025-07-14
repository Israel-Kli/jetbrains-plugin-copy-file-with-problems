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
import com.intellij.psi.util.PsiTreeUtil

class ProblemDetectionService {
    
    data class ProblemInfo(val severity: String, val message: String, val startOffset: Int, val endOffset: Int)
    
    fun findProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        
        try {
            // Method 1: Get highlights from DaemonCodeAnalyzer (native IDEA inspections)
            // This works best in IntelliJ IDEA, but may have limited functionality in other IDEs
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
            // This is more IDE-agnostic but may be slower
            val inspectionProblems = runInspectionsOnRange(psiFile, lineStartOffset, lineEndOffset)
            problems.addAll(inspectionProblems)
            
            // Method 3: Always run PSI-based error detection as it's the most reliable across IDEs
            val psiProblems = findPsiProblems(psiFile, lineStartOffset, lineEndOffset)
            problems.addAll(psiProblems)
            
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
            
            // Run a conservative subset of important inspections
            // Different IDEs may have different inspection availability
            for (toolWrapper in enabledInspections.take(5)) { // Reduced limit for better cross-IDE compatibility
                try {
                    if (toolWrapper.tool is LocalInspectionTool) {
                        val inspectionTool = toolWrapper.tool as LocalInspectionTool
                        val inspectionManager = InspectionManager.getInstance(project)
                        
                        // Run inspection on the file with proper error handling
                        val descriptors = ReadAction.compute<Array<ProblemDescriptor>, RuntimeException> {
                            try {
                                inspectionTool.checkFile(psiFile, inspectionManager, false) ?: emptyArray()
                            } catch (_: Exception) {
                                emptyArray<ProblemDescriptor>()
                            }
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
                } catch (_: Exception) {
                    // Skip this inspection if it fails - continue with others
                    continue
                }
            }
        } catch (_: Exception) {
            // If inspection running fails completely, continue with other methods
        }
        
        return problems
    }
    
    private fun findPsiProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        
        try {
            // Method 1: Use PsiTreeUtil to find all PSI errors in the range
            // This is more robust across different IDEs
            val rootElement = psiFile.findElementAt(lineStartOffset)?.containingFile
            if (rootElement != null) {
                val errorElements = PsiTreeUtil.findChildrenOfType(rootElement, PsiErrorElement::class.java)
                for (errorElement in errorElements) {
                    val errorStart = errorElement.textRange.startOffset
                    val errorEnd = errorElement.textRange.endOffset
                    
                    // Check if error overlaps with our range
                    if (errorStart < lineEndOffset && errorEnd > lineStartOffset) {
                        val errorMessage = errorElement.errorDescription
                        if (errorMessage.isNotBlank()) {
                            problems.add(ProblemInfo(
                                severity = "ERROR",
                                message = errorMessage,
                                startOffset = errorStart,
                                endOffset = errorEnd
                            ))
                        }
                    }
                }
            }
            
            // Method 2: Check for PSI errors at line start (fallback)
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