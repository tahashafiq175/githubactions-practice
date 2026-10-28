# GitHub Actions — Filters and Activity Types: The Complete Beginner-to-Advanced Guide

> **The goal of this guide:** Understand how GitHub Actions decides which events qualify for a workflow, which branches and files matter, and which specific activities should trigger automation.

Imagine GitHub Actions as an automated worker. An event tells the worker that something happened. Filters decide whether that event qualifies. Activity types identify the specific action that happened.

By the end of this guide, you will understand these concepts deeply enough to design practical CI/CD workflows without blindly copying YAML.

---

# Table of Contents

1. [The Big Picture](#1-the-big-picture)
2. [What Is an Event?](#2-what-is-an-event)
3. [What Is a Filter?](#3-what-is-a-filter)
4. [What Is an Activity Type?](#4-what-is-an-activity-type)
5. [Event vs Filter vs Activity Type](#5-event-vs-filter-vs-activity-type)
6. [Branch Filters](#6-branch-filters)
7. [Tag Filters](#7-tag-filters)
8. [Path Filters](#8-path-filters)
9. [Branch and Path Filters Together](#9-branch-and-path-filters)
10. [Positive and Negative Patterns](#10-positive-and-negative-patterns)
11. [Pull Request Activity Types](#11-pull-request-activity-types)
12. [Issue Activity Types](#12-issue-activity-types)
13. [Release Activity Types](#13-release-activity-types)
14. [Workflow Run Activity Types](#14-workflow-run-activity-types)
15. [Other Useful Activity Types](#15-other-useful-activity-types)
16. [Combining Filters and Activity Types](#16-combining-filters-and-activity-types)
17. [Understanding AND and OR Logic](#17-understanding-and-and-or-logic)
18. [Four Complete Practical Workflows](#18-four-complete-practical-workflows)
19. [Common Mistakes](#19-common-mistakes)
20. [Troubleshooting](#20-troubleshooting)
21. [Real-World CI/CD Examples](#21-real-world-cicd-examples)
22. [Final Mental Model](#22-final-mental-model)

---

# 1. The Big Picture

Imagine you are the manager of a factory.

Your factory receives many events:

- Raw material arrives.
- A customer places an order.
- A manager requests a report.
- A machine finishes a task.

You do not want every worker to react to every event.

For example, a worker responsible for packing boxes should not start packing when the factory receives a maintenance report.

You need rules that decide:

1. Which events matter?
2. Which specific activities matter?
3. Which conditions must be satisfied?

GitHub Actions uses a similar system.

Consider this workflow:

```yaml
name: Backend CI

on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'
```

This workflow is configured for push events, but not every push qualifies.

The push must match the branch filter and the path filter.

In simple words:

> Run this workflow when a push affects the `main` branch and the changes match the `backend/**` path pattern.

The workflow does not need to react to every change in the repository.

That is the purpose of filters.

Now consider:

```yaml
name: Pull Request CI

on:
  pull_request:
    types:
      - opened
      - synchronize
```

This workflow is configured to react when a pull request is opened or updated with new commits.

The `types` field selects the activity types for the event.

That is the purpose of activity types.

---

# 2. What Is an Event?

An event represents something that happens in or around a GitHub repository.

Examples include:

| Event | What happened? |
|---|---|
| `push` | Commits were pushed to a branch or tag |
| `pull_request` | A pull request activity occurred |
| `issues` | An issue activity occurred |
| `release` | A release activity occurred |
| `workflow_run` | Another workflow run reached a particular activity state |
| `schedule` | A scheduled execution time arrived |
| `workflow_dispatch` | A user manually requested a workflow run |

An event answers:

**What kind of thing happened?**

For example:

```yaml
on:
  push:
```

This tells GitHub that push events can trigger the workflow.

However, you may not want every push to trigger it.

Perhaps you only care about pushes to `main`.

That is where filters enter the picture.

---

# 3. What Is a Filter?

A filter is a rule that restricts which events qualify for a workflow.

Imagine your repository has these branches:

```text
main
develop
feature/login
feature/payment
release/v1
```

You may want to run production-related CI only when `main` receives changes.

You can write:

```yaml
on:
  push:
    branches:
      - main
```

Now GitHub evaluates the branch filter for push events.

| Push target | Does the branch filter match? |
|---|---|
| `main` | Yes |
| `develop` | No |
| `feature/login` | No |
| `feature/payment` | No |
| `release/v1` | No |

A matching event can trigger the workflow, provided the remaining requirements are satisfied.

## Common filters

| Filter | Purpose |
|---|---|
| `branches` | Include matching branch names |
| `branches-ignore` | Exclude matching branch names |
| `tags` | Include matching tag names |
| `tags-ignore` | Exclude matching tag names |
| `paths` | Include matching changed file paths |
| `paths-ignore` | Exclude events based on changed file paths |

Not every event supports every filter. For example, `branches` and `paths` are commonly used with `push` and `pull_request`, but they are not universal properties of all GitHub events.

**Important:** Filters restrict which events qualify. They do not change the event itself.

A push to `develop` is still a push event even when a workflow only listens for pushes to `main`.

---

# 4. What Is an Activity Type?

An activity type identifies a particular kind of action within an event category.

Imagine a pull request.

A pull request can be:

- Opened.
- Updated with new commits.
- Reopened after being closed.
- Closed.

These activities belong to the `pull_request` event.

You can choose which ones should trigger your workflow.

```yaml
on:
  pull_request:
    types:
      - opened
      - synchronize
```

This means the workflow responds to the selected pull request activity types.

## Why do activity types exist?

Suppose your team opens a pull request.

Your CI workflow should test the code when the pull request is opened.

Later, the developer pushes new commits to the same branch.

You want to test the updated code again.

But perhaps you do not want the same workflow to run for every other pull request activity.

Activity types let you specify which actions matter.

## Common activity types

| Event | Example activity types |
|---|---|
| `pull_request` | `opened`, `synchronize`, `reopened`, `closed` |
| `issues` | `opened`, `edited`, `closed`, `labeled` |
| `release` | `published`, `created`, `edited`, `released` |
| `workflow_run` | `requested`, `in_progress`, `completed` |
| `issue_comment` | `created`, `edited`, `deleted` |

The exact supported activity types depend on the event.

When `types` is omitted, GitHub uses that event's default activity types. The defaults are not necessarily every activity type supported by the event.

---

# 5. Event vs Filter vs Activity Type

These concepts are related, but they answer different questions.

| Concept | Question it answers | Example |
|---|---|---|
| Event | What kind of thing happened? | `push` |
| Filter | Which events qualify? | Only pushes to `main` |
| Activity type | Which specific action qualifies? | Only pull requests that are opened |

Consider this workflow:

```yaml
name: Filter and Activity Demo

on:
  pull_request:
    branches:
      - main
    types:
      - opened
      - synchronize
```

Read it in plain English:

- `pull_request`: Listen for pull request events.
- `branches: main`: Only pull requests targeting `main` qualify.
- `types: opened, synchronize`: Only the selected activity types qualify.

Imagine a pull request from `feature/login` to `main`.

When it is opened, the workflow can run.

When new commits are pushed to its source branch, the pull request is synchronized and the workflow can run again.

A pull request targeting `develop` does not match the target-branch filter.

A pull request targeting `main` that is closed does not match the configured activity types.

This is the difference between event selection, filtering, and activity selection.

---

# 6. Branch Filters

Branch filters are useful when different branches serve different purposes.

For example:

```text
main       → Production-ready code
develop    → Integration work
feature/*  → New features
release/*  → Release preparation
```

You might want one workflow to run only on `main`.

## 6.1 The `branches` filter

```yaml
name: Main Branch CI

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display Branch
        run: echo "Branch filter matched."
```

This workflow is configured for push events on `main`.

It does not match pushes to `develop` or `feature/login`.

## 6.2 Multiple branches

```yaml
on:
  push:
    branches:
      - main
      - develop
      - 'feature/**'
```

This matches:

- `main`
- `develop`
- `feature/login`
- `feature/payment`
- `feature/user/profile`

The `feature/**` pattern matches branch names under the `feature/` prefix, including nested paths.

## 6.3 The `branches-ignore` filter

Use this when you want most branches to qualify except specific branches or patterns.

```yaml
on:
  push:
    branches-ignore:
      - 'experimental/**'
      - 'temporary-*'
```

Pushes to matching ignored branches do not qualify for this workflow.

For the same event, do not define both `branches` and `branches-ignore`. Choose the appropriate configuration.

## 6.4 Important: `pull_request` branch filters

The meaning of `branches` depends on the event.

For `push`:

```yaml
on:
  push:
    branches:
      - main
```

This filters the branch being pushed.

For `pull_request`:

```yaml
on:
  pull_request:
    branches:
      - main
```

This filters the pull request's **target branch**.

Example:

```text
Source branch: feature/login
Target branch: main
```

This pull request matches the `main` target-branch filter.

The source branch does not have to be `main`.

## 6.5 Negative branch patterns

You can also use negative patterns with `!`.

```yaml
on:
  push:
    branches:
      - 'release/**'
      - '!release/**-alpha'
```

This includes matching release branches but excludes those matching the negative pattern.

The order of patterns matters: a later positive pattern can re-include something previously excluded, and a later negative pattern can exclude something previously included.

When you use negative patterns within `branches`, do not also use `branches-ignore` for the same event.

---

# 7. Tag Filters

A Git tag is a named reference that commonly identifies a particular version or release.

For example:

```text
v1.0.0
v1.1.0
v2.0.0
```

You can trigger workflows when matching tags are pushed.

## 7.1 Include specific tags

```yaml
name: Release Tag Workflow

on:
  push:
    tags:
      - 'v1.0.0'
```

This configuration matches pushes of the specified tag.

## 7.2 Include version-like tags

```yaml
on:
  push:
    tags:
      - 'v*'
```

This matches tag names beginning with `v`.

Examples include:

```text
v1.0.0
v1.2.0
v2.0.0
```

The pattern is a glob pattern, not a semantic-version validator. It does not guarantee that the tag follows semantic versioning rules.

## 7.3 Ignore certain tags

```yaml
on:
  push:
    tags-ignore:
      - 'test-*'
```

This excludes matching tag names from the trigger.

## 7.4 Branches versus tags

A branch ref looks like:

```text
refs/heads/main
```

A tag ref looks like:

```text
refs/tags/v1.0.0
```

Branches and tags are both Git references, but they serve different purposes.

Branch filters do not automatically mean tag filters. If you need both, configure them explicitly.

For `push`, if you configure branch filters without tag filters, tag pushes are not included by that branch-filtered trigger configuration.

---

# 8. Path Filters

Path filters allow a workflow to respond only when changes involve particular files or directories.

Imagine a monorepo:

```text
repository/
├── backend/
│   ├── UserService/
│   └── HotelService/
├── frontend/
├── infrastructure/
└── docs/
```

Suppose you have a backend CI workflow.

You do not want it to run merely because someone changes a spelling mistake in the documentation.

You can use path filters.

## 8.1 The `paths` filter

```yaml
name: Backend CI

on:
  push:
    paths:
      - 'backend/**'

jobs:
  backend:
    runs-on: ubuntu-latest

    steps:
      - name: Backend Check
        run: echo "Backend changes matched."
```

The workflow is configured for push events whose changed paths match `backend/**`.

Examples:

| Changed file | Does the path pattern match? |
|---|---|
| `backend/UserService/pom.xml` | Yes |
| `backend/HotelService/src/App.java` | Yes |
| `frontend/src/App.jsx` | No |
| `docs/README.md` | No |

## 8.2 Match specific files

```yaml
on:
  push:
    paths:
      - 'pom.xml'
      - '**/pom.xml'
```

This matches the root Maven file and Maven files in nested directories.

## 8.3 Ignore documentation changes

```yaml
on:
  push:
    paths-ignore:
      - 'docs/**'
      - '**/*.md'
```

The workflow is skipped when all changed files match the ignored patterns.

However, if a push changes both documentation and application code, the workflow can still run because not all changes are ignored.

## 8.4 Include multiple directories

```yaml
on:
  push:
    paths:
      - 'backend/**'
      - 'infrastructure/**'
```

This workflow responds to changes matching either included path pattern.

## 8.5 Important path-filter limitations

Path filtering has practical limitations.

- GitHub uses event-specific file-change comparisons.
- Very large pushes or diffs may exceed GitHub's filtering limits.
- If GitHub cannot reliably evaluate the path filter because of those limits, the workflow may run rather than be skipped.
- A workflow skipped by path filters can leave a required check pending, which may prevent a pull request from being merged.

For critical required checks, consider a workflow design that always reports the required status and conditionally runs expensive jobs inside it.

---

# 9. Branch and Path Filters Together

This is one of the most useful real-world patterns.

Suppose you have a Java backend application.

You want the workflow to run only when:

1. A push happens on `main`.
2. At least one changed file matches the backend path pattern.

You can write:

```yaml
name: Backend Production CI

on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Build Backend
        run: echo "Backend production CI started."
```

Both the branch condition and the path condition must be satisfied.

| Branch | Changed files | Qualifies? |
|---|---|---|
| `main` | `backend/App.java` | Yes |
| `main` | `docs/README.md` | No |
| `develop` | `backend/App.java` | No |
| `develop` | `docs/README.md` | No |

In simple language:

> The event must match the branch filter AND the path filter.

Filters narrow down the events that should trigger the workflow.

---

# 10. Positive and Negative Patterns

GitHub Actions supports glob-style patterns in several filters.

These are not regular expressions. They use glob matching rules.

Common pattern elements include:

| Pattern | Meaning |
|---|---|
| `*` | Matches characters within a path segment |
| `**` | Matches across path segments |
| `?` | Matches one character |
| `!pattern` | Excludes a matching pattern when supported in the relevant filter list |

Examples:

```yaml
on:
  push:
    branches:
      - 'feature/*'
```

Matches branch names such as:

```text
feature/login
feature/payment
```

For nested branch names, use:

```yaml
on:
  push:
    branches:
      - 'feature/**'
```

This can match:

```text
feature/login
feature/user/profile
```

## Positive patterns

A positive pattern includes matching names or paths.

```yaml
branches:
  - 'release/**'
```

## Negative patterns

A negative pattern excludes matching names or paths.

```yaml
branches:
  - 'release/**'
  - '!release/**-alpha'
```

This includes matching release branches but excludes those matching the alpha pattern.

**Important:** When using ordered positive and negative patterns, pattern order matters. For precise behavior, consult the workflow syntax documentation and test your intended branch names.

---

# 11. Pull Request Activity Types

The `pull_request` event is one of the best places to understand activity types.

A pull request is not a single unchanging action.

It can be opened, updated, reopened, closed, reviewed, and merged.

The event name describes the general category. The activity type identifies the particular action.

## 11.1 The `opened` activity type

```yaml
name: PR Opened

on:
  pull_request:
    types:
      - opened

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "A pull request was opened."
```

This workflow is configured to run when a pull request is opened.

## 11.2 The `synchronize` activity type

```yaml
on:
  pull_request:
    types:
      - synchronize
```

This activity occurs when the pull request's head branch receives changes, such as new commits being pushed.

This is useful when you want to rerun checks after the proposed code changes.

## 11.3 The `reopened` activity type

```yaml
on:
  pull_request:
    types:
      - reopened
```

This responds when a previously closed pull request is reopened.

## 11.4 The `closed` activity type

```yaml
on:
  pull_request:
    types:
      - closed
```

This responds when a pull request is closed.

But be careful: **closed does not always mean merged.**

A pull request can be closed without merging.

If you want to run a job only when the pull request has actually been merged, you can use a job condition:

```yaml
name: After PR Merge

on:
  pull_request:
    types:
      - closed

jobs:
  merged:
    if: ${{ github.event.pull_request.merged == true }}
    runs-on: ubuntu-latest

    steps:
      - name: Confirm Merge
        run: echo "The pull request was merged."
```

The event triggers on closure, but the job runs only if the pull request's merged value is true.

## 11.5 Common pull request activity types

Examples include:

- `opened`
- `synchronize`
- `reopened`
- `closed`
- `edited`
- `assigned`
- `unassigned`
- `labeled`
- `unlabeled`
- `review_requested`
- `ready_for_review`
- `converted_to_draft`

The exact supported types are documented by GitHub.

---

# 12. Issue Activity Types

GitHub issues can also trigger automation.

Imagine your team wants to send a notification whenever someone creates an issue.

You can use:

```yaml
name: Issue Opened

on:
  issues:
    types:
      - opened

jobs:
  notify:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "A new issue was opened."
```

This workflow is configured for the `opened` activity type of the `issues` event.

Other examples include:

- `edited`
- `closed`
- `reopened`
- `labeled`
- `unlabeled`
- `assigned`
- `unassigned`

## Example: React to issue labels

```yaml
name: Issue Label Activity

on:
  issues:
    types:
      - labeled

jobs:
  label-job:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "An issue label was added."
```

You could use this pattern to automate triage, categorization, or notifications.

Remember that activity types are event-specific. An activity type supported by `issues` is not automatically valid for `pull_request`.

---

# 13. Release Activity Types

A release is a published version of software, often associated with a tag.

A release workflow can publish artifacts, package software, or perform other release-specific operations.

## Trigger when a release is published

```yaml
name: Release Published

on:
  release:
    types:
      - published

jobs:
  release-job:
    runs-on: ubuntu-latest

    steps:
      - name: Display Release Event
        run: echo "A release was published."
```

The `published` activity type is useful when you want to respond to a published release.

Other release activity types include `created`, `edited`, `deleted`, `prereleased`, and `released`, among others.

The exact behavior depends on the release action and GitHub's event definitions.

For example, creating a draft release is not the same as publishing a release. Choose an activity type that corresponds to the stage you actually want to automate.

---

# 14. Workflow Run Activity Types

The `workflow_run` event lets one workflow react to another workflow's execution.

For example:

```yaml
name: Post-Build Automation

on:
  workflow_run:
    workflows:
      - "Build and Test"
    types:
      - completed

jobs:
  follow-up:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "The Build and Test workflow completed."
```

The `workflows` list contains workflow names, not filenames.

The `completed` activity type means the upstream workflow run has finished. It does not guarantee success.

If you only want to run the job after a successful upstream run:

```yaml
jobs:
  follow-up:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest

    steps:
      - name: Continue
        run: echo "The upstream workflow succeeded."
```

The job condition checks the upstream workflow's conclusion.

Possible conclusions include success, failure, and cancellation.

**Security warning:** Follow-up workflows can have privileges that differ from the upstream workflow. Carefully validate artifacts and untrusted input, and never execute untrusted code in a privileged follow-up workflow.

---

# 15. Other Useful Activity Types

Different events support different activity types.

Here are some useful examples.

## 15.1 Issue comments

```yaml
name: Comment Activity

on:
  issue_comment:
    types:
      - created

jobs:
  comment:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "A comment was created."
```

This can be useful for comment-driven automation, but comments should be treated as untrusted user input.

## 15.2 Discussions

```yaml
name: Discussion Created

on:
  discussion:
    types:
      - created

jobs:
  discussion:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "A discussion was created."
```

## 15.3 Deployment status

```yaml
name: Deployment Status

on:
  deployment_status:

jobs:
  status:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "A deployment status event occurred."
```

This event is useful when reacting to deployment status updates. Its configuration differs from events that use an explicit `types` list.

The main lesson is to check the supported event properties instead of assuming every event has the same filters or activity types.

---

# 16. Combining Filters and Activity Types

Now combine everything you have learned.

Imagine a Java microservices repository.

You want a CI workflow that:

1. Responds to pull request events.
2. Only considers pull requests targeting `main`.
3. Runs for opened pull requests and pull requests updated with new commits.

The workflow can be:

```yaml
name: Java Pull Request CI

on:
  pull_request:
    branches:
      - main
    types:
      - opened
      - synchronize

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Ref
        run: echo "Ref: ${{ github.ref }}"
```

Read the configuration from top to bottom:

```text
pull_request
      |
      v
Target branch must be main
      |
      v
Activity must be opened or synchronize
      |
      v
Matching event can trigger workflow
```

The target branch and activity type requirements must both match.

---

# 17. Understanding AND and OR Logic

This is one of the most important concepts for designing reliable filters.

## 17.1 AND between filters

Consider:

```yaml
on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'
```

The branch and path conditions must both match.

```text
Branch matches? ── YES
                     |
                     v
Path matches? ──── YES
                     |
                     v
             Workflow qualifies
```

If the branch matches but the path does not, the workflow does not qualify.

If the path matches but the branch does not, the workflow does not qualify.

## 17.2 OR between patterns in the same include list

Consider:

```yaml
on:
  push:
    branches:
      - main
      - develop
```

This matches a push to `main` **or** a push to `develop`.

The workflow does not require a push to both branches.

## 17.3 OR between activity types

Consider:

```yaml
on:
  pull_request:
    types:
      - opened
      - synchronize
```

This allows either selected activity type to qualify.

## 17.4 OR between event entries

Consider:

```yaml
on:
  push:
    branches:
      - main

  workflow_dispatch:
```

This workflow can be triggered by a qualifying push to `main` or by a manual dispatch.

The events do not have to happen together.

## 17.5 A complete logical example

```yaml
name: Filter Logic

on:
  push:
    branches:
      - main
      - develop
    paths:
      - 'backend/**'
      - 'infrastructure/**'

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Message
        run: echo "The push matched the filters."
```

The logic is:

```text
Branch is main OR develop
               AND
Changed path matches backend/** OR infrastructure/**
               |
               v
        Workflow qualifies
```

The branch requirement and path requirement must both be satisfied. Within each positive list, matching any listed pattern is sufficient.

---

# 18. Four Complete Practical Workflows

These exercises demonstrate filters and activity types in a real repository.

## Practical 1 — Branch and Path Filters

File: `.github/workflows/01-branch-and-path-filters.yml`

```yaml
name: "01 - Branch and Path Filters"

on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'
      - '.github/workflows/01-branch-and-path-filters.yml'

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Ref
        run: echo "Ref: ${{ github.ref }}"

      - name: Confirm Match
        run: echo "The push matched the configured branch and path filters."
```

### Experiment

1. Commit and push this workflow to `main`.
2. Change a file under `backend/` and push again.
3. Change only a file under `docs/` and push again.
4. Observe which changes qualify.

The workflow file itself is included in the path filter so that changes to this workflow can also qualify.

If your repository does not have a `backend/` directory, create one or change the path patterns to match your repository.

## Practical 2 — Pull Request Activity Types

File: `.github/workflows/02-pull-request-activity.yml`

```yaml
name: "02 - Pull Request Activity Types"

on:
  pull_request:
    branches:
      - main
    types:
      - opened
      - synchronize
      - reopened

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Ref
        run: echo "Ref: ${{ github.ref }}"

      - name: Explain Activity
        run: echo "A selected pull request activity occurred."
```

### Experiment

1. Create a feature branch.
2. Commit a change and push it.
3. Open a pull request targeting `main`.
4. Observe the workflow run.
5. Push another commit to the same feature branch.
6. Observe the additional run.

The new commit updates the pull request and typically generates the `synchronize` activity.

## Practical 3 — Issue Activity Types

File: `.github/workflows/03-issue-activity.yml`

```yaml
name: "03 - Issue Activity Types"

on:
  issues:
    types:
      - opened
      - labeled
      - closed

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Action
        run: echo "Issue activity triggered this workflow."
```

### Experiment

1. Open a new issue.
2. Add a label to the issue.
3. Close the issue.
4. Inspect the runs in the Actions tab.

The same workflow responds to three selected activity types.

## Practical 4 — Release Activity Types

File: `.github/workflows/04-release-activity.yml`

```yaml
name: "04 - Release Activity Types"

on:
  release:
    types:
      - published

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Ref
        run: echo "Ref: ${{ github.ref }}"

      - name: Display SHA
        run: echo "SHA: ${{ github.sha }}"

      - name: Confirm Release
        run: echo "A release was published."
```

### Experiment

1. Open the repository's Releases page.
2. Create a release.
3. Publish it.
4. Inspect the workflow run.

This workflow listens for the `published` activity type, rather than reacting to every release-related activity.

---

# 19. Common Mistakes

## Mistake 1: Confusing event names with activity types

Incorrect:

```yaml
on:
  opened:
```

`opened` is not a top-level event in this example.

Correct:

```yaml
on:
  pull_request:
    types:
      - opened
```

## Mistake 2: Assuming `branches` always means the source branch

For `pull_request`, `branches` filters the target branch.

For `push`, it filters the branch being pushed.

## Mistake 3: Combining incompatible filters

For a given event, do not define both:

```yaml
branches:
branches-ignore:
```

Use one approach.

Likewise, do not define both `paths` and `paths-ignore` for the same event.

## Mistake 4: Assuming every activity type is supported by every event

Activity types are event-specific.

Consult GitHub's documentation for the valid types of the event you are using.

## Mistake 5: Forgetting AND logic

This configuration:

```yaml
on:
  push:
    branches:
      - main
    paths:
      - 'backend/**'
```

does not mean branch or path.

It means the branch filter and path filter must both match.

## Mistake 6: Assuming a closed pull request was merged

A pull request can be closed without being merged.

Check:

```yaml
${{ github.event.pull_request.merged }}
```

when you need to distinguish those cases.

## Mistake 7: Using overly restrictive path filters

A workflow skipped by a path filter may not report a required check, potentially leaving a pull request blocked by branch protection.

Plan required checks carefully.

## Mistake 8: Forgetting that filters belong to particular events

Not all event types support branch, tag, or path filters. Use the workflow syntax reference to verify supported properties.

---

# 20. Troubleshooting

When your workflow does not run, follow these steps.

### Step 1: Check the event

Does your `on` section include the event you actually produced?

### Step 2: Check the branch

Does the branch or target branch match the configured pattern?

### Step 3: Check the changed files

If you configured `paths`, do the changes match one of the included patterns?

### Step 4: Check the activity type

If you configured `types`, did the specific activity occur?

### Step 5: Check workflow availability

Is the workflow file present in the appropriate branch, particularly the default branch when the event requires it?

### Step 6: Check syntax

Verify indentation, quotes, lists, and the supported workflow syntax.

### Step 7: Inspect the Actions tab

If the run exists, inspect the job and step logs.

Remember the difference between:

- A workflow that was not triggered.
- A workflow that was triggered but skipped a job.
- A workflow that started and failed.

These are different situations with different causes.

---

# 21. Real-World CI/CD Examples

Let's connect the concepts to a Java microservices repository.

Suppose you have:

```text
UserService/
HotelService/
RatingService/
```

You want to avoid running every test for every unrelated change.

## Backend CI

```yaml
name: Backend CI

on:
  push:
    branches:
      - main
    paths:
      - 'UserService/**'
      - 'HotelService/**'
      - 'RatingService/**'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display CI Start
        run: echo "Backend changes matched."
```

This is a useful starting pattern for a monorepo.

## Pull request validation

```yaml
name: Pull Request Validation

on:
  pull_request:
    branches:
      - main
    types:
      - opened
      - synchronize
      - reopened

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Display Test Start
        run: echo "Selected pull request activity occurred."
```

## Release automation

```yaml
name: Release Automation

on:
  release:
    types:
      - published

jobs:
  release:
    runs-on: ubuntu-latest

    steps:
      - name: Display Release Start
        run: echo "Release automation started."
```

These examples demonstrate trigger configuration. Real build, test, and release jobs would need the appropriate checkout, build tools, credentials, and deployment steps.

---

# 22. Final Mental Model

Remember these three questions:

**1. Event — What happened?**

Example: `push`, `pull_request`, or `issues`.

**2. Filter — Which events qualify?**

Example: only pushes to `main` that match `backend/**`.

**3. Activity type — Which specific action qualifies?**

Example: only pull requests that are opened or updated with new commits.

The overall process looks like this:

```text
Something happens in GitHub
            |
            v
      Event is identified
            |
            v
   Supported filters are checked
            |
            v
  Configured activity type matches
            |
            v
  Matching workflow run can start
            |
            v
       Jobs execute
```

For a particular workflow event, the event configuration, filters, and activity types determine which events qualify. Job conditions can then impose additional rules on individual jobs.

Once you understand this distinction, GitHub Actions becomes much easier to reason about. Instead of memorizing YAML, you can explain exactly why a workflow should or should not run.

---

# Official Documentation

Use the official documentation to verify the supported filters and activity types for each event.

- [Workflow syntax for GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [Events that trigger workflows](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows)
- [GitHub Actions expressions](https://docs.github.com/en/actions/reference/workflows-and-actions/expressions)

**Best learning method:** Modify one filter or activity type at a time, create the corresponding event in your test repository, and inspect the Actions tab. You will learn much faster by observing which events qualify than by memorizing configuration examples.
