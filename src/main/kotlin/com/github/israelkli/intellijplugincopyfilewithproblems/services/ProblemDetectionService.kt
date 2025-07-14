package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile

class ProblemDetectionService {
    
    data class ProblemInfo(val severity: String, val message: String)
    
    fun findSimpleProblems(psiFile: PsiFile, lineStartOffset: Int, lineEndOffset: Int): List<ProblemInfo> {
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
            
            // Method 3: Pattern-based checks
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
}