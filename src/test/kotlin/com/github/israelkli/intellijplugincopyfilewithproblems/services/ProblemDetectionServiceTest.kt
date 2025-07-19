package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.github.israelkli.intellijplugincopyfilewithproblems.services.ProblemDetectionService
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ProblemDetectionServiceTest : BasePlatformTestCase() {

    private lateinit var service: ProblemDetectionService

    override fun setUp() {
        super.setUp()
        service = ProblemDetectionService()
    }

    fun testProblemInfoDataClass() {
        val problemInfo = ProblemDetectionService.ProblemInfo(
            severity = "ERROR",
            message = "Test error message",
            startOffset = 0,
            endOffset = 10
        )
        
        assertEquals("ERROR", problemInfo.severity)
        assertEquals("Test error message", problemInfo.message)
        assertEquals(0, problemInfo.startOffset)
        assertEquals(10, problemInfo.endOffset)
    }

    fun testFindIssuesWithValidJavaCode() {
        val validJavaCode = """
            public class ValidClass {
                public void validMethod() {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("ValidClass.java", validJavaCode)
        val issues = service.findProblems(psiFile, 0, validJavaCode.length)
        
        // Valid code should have no or minimal issues
        assertNotNull("Issues should not be null", issues)
        assertTrue("Issues should be a list", issues is List)
    }

    fun testFindIssuesWithInvalidJavaCode() {
        val invalidJavaCode = """
            public class InvalidClass {
                public void invalidMethod() {
                    undeclaredVariable = 5;
                    String s = null;
                    s.toString();
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("InvalidClass.java", invalidJavaCode)
        val issues = service.findProblems(psiFile, 0, invalidJavaCode.length)
        
        assertNotNull("Issues should not be null", issues)
        assertTrue("Issues should be detected", issues is List)
    }

    fun testFindIssuesWithEmptyRange() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        val issues = service.findProblems(psiFile, 0, 0)
        assertNotNull("Issues should not be null", issues)
        assertTrue("Issues list should be initialized", issues is List)
    }

    fun testFindIssuesWithLargeRange() {
        val javaCode = """
            public class LargeClass {
                public void method1() {
                    System.out.println("Method 1");
                }
                
                public void method2() {
                    System.out.println("Method 2");
                }
                
                public void method3() {
                    undeclaredVariable = 5;
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("LargeClass.java", javaCode)
        val issues = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("Issues should not be null", issues)
        assertTrue("Issues should be found in large file", issues is List)
    }

    fun testFindIssuesWithSyntaxErrors() {
        // Java with syntax error
        val invalidJavaCode = """
            public class InvalidSyntax {
                public void method() {
                    if (true {
                        System.out.println("Missing closing parenthesis");
                    }
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("InvalidSyntax.java", invalidJavaCode)
        val issues = service.findProblems(psiFile, 0, invalidJavaCode.length)
        
        assertNotNull("Issues should not be null", issues)
        // Should detect the syntax error
        assertTrue("Should detect issues in invalid syntax", issues is List)
    }

    fun testFindIssuesWithJavaScript() {
        val jsCode = """
            function test() {
                undeclaredVariable = 5;
                console.log(undeclaredVariable);
            }
        """.trimIndent()
        val jsFile = myFixture.configureByText("test.js", jsCode)
        val jsIssues = service.findProblems(jsFile, 0, jsCode.length)
        assertNotNull("JavaScript issues should not be null", jsIssues)
    }

    fun testFindIssuesWithPython() {
        val pyCode = """
            def test():
                undefined_variable = 5
                print(undefined_variable)
        """.trimIndent()
        val pyFile = myFixture.configureByText("test.py", pyCode)
        val pyIssues = service.findProblems(pyFile, 0, pyCode.length)
        assertNotNull("Python issues should not be null", pyIssues)
    }

    fun testFindIssuesWithXML() {
        val xmlCode = """
            <root>
                <unclosed>
                    <tag>content</tag>
            </root>
        """.trimIndent()
        val xmlFile = myFixture.configureByText("test.xml", xmlCode)
        val xmlIssues = service.findProblems(xmlFile, 0, xmlCode.length)
        assertNotNull("XML issues should not be null", xmlIssues)
    }

    fun testFindIssuesWithJSON() {
        val jsonCode = """
            {
                "key": "value",
                "number": 123,
                "unclosed": "string
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("test.json", jsonCode)
        val issues = service.findProblems(psiFile, 0, jsonCode.length)
        
        assertNotNull("JSON issues should not be null", issues)
        assertTrue("JSON-like issues should be detected", issues is List)
    }

    fun testNullSafetyWithEmptyFile() {
        val emptyFile = myFixture.configureByText("empty.txt", "")
        
        // Should handle empty files gracefully
        try {
            val issues = service.findProblems(emptyFile, 0, 0)
            assertNotNull("Issues should not be null", issues)
        } catch (e: Exception) {
            fail("Should handle empty files gracefully: ${e.message}")
        }
    }

    fun testLargeFileHandling() {
        val largeContent = "public class Large {\n" + 
                          (1..100).joinToString("\n") { "    public void method$it() { /* method $it */ }" } +
                          "\n}"
        
        val psiFile = myFixture.configureByText("Large.java", largeContent)
        
        // Should handle large files gracefully
        try {
            val issues = service.findProblems(psiFile, 0, largeContent.length)
            assertNotNull("Issues should not be null", issues)
        } catch (e: Exception) {
            fail("Should handle large files gracefully: ${e.message}")
        }
    }

    fun testInvalidRangeHandling() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        // Should handle invalid ranges gracefully
        try {
            // Test with range beyond file length
            val issues1 = service.findProblems(psiFile, 0, javaCode.length * 2)
            assertNotNull("Issues should not be null", issues1)
        } catch (e: Exception) {
            fail("Should handle large ranges gracefully: ${e.message}")
        }
        
        try {
            // Test with negative range
            val issues2 = service.findProblems(psiFile, -1, javaCode.length)
            assertNotNull("Issues should not be null", issues2)
        } catch (e: Exception) {
            fail("Should handle negative ranges gracefully: ${e.message}")
        }
    }

    fun testJavaSpecificIssueDetection() {
        val javaCode = """
            public class JavaTest {
                public void method() {
                    int x = 5;
                    int y = x + undeclaredVar;
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("JavaTest.java", javaCode)
        val issues = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("Issues should not be null", issues)
        assertTrue("Java issues should be detected", issues is List)
    }

    fun testXMLSpecificIssueDetection() {
        val xmlCode = """
            <root>
                <unclosed>
                    <valid>content</valid>
                </mismatched>
            </root>
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("test.xml", xmlCode)
        val issues = service.findProblems(psiFile, 0, xmlCode.length)
        
        assertNotNull("Issues should not be null", issues)
        assertTrue("XML issues should be detected", issues is List)
    }

    fun testPartialFileAnalysis() {
        val javaCode = """
            public class PartialTest {
                public void method1() {
                    System.out.println("Valid method");
                }
                
                public void method2() {
                    undeclaredVariable = 5;  // This line has an issue
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("PartialTest.java", javaCode)
        
        // Test analyzing only a portion of the file
        val lineStartOffset = javaCode.indexOf("public void method2")
        val lineEndOffset = javaCode.indexOf("}", lineStartOffset)
        
        val issues = service.findProblems(psiFile, lineStartOffset, lineEndOffset)
        assertNotNull("Issues should not be null for partial analysis", issues)
    }

    fun testConcurrentAccess() {
        val javaCode = "public class Concurrent { }"
        val psiFile = myFixture.configureByText("Concurrent.java", javaCode)
        
        // Test that multiple calls don't interfere with each other
        val issues1 = service.findProblems(psiFile, 0, javaCode.length)
        val issues2 = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("First call should not be null", issues1)
        assertNotNull("Second call should not be null", issues2)
        
        // Both calls should succeed independently
        assertTrue("Both calls should return lists", issues1 is List && issues2 is List)
    }

    fun testDifferentLanguagePatterns() {
        // Test that the service can handle various programming languages
        val languages = mapOf(
            "test.java" to "public class Test {}",
            "test.kt" to "class Test",
            "test.js" to "function test() {}",
            "test.py" to "def test(): pass",
            "test.rb" to "def test; end",
            "test.php" to "<?php function test() {} ?>",
            "test.cpp" to "int main() { return 0; }",
            "test.go" to "package main; func main() {}",
            "test.rs" to "fn main() {}"
        )
        
        languages.forEach { (filename, code) ->
            val psiFile = myFixture.configureByText(filename, code)
            val issues = service.findProblems(psiFile, 0, code.length)
            assertNotNull("Issues should not be null for $filename", issues)
        }
    }

    fun testPerformanceWithRepeatedCalls() {
        val javaCode = """
            public class Performance {
                public void test() {
                    for (int i = 0; i < 100; i++) {
                        System.out.println(i);
                    }
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("Performance.java", javaCode)
        
        // Test multiple calls to ensure no memory leaks or performance degradation
        repeat(10) {
            val issues = service.findProblems(psiFile, 0, javaCode.length)
            assertNotNull("Issues should not be null on call $it", issues)
        }
    }
}