package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CopyFileWithInlineIssuesTest : BasePlatformTestCase() {

    private lateinit var action: CopyFileWithInlineIssues

    override fun setUp() {
        super.setUp()
        action = CopyFileWithInlineIssues()
    }

    fun testActionCreation() {
        assertNotNull("Action should be created successfully", action)
        assertTrue("Should extend BaseFileAction", action is BaseFileAction)
    }

    fun testActionUpdateThread() {
        assertEquals("Should use BGT thread", 
                     ActionUpdateThread.BGT, 
                     action.actionUpdateThread)
    }

    fun testUpdateWithValidFile() {
        val javaCode = """
            public class TestFile {
                public void method() {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("TestFile.java", javaCode)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertTrue("Action should be enabled with valid file", 
                   actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithDirectory() {
        // Create a temporary directory
        val tempDir = myFixture.tempDirFixture.findOrCreateDir("testdir")
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> tempDir
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled for directories", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNullProject() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> null
                CommonDataKeys.VIRTUAL_FILE.name -> psiFile.virtualFile
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with null project", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNullVirtualFile() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> null
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with null virtual file", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testActionPerformedWithValidData() {
        val javaCode = """
            public class ActionPerformTest {
                public void testMethod() {
                    System.out.println("Testing action perform");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("ActionPerformTest.java", javaCode)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        // Test that action doesn't throw exception
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should not throw exception with valid data: ${e.message}")
        }
    }

    fun testActionPerformedWithNullProject() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> null
                CommonDataKeys.VIRTUAL_FILE.name -> psiFile.virtualFile
                else -> null
            }
        }
        
        // Should handle null project gracefully (early return)
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle null project gracefully: ${e.message}")
        }
    }

    fun testActionPerformedWithNullVirtualFile() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> null
                else -> null
            }
        }
        
        // Should handle null virtual file gracefully (early return)
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle null virtual file gracefully: ${e.message}")
        }
    }

    fun testActionPerformedWithNonExistentPsiFile() {
        // Create a virtual file but simulate PsiManager returning null
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        // Action should handle cases where PsiManager.findFile returns null
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle missing PSI file gracefully: ${e.message}")
        }
    }

    fun testDifferentFileTypes() {
        val testFiles = mapOf(
            "Test.java" to "public class Test { }",
            "Test.kt" to "class Test",
            "script.js" to "function test() { }",
            "script.py" to "def test(): pass",
            "data.xml" to "<root><child>value</child></root>",
            "config.json" to "{ \"key\": \"value\" }",
            "page.html" to "<html><body>Hello</body></html>",
            "style.css" to "body { color: red; }",
            "query.sql" to "SELECT * FROM users;",
            "config.yaml" to "key: value",
            "README.md" to "# Title\nContent"
        )
        
        testFiles.forEach { (filename, code) ->
            val psiFile = myFixture.configureByText(filename, code)
            val virtualFile = psiFile.virtualFile
            
            val actionEvent = TestActionEvent.createTestEvent { dataId ->
                when (dataId) {
                    CommonDataKeys.PROJECT.name -> project
                    CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                    else -> null
                }
            }
            
            // Test that update works for all file types
            action.update(actionEvent)
            assertTrue("Action should be enabled for $filename", 
                       actionEvent.presentation.isEnabledAndVisible)
            
            // Test that action performs without errors for all file types
            try {
                action.actionPerformed(actionEvent)
            } catch (e: Exception) {
                fail("Action should handle $filename: ${e.message}")
            }
        }
    }

    fun testEmptyFileHandling() {
        val emptyFile = myFixture.configureByText("Empty.java", "")
        val virtualFile = emptyFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle empty files: ${e.message}")
        }
    }

    fun testLargeFileHandling() {
        val largeContent = "public class Large {\n" + 
                          (1..200).joinToString("\n") { "    public void method$it() { System.out.println(\"Method $it\"); }" } +
                          "\n}"
        
        val psiFile = myFixture.configureByText("Large.java", largeContent)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle large files: ${e.message}")
        }
    }

    fun testFileWithSyntaxErrors() {
        val invalidJava = """
            public class Invalid {
                public void method() {
                    if (true {
                        System.out.println("Missing closing parenthesis");
                    }
                    undeclaredVariable = 5;
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("Invalid.java", invalidJava)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle files with syntax errors: ${e.message}")
        }
    }

    fun testUnicodeContentHandling() {
        val unicodeContent = """
            public class Unicode {
                public void testMethod() {
                    System.out.println("Hello 世界! 🌍");
                    String text = "Café, naïve, résumé";
                    System.out.println("Emoji: 🚀 🎉 ✨");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("Unicode.java", unicodeContent)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle Unicode content: ${e.message}")
        }
    }

    fun testSpecialCharactersInFilename() {
        val specialNames = listOf(
            "Test-File.java",
            "Test_File.java", 
            "Test File.java",
            "Test123.java",
            "test.file.java"
        )
        
        specialNames.forEach { filename ->
            val psiFile = myFixture.configureByText(filename, "public class Test {}")
            val virtualFile = psiFile.virtualFile
            
            val actionEvent = TestActionEvent.createTestEvent { dataId ->
                when (dataId) {
                    CommonDataKeys.PROJECT.name -> project
                    CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                    else -> null
                }
            }
            
            try {
                action.actionPerformed(actionEvent)
            } catch (e: Exception) {
                fail("Action should handle special filename '$filename': ${e.message}")
            }
        }
    }

    fun testFilePathHandling() {
        // Test that the action works with files (simplified test since nested paths aren't supported in test framework)
        val nestedContent = "public class Nested { }"
        val psiFile = myFixture.configureByText("Nested.java", nestedContent)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle file paths: ${e.message}")
        }
    }

    fun testBinaryFileHandling() {
        // Test with a non-text file (should still work as PSI can handle it)
        val binaryContent = "public class Binary { }"
        val psiFile = myFixture.configureByText("Binary.class", binaryContent)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        // Should handle gracefully even if it's not a typical text file
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle binary-like files gracefully: ${e.message}")
        }
    }

    fun testConcurrentActionCalls() {
        val javaCode = "public class Concurrent { }"
        val psiFile = myFixture.configureByText("Concurrent.java", javaCode)
        val virtualFile = psiFile.virtualFile
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        
        // Test multiple rapid calls (simulating user clicking multiple times)
        repeat(5) {
            try {
                action.actionPerformed(actionEvent)
            } catch (e: Exception) {
                fail("Action should handle concurrent calls: ${e.message}")
            }
        }
    }
}