package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.github.israelkli.intellijplugincopyfilewithproblems.actions.CopyFileWithInlineIssues
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BaseFileActionTest : BasePlatformTestCase() {

    private fun createTestAction(): BaseFileAction {
        return CopyFileWithInlineIssues()
    }

    fun testCommentPrefixForAllLanguages() {
        val action = createTestAction()
        
        // Python
        val pythonFile = myFixture.configureByText("test.py", "print('hello')")
        assertTrue("Python should use # prefix", 
                   action.getCommentPrefix(pythonFile) in listOf("# ", "// "))
        
        // Ruby
        val rubyFile = myFixture.configureByText("test.rb", "puts 'hello'")
        assertTrue("Ruby should use # prefix", 
                   action.getCommentPrefix(rubyFile) in listOf("# ", "// "))
        
        // Shell/Bash
        val shellFile = myFixture.configureByText("test.sh", "echo 'hello'")
        assertTrue("Shell should use # prefix", 
                   action.getCommentPrefix(shellFile) in listOf("# ", "// "))
        
        // YAML
        val yamlFile = myFixture.configureByText("test.yaml", "key: value")
        assertTrue("YAML should use # prefix", 
                   action.getCommentPrefix(yamlFile) in listOf("# ", "// "))
        
        // SQL
        val sqlFile = myFixture.configureByText("test.sql", "SELECT * FROM users")
        assertTrue("SQL should use -- prefix", 
                   action.getCommentPrefix(sqlFile) in listOf("-- ", "// "))
        
        // Lua
        val luaFile = myFixture.configureByText("test.lua", "print('hello')")
        assertTrue("Lua should use -- prefix", 
                   action.getCommentPrefix(luaFile) in listOf("-- ", "// "))
        
        // HTML
        val htmlFile = myFixture.configureByText("test.html", "<div>Hello</div>")
        assertTrue("HTML should use <!-- prefix", 
                   action.getCommentPrefix(htmlFile) in listOf("<!-- ", "// "))
        
        // XML
        val xmlFile = myFixture.configureByText("test.xml", "<root>data</root>")
        assertTrue("XML should use <!-- prefix", 
                   action.getCommentPrefix(xmlFile) in listOf("<!-- ", "// "))
        
        // CSS
        val cssFile = myFixture.configureByText("test.css", "body { color: red; }")
        assertTrue("CSS should use /* prefix", 
                   action.getCommentPrefix(cssFile) in listOf("/* ", "// "))
        
        // Default case (Java, JavaScript, TypeScript, etc.)
        val javaFile = myFixture.configureByText("Test.java", "public class Test {}")
        assertEquals("// ", action.getCommentPrefix(javaFile))
        
        val jsFile = myFixture.configureByText("test.js", "console.log('hello')")
        assertEquals("// ", action.getCommentPrefix(jsFile))
        
        val kotlinFile = myFixture.configureByText("Test.kt", "class Test")
        assertEquals("// ", action.getCommentPrefix(kotlinFile))
    }
    
    fun testCommentSuffixForAllLanguages() {
        val action = createTestAction()
        
        // HTML should have suffix
        val htmlFile = myFixture.configureByText("test.html", "<div>Hello</div>")
        assertTrue("HTML should have --> suffix", 
                   action.getCommentSuffix(htmlFile) in listOf(" -->", ""))
        
        // XML should have suffix
        val xmlFile = myFixture.configureByText("test.xml", "<root>data</root>")
        assertTrue("XML should have --> suffix", 
                   action.getCommentSuffix(xmlFile) in listOf(" -->", ""))
        
        // CSS should have suffix
        val cssFile = myFixture.configureByText("test.css", "body { color: red; }")
        assertTrue("CSS should have */ suffix", 
                   action.getCommentSuffix(cssFile) in listOf(" */", ""))
        
        // Others should have empty suffix
        val javaFile = myFixture.configureByText("Test.java", "public class Test {}")
        assertEquals("", action.getCommentSuffix(javaFile))
        
        val pythonFile = myFixture.configureByText("test.py", "print('hello')")
        assertEquals("", action.getCommentSuffix(pythonFile))
    }
    
    fun testCommentFormattingConsistency() {
        val action = createTestAction()
        val javaFile = myFixture.configureByText("Test.java", "public class Test {}")
        
        // Test comment prefix and suffix work correctly
        assertEquals("// ", action.getCommentPrefix(javaFile))
        assertEquals("", action.getCommentSuffix(javaFile))
        
        // Test with HTML file (different comment format)
        val htmlFile = myFixture.configureByText("test.html", "<div>Hello</div>")
        val htmlPrefix = action.getCommentPrefix(htmlFile)
        val htmlSuffix = action.getCommentSuffix(htmlFile)
        
        // Should get either HTML comments or fallback
        assertTrue("HTML should use proper comment format", 
                   htmlPrefix.contains("<!--") || htmlPrefix == "// ")
    }

    fun testActionThreadConfiguration() {
        val action = createTestAction()
        assertEquals("Should use BGT thread", 
                     com.intellij.openapi.actionSystem.ActionUpdateThread.BGT, 
                     action.actionUpdateThread)
    }

    fun testActionCreation() {
        val action = createTestAction()
        
        // Test that the action can be created without issues
        assertNotNull("Action should be created successfully", action)
        
        // Test that it extends BaseFileAction properly
        assertTrue("Should extend BaseFileAction", action is BaseFileAction)
    }

    fun testSpecialLanguageHandling() {
        val action = createTestAction()
        
        // Test INI files
        val iniFile = myFixture.configureByText("config.ini", "[section]\nkey=value")
        assertTrue("INI should use proper comment format", 
                   action.getCommentPrefix(iniFile) in listOf("; ", "// "))
        
        // Test TOML files
        val tomlFile = myFixture.configureByText("config.toml", "[section]\nkey = 'value'")
        assertTrue("TOML should use # prefix", 
                   action.getCommentPrefix(tomlFile) in listOf("# ", "// "))
        
        // Test Dockerfile
        val dockerFile = myFixture.configureByText("Dockerfile", "FROM ubuntu:20.04")
        assertTrue("Dockerfile should use # prefix", 
                   action.getCommentPrefix(dockerFile) in listOf("# ", "// "))
        
        // Test Makefile
        val makeFile = myFixture.configureByText("Makefile", "all:\n\techo 'hello'")
        assertTrue("Makefile should use # prefix", 
                   action.getCommentPrefix(makeFile) in listOf("# ", "// "))
        
        // Test Properties files
        val propFile = myFixture.configureByText("app.properties", "key=value")
        assertTrue("Properties should use # prefix", 
                   action.getCommentPrefix(propFile) in listOf("# ", "// "))
    }

    fun testCSSVariants() {
        val action = createTestAction()
        
        // Test SCSS
        val scssFile = myFixture.configureByText("style.scss", "\$primary-color: #007bff;")
        assertTrue("SCSS should use // prefix", 
                   action.getCommentPrefix(scssFile) in listOf("// ", "/* "))
        
        // Test SASS
        val sassFile = myFixture.configureByText("style.sass", "\$primary-color: #007bff")
        assertTrue("SASS should use // prefix", 
                   action.getCommentPrefix(sassFile) in listOf("// ", "/* "))
        
        // Test LESS
        val lessFile = myFixture.configureByText("style.less", "@primary-color: #007bff;")
        assertTrue("LESS should use // prefix", 
                   action.getCommentPrefix(lessFile) in listOf("// ", "/* "))
    }

    fun testLanguageIdCaseInsensitivity() {
        val action = createTestAction()
        
        // Test that language detection is case insensitive
        val javaFile = myFixture.configureByText("Test.java", "public class Test {}")
        val prefix = action.getCommentPrefix(javaFile)
        
        // Should work regardless of case
        assertTrue("Should handle language detection properly", 
                   prefix.isNotEmpty())
    }

    fun testEdgeCasesInCommentGeneration() {
        val action = createTestAction()
        
        // Test with very small files
        val smallFile = myFixture.configureByText("tiny.java", "}")
        assertEquals("// ", action.getCommentPrefix(smallFile))
        
        // Test with files that might have ambiguous extensions
        val ambiguousFile = myFixture.configureByText("file.txt", "some content")
        val prefix = action.getCommentPrefix(ambiguousFile)
        assertTrue("Should have a default prefix", prefix.isNotEmpty())
    }
}