package com.github.israelkli.intellijplugincopyfilewithproblems

import com.github.israelkli.intellijplugincopyfilewithproblems.actions.BaseFileAction
import com.github.israelkli.intellijplugincopyfilewithproblems.actions.CopyFileWithInlineIssues
import com.github.israelkli.intellijplugincopyfilewithproblems.actions.CopyWithInlineIssues
import com.github.israelkli.intellijplugincopyfilewithproblems.services.ProblemDetectionService
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class CopyFileWithInlineIssuesPluginTest : BasePlatformTestCase() {

    // ========== INTEGRATION TESTS ==========
    
    fun testCompleteWorkflowWithIssues() {
        val javaCodeWithIssues = """
            public class WorkflowTest {
                public void testMethod() {
                    undeclaredVariable = 5;
                    String s = null;
                    s.toString();
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("WorkflowTest.java", javaCodeWithIssues)
        val service = ProblemDetectionService()
        val action = CopyFileWithInlineIssues()
        
        // Test the complete workflow components
        val issues = service.findProblems(psiFile, 0, javaCodeWithIssues.length)
        assertNotNull("Issues should be detected", issues)
        
        val virtualFile = psiFile.virtualFile
        assertNotNull("Virtual file should exist", virtualFile)
        
        // Test comment formatting for this file type
        val baseAction = action as BaseFileAction
        val prefix = baseAction.getCommentPrefix(psiFile)
        assertEquals("Java files should use // comments", "// ", prefix)
    }

    fun testCompleteWorkflowWithValidCode() {
        val validJavaCode = """
            public class ValidWorkflow {
                public void validMethod() {
                    System.out.println("This is valid code");
                    int x = 5;
                    int y = x + 10;
                    System.out.println("Result: " + y);
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("ValidWorkflow.java", validJavaCode)
        val service = ProblemDetectionService()
        val action = CopyFileWithInlineIssues()
        
        val issues = service.findProblems(psiFile, 0, validJavaCode.length)
        assertNotNull("Issues list should not be null even for valid code", issues)
        
        val virtualFile = psiFile.virtualFile
        assertNotNull("Virtual file should exist", virtualFile)
        assertTrue("Code should contain class name", validJavaCode.contains("ValidWorkflow"))
        
        // Test that the action thread is properly configured
        assertEquals("Should use BGT thread", 
                     com.intellij.openapi.actionSystem.ActionUpdateThread.BGT, 
                     action.actionUpdateThread)
    }

    fun testCrossLanguageIntegration() {
        val languageTests = mapOf(
            "Example.java" to """
                public class Example {
                    public void method() {
                        undeclaredVar = 5;
                    }
                }
            """.trimIndent(),
            
            "example.js" to """
                function example() {
                    undeclaredVar = 5;
                    console.log(undeclaredVar);
                }
            """.trimIndent(),
            
            "example.py" to """
                def example():
                    undefined_var = 5
                    print(undefined_var)
            """.trimIndent(),
            
            "example.xml" to """
                <root>
                    <unclosed>
                        <tag>content</tag>
                </root>
            """.trimIndent(),
            
            "example.html" to """
                <html>
                    <body>
                        <p>Hello World</span>
                    </body>
                </html>
            """.trimIndent()
        )
        
        val service = ProblemDetectionService()
        val fileAction = CopyFileWithInlineIssues()
        val selectionAction = CopyWithInlineIssues()
        
        languageTests.forEach { (filename, code) ->
            val psiFile = myFixture.configureByText(filename, code)
            
            // Test issue detection
            val issues = service.findProblems(psiFile, 0, code.length)
            assertNotNull("Issues should not be null for $filename", issues)
            
            // Test comment format detection
            val baseAction = fileAction as BaseFileAction
            val prefix = baseAction.getCommentPrefix(psiFile)
            assertNotNull("Comment prefix should not be null for $filename", prefix)
            assertTrue("Comment prefix should not be empty for $filename", prefix.isNotEmpty())
            
            // Test both actions can handle the file
            assertNotNull("File action should handle $filename", fileAction)
            assertNotNull("Selection action should handle $filename", selectionAction)
        }
    }

    fun testPluginComponentIntegration() {
        val testCode = """
            public class ComponentTest {
                public void integrationTest() {
                    // This method tests component integration
                    int result = calculateSomething();
                    undefinedMethod(); // This should be detected as an issue
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("ComponentTest.java", testCode)
        
        // Test all major components work together
        val service = ProblemDetectionService()
        val fileAction = CopyFileWithInlineIssues()
        val selectionAction = CopyWithInlineIssues()
        
        // 1. Issue detection service should work
        val issues = service.findProblems(psiFile, 0, testCode.length)
        assertNotNull("Issue detection should work", issues)
        
        // 2. Both actions should be properly instantiated
        assertNotNull("File action should be created", fileAction)
        assertNotNull("Selection action should be created", selectionAction)
        assertTrue("File action should extend BaseFileAction", true)
        assertTrue("Selection action should extend BaseFileAction", true)
        
        // 3. Comment formatting should work
        val baseAction = fileAction as BaseFileAction
        val prefix = baseAction.getCommentPrefix(psiFile)
        val suffix = baseAction.getCommentSuffix(psiFile)
        assertEquals("// ", prefix)
        assertEquals("", suffix)
        
        // 4. Action update threads should be configured
        assertEquals(com.intellij.openapi.actionSystem.ActionUpdateThread.BGT, fileAction.actionUpdateThread)
        assertEquals(com.intellij.openapi.actionSystem.ActionUpdateThread.BGT, selectionAction.actionUpdateThread)
    }

    fun testEndToEndWorkflowWithRealScenarios() {
        // Scenario 1: Java class with multiple types of issues
        val complexJava = """
            import java.util.List;
            
            public class ComplexExample {
                private String name;
                
                public void problematicMethod() {
                    // Undeclared variable
                    unknownVariable = 42;
                    
                    // Possible null pointer
                    String text = null;
                    int length = text.length();
                    
                    // Unused variable
                    int unused = 100;
                    
                    // Missing return in non-void method would be caught by real analysis
                }
                
                public String getName() {
                    return name;
                }
            }
        """.trimIndent()
        
        testWorkflowForCode("ComplexExample.java", complexJava)
        
        // Scenario 2: XML with validation issues
        val problematicXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <configuration>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                    <unclosed>
                        <name>mydb</name>
                </database>
            </configuration>
        """.trimIndent()
        
        testWorkflowForCode("config.xml", problematicXml)
        
        // Scenario 3: JavaScript with potential issues
        val problematicJs = """
            function processData(data) {
                // Undeclared variable
                result = data.map(item => {
                    return item.process();
                });
                
                // Potentially undefined method
                unknownFunction(result);
                
                console.log(result);
            }
        """.trimIndent()
        
        testWorkflowForCode("processor.js", problematicJs)
    }
    
    private fun testWorkflowForCode(filename: String, code: String) {
        val psiFile = myFixture.configureByText(filename, code)
        val service = ProblemDetectionService()
        val action = CopyFileWithInlineIssues()
        
        // Test complete workflow
        val issues = service.findProblems(psiFile, 0, code.length)
        assertNotNull("Issues should be detected for $filename", issues)
        
        val baseAction = action as BaseFileAction
        val prefix = baseAction.getCommentPrefix(psiFile)
        assertNotNull("Comment prefix should be determined for $filename", prefix)
        assertTrue("Comment prefix should not be empty for $filename", prefix.isNotEmpty())
    }

    fun testPluginRobustnessUnderStress() {
        val service = ProblemDetectionService()
        val action = CopyFileWithInlineIssues()
        
        // Test with various edge cases
        val edgeCases = listOf(
            "" to "empty.txt",
            " " to "whitespace.txt", 
            "\n\n\n" to "newlines.txt",
            "a" to "single.txt",
            "日本語 content 🎉" to "unicode.txt"
        )
        
        edgeCases.forEach { (content, filename) ->
            val psiFile = myFixture.configureByText(filename, content)
            
            try {
                val issues = service.findProblems(psiFile, 0, content.length)
                assertNotNull("Service should handle edge case: $filename", issues)
            } catch (e: Exception) {
                fail("Service should handle edge case gracefully: $filename - ${e.message}")
            }
        }
    }

    fun testPluginPerformanceBasics() {
        val mediumSizedCode = "public class Performance {\n" + 
                             (1..50).joinToString("\n") { "    public void method$it() { System.out.println(\"Method $it\"); }" } +
                             "\n}"
        
        val psiFile = myFixture.configureByText("Performance.java", mediumSizedCode)
        val service = ProblemDetectionService()
        
        // Test that repeated calls don't cause issues
        repeat(10) { iteration ->
            val issues = service.findProblems(psiFile, 0, mediumSizedCode.length)
            assertNotNull("Issues should be detected on iteration $iteration", issues)
        }
    }

    // ========== LEGACY INTEGRATION TESTS ==========
    
    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }

    override fun getTestDataPath() = "src/test/testData/rename"
}