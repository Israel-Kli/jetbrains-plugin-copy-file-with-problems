# Copy File with Problems

![Build](https://github.com/Israel-Kli/intellij-plugin-copy-file-with-problems/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## 🔍 Copy Code with Errors & Warnings Inline

Never lose context when sharing code snippets or entire files. This plugin automatically includes all compilation errors, warnings, and syntax issues as inline comments.

<!-- Plugin description -->
**Copy File with Problems** is a powerful plugin for all JetBrains IDEs (IntelliJ IDEA, WebStorm, PyCharm, PhpStorm, etc.) that revolutionizes how you share and document code issues. 
Never lose context when copying code snippets or entire files - this plugin automatically includes all compilation errors, 
warnings, and syntax issues as inline comments using the correct comment syntax for each programming language.

## ✨ Key Features

- **📋 Smart Copy with Context** - Copy selected code with errors/warnings as inline comments
- **📄 Complete File Export** - Copy entire files with all detected problems included
- **🎯 Multi-Language Support** - Works with Java, Kotlin, JavaScript, TypeScript, Python, PHP, YAML, JSON, and many other file types
- **🔍 Comprehensive Error Detection** - Captures syntax errors, semantic issues, and inspection warnings
- **🌐 Context Menu Integration** - Seamlessly integrated into editor and project tree context menus
- **📍 Relative Path Headers** - Shows clean file paths relative to your project root
- **🗣️ Language-Aware Comments** - Automatically uses the correct comment syntax for each programming language
- **🔄 Cross-Platform Compatibility** - Works consistently across IntelliJ IDEA, WebStorm, PyCharm, and other JetBrains IDEs
- **⚡ Robust Error Detection** - Multiple fallback mechanisms ensure error detection works even when IDE-specific features aren't available

## 🚀 Perfect For

- **AI-Assisted Code Fixing** - Copy problematic files with inline errors directly to AI tools (e.g., Claude Code, ChatGPT, Gemini, or other external chats) for quick and accurate fixes.
- **Code Reviews** - Share problematic code with complete error context
- **Bug Reports** - Include exact error messages with the failing code
- **Team Collaboration** - Share code issues without losing IDE context
- **Documentation** - Document known issues with precise error details
- **Stack Overflow** - Post complete, context-rich code examples

## 💡 How It Works

1. **Right-click in editor** → Select "Copy With Problems" for selected text
2. **Right-click on file in project tree** → Select "Copy File with Problems" for entire files
3. **Paste anywhere** → Get your code with all errors as language-appropriate comments

## 📝 Example Output

The plugin automatically uses the correct comment syntax for each programming language:

**Python files:**
```python
# FILE: src/calculator.py

def calculate(a, b):
    result = a + c
    # ERROR: undefined variable 'c'
    return result
```

**Java files:**
```java
// FILE: src/Calculator.java

public class Calculator {
    public int add(int a, int b) {
        return a + c;
        // ERROR: cannot resolve symbol 'c'
    }
}
```

**HTML files:**
```html
<!-- FILE: index.html -->

<div>
    <p>Hello World</span>
    <!-- ERROR: mismatched closing tag -->
</div>
```

**SQL files:**
```sql
-- FILE: queries/users.sql

SELECT * FROM users
WHERE invalid_column = 'value';
-- ERROR: column 'invalid_column' doesn't exist
```

## 🔧 Advanced Detection

- **Java/Kotlin** - Compilation errors, missing return types, annotation issues, split identifiers
- **JavaScript/TypeScript** - Syntax errors, type mismatches, ESLint warnings
- **Python** - Syntax errors, import issues, PEP violations
- **PHP** - Parse errors, undefined variables, coding standard violations
- **YAML** - Block mapping errors, indentation issues, syntax violations
- **General** - PSI syntax errors, unresolved references, inspection warnings

*Transform your debugging workflow - never copy "silent" broken code again!*

## 🔍 Keywords & Search Terms
**AI Assistant Ready** | **Claude** | **ChatGPT** | **Gemini** | **Grok** | **Copy with Errors** | **Copy Warnings** | **Error Context** | **Bug Report** | **Code Review** | **Debugging** | **Syntax Errors** | **Compilation Errors** | **IDE Integration** | **Developer Tools** | **Code Analysis** | **Problem Detection** | **Error Messages** | **Warning Messages** | **Context Preservation** | **Code Sharing** | **Stack Overflow** | **Team Collaboration** | **Copy Code with Context** | **Error Inline Comments** | **Debug Helper** | **Code Quality** | **Issue Tracking** | **Development Workflow**

<!-- Plugin description end -->

## 🌐 Cross-Platform Excellence

This plugin has been specifically optimized for cross-platform compatibility:

### ✅ **Key Benefits:**

1. **🔄 Cross-Platform Compatibility**: Works consistently across IntelliJ IDEA, WebStorm, PyCharm, and other JetBrains IDEs
2. **🗣️ Language-Aware Comments**: Automatically uses the correct comment syntax for each programming language
3. **🔍 Robust Error Detection**: Multiple fallback mechanisms ensure error detection works even when IDE-specific features aren't available
4. **⚡ Better Performance**: Optimized inspection running with proper error handling and limits
5. **🧪 Comprehensive Testing**: Tests verify language-specific functionality and cross-platform compatibility

### 🎯 **Language-Specific Comment Support:**

- **Python, Ruby, Shell, YAML**: `# ERROR: message`
- **Java, JavaScript, TypeScript, Kotlin**: `// ERROR: message`
- **SQL, Lua, Haskell**: `-- ERROR: message`
- **HTML, XML**: `<!-- ERROR: message -->`
- **CSS**: `/* ERROR: message */`

### 🔧 **Enhanced Error Detection:**

- **PSI-based detection**: Works across all IDE environments
- **Inspection system integration**: Leverages IDE-specific inspections when available
- **Fallback mechanisms**: Ensures functionality even when some features aren't available
- **Multiple detection methods**: Combines different approaches for comprehensive error coverage

## Compatibility

This plugin is compatible with all JetBrains IDEs including:
- IntelliJ IDEA (Community & Ultimate)
- WebStorm
- PyCharm (Community & Professional)
- PhpStorm
- GoLand
- CLion
- DataGrip
- Rider
- And other JetBrains IDEs based on the IntelliJ Platform

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Copy File with Problems"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/Israel-Kli/intellij-plugin-copy-file-with-problems/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


Plugin is inspired by other plugins which have only partial functionality or are outdated, such as:
- [Code-File-Grabber](https://plugins.jetbrains.com/plugin/21269-code-file-grabber)
- [CopyWithProblems](https://plugins.jetbrains.com/plugin/23051-copywithproblems)
- [SLAMP](https://plugins.jetbrains.com/plugin/26544-slamp)

## Development

### Building the Plugin

To build the plugin ZIP file for distribution:

```bash
mkdir -p build/tmp/buildSearchableOptions && ./gradlew buildPlugin -x buildSearchableOptions
```

This creates the distributable ZIP at: `build/distributions/intellij-plugin-copy-file-with-problems-1.0.0.zip`

**If you encounter build issues, try a clean build:**
```bash
./gradlew clean buildPlugin -x buildSearchableOptions
```

**Alternative build commands:**
- `./gradlew build` - Full build with tests
- `./gradlew buildPlugin` - Full plugin build (may require closing IntelliJ)
- `./gradlew jar` - Compile only, faster for development

### Testing the Plugin

To run IntelliJ IDEA with the plugin for testing:

```bash
./gradlew runIde
```

This launches a sandbox IntelliJ instance with your plugin pre-installed.

### Installation from Source

After building:
1. Install the ZIP file: <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install Plugin from Disk</kbd>
2. Select: `build/distributions/intellij-plugin-copy-file-with-problems-1.0.0.zip`
3. Restart IntelliJ to ensure clean plugin loading
4. Test both actions:
   - **Editor**: Select text → right-click → "Copy With Problems"
   - **Project Tree**: Right-click file → "Copy File with Problems"

---