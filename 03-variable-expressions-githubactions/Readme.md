# GitHub Actions Variables and Expressions — From Zero to Crystal Clear

> A complete, beginner-friendly guide to understanding variables, environment variables, contexts, expressions, scope, evaluation, and how GitHub Actions and runners work together.

---

## Table of Contents

1. [Why Do We Need Variables?](#1-why-do-we-need-variables)
2. [What Is a Variable?](#2-what-is-a-variable)
3. [The Two Worlds of GitHub Actions](#3-the-two-worlds-of-github-actions)
4. [Environment Variables (`env`)](#4-environment-variables-env)
5. [Variable Scope](#5-variable-scope)
6. [What Are Contexts?](#6-what-are-contexts)
7. [What Are Expressions?](#7-what-are-expressions)
8. [Expression Syntax vs Shell Syntax](#8-expression-syntax-vs-shell-syntax)
9. [How GitHub Actions and the Runner Work Together](#9-how-github-actions-and-the-runner-work-together)
10. [Program 1 — Environment Variables](#10-program-1--environment-variables)
11. [Program 2 — Predefined Contexts](#11-program-2--predefined-contexts)
12. [Program 3 — Two Ways to Access an Environment Variable](#12-program-3--two-ways-to-access-an-environment-variable)
13. [Program 4 — Combining Variables, Contexts, and Expressions](#13-program-4--combining-variables-contexts-and-expressions)
14. [The `vars` Context](#14-the-vars-context)
15. [The `secrets` Context](#15-the-secrets-context)
16. [The `github`, `runner`, `job`, `steps`, and `needs` Contexts](#16-the-github-runner-job-steps-and-needs-contexts)
17. [Expression Operators and Functions](#17-expression-operators-and-functions)
18. [Conditional Execution with `if`](#18-conditional-execution-with-if)
19. [Passing Information Between Steps](#19-passing-information-between-steps)
20. [Passing Information Between Jobs](#20-passing-information-between-jobs)
21. [Common Mistakes](#21-common-mistakes)
22. [Important Rules to Remember](#22-important-rules-to-remember)
23. [Practice Exercises](#23-practice-exercises)
24. [Final Mental Model](#24-final-mental-model)

---

# 1. Why Do We Need Variables?

Imagine you are building a Java application called `NetRescue`.

Your workflow needs to know:

- The application name.
- The Java version.
- The repository name.
- The person who triggered the workflow.
- The environment in which the application should run.
- Whether the current branch is `main`.

You could write every value directly into every command.

```yaml
name: Java CI

on: workflow_dispatch

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display information
        run: |
          echo "Application: NetRescue"
          echo "Application: NetRescue"
          echo "Application: NetRescue"
```

This works, but imagine changing the application name in 50 different places.

You would have to find and change every occurrence.

That creates unnecessary work and increases the chance of mistakes.

Instead, store the value once.

```yaml
env:
  APP_NAME: NetRescue
```

Now you can reuse it:

```yaml
run: echo "$APP_NAME"
```

**The main purpose of variables is to give names to values so that we can reuse and manage those values more easily.**

However, GitHub Actions has another requirement.

Sometimes, we need values that GitHub already knows, such as the repository name, branch, commit, and triggering user.

That is where **contexts and expressions** become important.

---

# 2. What Is a Variable?

A variable is a named place for accessing a value.

For example:

```text
APP_NAME = NetRescue
JAVA_VERSION = 21
ENVIRONMENT = development
```

Think of a labelled box.

```text
+---------------------------+
| Name: APP_NAME            |
| Value: NetRescue          |
+---------------------------+
```

When you need the application name, you use the variable's name rather than rewriting the value.

In GitHub Actions, however, not every named value belongs to the same category.

Common ways to work with information include:

| Mechanism | Purpose | Example |
|---|---|---|
| `env` | Define environment variables in a workflow | `APP_NAME` |
| `vars` | Access configuration variables stored in GitHub | `${{ vars.APP_NAME }}` |
| `secrets` | Access sensitive information | `${{ secrets.DOCKER_TOKEN }}` |
| Contexts | Access structured information provided to the workflow | `${{ github.repository }}` |
| Outputs | Pass values between steps or jobs | `${{ steps.info.outputs.version }}` |

An important distinction:

**Contexts are not simply another name for predefined variables.** A context is an object containing information, and the values inside it are accessed through properties.

Also, user-defined information is not always an environment variable. GitHub configuration variables, secrets, and workflow outputs are different mechanisms.

---

# 3. The Two Worlds of GitHub Actions

This is the most important foundation for understanding everything else.

A GitHub Actions workflow involves two major areas of responsibility.

## World 1 — GitHub Actions orchestration and expression evaluation

GitHub Actions reads the workflow and manages the workflow's execution.

It determines things such as:

- Which event triggered the workflow.
- Which jobs should run.
- Which runner a job requires.
- Whether an `if` condition is satisfied.
- Which values should be supplied to actions and commands through supported expressions.

For example:

```yaml
if: github.ref == 'refs/heads/main'
```

GitHub Actions evaluates the condition using the available context.

## World 2 — The runner

A runner is the machine that executes a job.

It may be a GitHub-hosted virtual machine, such as an Ubuntu runner, or a self-hosted machine.

The runner executes steps, runs actions, starts shells, and launches programs.

For example:

```yaml
run: echo "Hello World"
```

The shell on the runner executes this command.

## Visualizing the relationship

```text
             GitHub Repository
                     |
                     v
              Workflow YAML
                     |
                     v
          GitHub Actions Engine
                     |
          +----------+----------+
          |                     |
          v                     v
   Reads workflow          Evaluates supported
   and manages jobs        expressions and conditions
          |                     |
          +----------+----------+
                     |
                     v
                Job is sent
                 to a runner
                     |
                     v
             Runner executes steps
                     |
                     v
             Shell runs commands
                     |
                     v
                Final output
```

### The key distinction

GitHub Actions does not execute every line of your YAML as a shell command.

For example:

```yaml
runs-on: ubuntu-latest
```

is a workflow instruction.

```yaml
if: github.ref == 'refs/heads/main'
```

is a condition.

```yaml
run: echo "Hello"
```

specifies a shell command to execute on the runner.

Different parts of the YAML have different responsibilities.

---

# 4. Environment Variables (`env`)

An environment variable is a named value made available to a process through its environment.

You can define custom environment variables in a workflow using `env`.

```yaml
name: Environment Variables Demo

on: workflow_dispatch

env:
  APP_NAME: NetRescue
  JAVA_VERSION: '21'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display variables
        run: |
          echo "Application: $APP_NAME"
          echo "Java Version: $JAVA_VERSION"
```

Output:

```text
Application: NetRescue
Java Version: 21
```

## What happens behind the scenes?

### Step 1 — You define the variables

```yaml
env:
  APP_NAME: NetRescue
  JAVA_VERSION: '21'
```

You have defined two custom environment variables.

### Step 2 — The job starts on a runner

GitHub assigns the job to an appropriate runner.

### Step 3 — The variables become available at their defined scope

The runner's processes receive the appropriate environment variables.

### Step 4 — The shell reads them

When the shell executes:

```bash
echo "$APP_NAME"
```

it looks up `APP_NAME` in its environment and expands the reference to its value.

The effective command is equivalent to:

```bash
echo "NetRescue"
```

The output is:

```text
NetRescue
```

**Remember:** `$APP_NAME` is shell syntax. The shell expands it using the environment available to the process.

This example uses Bash on Ubuntu. Shell-variable syntax differs between shells; for example, PowerShell generally uses `$env:APP_NAME`.

---

# 5. Variable Scope

Scope answers one question:

**Where is this variable available?**

GitHub Actions allows environment variables to be defined at three levels.

## 5.1 Workflow-level variables

These are defined at the top level of the workflow.

```yaml
name: Workflow Scope

on: workflow_dispatch

env:
  APP_NAME: NetRescue

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - run: echo "$APP_NAME"

  test:
    runs-on: ubuntu-latest

    steps:
      - run: echo "$APP_NAME"
```

Both jobs can access `APP_NAME`.

Workflow-level environment variables apply throughout the workflow, subject to the environment-variable scope rules.

## 5.2 Job-level variables

These are defined inside a job.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest

    env:
      JAVA_VERSION: '21'

    steps:
      - run: echo "$JAVA_VERSION"

      - run: echo "$JAVA_VERSION"
```

Both steps can access `JAVA_VERSION`.

A different job cannot access this variable through the same job-level definition.

## 5.3 Step-level variables

These are defined inside a particular step.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Step One
        env:
          MESSAGE: Hello
        run: echo "$MESSAGE"

      - name: Step Two
        run: echo "The previous step had its own MESSAGE variable"
```

The first step can access `MESSAGE`.

The second step does not inherit it from the first step.

## Scope hierarchy

```text
Workflow
|
|-- APP_NAME
|
|-- Job: build
|   |
|   |-- JAVA_VERSION
|   |
|   |-- Step 1
|   |   |
|   |   |-- MESSAGE
|   |
|   |-- Step 2
|
|-- Job: test
```

A more specific `env` definition can override a variable of the same name from a broader scope.

For example, a job-level `APP_NAME` overrides the workflow-level `APP_NAME` inside that job, and a step-level definition overrides it within that step.

---

# 6. What Are Contexts?

A context is an object that contains information available to a GitHub Actions workflow.

Think of a context as a labelled information folder.

For example, GitHub knows information about the event, repository, user, runner, job, and previous steps.

It exposes relevant information through contexts.

## Example: The `github` context

```yaml
${{ github.actor }}
${{ github.repository }}
${{ github.ref }}
${{ github.sha }}
```

These expressions access properties of the `github` context.

| Expression | Meaning |
|---|---|
| `${{ github.actor }}` | The username associated with the event's triggering actor |
| `${{ github.repository }}` | Repository name in `owner/repository` format |
| `${{ github.ref }}` | The fully formed ref associated with the triggering event |
| `${{ github.sha }}` | The commit SHA associated with the event |

For example, the expression:

```yaml
${{ github.repository }}
```

might resolve to:

```text
tahashafiq175/NetRescue
```

The exact value depends on the workflow run.

## Context versus property

Consider:

```yaml
${{ github.actor }}
```

Break it down:

```text
${{ github.actor }}
     |       |
     |       +-- Property
     |
     +-- Context
```

`github` is the context object.

`actor` is a property inside that context.

The expression accesses the property and returns its value.

## Important: Contexts are not all the same

Different contexts provide different information.

| Context | What it provides |
|---|---|
| `github` | Repository, event, ref, actor, and related GitHub information |
| `env` | Custom environment variables defined in the workflow |
| `vars` | Configuration variables defined in GitHub settings |
| `secrets` | Sensitive values made available to the workflow |
| `runner` | Information about the runner |
| `job` | Information about the current job |
| `steps` | Outputs and status information from steps |
| `needs` | Outputs and results from jobs that the current job depends on |
| `matrix` | Values for the current matrix job combination |
| `strategy` | Information about the job's execution strategy |

Not every context is available in every part of the workflow. Availability depends on the particular workflow key and the stage of execution.

---

# 7. What Are Expressions?

An expression is a piece of logic that GitHub Actions evaluates to obtain a value or determine a result.

Expressions use this syntax:

```yaml
${{ expression }}
```

Examples:

```yaml
${{ github.actor }}
${{ github.repository }}
${{ env.APP_NAME }}
${{ vars.DEPLOY_ENVIRONMENT }}
${{ github.ref == 'refs/heads/main' }}
```

Expressions can access information, compare values, perform logical operations, and call supported functions.

## Example 1 — Retrieve information

```yaml
${{ github.actor }}
```

Meaning:

> Get the actor value from the `github` context.

## Example 2 — Retrieve an environment variable

```yaml
${{ env.APP_NAME }}
```

Meaning:

> Get `APP_NAME` from the workflow's `env` context.

## Example 3 — Compare values

```yaml
${{ github.ref == 'refs/heads/main' }}
```

Meaning:

> Is the current Git reference equal to `refs/heads/main`?

The result is a Boolean value: `true` or `false`.

## Example 4 — Combine conditions

```yaml
${{ github.event_name == 'push' && github.ref == 'refs/heads/main' }}
```

Meaning:

> Did a push event occur, and is the reference `main`?

Both conditions must be true for the expression to evaluate to true.

### The central idea

The expression syntax is not a shell command.

GitHub Actions evaluates the expression in a supported workflow location and uses the resulting value according to that location's rules.

---

# 8. Expression Syntax vs Shell Syntax

This is one of the most important distinctions in GitHub Actions.

Consider these two references:

```yaml
${{ env.APP_NAME }}
```

```bash
$APP_NAME
```

They may return the same value, but they are evaluated by different mechanisms.

## Method 1 — GitHub Actions expression

```yaml
name: Expression Demo

on: workflow_dispatch

env:
  APP_NAME: NetRescue

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Expression
        run: echo "${{ env.APP_NAME }}"
```

GitHub Actions evaluates the expression in the workflow command before the shell executes the resulting command.

Conceptually, the shell receives:

```bash
echo "NetRescue"
```

## Method 2 — Shell environment-variable expansion

```yaml
name: Shell Demo

on: workflow_dispatch

env:
  APP_NAME: NetRescue

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Shell
        run: echo "$APP_NAME"
```

The shell expands `$APP_NAME` using the environment variable available to the process.

## Comparison

| Feature | `${{ env.APP_NAME }}` | `$APP_NAME` |
|---|---|---|
| Syntax type | GitHub Actions expression | Shell variable expansion |
| Evaluated by | GitHub Actions expression evaluator | Shell on the runner |
| Where used | Supported workflow expression locations | Shell commands |
| Requires an `env` context entry? | Yes | Requires the variable in the process environment |
| Works in `if` expressions? | The expression syntax is used there, but shell syntax is not | No |

### A security consideration

Expressions inserted directly into shell scripts can create script-injection risks when their values contain untrusted data.

For values originating from user-controlled event content, prefer passing the value through an environment variable and using a properly quoted shell reference.

For example:

```yaml
steps:
  - name: Safely handle event title
    env:
      EVENT_TITLE: ${{ github.event.pull_request.title }}
    run: |
      printf '%s\n' "$EVENT_TITLE"
```

This separates expression evaluation from shell interpretation and avoids directly inserting the untrusted title into the shell program.

The general lesson is to treat event content and other externally controlled values as data, not executable shell code.

---

# 9. How GitHub Actions and the Runner Work Together

Let's follow a complete example.

```yaml
name: Execution Flow

on: workflow_dispatch

env:
  APP_NAME: NetRescue

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Print information
        run: |
          echo "App: $APP_NAME"
          echo "Actor: ${{ github.actor }}"
```

## Step 1 — You write the workflow

You define the custom variable:

```yaml
env:
  APP_NAME: NetRescue
```

You also use the predefined GitHub context:

```yaml
${{ github.actor }}
```

## Step 2 — GitHub Actions processes the workflow

GitHub Actions interprets the workflow structure, resolves supported expressions at the appropriate time, and manages job execution.

For this example, suppose the actor is `tahashafiq175`.

The expression is evaluated to that actor value.

Conceptually, the runner-side script contains:

```bash
echo "App: $APP_NAME"
echo "Actor: tahashafiq175"
```

## Step 3 — The runner receives the job

The job runs on an Ubuntu runner.

The `APP_NAME` environment variable is made available to the relevant process.

## Step 4 — The shell executes the script

The shell expands:

```bash
$APP_NAME
```

to:

```text
NetRescue
```

## Step 5 — The output appears

```text
App: NetRescue
Actor: tahashafiq175
```

### Remember

Not every expression is evaluated at exactly the same moment. Evaluation timing and available contexts depend on the workflow key and execution stage.

The example above demonstrates the essential distinction, not a claim that GitHub substitutes every expression in the entire YAML file in one universal pass.

---

# 10. Program 1 — Environment Variables

**Objective:** Understand how custom environment variables work.

```yaml
name: Program 1 - Environment Variables

on: workflow_dispatch

env:
  APP_NAME: NetRescue
  JAVA_VERSION: '21'

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Print variables
        run: |
          echo "Application: $APP_NAME"
          echo "Java Version: $JAVA_VERSION"
```

### Expected output

```text
Application: NetRescue
Java Version: 21
```

### What did we learn?

- `env` defines environment variables.
- Workflow-level variables are available throughout the workflow.
- The runner provides the variables to the relevant processes.
- The shell expands `$APP_NAME` and `$JAVA_VERSION`.

---

# 11. Program 2 — Predefined Contexts

**Objective:** Understand how to retrieve information GitHub Actions provides.

```yaml
name: Program 2 - Contexts

on: workflow_dispatch

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Display GitHub information
        run: |
          echo "Actor: ${{ github.actor }}"
          echo "Repository: ${{ github.repository }}"
          echo "Event: ${{ github.event_name }}"
          echo "Runner OS: ${{ runner.os }}"
```

### Expected output

Your output will depend on the workflow run.

For example:

```text
Actor: tahashafiq175
Repository: tahashafiq175/NetRescue
Event: workflow_dispatch
Runner OS: Linux
```

### What did we learn?

- Contexts provide structured information.
- Expressions access values inside contexts.
- `${{ github.actor }}` is not a shell variable.
- `${{ runner.os }}` retrieves information about the runner.

---

# 12. Program 3 — Two Ways to Access an Environment Variable

**Objective:** Understand the difference between expression evaluation and shell expansion.

```yaml
name: Program 3 - Expressions vs Shell

on: workflow_dispatch

env:
  APP_NAME: NetRescue

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Access variable in two ways
        run: |
          echo "Method 1 - Shell: $APP_NAME"
          echo "Method 2 - Expression: ${{ env.APP_NAME }}"
```

### Expected output

```text
Method 1 - Shell: NetRescue
Method 2 - Expression: NetRescue
```

### What did we learn?

Both expressions access the same logical value, but through different mechanisms.

- `$APP_NAME`: expanded by the shell.
- `${{ env.APP_NAME }}`: evaluated by GitHub Actions.

This distinction is fundamental to understanding workflow execution.

---

# 13. Program 4 — Combining Variables, Contexts, and Expressions

**Objective:** Bring everything together.

```yaml
name: Program 4 - Complete Variables Demo

on: workflow_dispatch

env:
  APP_NAME: NetRescue
  JAVA_VERSION: '21'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Show information
        env:
          ENVIRONMENT: development

        run: |
          echo "========== APPLICATION =========="
          echo "App: $APP_NAME"
          echo "Java: $JAVA_VERSION"
          echo "Environment: $ENVIRONMENT"

          echo ""
          echo "========== GITHUB INFORMATION =========="
          echo "Actor: ${{ github.actor }}"
          echo "Repository: ${{ github.repository }}"
          echo "Event: ${{ github.event_name }}"

          echo ""
          echo "========== COMBINED INFORMATION =========="
          echo "${{ github.repository }} is running $APP_NAME"
```

### Expected output

```text
========== APPLICATION ==========
App: NetRescue
Java: 21
Environment: development

========== GITHUB INFORMATION ==========
Actor: tahashafiq175
Repository: tahashafiq175/NetRescue
Event: workflow_dispatch

========== COMBINED INFORMATION ==========
tahashafiq175/NetRescue is running NetRescue
```

Your actor and repository values may differ.

### What did we learn?

This workflow combines:

- Workflow-level environment variables.
- Step-level environment variables.
- The `github` context.
- GitHub Actions expressions.
- Shell environment-variable expansion.
- Runner-side command execution.

---

# 14. The `vars` Context

So far, we have defined environment variables directly in the YAML file.

GitHub also allows you to define configuration variables in repository, organization, or environment settings, subject to the relevant permissions and availability rules.

These are accessed through the `vars` context.

For example:

```yaml
${{ vars.APP_NAME }}
```

Suppose you define a repository configuration variable:

```text
Name: APP_NAME
Value: NetRescue
```

You can use it in a workflow:

```yaml
name: Configuration Variables

on: workflow_dispatch

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Display configuration
        run: echo "Application: ${{ vars.APP_NAME }}"
```

### Why use `vars`?

Imagine several workflows use the same application name or deployment region.

You can centrally manage configuration values in GitHub settings rather than repeating the same configuration in each workflow.

### `env` versus `vars`

| `env` | `vars` |
|---|---|
| Defined in workflow YAML | Defined in GitHub configuration settings |
| Can have workflow, job, and step scope | Available through the relevant GitHub configuration scope |
| Accessed by shell as `$APP_NAME` when exported to the process | Accessed through `${{ vars.APP_NAME }}` in supported expression locations |
| Useful for workflow-specific environment values | Useful for reusable configuration |

A configuration variable is not automatically identical to an environment variable. If you want to use a `vars` value as a shell environment variable, explicitly map it:

```yaml
jobs:
  demo:
    runs-on: ubuntu-latest

    env:
      APP_NAME: ${{ vars.APP_NAME }}

    steps:
      - run: echo "$APP_NAME"
```

---

# 15. The `secrets` Context

A secret is sensitive information, such as a token, password, or private key.

For example:

```yaml
${{ secrets.DOCKER_TOKEN }}
```

Secrets are not ordinary configuration variables.

Do not hardcode passwords or tokens directly into workflow files.

Instead, create the appropriate GitHub Actions secret and access it in a supported context.

Example:

```yaml
name: Secret Demo

on: workflow_dispatch

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Use a secret
        env:
          DOCKER_TOKEN: ${{ secrets.DOCKER_TOKEN }}
        run: |
          if [ -n "$DOCKER_TOKEN" ]; then
            echo "A Docker token is available."
          else
            echo "No Docker token is available."
          fi
```

This example checks whether a value is present without printing the secret itself.

### Why map the secret into `env`?

This lets the shell use the value through:

```bash
"$DOCKER_TOKEN"
```

It also avoids directly inserting the secret into the shell script.

Remember that secrets have availability restrictions, and GitHub generally does not pass secrets to workflows triggered by forks of repositories, except for certain permitted scenarios such as the documented `GITHUB_TOKEN` behavior.

Never print secrets intentionally. Masking is a safeguard, not a guarantee that sensitive information cannot leak.

---

# 16. The `github`, `runner`, `job`, `steps`, and `needs` Contexts

These contexts expose different types of information.

## 16.1 The `github` context

Example:

```yaml
${{ github.repository }}
${{ github.actor }}
${{ github.event_name }}
```

It provides information related to GitHub and the workflow event.

## 16.2 The `runner` context

Example:

```yaml
${{ runner.os }}
${{ runner.arch }}
```

It provides information about the runner.

Example:

```yaml
- name: Runner information
  run: |
    echo "OS: ${{ runner.os }}"
    echo "Architecture: ${{ runner.arch }}"
```

## 16.3 The `job` context

The `job` context provides information about the current job.

For example, `job.status` represents the current job status in supported contexts.

```yaml
${{ job.status }}
```

## 16.4 The `steps` context

The `steps` context provides information about steps, including their outputs and outcomes.

For example:

```yaml
${{ steps.info.outputs.version }}
```

This accesses the `version` output from a step with the ID `info`.

## 16.5 The `needs` context

The `needs` context provides information about jobs that the current job depends on.

For example:

```yaml
${{ needs.build.outputs.app_version }}
```

This can access an output exported by the `build` job when the current job declares the dependency.

## 16.6 The `matrix` context

When using a matrix strategy, the `matrix` context contains the values for the current job combination.

Example:

```yaml
strategy:
  matrix:
    java: ['17', '21']

steps:
  - name: Display Java version
    run: echo "Java version: ${{ matrix.java }}"
```

The workflow runs the job for the defined Java versions.

### Important rule

A context's existence does not mean it is available in every expression location. Always check the supported context list for the workflow key you are using.

---

# 17. Expression Operators and Functions

Expressions can do more than retrieve values.

They can compare values, combine conditions, and call functions.

## 17.1 Comparison operators

```yaml
${{ github.ref == 'refs/heads/main' }}
```

The `==` operator checks equality.

Other comparison operators include:

| Operator | Meaning |
|---|---|
| `==` | Equal |
| `!=` | Not equal |
| `>` | Greater than |
| `>=` | Greater than or equal |
| `<` | Less than |
| `<=` | Less than or equal |

Example:

```yaml
if: ${{ github.event_name == 'push' }}
```

## 17.2 Logical operators

### AND (`&&`)

Both conditions must be true.

```yaml
${{ github.event_name == 'push' && github.ref == 'refs/heads/main' }}
```

### OR (`||`)

At least one condition must be true.

```yaml
${{ github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop' }}
```

### NOT (`!`)

Negates a Boolean condition.

```yaml
${{ !cancelled() }}
```

When using `!` directly in YAML, be careful: YAML treats an unquoted leading exclamation mark as special syntax. GitHub's documentation recommends using the expression delimiters, such as:

```yaml
if: ${{ !cancelled() }}
```

## 17.3 Useful functions

### `contains()`

Checks whether a string or collection contains a value.

```yaml
${{ contains('apple,banana,orange', 'banana') }}
```

### `startsWith()`

Checks whether a string begins with a value.

```yaml
${{ startsWith(github.ref, 'refs/heads/release/') }}
```

### `endsWith()`

Checks whether a string ends with a value.

```yaml
${{ endsWith(github.ref, '/main') }}
```

### `format()`

Creates a formatted string.

```yaml
${{ format('Application: {0}', env.APP_NAME) }}
```

### `join()`

Joins the items in an array into a string.

```yaml
${{ join(matrix.java, ', ') }}
```

Use `join()` with a suitable array value; `matrix.java` in the earlier matrix example is a scalar for each job, not an array.

### `toJSON()`

Converts a value into JSON-formatted text, which can be useful for debugging non-sensitive context data.

```yaml
${{ toJSON(github.event) }}
```

Use care when printing event data, as it may contain user-controlled or sensitive information.

---

# 18. Conditional Execution with `if`

One of the most useful applications of expressions is deciding whether a job or step should run.

Suppose you want a deployment step to run only on the `main` branch.

```yaml
name: Conditional Execution

on:
  push:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Build application
        run: echo "Building application"

      - name: Deploy application
        if: ${{ github.ref == 'refs/heads/main' }}
        run: echo "Deploying application"
```

### What happens?

If the push is to `main`, the condition is true and the deployment step is eligible to run, subject to the normal status and execution rules.

If the push is to `develop`, the condition is false, so the deployment step is skipped.

### Why is this useful?

Expressions let you build workflows that react to different branches, events, job results, and configuration values without maintaining separate workflow files for every scenario.

Note that GitHub Actions also supports status-check functions such as `success()`, `failure()`, `cancelled()`, and `always()`. These affect how conditions interact with previous step and job results.

---

# 19. Passing Information Between Steps

Environment variables defined at a step scope are not automatically shared with later steps.

If a later step needs a value produced by an earlier step, step outputs are often the right mechanism.

Example:

```yaml
name: Step Outputs

on: workflow_dispatch

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: Generate version
        id: info
        run: echo "version=1.0.0" >> "$GITHUB_OUTPUT"

      - name: Read version
        run: echo "Version: ${{ steps.info.outputs.version }}"
```

### Understand the flow

The first step writes an output to the special file referenced by `GITHUB_OUTPUT`.

The step has the ID:

```yaml
id: info
```

The next step reads the output through:

```yaml
${{ steps.info.outputs.version }}
```

The value is:

```text
1.0.0
```

Output:

```text
Version: 1.0.0
```

### Why use outputs instead of `env`?

Use environment variables to provide values to processes.

Use outputs when you need to pass a result from one step to another through GitHub Actions' workflow data mechanism.

---

# 20. Passing Information Between Jobs

Jobs normally run in separate execution environments. A shell variable created in one job does not automatically become a shell variable in another job.

To pass information between jobs, use job outputs and the `needs` context.

```yaml
name: Job Outputs

on: workflow_dispatch

jobs:
  build:
    runs-on: ubuntu-latest

    outputs:
      app_version: ${{ steps.info.outputs.version }}

    steps:
      - name: Generate version
        id: info
        run: echo "version=1.0.0" >> "$GITHUB_OUTPUT"

  deploy:
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Read build output
        run: echo "Deploying version ${{ needs.build.outputs.app_version }}"
```

### Execution flow

```text
Job: build
    |
    | Creates step output
    v
steps.info.outputs.version
    |
    | Exposed as a job output
    v
needs.build.outputs.app_version
    |
    v
Job: deploy
```

The `needs: build` declaration makes the build job a dependency of the deploy job and exposes its outputs through the `needs` context.

This is a common pattern in real CI/CD pipelines.

---

# 21. Common Mistakes

## Mistake 1 — Using shell syntax in an `if` condition

Incorrect:

```yaml
if: $APP_NAME == 'NetRescue'
```

Correct:

```yaml
if: ${{ env.APP_NAME == 'NetRescue' }}
```

The `if` key uses GitHub Actions expression semantics, not shell variable expansion.

## Mistake 2 — Assuming `env` variables automatically cross jobs

A job-level environment variable belongs to that job.

If another job needs the value, define it in that job too or pass the result using an appropriate output.

## Mistake 3 — Confusing `vars` with `env`

Incorrect assumption:

> Every configuration variable defined in GitHub settings is automatically available as a shell variable.

Instead, access it through the `vars` context or explicitly map it into `env`.

## Mistake 4 — Printing secrets

Avoid commands such as:

```yaml
run: echo "$DOCKER_TOKEN"
```

Even with masking, printing sensitive values is unsafe.

## Mistake 5 — Assuming every expression is evaluated on the runner

Expressions such as:

```yaml
${{ github.repository }}
```

are evaluated by GitHub Actions in supported workflow contexts.

They are not ordinary shell variables.

## Mistake 6 — Assuming every context is available everywhere

Context availability depends on the workflow key and execution stage.

For example, some contexts are available in a job's `if` condition while others are not.

## Mistake 7 — Assuming variables are automatically available inside every action

An environment variable set for a step is available to that step's process environment, including the action process where applicable. But an action's internal inputs, configuration, and behavior are defined by that action's own interface.

For a reusable action, use its documented inputs when that is the intended configuration mechanism.

## Mistake 8 — Assuming the shell syntax is universal

On Ubuntu, the default shell for a `run` step is typically Bash.

On Windows, the default shell is typically PowerShell.

For example:

```bash
echo "$APP_NAME"
```

is Bash-style syntax.

In PowerShell, use:

```powershell
$env:APP_NAME
```

---

# 22. Important Rules to Remember

1. **Variables store or expose values.** They save repetition and make workflows easier to maintain.

2. **`env` defines environment variables.** The scope can be workflow-wide, job-wide, or step-specific.

3. **Contexts are structured objects.** They provide access to information such as repository details, runner details, and step outputs.

4. **Expressions use `${{ ... }}`.** GitHub Actions evaluates expressions in supported workflow locations.

5. **Shell variables use shell-specific syntax.** For example, Bash uses `$APP_NAME`.

6. **GitHub Actions and runners have different responsibilities.** GitHub manages workflow orchestration and expression evaluation; runners execute jobs and commands.

7. **`vars` is for configuration variables.** Values are stored in GitHub settings and accessed through the `vars` context.

8. **`secrets` is for sensitive values.** Do not hardcode credentials in workflow files or print them.

9. **Outputs transfer results.** Use step outputs and job outputs when values must flow between steps or jobs.

10. **Context availability depends on location.** Consult the GitHub Actions documentation for the relevant workflow key.

11. **Expression evaluation is context-dependent.** Do not assume that all expressions are evaluated at one universal time.

12. **Treat untrusted data as data.** Avoid injecting user-controlled values directly into shell scripts.

---

# 23. Practice Exercises

Try these exercises yourself before checking the solutions.

## Exercise 1 — Create your own variables

Define these variables at workflow scope:

```text
APP_NAME = NetRescue
LANGUAGE = Java
JAVA_VERSION = 21
```

Print all three in one step.

<details>
<summary>Solution</summary>

```yaml
name: Exercise 1

on: workflow_dispatch

env:
  APP_NAME: NetRescue
  LANGUAGE: Java
  JAVA_VERSION: '21'

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - run: |
          echo "App: $APP_NAME"
          echo "Language: $LANGUAGE"
          echo "Java: $JAVA_VERSION"
```

</details>

## Exercise 2 — Retrieve GitHub information

Print:

- The actor.
- The repository.
- The triggering event.

<details>
<summary>Solution</summary>

```yaml
name: Exercise 2

on: workflow_dispatch

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - run: |
          echo "Actor: ${{ github.actor }}"
          echo "Repository: ${{ github.repository }}"
          echo "Event: ${{ github.event_name }}"
```

</details>

## Exercise 3 — Override a variable

Define `APP_NAME` at workflow scope as `DefaultApp`.

Override it at job scope as `NetRescue`.

Print the value inside the job.

<details>
<summary>Solution</summary>

```yaml
name: Exercise 3

on: workflow_dispatch

env:
  APP_NAME: DefaultApp

jobs:
  demo:
    runs-on: ubuntu-latest

    env:
      APP_NAME: NetRescue

    steps:
      - run: echo "$APP_NAME"
```

Expected output:

```text
NetRescue
```

</details>

## Exercise 4 — Run only on main

Create a step that runs only when the workflow reference is `refs/heads/main`.

<details>
<summary>Solution</summary>

```yaml
- name: Main branch step
  if: ${{ github.ref == 'refs/heads/main' }}
  run: echo "Running on main"
```

</details>

## Exercise 5 — Pass a value between steps

Create a step output called `version` with the value `2.0.0`, then print it in the next step.

<details>
<summary>Solution</summary>

```yaml
steps:
  - name: Create version
    id: info
    run: echo "version=2.0.0" >> "$GITHUB_OUTPUT"

  - name: Print version
    run: echo "${{ steps.info.outputs.version }}"
```

</details>

---

# 24. Final Mental Model

If you remember only one section from this entire guide, remember this one.

## The complete picture

```text
              WORKFLOW YAML
                    |
                    v
          GITHUB ACTIONS ENGINE
                    |
         +----------+----------+
         |                     |
         v                     v
  Workflow instructions    Contexts and expressions
  jobs, steps, runs-on     ${{ github.actor }}
  conditions, dependencies ${{ env.APP_NAME }}
         |                     |
         +----------+----------+
                    |
                    v
               JOB RUNNER
          Ubuntu / Windows /
           macOS / self-hosted
                    |
                    v
             ENVIRONMENT
          APP_NAME=NetRescue
                    |
                    v
            SHELL EXECUTION
          echo "$APP_NAME"
                    |
                    v
                 OUTPUT
             NetRescue
```

### The four concepts

**1. Environment variables**

Values that you define and make available to processes.

```yaml
env:
  APP_NAME: NetRescue
```

**2. Contexts**

Objects containing information available to the workflow.

```yaml
${{ github.repository }}
```

**3. Expressions**

The syntax GitHub Actions uses to evaluate values and logic.

```yaml
${{ github.ref == 'refs/heads/main' }}
```

**4. Shell expansion**

The mechanism through which the shell accesses environment variables.

```bash
echo "$APP_NAME"
```

### The final explanation in simple words

When you write a GitHub Actions workflow, you give GitHub Actions instructions about what should happen.

GitHub Actions provides contexts containing information about the repository, event, runner, jobs, and other workflow data. You can use expressions to retrieve this information, compare values, and control workflow behavior.

You can also define your own environment variables using `env`. When a job runs on a runner, those variables become available to the appropriate processes. The shell can then expand references such as `$APP_NAME` while executing your commands.

If you need to pass information between steps or jobs, use outputs. If you need reusable configuration, consider `vars`. If you need sensitive credentials, use `secrets`.

**That is the foundation of variables and expressions in GitHub Actions.**

---

## Official Documentation

- [Variables](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-variables)
- [Contexts reference](https://docs.github.com/en/actions/reference/workflows-and-actions/contexts)
- [Expressions reference](https://docs.github.com/en/actions/reference/workflows-and-actions/expressions)
- [Workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [Passing information between steps](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/pass-job-outputs)
- [Using secrets in GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets)

**Learning recommendation:** Run Programs 1–4 in your own repository, inspect the output, and then complete the exercises. The concepts become much clearer when you see the expressions evaluated and the shell commands executed in an actual workflow run.