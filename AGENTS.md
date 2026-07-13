# Repository automation rules

These rules apply to every automated or manual change in this repository.

## Scope selection

- Read `ROADMAP.md`, open issues, open pull requests, and recent commits before choosing work.
- Prefer unchecked roadmap items with clear acceptance criteria.
- Never start an item already covered by an open pull request or remote `agent/*` branch.
- Up to ten independent improvements may be proposed in one daily run. Stop early when fewer worthwhile items exist.

## Change quality

- One pull request must represent one coherent feature, fix, test improvement, documentation correction, or maintenance task.
- Never create empty commits, timestamp-only changes, generated noise, meaningless formatting churn, or artificial commit splits.
- Keep API compatibility unless the roadmap item explicitly calls for a breaking change.
- Add or update tests for behavior changes and update documentation when user-facing behavior changes.
- Database changes require a new Flyway migration; never edit an applied migration.

## Verification and publishing

- Run `./gradlew check --no-daemon` before publishing.
- Inspect `git status` and stage only files belonging to the selected item.
- Use an `agent/<short-description>` branch and a terse conventional commit message.
- Push the branch and open a draft pull request against `main` with the motivation, impact, and checks performed.
- Do not merge automatically, bypass CI, force-push shared branches, or modify unrelated user work.
- Mark a roadmap checkbox in the same branch only when that PR fully satisfies its acceptance criteria.
