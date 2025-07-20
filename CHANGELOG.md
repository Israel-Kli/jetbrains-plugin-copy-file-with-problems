# Changelog

All notable changes to the "Copy with inline issues" plugin will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.4/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.6] - 2025-01-19

### 🛠️ Fixed

- **Deprecated API updates** - Replaced deprecated `AnActionEvent.getRequiredData()` calls with modern `getData()` API
- **Enhanced null safety** - Added explicit null checks for improved error handling and compatibility
- **Future-proofing** - Eliminated "scheduled for removal" API warnings in IntelliJ Platform 252+

### 🔧 Technical

- Updated `CopyWithInlineIssues.kt` to use `getData()` with null safety
- Updated `CopyFileWithInlineIssues.kt` to use `getData()` with null safety
- Maintained identical functionality while improving API compatibility

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

### 📋 Requirements

- IntelliJ IDEA 2024.2+ (Build 242+)
- Compatible with all IntelliJ-based IDEs

[Unreleased]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.6...HEAD
[1.0.6]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.0...v1.0.4
[1.0.0]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/commits/v1.0.0
