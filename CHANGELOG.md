# Changelog

All notable changes to the "Copy File with Problems" plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-13

### 🎉 Initial Release

#### ✨ Added
- **Copy With Problems** action for selected text in editor
- **Copy File with Problems** action for entire files in project tree
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
  - Full file: Shows relative path from project root
- **Context menu integration:**
  - Editor popup menu for selected text
  - Project view popup menu for files
- **Multi-language support** for all file types
- Custom icons for both actions

#### 🔧 Technical Features
- Multiple error detection strategies for comprehensive coverage
- Relative path calculation from project root
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
