package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CopyWithInlineIssuesTest : BasePlatformTestCase() {

    private lateinit var action: CopyWithInlineIssues

    override fun setUp() {
        super.setUp()
        action = CopyWithInlineIssues()
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

    fun testActionText() {
        // Test that the action has the correct display text
        val actionEvent = TestActionEvent.createTestEvent()
        assertNotNull("Action should have presentation", actionEvent.presentation)
    }

    fun testUpdateWithValidSelection() {
        val javaCode = """
            public class TestClass {
                public void method() {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("TestClass.java", javaCode)
        myFixture.editor.selectionModel.setSelection(0, javaCode.length)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertTrue("Action should be enabled with valid selection", 
                   actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNoSelection() {
        val javaCode = "public class TestClass {}"
        val psiFile = myFixture.configureByText("TestClass.java", javaCode)
        
        // No selection
        myFixture.editor.selectionModel.removeSelection()
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with no selection", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNullProject() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> null
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> null
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with null project", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNullEditor() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> null
                CommonDataKeys.PSI_FILE.name -> null
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with null editor", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testUpdateWithNullPsiFile() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> null
                else -> null
            }
        }
        
        action.update(actionEvent)
        
        assertFalse("Action should be disabled with null PSI file", 
                    actionEvent.presentation.isEnabledAndVisible)
    }

    fun testActionPerformedWithValidData() {
        val javaCode = """
            public class ActionTest {
                public void testMethod() {
                    System.out.println("Testing action");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("ActionTest.java", javaCode)
        myFixture.editor.selectionModel.setSelection(0, javaCode.length)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
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
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> null
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> null
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

    fun testActionPerformedWithNullEditor() {
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> null
                CommonDataKeys.PSI_FILE.name -> null
                else -> null
            }
        }
        
        // Should handle null editor gracefully (early return)
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle null editor gracefully: ${e.message}")
        }
    }

    fun testSelectionHandling() {
        val javaCode = """
            public class SelectionTest {
                public void method1() {
                    System.out.println("Line 1");
                }
                
                public void method2() {
                    System.out.println("Line 2");
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("SelectionTest.java", javaCode)
        
        // Test partial selection
        val startOffset = javaCode.indexOf("method1")
        val endOffset = javaCode.indexOf("}", startOffset) + 1
        myFixture.editor.selectionModel.setSelection(startOffset, endOffset)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        assertTrue("Should have selection", myFixture.editor.selectionModel.hasSelection())
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle partial selection: ${e.message}")
        }
    }

    fun testMultilineSelectionHandling() {
        val javaCode = """
            public class MultilineTest {
                public void multilineMethod() {
                    int x = 1;
                    int y = 2;
                    int z = x + y;
                    System.out.println(z);
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("MultilineTest.java", javaCode)
        
        // Select multiple lines
        val startOffset = javaCode.indexOf("int x")
        val endOffset = javaCode.indexOf("System.out.println(z);") + "System.out.println(z);".length
        myFixture.editor.selectionModel.setSelection(startOffset, endOffset)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle multiline selection: ${e.message}")
        }
    }

    fun testDifferentFileTypes() {
        val testFiles = mapOf(
            "test.java" to "public class Test { }",
            "test.kt" to "class Test",
            "test.js" to "function test() { }",
            "test.py" to "def test(): pass",
            "test.xml" to "<root><child>value</child></root>",
            "test.json" to "{ \"key\": \"value\" }",
            "test.html" to "<html><body>Hello</body></html>"
        )
        
        testFiles.forEach { (filename, code) ->
            val psiFile = myFixture.configureByText(filename, code)
            myFixture.editor.selectionModel.setSelection(0, code.length)
            
            val actionEvent = TestActionEvent.createTestEvent { dataId ->
                when (dataId) {
                    CommonDataKeys.PROJECT.name -> project
                    CommonDataKeys.EDITOR.name -> myFixture.editor
                    CommonDataKeys.PSI_FILE.name -> psiFile
                    else -> null
                }
            }
            
            try {
                action.actionPerformed(actionEvent)
            } catch (e: Exception) {
                fail("Action should handle $filename: ${e.message}")
            }
        }
    }

    fun testEmptySelectionHandling() {
        val javaCode = "public class Empty { }"
        val psiFile = myFixture.configureByText("Empty.java", javaCode)
        
        // Set selection to empty range
        myFixture.editor.selectionModel.setSelection(10, 10)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        // Action should return early if no actual selection
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle empty selection gracefully: ${e.message}")
        }
    }

    fun testLargeSelectionHandling() {
        val largeCode = "public class Large {\n" + 
                       (1..50).joinToString("\n") { "    public void method$it() { System.out.println(\"Method $it\"); }" } +
                       "\n}"
        
        val psiFile = myFixture.configureByText("Large.java", largeCode)
        myFixture.editor.selectionModel.setSelection(0, largeCode.length)
        
        val actionEvent = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> psiFile
                else -> null
            }
        }
        
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle large selections: ${e.message}")
        }
    }
}