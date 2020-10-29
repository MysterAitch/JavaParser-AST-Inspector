# Changelog

## [Unreleased]
### Added

### Changed
- Upgraded to using JavaParser v3.16.2
- Upgraded multiple dependencies
- Upgraded compatability with recent intellij builds
- Switched to kotlin DSL for builds

### Deprecated

### Removed

### Fixed

### Security


## [0.4.3]
### Changed
- Upgraded to using JavaParser v3.15.21

## [0.4.2]
### Added
- Initial submitted release of the plugin to the Jetbrains plugin repo, using JavaParser v3.15.18
- Parsing via the plugin UI is feature complete, with analysis/generation/symbol resolution to come in future versions.
- Key highlights:
    - Display of the AST produced, which can be explored (clicking on an item in the AST will highlight the relevant section of source code)
    - Display of the parsed tokens
    - Exporting of alternative representations of the AST (including as YAML, DOT, XML, Cypher, and others)
    - Being able to view a log of parse attempts (including any errors, and the configuration used in the parse)

