<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# ngxs Changelog

## [Unreleased]
### Fixed
- Compatibility issues

### Changed
- Support all future version of Jetbrains

### Added
- Support for NGXS Version 18

## [0.0.9]
### Changed
- Synced with Template Changes
- Supports version 2024.2 or 2024.*

## [0.0.8] - 2023-10-19

### Added

- Introducing FileTypes for `*.state.ts;*actions.ts;*.selectors.ts` for Jetbrains to recommend NGXS plugin

### Fixed

- Fixed Error - Java.lang.RuntimeException: java.lang.Exception: NgxsAnnotator

## [0.0.7] - 2023-10-15

### Fixed

- Error - Dir is null when loading IntentionActions.

### Added

- #30 - NGXS Advance Live Templates 
  - `methodName-ClassName-action`
  - `methodName-payload1:string,payload2:string-action-payload`
  - `methodName-ClassName-payload1:string,payload2:string-action-payload`
  - `methodName-selector-meta`
  - `methodName-selector`

## [0.0.6] - 2023-10-07

### Added

- #4 - NGXS Live templates 

### Fixed

- Moved all the deprecated APIs to latest ones.

## [0.0.5] - 2023-09-19

### Added

- #20 - Code insights/Quickfix when an Action has no implementation (create with Payload) - Part 2

## [0.0.4] - 2023-09-18

### Added

- #20 - Code insights/Quickfix when an Action has no implementation in the *.state.ts

### Fixed

- Duplicate Actions will not show in gutter

### Changed

- ActionIcon - increased size.

## [0.0.3] - 2023-09-08

### Added

- #5 - Gutter Icons for linking states and actions

## [0.0.2] - 2023-09-01

### Fixed

- Removing `nbsp` after files are generated [ngxs/cli#9](https://github.com/ngxs/cli/issues/9) within intellij
- Upgrading redux store package from `0.5.5` -> `0.6.1`

## [0.0.1] - 2023-08-26

### Added

- Initial NGXS Store Generation
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Unreleased]: https://github.com/dinbtechit/ngxs/compare/v0.0.8...HEAD
[0.0.8]: https://github.com/dinbtechit/ngxs/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/dinbtechit/ngxs/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/dinbtechit/ngxs/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/dinbtechit/ngxs/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/dinbtechit/ngxs/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/dinbtechit/ngxs/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/dinbtechit/ngxs/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/dinbtechit/ngxs/commits/v0.0.1
