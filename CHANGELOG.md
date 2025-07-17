# Changelog

All notable changes to the "Copy File with Problems" plugin will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.2] - 2025-07-17

- Fix the sign and publish by @Israel-Kli in https://github.com/Israel-Kli/jetbrains-plugin-copy-file-with-problems/pull/7

## [1.0.0] - 2025-01-13

### 🎉 Initial Release

- **Copy With Problems** action for selected text in the editor
- **Copy File with Problems** action for entire files in a project tree
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

[Unreleased]: https://github.com/Israel-Kli/idea-plugin-copy-file-with-problems/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/Israel-Kli/idea-plugin-copy-file-with-problems/compare/v1.0.0...v1.0.2
[1.0.0]: https://github.com/Israel-Kli/idea-plugin-copy-file-with-problems/commits/v1.0.0
