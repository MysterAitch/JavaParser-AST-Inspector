# Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.5.2]
### Changed
- Bumped `pluginUntilBuild` to allow for 2021.3
- Upgraded to using JavaParser v3.24.0
- Update the project with recent template project changes (workflow tweaks, added qodana, keeping detekt (for now))
- Multiple other dependency updates

## [0.5.1]
### Changed
- Builds now require JDK11 minimum, per IntelliJ Plugin requirement
- Upgraded to using JavaParser v3.23.0
- Multiple other dependency updates

## [0.5.0]
### Added
- Detection of the IntelliJ's project language level, and default to that within the AST Inspector

### Changed
- Upgraded to using JavaParser v3.22.1
- Upgraded multiple dependencies
- Made the plugin `dumbAware`, enabling it to be used while the project is being indexed
- The exporters now use the system's newline, rather than `\n`

### Fixed
- Exporters now respect the `include new line` configuration option

## [0.4.5]
### Changed
- Upgraded to using JavaParser v3.20.2
- Upgraded many other dependencies too
- Upgraded compatibility with recent intellij builds

## [0.4.4]
### Changed
- Upgraded to using JavaParser v3.16.2
- Upgraded multiple dependencies
- Upgraded compatibility with recent intellij builds
- Switched to kotlin DSL for builds

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
