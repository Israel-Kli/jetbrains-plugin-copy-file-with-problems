# Copy File with Problems

![Build](https://github.com/Israel-Kli/intellij-plugin-copy-file-with-problems/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## 🔍 Copy Code with Errors & Warnings Inline

Never lose context when sharing code snippets or entire files. This plugin automatically includes all compilation errors, warnings, and syntax issues as inline comments.

<!-- Plugin description -->
**Copy File with Problems** is a powerful IntelliJ IDEA plugin that revolutionizes how you share and document code issues. 
Never lose context when copying code snippets or entire files - this plugin automatically includes all compilation errors, 
warnings, and syntax issues as inline comments.

## ✨ Key Features

- **📋 Smart Copy with Context** - Copy selected code with errors/warnings as inline comments
- **📄 Complete File Export** - Copy entire files with all detected problems included
- **🎯 Multi-Language Support** - Works with Java, Kotlin, YAML, JSON, and many other file types
- **🔍 Comprehensive Error Detection** - Captures syntax errors, semantic issues, and inspection warnings
- **🌐 Context Menu Integration** - Seamlessly integrated into editor and project tree context menus
- **📍 Relative Path Headers** - Shows clean file paths relative to your project root

## 🚀 Perfect For

- **Code Reviews** - Share problematic code with complete error context
- **Bug Reports** - Include exact error messages with the failing code
- **Team Collaboration** - Share code issues without losing IDE context
- **Documentation** - Document known issues with precise error details
- **Stack Overflow** - Post complete, context-rich code examples

## 💡 How It Works

1. **Right-click in editor** → Select "Copy With Problems" for selected text
2. **Right-click on file in project tree** → Select "Copy File with Problems" for entire files
3. **Paste anywhere** → Get your code with all errors as // ERROR: comments

## 🔧 Advanced Detection

- **Java/Kotlin** - Compilation errors, missing return types, annotation issues, split identifiers
- **YAML** - Block mapping errors, indentation issues, syntax violations
- **General** - PSI syntax errors, unresolved references, inspection warnings

*Transform your debugging workflow - never copy "silent" broken code again!*
<!-- Plugin description end -->

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
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
