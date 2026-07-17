# Changelog

All notable changes to Spring Daily Lab are documented here. The project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Initial task-management API with Flyway-backed persistence, validation, and health checks.

## Release process

1. Move completed entries from **Unreleased** into a versioned section with its release date.
2. Create and push an annotated tag named `vMAJOR.MINOR.PATCH`.
3. The release workflow verifies that the tag points at the tagged commit and creates a GitHub Release with generated notes.

GitHub-generated notes supplement this curated changelog; they do not replace it.
