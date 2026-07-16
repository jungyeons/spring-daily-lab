# README language synchronization

`README.md` is the Korean README and `README.en.md` is the English README. Both are first-class entry points: neither is generated from the other.

When a change affects setup, API behavior, supported configuration, verification, repository policy, or links, update both files in the same pull request. Keep their section order aligned so reviewers can compare them quickly:

1. Introduction and language links
2. Technology and run instructions
3. API examples and endpoint table
4. Verification and database configuration
5. Contribution/automation policy and license

Translations may use natural wording, but commands, paths, environment-variable names, HTTP methods, JSON field names, and default values must be identical. Reviewers should confirm the language links work and that no behavior is documented in only one language.
