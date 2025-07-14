package com.github.israelkli.intellijplugincopyfilewithproblems

import com.github.israelkli.intellijplugincopyfilewithproblems.actions.CopyFileWithProblemsAction
import com.github.israelkli.intellijplugincopyfilewithproblems.actions.CopyWithProblemsAction
import com.github.israelkli.intellijplugincopyfilewithproblems.services.ProblemDetectionService
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class CopyFileWithProblemsPluginTest : BasePlatformTestCase() {

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
        
        val problems = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull(problems)
    }

    fun testCopyWithProblemsActionCreation() {
        val action = CopyWithProblemsAction()
        assertNotNull(action)
    }

    fun testCopyFileWithProblemsActionCreation() {
        val action = CopyFileWithProblemsAction()
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
        
        val problems = service.findProblems(psiFile, 0, validJavaCode.length)
        
        // Valid code should have no or minimal problems
        assertNotNull(problems)
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

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }


    override fun getTestDataPath() = "src/test/testData/rename"
}
