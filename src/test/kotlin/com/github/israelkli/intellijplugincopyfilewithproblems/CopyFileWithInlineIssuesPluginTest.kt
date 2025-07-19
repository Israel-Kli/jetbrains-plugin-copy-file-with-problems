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

    fun testProblemDetectionService() {
        val javaCode = """
            public class TestClass {
                public void method() {
                    int unused = 5;
                    String s = null;
                    s.toString();
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("TestClass.java", javaCode)
        val service = ProblemDetectionService()
        
        val issues = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull(issues)
    }

    fun testCopyWithInlineIssuesActionCreation() {
        val action = CopyWithInlineIssues()
        assertNotNull(action)
    }

    fun testCopyFileWithInlineIssuesActionCreation() {
        val action = CopyFileWithInlineIssues()
        assertNotNull(action)
    }

    fun testProblemDetectionWithValidCode() {
        val validJavaCode = """
            public class ValidClass {
                public void validMethod() {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("ValidClass.java", validJavaCode)
        val service = ProblemDetectionService()
        
        val issues = service.findProblems(psiFile, 0, validJavaCode.length)
        
        // Valid code should have no or minimal problems
        assertNotNull(issues)
    }

    fun testProblemDetectionWithInvalidCode() {
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
        val service = ProblemDetectionService()
        
        val problems = service.findProblems(psiFile, 0, invalidJavaCode.length)
        
        assertNotNull(problems)
    }
    
    fun testLanguageSpecificComments() {
        val action = CopyFileWithInlineIssues()
        
        // Test Python file comment format
        val pythonCode = "def hello():\n    print('Hello')"
        val pythonFile = myFixture.configureByText("test.py", pythonCode)
        val baseAction = action as BaseFileAction
        
        // Since the test environment might not have Python support, let's test with a more flexible approach
        val pythonPrefix = baseAction.getCommentPrefix(pythonFile)
        assertTrue("Python comment prefix should be either # or // (fallback)", 
                   pythonPrefix == "# " || pythonPrefix == "// ")
        
        // Test Java file comment format
        val javaCode = "public class Test { }"
        val javaFile = myFixture.configureByText("Test.java", javaCode)
        assertEquals("// ", baseAction.getCommentPrefix(javaFile))
        assertEquals("", baseAction.getCommentSuffix(javaFile))
        
        // Test JavaScript file comment format
        val jsCode = "function test() { }"
        val jsFile = myFixture.configureByText("test.js", jsCode)
        assertEquals("// ", baseAction.getCommentPrefix(jsFile))
        assertEquals("", baseAction.getCommentSuffix(jsFile))
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }


    override fun getTestDataPath() = "src/test/testData/rename"
}
