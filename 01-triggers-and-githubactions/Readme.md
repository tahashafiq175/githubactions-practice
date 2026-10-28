# GitHub Actions Workflow Triggers — Complete Beginner-to-Advanced Guide

> **The purpose of this guide:** Understand exactly what starts a GitHub Actions workflow, how GitHub recognizes an event, how trigger filters work, and how to build practical CI/CD pipelines using different types of triggers.

---

# Table of Contents

1. [What Is a Workflow Trigger?](#1-what-is-a-workflow-trigger)
2. [Why Do We Need Triggers?](#2-why-do-we-need-triggers)
3. [The Complete Workflow Execution Journey](#3-the-complete-workflow-execution-journey)
4. [Understanding the `on` Keyword](#4-understanding-the-on-keyword)
5. [Understanding Events, Commit SHAs, and Git Refs](#5-understanding-events-commit-shas-and-git-refs)
6. [Repository Events](#6-repository-events)
7. [The `push` Trigger](#7-the-push-trigger)
8. [The `pull_request` Trigger](#8-the-pull_request-trigger)
9. [The `workflow_dispatch` Trigger](#9-the-workflow_dispatch-trigger)
10. [The `schedule` Trigger](#10-the-schedule-trigger)
11. [The `workflow_run` Trigger](#11-the-workflow_run-trigger)
12. [The `repository_dispatch` Trigger](#12-the-repository_dispatch-trigger)
13. [The `workflow_call` Trigger](#13-the-workflow_call-trigger)
14. [The `pull_request_target` Trigger](#14-the-pull_request_target-trigger)
15. [Other Important Events](#15-other-important-events)
16. [Branch and Tag Filters](#16-branch-and-tag-filters)
17. [Path Filters](#17-path-filters)
18. [Activity Types](#18-activity-types)
19. [Using Multiple Triggers](#19-using-multiple-triggers)
20. [Understanding GitHub Context Values](#20-understanding-github-context-values)
21. [Four Complete Practical Workflows](#21-four-complete-practical-workflows)
22. [Common Trigger Mistakes](#22-common-trigger-mistakes)
23. [Trigger Security and Permissions](#23-trigger-security-and-permissions)
24. [Troubleshooting Workflows That Do Not Run](#24-troubleshooting-workflows-that-do-not-run)
25. [Real-World CI/CD Trigger Strategies](#25-real-world-cicd-trigger-strategies)
26. [Final Mental Model](#26-final-mental-model)

---

# 1. What Is a Workflow Trigger?

Imagine you own a factory.

The factory has machines, workers, tools, and a production line. However, the factory does not need to produce something every second.

It needs a signal telling it when to start working.

For example:

- Raw material arrives.
- A customer places an order.
- A manager presses a button.
- A timer reaches a particular time.
- Another factory sends a request.

Each of these events can cause the factory to begin a task.

GitHub Actions works in a similar way.

A GitHub Actions workflow is a collection of automated jobs and steps. A **workflow trigger** tells GitHub when a workflow should be considered for execution.

For example:

```yaml
name: My First Workflow

on:
  push:

jobs:
  hello:
    runs-on: ubuntu-latest

    steps:
      - name: Say Hello
        run: echo "Hello from GitHub Actions!"
```

What does this mean?

1. You create or update code in your repository.
2. You push your changes to GitHub.
3. GitHub receives a `push` event.
4. GitHub checks whether this workflow matches that event.
5. If it matches and the other execution requirements are satisfied, GitHub schedules a workflow run.
6. A runner executes the job.
7. The command prints a message.

The most important line is:

```yaml
on:
  push:
```

It tells GitHub that a push event is a reason to trigger this workflow.

**Remember:** A trigger defines when a workflow can start. Jobs define what work the workflow performs.

---

# 2. Why Do We Need Triggers?

Imagine you are developing a Java Spring Boot application.

You want to:

- Compile your application after every push.
- Run tests whenever a pull request is opened.
- Deploy manually when you decide to release a new version.
- Check dependencies every night.
- Run a notification workflow after another workflow finishes.

Without automation, you might have to perform these tasks manually.

Triggers allow GitHub to react to events automatically.

For example:

```text
Developer pushes Java code
          |
          v
       push event
          |
          v
   GitHub Actions starts
          |
          v
      Build project
          |
          v
       Run tests
          |
          v
   Report the result
```

Different situations require different triggers.

| Requirement | Suitable trigger |
|---|---|
| Build code after a push | `push` |
| Test proposed changes | `pull_request` |
| Start deployment manually | `workflow_dispatch` |
| Run scheduled maintenance | `schedule` |
| React to another workflow finishing | `workflow_run` |
| Receive an external system's request | `repository_dispatch` |
| Reuse a workflow from another workflow | `workflow_call` |

Choosing the correct trigger is one of the first decisions you make when designing a CI/CD pipeline.

---

# 3. The Complete Workflow Execution Journey

Before learning individual triggers, understand the complete journey.

Suppose you have a workflow containing:

```yaml
name: Java CI

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Workflow started"
```

You push a commit to `main`.

What happens behind the scenes?

## Step 1: An event occurs

You execute:

```bash
git add .
git commit -m "Add new feature"
git push origin main
```

GitHub receives the push operation and processes a push event.

An event is a record of something that happened, such as a commit being pushed or a pull request being opened.

## Step 2: GitHub associates the event with repository information

The event contains information about the affected repository and the relevant commit and ref.

A **commit SHA** identifies a particular Git commit.

A **Git ref** identifies a reference, such as a branch or tag.

For example:

```text
Event: push
Commit SHA: 9f3e2a...
Git Ref: refs/heads/main
```

These are illustrative values.

The SHA tells you which commit is associated with the event. The ref tells you which branch or tag reference is involved.

## Step 3: GitHub finds applicable workflow files

Workflow files are normally stored in:

```text
your-repository/
└── .github/
    └── workflows/
        ├── java-ci.yml
        ├── deploy.yml
        └── tests.yml
```

GitHub checks workflow definitions associated with the event's commit or ref, subject to event-specific rules.

Some events, including scheduled events and manual dispatch, have default-branch requirements.

## Step 4: GitHub checks the `on` configuration

Your workflow says:

```yaml
on:
  push:
    branches:
      - main
```

GitHub asks:

- Is this a `push` event?
- Does the event match the branch filter?
- Are any other trigger requirements satisfied?

A push to `main` matches.

A push to `develop` does not match this workflow's branch filter.

## Step 5: GitHub creates a workflow run

When the event matches, GitHub can create a workflow run.

The workflow run has information about the event, its associated commit, and the ref.

## Step 6: GitHub schedules jobs

GitHub evaluates job dependencies, conditions, permissions, and runner requirements.

If the job is eligible to run, GitHub assigns it to a suitable runner.

## Step 7: The runner executes the steps

The runner executes the commands in the workflow.

```text
Event occurs
     |
     v
GitHub evaluates triggers
     |
     v
Workflow run is created
     |
     v
Jobs are evaluated and scheduled
     |
     v
Runner executes steps
     |
     v
Workflow result
```

A matching trigger does not guarantee that every job will succeed. Jobs can fail because of invalid YAML, unavailable runners, failed commands, permissions, or other execution problems.

---

# 4. Understanding the `on` Keyword

The `on` keyword defines the events that can trigger a workflow.

It is a special part of GitHub Actions workflow syntax.

For example:

```yaml
on:
  push:
  pull_request:
  workflow_dispatch:
```

This workflow has three configured trigger types.

It can respond to:

- Push events.
- Pull request events.
- Manual dispatch requests.

You do not need to create three separate workflows just because you have three trigger types.

One workflow can have multiple triggers.

## A simple example

```yaml
name: Multiple Triggers

on:
  push:
  workflow_dispatch:

jobs:
  show-event:
    runs-on: ubuntu-latest

    steps:
      - name: Print Event Name
        run: echo "Event: ${{ github.event_name }}"
```

When triggered by a push:

```text
Event: push
```

When started manually:

```text
Event: workflow_dispatch
```

The same job can behave differently depending on the event.

## YAML quoting note

Some YAML editors interpret the unquoted key `on` as a Boolean in YAML 1.1 mode. This can cause misleading editor warnings or incorrect parsing by tools that do not properly use GitHub's workflow syntax.

GitHub's official examples commonly use the unquoted form:

```yaml
on:
  push:
```

If your editor or a custom YAML parser has a problem with it, you can use:

```yaml
'on':
  push:
```

Both forms are understood by GitHub Actions.

For learning, use GitHub's workflow syntax and validate the complete file rather than relying only on editor highlighting.

---

# 5. Understanding Events, Commit SHAs, and Git Refs

This is one of the most important foundations of GitHub Actions.

## What is an event?

An event represents something that happened.

Examples:

```text
push
pull_request
issues
release
workflow_dispatch
schedule
```

The event name tells you the kind of activity that triggered the workflow.

## What is a commit SHA?

Every Git commit has an identifier.

For example:

```text
a1b2c3d4...
```

The full SHA identifies a specific commit.

A workflow can display its associated SHA using:

```yaml
${{ github.sha }}
```

Example:

```yaml
- name: Display SHA
  run: echo "Commit SHA: ${{ github.sha }}"
```

The precise meaning of the SHA depends on the event.

For a normal push, it is associated with the commit at the tip of the pushed ref. For a `pull_request` event, it commonly represents GitHub's synthetic merge commit. A `workflow_run` event has its own event context and associated commit information.

Do not assume every trigger uses the same SHA semantics.

## What is a Git ref?

A ref identifies a Git reference, such as a branch or tag.

Examples:

```text
refs/heads/main
refs/heads/develop
refs/tags/v1.0.0
```

Display it using:

```yaml
${{ github.ref }}
```

For a push to `main`:

```text
refs/heads/main
```

For a push of tag `v1.0.0`:

```text
refs/tags/v1.0.0
```

For a typical pull request event:

```text
refs/pull/12/merge
```

Here, `12` is the pull request number. The exact context depends on the event.

## Compare the three values

| Value | Meaning |
|---|---|
| `github.event_name` | The event type |
| `github.sha` | The event-associated commit SHA |
| `github.ref` | The event-associated branch or tag ref, or another event-specific ref |

Use this workflow to inspect them:

```yaml
name: Inspect Event Context

on:
  push:
  pull_request:
  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Print Context
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
```

This is a useful experiment because it lets you observe the values instead of memorizing them.

---

# 6. Repository Events

Repository events happen because of activity involving a repository or its contents.

Some important examples include:

| Event | What it represents |
|---|---|
| `push` | Commits are pushed to a branch or tag |
| `pull_request` | A pull request has an activity event |
| `issues` | An issue has an activity event |
| `issue_comment` | An issue or pull request comment event occurs |
| `release` | A release has an activity event |
| `create` | A branch or tag is created |
| `delete` | A branch or tag is deleted |
| `fork` | A repository is forked |
| `watch` | A user stars the repository |
| `discussion` | A discussion has an activity event |
| `workflow_dispatch` | A workflow is manually requested |
| `workflow_run` | Another workflow run reaches a configured activity type |

Not every event is triggered by a Git commit. For example, an issue can be opened without changing the source code.

That is why GitHub Actions is more than a build tool. It can automate work in response to repository activity.

---

# 7. The `push` Trigger

The `push` event is one of the most common triggers in CI/CD.

It is used when commits are pushed to a branch or tag.

## Basic example

```yaml
name: Push Event

on:
  push:

jobs:
  show:
    runs-on: ubuntu-latest

    steps:
      - name: Print Event
        run: echo "A push event triggered this workflow."
```

Whenever a matching push event occurs, GitHub can start this workflow.

## Trigger only on `main`

```yaml
on:
  push:
    branches:
      - main
```

Now the workflow is restricted to pushes on `main`.

## Trigger on several branches

```yaml
on:
  push:
    branches:
      - main
      - develop
      - 'feature/**'
```

This matches `main`, `develop`, and branches under the `feature/` prefix.

## Trigger on tags

```yaml
on:
  push:
    tags:
      - 'v*'
```

A tag such as `v1.0.0` matches this pattern.

A branch push and a tag push can both be push events, but their refs differ.

## When should you use `push`?

Use it when you want to:

- Compile code after commits are pushed.
- Run tests after changes reach a branch.
- Publish an image after a merge.
- Deploy automatically after code reaches a release branch.
- Run repository checks after updates.

### Important distinction

A `push` workflow does not necessarily need to check out the code to print event metadata. However, most real CI workflows use `actions/checkout` before building or testing the repository.

---

# 8. The `pull_request` Trigger

A pull request is a proposal to merge changes from one branch into another.

Imagine your repository has:

```text
main
  |
  |---- feature/login
```

You implement a login feature on `feature/login`.

You open a pull request asking GitHub to merge it into `main`.

A `pull_request` workflow can automatically test the proposed changes before they are merged.

## Basic example

```yaml
name: Pull Request Checks

on:
  pull_request:
    branches:
      - main

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Ref
        run: echo "Ref: ${{ github.ref }}"
```

This workflow listens for pull request activity where the target branch is `main`.

## What does `branches` mean here?

Under `pull_request`, this:

```yaml
on:
  pull_request:
    branches:
      - main
```

means the pull request targets `main`.

It does not mean the source branch must be `main`.

For example:

```text
Source branch: feature/login
Target branch: main
```

This pull request matches the filter.

## Pull request activity types

You can configure the activity types that matter to you:

```yaml
on:
  pull_request:
    types:
      - opened
      - synchronize
      - reopened
```

These mean:

- `opened`: A pull request is opened.
- `synchronize`: The pull request's source branch receives new commits, updating the pull request.
- `reopened`: A previously closed pull request is reopened.

If you omit `types`, GitHub uses the default activity types for the event.

## Why use this trigger?

A common CI process is:

```text
Developer writes code
         |
         v
Opens pull request
         |
         v
CI workflow runs
         |
         v
Compile and test
         |
         v
Review results
         |
         v
Merge if requirements are satisfied
```

**Security note:** Pull requests from forks commonly receive restricted token permissions and do not receive ordinary repository secrets. Never assume untrusted pull request code can safely access privileged credentials.

---

# 9. The `workflow_dispatch` Trigger

`workflow_dispatch` lets you manually start a workflow.

Imagine you have a deployment workflow.

You do not want every push to deploy your application to production. Instead, you want to choose when to deploy.

A manual trigger is useful for this situation.

## Basic example

```yaml
name: Manual Workflow

on:
  workflow_dispatch:

jobs:
  manual-job:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"
```

Once the workflow is available on the repository's default branch, you can normally start it through the Actions tab.

## Add user inputs

```yaml
name: Manual Deployment

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose an environment"
        required: true
        type: choice
        options:
          - development
          - staging
          - production

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Display Selection
        env:
          TARGET_ENV: ${{ inputs.environment }}
        run: echo "Selected environment: $TARGET_ENV"
```

When you start the workflow, GitHub asks you to choose an environment.

If you choose `staging`, the job prints:

```text
Selected environment: staging
```

This example only displays the selected environment. It does not deploy anything.

## Why are inputs useful?

Inputs allow a user to supply information when starting a workflow.

For example:

- Which environment should receive a deployment?
- Which version should be released?
- Should a maintenance job perform a dry run?
- Which test suite should run?

Manual triggers are particularly useful for controlled operational tasks.

### Important

The workflow file must be present on the default branch for the workflow to be manually dispatched through the normal GitHub Actions interface. You can select a branch for a manual run, but the workflow must first be available on the default branch.

---

# 10. The `schedule` Trigger

The `schedule` trigger starts workflows according to a cron schedule.

Imagine you want to check dependencies every night.

You do not want to open GitHub and click a button every day.

You can let GitHub schedule the work.

## Basic example

```yaml
name: Nightly Check

on:
  schedule:
    - cron: '0 2 * * *'

jobs:
  check:
    runs-on: ubuntu-latest

    steps:
      - name: Print Schedule Event
        run: echo "Event: ${{ github.event_name }}"
```

The cron expression:

```text
0 2 * * *
```

means every day at 02:00 UTC.

## Understanding cron

A cron expression contains five fields:

```text
┌──────── minute (0–59)
│ ┌────── hour (0–23)
│ │ ┌──── day of month (1–31)
│ │ │ ┌── month (1–12)
│ │ │ │ ┌ day of week (0–6)
│ │ │ │ │
* * * * *
```

Examples:

| Cron | Meaning |
|---|---|
| `0 2 * * *` | Every day at 02:00 UTC |
| `0 0 * * 1` | Every Monday at 00:00 UTC |
| `30 6 * * *` | Every day at 06:30 UTC |
| `0 0 1 * *` | At midnight on the first day of each month |
| `*/15 * * * *` | Every 15 minutes |

GitHub scheduled workflows use UTC.

## Important scheduling behavior

Scheduled workflows:

- Must be present on the default branch.
- Can be delayed during periods of high GitHub Actions load.
- Can be dropped during particularly high load.
- Run using the latest commit on the default branch, rather than a historical commit selected by an earlier event.

Do not use GitHub Actions schedules as a precision timing system for tasks that must run at an exact second.

## Common use cases

- Nightly integration tests.
- Dependency audits.
- Scheduled reports.
- Repository maintenance.
- Periodic cleanup.
- Regular security checks.

---

# 11. The `workflow_run` Trigger

The `workflow_run` trigger allows a workflow to react to the execution of another workflow.

Imagine you separate your pipeline into two workflows:

```text
Workflow A: Build and Test
            |
            v
        Completes
            |
            v
Workflow B: Notify or Deploy
```

You can configure Workflow B to respond to Workflow A's completion.

## Example

```yaml
name: Follow-up Workflow

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
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"
```

The name in `workflows` must match the **name of the other workflow**, not its filename.

For example, if the other workflow contains:

```yaml
name: Build and Test
```

then the name to use is:

```yaml
workflows:
  - "Build and Test"
```

## What does `completed` mean?

It means the other workflow run has completed.

Completion does not necessarily mean success.

The upstream workflow could have:

- Succeeded.
- Failed.
- Been cancelled.

If you only want to proceed when the upstream workflow succeeded, check its conclusion.

For example:

```yaml
jobs:
  follow-up:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest

    steps:
      - name: Continue
        run: echo "The upstream workflow succeeded."
```

## When is this useful?

- Separate CI and deployment workflows.
- Notifications after a build.
- Reporting after tests finish.
- Post-processing workflow results.
- Controlled follow-up automation.

### Security warning

A workflow triggered by `workflow_run` can have access to privileges that an upstream untrusted workflow does not have. Carefully validate artifacts, branch information, and any data received from an untrusted workflow. Never execute untrusted downloaded artifacts in a privileged follow-up workflow.

---

# 12. The `repository_dispatch` Trigger

`repository_dispatch` is designed for external systems to request an event in a repository.

Imagine you have an external application that finishes an operation.

It now needs to tell GitHub:

> Please start this workflow because my operation has completed.

An external service can send an authenticated request to the GitHub REST API to create a repository dispatch event.

## Workflow example

```yaml
name: External Event Workflow

on:
  repository_dispatch:
    types:
      - deploy-request

jobs:
  external-job:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "Event: ${{ github.event_name }}"

      - name: Display Event Type
        run: echo "External event received."
```

This workflow listens for a custom event type called `deploy-request`.

## How does it work?

```text
External application
         |
         v
Authenticated API request
         |
         v
GitHub repository
         |
         v
repository_dispatch event
         |
         v
Workflow trigger matches
         |
         v
Workflow run starts
```

## Example API request

An external application can send a request to:

```text
POST /repos/OWNER/REPO/dispatches
```

The JSON request body can look like:

```json
{
  "event_type": "deploy-request",
  "client_payload": {
    "environment": "staging"
  }
}
```

Replace `OWNER` and `REPO` with the repository owner and repository name.

The request must be authenticated with a credential that has the necessary repository permissions.

The workflow can read the payload through the event context:

```yaml
- name: Display Payload
  env:
    TARGET_ENV: ${{ github.event.client_payload.environment }}
  run: echo "Environment: $TARGET_ENV"
```

## When should you use it?

- An external deployment platform requests a workflow.
- A separate application requests a repository operation.
- An external monitoring service initiates automation.
- Another system needs to signal a custom event.

The important idea is that `repository_dispatch` does not happen merely because a random external system exists. Something must send the authenticated API request.

---

# 13. The `workflow_call` Trigger

`workflow_call` allows a workflow to be called by another workflow.

It is primarily used to create reusable workflows.

Imagine you maintain several Java services:

```text
UserService
HotelService
RatingService
```

Each service needs a similar build-and-test process.

Instead of copying the same workflow into every repository or writing the same jobs repeatedly, you can define a reusable workflow.

## Reusable workflow

Create a file named:

`.github/workflows/reusable-java-ci.yml`

```yaml
name: Reusable Java CI

on:
  workflow_call:
    inputs:
      java-version:
        required: true
        type: string

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display Java Version
        env:
          JAVA_VERSION: ${{ inputs.java-version }}
        run: echo "Requested Java version: $JAVA_VERSION"
```

This workflow defines a callable interface with an input.

## Calling workflow

Create another workflow:

`.github/workflows/call-reusable-ci.yml`

```yaml
name: Call Reusable Java CI

on:
  push:
    branches:
      - main

jobs:
  call-ci:
    uses: ./.github/workflows/reusable-java-ci.yml
    with:
      java-version: '21'
```

When the caller's push trigger matches, GitHub executes the calling workflow, which invokes the reusable workflow.

```text
Push event
    |
    v
Calling workflow
    |
    v
workflow_call interface
    |
    v
Reusable workflow
    |
    v
Build job
```

The reusable workflow is not automatically triggered by a push just because it contains `workflow_call`. It is invoked by another workflow.

## `workflow_run` versus `workflow_call`

These two triggers solve different problems.

| `workflow_run` | `workflow_call` |
|---|---|
| Reacts to another workflow run reaching an activity type | Allows a workflow to invoke a reusable workflow |
| Commonly used for follow-up automation | Commonly used to share jobs and steps |
| Triggered by the other workflow's run event | Invoked through a workflow job using `uses` |
| Can react after success or failure when configured for completion | Executes as part of the calling workflow's run |

Do not confuse a workflow finishing with a reusable workflow being called.

---

# 14. The `pull_request_target` Trigger

`pull_request_target` is related to pull requests, but its security and execution behavior differs from `pull_request`.

It runs in the context of the base repository and uses the base branch's workflow definition. This can provide access to the base repository's token permissions and secrets that ordinary fork pull request workflows do not receive.

That makes it powerful, but also dangerous when used incorrectly.

## Example

```yaml
name: Pull Request Metadata

on:
  pull_request_target:
    types:
      - opened

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: echo "A pull request was opened."
```

This example does not check out or execute pull request code.

## Why does this trigger exist?

It is useful for trusted automation involving pull request metadata, such as:

- Adding labels.
- Posting controlled comments.
- Managing repository metadata.
- Applying repository-specific policies.

## Critical security rule

**Never casually check out and execute untrusted pull request code in a privileged `pull_request_target` workflow.**

An attacker may create a pull request containing malicious code. If a privileged workflow executes that code while secrets or write permissions are available, the attacker could potentially steal secrets or modify the repository.

Use the least privileges possible and avoid executing untrusted code.

For ordinary building and testing of proposed code, `pull_request` is usually the appropriate starting point.

---

# 15. Other Important Events

GitHub Actions supports many more events.

You do not need to memorize every event at once. Learn the ones that match the automation you want to build.

## `issues`

Runs in response to configured issue activity.

```yaml
on:
  issues:
    types:
      - opened
      - closed
```

Possible use: automate issue labels or notifications.

## `issue_comment`

Responds to comments on issues and pull requests.

```yaml
on:
  issue_comment:
    types:
      - created
```

Possible use: trigger a controlled bot action after a comment is created.

Be careful with comment-triggered automation. A comment is input from a user and should not automatically be treated as a trusted command.

## `release`

Responds to release activity.

```yaml
on:
  release:
    types:
      - published
```

Possible use: package or publish an application when a release is published.

## `create`

Responds when a branch or tag is created.

```yaml
on:
  create:
```

Possible use: run branch initialization automation.

## `delete`

Responds when a branch or tag is deleted.

```yaml
on:
  delete:
```

Possible use: clean up related resources when a branch is removed.

## `workflow_call`

Allows a reusable workflow to be invoked by another workflow.

## `deployment_status`

Responds to deployment status events.

```yaml
on:
  deployment_status:
```

Possible use: react to deployment status changes.

## `check_run` and `check_suite`

These events are associated with GitHub check runs and check suites.

They are useful for certain integrations and automation around status checks.

## `discussion`

Responds to discussion activity.

```yaml
on:
  discussion:
    types:
      - created
```

Possible use: discussion notifications or moderation automation.

## `watch`

Responds when a user stars a repository.

```yaml
on:
  watch:
    types:
      - started
```

Possible use: repository activity reporting.

For a full list and the precise supported activity types, consult GitHub's official event documentation.

---

# 16. Branch and Tag Filters

A trigger can specify not only the event but also which branches or tags matter.

Consider:

```yaml
on:
  push:
    branches:
      - main
```

The workflow is configured for pushes to `main`.

A push to another branch can still occur, but it does not match this workflow's filter.

## Include multiple branches

```yaml
on:
  push:
    branches:
      - main
      - develop
      - 'release/**'
```

## Exclude branches

```yaml
on:
  push:
    branches-ignore:
      - 'experimental/**'
```

Do not use `branches` and `branches-ignore` together for the same event. Use positive patterns or the ignore form, according to the behavior you want.

## Filter tags

```yaml
on:
  push:
    tags:
      - 'v*'
```

This matches tag names beginning with `v`, such as `v1.0.0`.

## Exclude tags

```yaml
on:
  push:
    tags-ignore:
      - 'test-*'
```

When you use `branches` or `branches-ignore` for a `push` trigger but do not specify tag filters, tag pushes are not included by that branch filter configuration.

For `push`, branch filters match the branch being pushed. For `pull_request`, branch filters match the target branch of the pull request.

---

# 17. Path Filters

Sometimes a workflow should run only when certain files change.

Imagine your repository contains:

```text
repository/
├── backend/
├── frontend/
├── docs/
└── .github/
    └── workflows/
```

You may want the backend workflow to run only when backend files change.

## Use `paths`

```yaml
name: Backend CI

on:
  push:
    paths:
      - 'backend/**'
      - '.github/workflows/backend-ci.yml'

jobs:
  backend:
    runs-on: ubuntu-latest

    steps:
      - name: Run Backend Checks
        run: echo "Backend-related changes detected."
```

This configuration filters push events by the changed paths.

## Use `paths-ignore`

```yaml
on:
  push:
    paths-ignore:
      - 'docs/**'
      - '*.md'
```

This excludes pushes whose changed files are limited to the ignored paths.

Do not combine `paths` and `paths-ignore` for the same event. Use the appropriate form for your intended behavior.

## Important details

Path filters are not a replacement for job-level conditions.

GitHub evaluates them when deciding whether the workflow should run for a qualifying event.

Also, GitHub's path filtering has limitations for very large changesets. If the change set exceeds the limits used by the event filtering mechanism, the workflow may run rather than being skipped.

For pull request workflows, skipped required checks caused by trigger filters can leave required checks pending and prevent a merge. Design branch protection and workflow filters carefully.

---

# 18. Activity Types

An event describes a category of activity.

An activity type describes the particular action that occurred within that category.

For example, a pull request event can involve a pull request being:

- Opened.
- Reopened.
- Updated with new commits.
- Closed.

These are different activity types associated with the `pull_request` event.

## Example

```yaml
name: Pull Request Activity

on:
  pull_request:
    types:
      - opened
      - synchronize
      - reopened

jobs:
  show:
    runs-on: ubuntu-latest

    steps:
      - name: Display Activity
        run: echo "Pull request activity triggered this workflow."
```

The workflow is configured for the specified activity types.

## Event versus activity type

Think of it this way:

```text
Event: pull_request
        |
        +-- opened
        |
        +-- synchronize
        |
        +-- reopened
        |
        +-- closed
```

The event is the general category. The activity type identifies the particular activity.

## Another example: issues

```yaml
on:
  issues:
    types:
      - opened
      - labeled
      - closed
```

The workflow can respond to issue opening, labeling, or closing.

## Why specify activity types?

You might want to:

- Test code when a pull request is opened or updated.
- Notify users when an issue is opened.
- Perform cleanup when a pull request is closed.
- React only when a release is published.

If you omit `types`, GitHub uses the default activity types for that event. Not every possible activity type is included in the default set.

---

# 19. Using Multiple Triggers

A workflow can respond to multiple event types.

For example:

```yaml
name: Multi-Event CI

on:
  push:
    branches:
      - main

  pull_request:
    branches:
      - main

  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Display Trigger
        run: echo "Triggered by: ${{ github.event_name }}"
```

This workflow has three possible trigger sources:

1. A push to `main`.
2. A matching pull request event targeting `main`.
3. A manual workflow dispatch.

Each event is evaluated according to its own configuration.

## Important distinction: OR versus AND

Multiple event entries under `on` generally mean **OR**.

For example:

```yaml
on:
  push:
  workflow_dispatch:
```

This means a matching push event **or** a manual dispatch can start the workflow.

It does not mean both events must occur.

If you want a job to run only when a specific condition is true, use a job-level `if` condition.

For example:

```yaml
jobs:
  deploy:
    if: ${{ github.event_name == 'push' }}
    runs-on: ubuntu-latest

    steps:
      - run: echo "This job runs for push events."
```

The workflow may have multiple triggers, but this job is restricted by its own condition.

---

# 20. Understanding GitHub Context Values

GitHub provides contexts containing information about the workflow run.

The `github` context contains information about the event, repository, workflow, ref, and related metadata.

## Common values

| Expression | Meaning |
|---|---|
| `github.event_name` | Event type that triggered the run |
| `github.sha` | Event-associated commit SHA |
| `github.ref` | Event-associated Git ref |
| `github.ref_name` | Short branch or tag name in applicable contexts |
| `github.ref_type` | Whether the ref is a branch or tag in applicable contexts |
| `github.repository` | Repository name in `owner/repo` format |
| `github.actor` | User or actor associated with the workflow run |
| `github.event` | Event payload |
| `github.workflow` | Workflow name |
| `github.run_id` | Unique identifier for the workflow run |
| `github.run_number` | Run number for the workflow |
| `github.event_path` | Path to the event payload file on the runner |

## Practical example

```yaml
name: Context Explorer

on:
  push:
  pull_request:
  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Print GitHub Context
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
          echo "Ref Name: ${{ github.ref_name }}"
          echo "Ref Type: ${{ github.ref_type }}"
          echo "Repository: ${{ github.repository }}"
          echo "Actor: ${{ github.actor }}"
          echo "Workflow: ${{ github.workflow }}"
          echo "Run ID: ${{ github.run_id }}"
```

The output depends on the event and repository state.

For example, `github.ref` may identify a branch on a push event or a pull request merge ref on a `pull_request` event.

Do not assume that the same context value means exactly the same thing for every trigger.

## Safer handling of external values

When a value comes from a user-controlled event payload, avoid inserting it directly into a shell command.

For example, instead of embedding an untrusted value into a shell expression, pass it through an environment variable:

```yaml
- name: Display Input
  env:
    COMMENT_BODY: ${{ github.event.comment.body }}
  run: |
    printf '%s\n' "$COMMENT_BODY"
```

This helps prevent shell-injection problems associated with interpolating untrusted text into scripts. It does not eliminate every security risk, so treat external input carefully.

---

# 21. Four Complete Practical Workflows

These four workflows are designed as practical exercises for a learning repository.

## Practical 1 — Push and Pull Request

File: `.github/workflows/01-push-and-pr.yml`

```yaml
name: "01 - Push and Pull Request"

on:
  push:
    branches:
      - main

  pull_request:
    branches:
      - main

jobs:
  event-info:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event Information
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
          echo "Repository: ${{ github.repository }}"
```

**Experiment**

1. Push a commit to `main`.
2. Observe the event name.
3. Open a pull request targeting `main`.
4. Observe the event name in the new run.

Expected event names:

```text
push
pull_request
```

The workflow uses the same job for two different event types.

## Practical 2 — Manual Dispatch

File: `.github/workflows/02-manual-trigger.yml`

```yaml
name: "02 - Manual Trigger"

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose environment"
        required: true
        type: choice
        options:
          - development
          - staging
          - production

jobs:
  manual:
    runs-on: ubuntu-latest

    steps:
      - name: Display Manual Input
        env:
          ENVIRONMENT: ${{ inputs.environment }}
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "Selected environment: $ENVIRONMENT"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
```

**Experiment**

Open the Actions tab, select this workflow, click **Run workflow**, and choose an environment.

Notice that the event is `workflow_dispatch`, not `push`.

## Practical 3 — Scheduled Workflow

File: `.github/workflows/03-scheduled-trigger.yml`

```yaml
name: "03 - Scheduled Trigger"

on:
  schedule:
    - cron: '0 2 * * *'

  workflow_dispatch:

jobs:
  scheduled:
    runs-on: ubuntu-latest

    steps:
      - name: Display Schedule Event
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
          echo "Scheduled check executed."
```

**Experiment**

Use the manual trigger to test the workflow immediately. The scheduled run is configured for 02:00 UTC daily.

This demonstrates that a workflow can support both automatic scheduling and manual execution.

## Practical 4 — External and Cross-Workflow Triggers

File: `.github/workflows/04-external-and-cross-workflow.yml`

```yaml
name: "04 - External and Cross-Workflow"

on:
  repository_dispatch:
    types:
      - custom-event

  workflow_run:
    workflows:
      - "01 - Push and Pull Request"
    types:
      - completed

jobs:
  event-info:
    runs-on: ubuntu-latest

    steps:
      - name: Display Event
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "SHA: ${{ github.sha }}"
          echo "Ref: ${{ github.ref }}"
```

**Experiment A: External event**

Send an authenticated repository dispatch request with:

```json
{
  "event_type": "custom-event"
}
```

The workflow listens for the `custom-event` type.

**Experiment B: Cross-workflow event**

Complete the workflow named `01 - Push and Pull Request`.

The second workflow can respond to its completion.

If you need the second workflow to proceed only after a successful run, add a job condition that checks the upstream conclusion.

---

# 22. Common Trigger Mistakes

## Mistake 1: Wrong workflow directory

Incorrect:

```text
repository/
└── workflows/
    └── ci.yml
```

Correct:

```text
repository/
└── .github/
    └── workflows/
        └── ci.yml
```

Workflow files normally belong in `.github/workflows/` at the repository root.

## Mistake 2: Assuming `workflow_dispatch` works without the default-branch requirement

A workflow generally needs to exist on the default branch before it can be manually dispatched through GitHub's normal interface.

Check that the workflow has been committed and pushed to the default branch.

## Mistake 3: Confusing event names with activity types

This is an event:

```yaml
pull_request:
```

These are activity types:

```yaml
types:
  - opened
  - synchronize
```

Do not use an activity type as if it were a separate top-level event.

## Mistake 4: Filtering the wrong branch

For `pull_request`, `branches` filters the target branch.

For `push`, `branches` filters the branch being pushed.

These are not interchangeable.

## Mistake 5: Forgetting the default branch requirement for scheduled workflows

A schedule does not simply run against whichever branch was last updated. Scheduled workflows run from the default branch.

## Mistake 6: Using the wrong workflow name with `workflow_run`

The `workflows` field uses the other workflow's declared `name`, not the filename.

## Mistake 7: Expecting every event to provide the same SHA or ref

Different events have different payloads and context semantics.

Inspect `github.event_name`, `github.sha`, and `github.ref` during your experiments.

## Mistake 8: Assuming a matching event guarantees a successful run

A workflow can match an event and still fail because of:

- Invalid workflow syntax.
- Missing permissions.
- Job conditions.
- Runner availability.
- Failed shell commands.
- Missing secrets.
- Dependency installation failures.

## Mistake 9: Putting secrets into workflow output

Do not print passwords, access tokens, private keys, or other secrets in logs.

Use GitHub's secrets mechanism and grant the minimum permissions required.

## Mistake 10: Forgetting YAML indentation

This is valid:

```yaml
on:
  push:

jobs:
  build:
    runs-on: ubuntu-latest
```

YAML structure depends on indentation. Spaces matter.

---

# 23. Trigger Security and Permissions

Triggers decide when automation starts, but they do not automatically make the triggered code trustworthy.

This is especially important in CI/CD systems.

## Principle 1: Use least privilege

If a workflow only needs to read the repository, do not grant it unnecessary write permissions.

For example:

```yaml
permissions:
  contents: read
```

This limits the default `GITHUB_TOKEN` permissions for the workflow to read-only repository contents, subject to the workflow's other requirements.

## Principle 2: Protect deployment environments

For production deployments, consider using GitHub Environments with required reviewers, environment protection rules, and appropriately scoped secrets.

A manual trigger alone is not a security boundary.

## Principle 3: Treat external payloads as untrusted

Values received through:

- Pull request titles.
- Issue comments.
- Dispatch payloads.
- External application requests.

should not be blindly executed as shell commands.

## Principle 4: Be careful with privileged follow-up workflows

`pull_request_target` and `workflow_run` require particular care because of the privileges that may be available to those workflows.

Do not execute untrusted artifacts or pull request code with privileged credentials.

## Principle 5: Separate testing from deployment

A common pattern is:

```text
Pull request
     |
     v
Build and test
     |
     v
Merge to main
     |
     v
Build release artifact
     |
     v
Protected deployment
```

This separates validation of proposed code from production operations.

---

# 24. Troubleshooting Workflows That Do Not Run

When a workflow does not start, follow this sequence.

### Step 1: Check the file location

Is the file inside:

```text
.github/workflows/
```

at the repository root?

### Step 2: Check the trigger

Does the `on` configuration include the event you are producing?

For example:

```yaml
on:
  push:
    branches:
      - main
```

A push to `develop` will not match this branch filter.

### Step 3: Check the default branch

For `schedule` and manual dispatch, confirm that the workflow is present on the repository's default branch.

### Step 4: Check branch and path filters

A workflow may be skipped because the branch or changed files do not match the configured filters.

### Step 5: Check YAML syntax

Validate the YAML structure and GitHub Actions syntax. An editor's red underline is not always conclusive.

### Step 6: Check repository settings and permissions

Some events depend on repository settings, event-specific permissions, or the context in which the event was generated.

### Step 7: Check the Actions tab

If a workflow run exists, open it and inspect:

- The workflow run status.
- The job status.
- The failing step.
- The logs.
- The associated event and ref.

Distinguish between a workflow that never started and a workflow that started but failed.

---

# 25. Real-World CI/CD Trigger Strategies

Let's connect the concepts to a Java microservices project.

Suppose you have:

```text
UserService
HotelService
RatingService
```

You want reliable automation.

A possible design is:

## Pull request: Validate changes

```yaml
on:
  pull_request:
    branches:
      - main
```

Run compilation, unit tests, and code quality checks.

## Push: Build merged code

```yaml
on:
  push:
    branches:
      - main
```

Build the application and create a versioned artifact or container image.

## Release: Publish a version

```yaml
on:
  release:
    types:
      - published
```

Publish a release artifact or carry out release-specific automation.

## Manual dispatch: Controlled deployment

```yaml
on:
  workflow_dispatch:
```

Allow an authorized operator to initiate a deployment workflow. Use environment protection and permissions for actual production deployment.

## Schedule: Maintenance

```yaml
on:
  schedule:
    - cron: '0 2 * * *'
```

Run periodic checks and maintenance.

## Workflow run: Follow-up automation

```yaml
on:
  workflow_run:
    workflows:
      - "Build and Test"
    types:
      - completed
```

Notify a team or perform a follow-up operation when the build workflow finishes. Check the upstream conclusion if success is required.

The overall architecture might look like:

```text
                  Pull Request
                       |
                       v
                 Build and Test
                       |
                       v
                   Code Review
                       |
                       v
                     Merge
                       |
                       v
                 Push to main
                       |
                       v
                  Build Image
                       |
                       v
                 Publish Artifact
                       |
                       v
              Protected Deployment
```

This is an example design, not a requirement to create a separate workflow for every stage.

For smaller projects, a single workflow with several jobs may be easier to maintain. Separate workflows become useful when you need different permissions, independent responsibilities, reuse, or clear operational boundaries.

---

# 26. Final Mental Model

If you remember only one thing, remember this:

**A workflow trigger answers the question: "What happened that should cause GitHub to consider running this workflow?"**

Different triggers represent different reasons:

```text
Code pushed
    |
    v
   push
```

```text
Pull request opened or updated
    |
    v
pull_request
```

```text
User clicks Run workflow
    |
    v
workflow_dispatch
```

```text
Scheduled time arrives
    |
    v
 schedule
```

```text
Another workflow completes
    |
    v
workflow_run
```

```text
External system sends an API event
    |
    v
repository_dispatch
```

```text
One workflow invokes another reusable workflow
    |
    v
workflow_call
```

The general execution journey is:

```text
Event occurs
     |
     v
GitHub evaluates trigger configuration
     |
     v
Matching workflow run is created
     |
     v
Jobs are evaluated and scheduled
     |
     v
Runner executes steps
     |
     v
Success, failure, or cancellation
```

Keep these distinctions clear:

- **Event:** What happened?
- **Trigger configuration:** Which events should this workflow respond to?
- **Filters:** Which branches, tags, paths, or activity types qualify?
- **Context:** What information is available about the event and run?
- **Workflow:** What automated process is defined?
- **Job:** What unit of work should execute?
- **Runner:** Where does that job execute?
- **Step:** What individual action or command is performed?

Once these concepts are clear, GitHub Actions triggers become much easier to understand. You can move from simple push-based CI to manual deployments, scheduled maintenance, reusable workflows, and event-driven automation without treating each new trigger as an entirely different system.

---

## Official Documentation

Use the official documentation when checking exact event support, activity types, permissions, and syntax:

- [Workflow syntax for GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [Events that trigger workflows](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows)
- [Using workflow dispatch](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow)
- [Reusable workflows](https://docs.github.com/en/actions/how-tos/reuse-automations/reuse-workflows)
- [GitHub Actions security](https://docs.github.com/en/actions/reference/security/secure-use)

**Final advice:** Do not merely memorize trigger names. Create a small workflow, generate the event yourself, inspect the run, and observe `github.event_name`, `github.sha`, and `github.ref`. Practical experiments are the fastest way to develop a reliable mental model of GitHub Actions.
