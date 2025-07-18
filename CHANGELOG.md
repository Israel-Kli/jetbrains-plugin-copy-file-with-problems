# Changelog

All notable changes to the "Copy File with Problems" plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.5] - 2025-01-18

### 🔄 Changed
- **Complete name consistency** - Updated all references from "Copy File with Problems" to "Copy with inline issues" across the entire project
- **Fixed marketplace display** - Plugin now correctly shows as "Copy with inline issues" in JetBrains Marketplace
- **Updated documentation** - All installation instructions and references use the new name consistently
- **Code cleanup** - Consistent naming throughout codebase, configuration files, and action classes

## [1.0.4] - 2025-01-18

### 🔄 Changed
- **Plugin name** updated from "Copy File with Problems" to "Copy with inline issues"
- **Plugin description** updated to reflect new branding
- **Version bumped** to 1.0.4 across all configuration files
- **Documentation** updated to reflect new plugin name

### 🛠️ Technical
- Added CLAUDE.md for development guidance and Claude Code integration
- Updated GitHub Actions workflow with proper token usage
- **Automatic deployment** - Added automated plugin publishing to JetBrains Marketplace
- Improved build documentation and distribution paths

## [1.0.0] - 2025-01-13

### 🎉 Initial Release

#### ✨ Added
- **Copy With Problems** action for selected text in the editor
- **Copy File With Inline Issues** action for entire files in a project tree
- Comprehensive error detection system with multiple detection methods:
  - PSI syntax error detection
  - Editor markup highlight analysis
  - IntelliJ inspection system integration
  - PsiReference resolution for unresolved symbols
- **Java-specific validations:**
  - Split identifier detection (e.g., "cacheM anager" → "cacheManager")
  - Method declaration validation
  - Annotation placement verification
- **YAML-specific validations:**
  - Block mapping error detection ("Invalid child element in a block mapping")
  - Indentation validation
  - Tab usage detection
  - Special character warnings
- **Smart file headers:**
  - Selected text: Shows filename only
  - Full file: Shows a relative path from the project root
- **Context menu integration:**
  - Editor popup menu for selected text
  - Project view popup menu for files
- **Multi-language support** for all file types
- Custom icons for both actions

#### 🔧 Technical Features
- Multiple error detection strategies for comprehensive coverage
- Relative path calculation from the project root
- Distinct severity levels (ERROR, WARNING, INFO)
- Pattern-based validation for common syntax issues
- Integration with IntelliJ's inspection profile system

#### 🎯 Use Cases
- Code reviews with complete error context
- Bug reporting with precise error information
- Team collaboration without losing IDE context
- Documentation of known issues
- Stack Overflow posts with comprehensive context

### 📋 Requirements
- IntelliJ IDEA 2024.2+ (Build 242+)
- Compatible with all IntelliJ-based IDEs

### 🚀 Installation
Install directly from the JetBrains Marketplace or download the plugin ZIP file.
