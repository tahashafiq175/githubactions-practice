# GitHub Actions Contexts — Complete Beginner-to-Advanced Guide

> **The goal of this guide:** Understand GitHub Actions contexts so clearly that you know what they are, why they exist, how they work, when to use them, and how to use them in real CI/CD workflows.

---

# Table of Contents

1. What Is a Context?
2. Why Do Contexts Exist?
3. Understanding Contexts Through a Simple Story
4. What Is an Object and a Property?
5. Understanding Expression Syntax
6. How Contexts Work Behind the Scenes
7. Contexts vs. Default Environment Variables
8. Why Contexts Can Be Used Before a Runner Starts
9. Important GitHub Actions Contexts
10. The `github` Context
11. The `env` Context
12. The `vars` Context
13. The `secrets` Context
14. The `runner` Context
15. The `job` Context
16. The `steps` Context
17. The `needs` Context
18. The `matrix` Context
19. The `strategy` Context
20. The `inputs` Context
21. The `needs` Context and Job Outputs
22. Context Availability
23. Practical Workflow Examples
24. Common Beginner Mistakes
25. Security: Never Trust Every Context Value
26. Best Practices
27. Final Revision

---

# 1. What Is a Context?

Imagine you have created a GitHub Actions workflow.

You push code to GitHub, and GitHub starts your workflow.

During that process, GitHub needs to know many things:

- Who triggered the workflow?
- Which repository contains the code?
- Which branch received the push?
- Which commit was pushed?
- Which operating system is running the job?
- Did the previous step succeed?
- What value did an earlier job produce?
- Which matrix configuration is currently being executed?

Where does GitHub Actions get this information?

GitHub Actions provides special objects called **contexts**.

A context is an object containing information about a particular part of a workflow or its execution.

For example:

```yaml
${{ github.actor }}
```

This expression accesses the username of the person or account that triggered the workflow.

Another example:

```yaml
${{ runner.os }}
```

This expression accesses information about the operating system of the runner executing the job.

Another example:

```yaml
${{ matrix.java }}
```

This expression accesses a value from the current matrix configuration, provided the job uses a matrix containing that property.

## The basic idea

Think of contexts as information boxes maintained by GitHub Actions.

```text
GitHub Actions
      |
      |---- github context
      |       |---- actor
      |       |---- repository
      |       |---- ref
      |       |---- sha
      |
      |---- runner context
      |       |---- os
      |       |---- arch
      |
      |---- matrix context
              |---- java
              |---- os
```

Each context contains information related to a particular subject.

**Remember:** Contexts are not something you need to create before using them. GitHub Actions provides the built-in contexts. You access their properties using expressions.

---

# 2. Why Do Contexts Exist?

Let's understand the problem before learning the solution.

Suppose you want to run tests only when someone pushes code to the `main` branch.

Your requirement is:

> If the workflow event belongs to the main branch, run the tests. Otherwise, skip them.

GitHub must know which branch triggered the workflow.

You can access this information through:

```yaml
${{ github.ref }}
```

For a push to the main branch, its value is normally:

```text
refs/heads/main
```

You can then write:

```yaml
jobs:
  test:
    if: ${{ github.ref == 'refs/heads/main' }}
    runs-on: ubuntu-latest

    steps:
      - name: Run tests
        run: echo "Running tests on main"
```

GitHub evaluates the condition to determine whether the job should run.

But notice something important.

GitHub must evaluate the job-level condition before sending that job to a runner.

The runner cannot be responsible for every decision because GitHub needs to decide whether the job should be sent to a runner in the first place.

Contexts allow GitHub Actions to access information when processing workflow configuration, as well as during job execution where the relevant context is available.

That is one of their most important purposes.

---

# 3. Understanding Contexts Through a Simple Story

Imagine a software company.

The company receives a new software delivery request.

Before assigning the work to an employee, the manager needs to know:

- Who submitted the request?
- Which project is involved?
- What type of work is required?

After assigning the work, the employee can provide additional information:

- Which computer is being used?
- Which task has completed?
- What result did the task produce?

GitHub Actions works in a similar way.

| Real-world example | GitHub Actions equivalent |
|---|---|
| Request submitter | `github.actor` |
| Project name | `github.repository` |
| Requested branch | `github.ref` |
| Employee's computer environment | `runner.os` |
| Earlier step's result | `steps.<step_id>.outcome` |
| Earlier job's output | `needs.<job_id>.outputs.<output_name>` |
| Current matrix configuration | `matrix` |

This analogy helps you understand why GitHub provides different contexts.

Each context provides information about a different part of the process.

---

# 4. What Is an Object and a Property?

Before going further, understand two simple programming concepts.

## 4.1 Object

An object groups related information together.

For example, imagine an object named `student`:

```text
student
   |
   |---- name
   |---- semester
   |---- university
```

The object is `student`.

It groups information about a student.

GitHub Actions contexts work similarly.

For example:

```text
github
   |
   |---- actor
   |---- repository
   |---- ref
   |---- sha
```

Here, `github` is the context object.

## 4.2 Property

A property is an individual piece of information inside an object.

For example:

```yaml
${{ github.repository }}
```

Break it down:

- `github` is the context.
- `repository` is the property.
- `${{ ... }}` is the expression syntax used to access and evaluate it.

The expression might evaluate to:

```text
tahashafiq175/githubactions-practice
```

The exact value depends on the workflow run.

## 4.3 Another example

```yaml
${{ runner.os }}
```

Here:

- `runner` is the context.
- `os` is the property.

Its value might be:

```text
Linux
```

**Important:** A context does not always contain only simple text values. Contexts can also contain nested objects, booleans, arrays, and other structured values.

---

# 5. Understanding Expression Syntax

GitHub Actions uses a special expression syntax:

```yaml
${{ expression }}
```

The expression tells GitHub Actions to evaluate the expression and obtain its value where expression evaluation is supported.

Examples:

```yaml
${{ github.actor }}
```

```yaml
${{ github.repository }}
```

```yaml
${{ runner.os }}
```

```yaml
${{ matrix.java }}
```

```yaml
${{ needs.build.result }}
```

You can use expressions in supported workflow fields.

For example:

```yaml
name: Context Demonstration

on:
  workflow_dispatch:

jobs:
  information:
    runs-on: ubuntu-latest

    steps:
      - name: Display information
        run: |
          echo "Repository: ${{ github.repository }}"
          echo "Actor: ${{ github.actor }}"
          echo "Operating system: ${{ runner.os }}"
```

Possible output:

```text
Repository: tahashafiq175/githubactions-practice
Actor: TahaShafiq
Operating system: Linux
```

These are illustrative values. Your actual output depends on your repository, account, and runner.

## A useful detail

In an `if` condition, GitHub Actions generally allows you to omit the `${{ }}` delimiters.

These are both valid:

```yaml
if: ${{ github.ref == 'refs/heads/main' }}
```

```yaml
if: github.ref == 'refs/heads/main'
```

The expression is still evaluated by GitHub Actions.

---

# 6. How Contexts Work Behind the Scenes

Let's follow a workflow from the beginning.

Imagine you push a commit to GitHub.

```text
Step 1: You push code
          |
          v
Step 2: GitHub receives the event
          |
          v
Step 3: GitHub identifies workflow information
          |
          |---- Repository
          |---- Actor
          |---- Branch or ref
          |---- Commit SHA
          |
          v
Step 4: GitHub evaluates applicable conditions
          |
          v
Step 5: GitHub determines which jobs should run
          |
          v
Step 6: A runner executes an eligible job
          |
          |---- Commands execute
          |---- Steps produce results
          |---- Outputs may be generated
          |
          v
Step 7: GitHub makes relevant results available
          |
          v
Step 8: Dependent jobs can use those results
```

This is a simplified explanation. Actual workflow scheduling and evaluation depend on triggers, dependencies, conditions, and the specific workflow fields being processed.

The key lesson is that context information is used at different points in this process.

Some information exists from the beginning of the run. Other information becomes available only after a job or step has executed.

For example:

```yaml
${{ github.repository }}
```

identifies the repository associated with the run.

But:

```yaml
${{ steps.compile.outputs.version }}
```

depends on a step with the ID `compile` having produced an output named `version`.

GitHub cannot obtain that step's actual output before the step has produced it.

**Therefore, context availability depends on both the context and the stage of execution.**

---

# 7. Contexts vs. Default Environment Variables

This is one of the most confusing topics for beginners.

Let's make it clear.

GitHub Actions provides both:

1. Contexts.
2. Default environment variables.

They are related, but they are not the same thing.

## 7.1 What are default environment variables?

Default environment variables are predefined environment variables made available to runner processes.

Examples include:

```text
GITHUB_SHA
GITHUB_REF
GITHUB_REPOSITORY
GITHUB_ACTOR
RUNNER_OS
```

You commonly access them inside shell commands.

For example:

```yaml
steps:
  - name: Display repository
    run: echo "$GITHUB_REPOSITORY"
```

The shell running on the runner reads the environment variable.

## 7.2 What are contexts?

Contexts are objects accessed using GitHub Actions expression syntax.

For example:

```yaml
steps:
  - name: Display repository
    run: echo "${{ github.repository }}"
```

GitHub Actions evaluates the expression and makes its value part of the generated command.

The two examples may display the same repository name, but they access information through different mechanisms.

## 7.3 Compare them

| Feature | Context | Default environment variable |
|---|---|---|
| Example | `${{ github.ref }}` | `$GITHUB_REF` |
| Access mechanism | GitHub Actions expression | Runner process environment |
| Typical use | Workflow conditions and configuration | Shell commands |
| Available before a runner starts? | Some contexts and properties are available during workflow processing | No runner environment exists before the runner executes the job |
| Available inside a running job? | Yes, when the relevant context is available | Yes, for the variables provided to the runner |
| Can be used everywhere? | No; context availability rules apply | No; environment variables exist only in their supported environment |

## 7.4 The most important correction

Do not think:

> If the runner is doing something, I must use default environment variables.

That is not correct.

You can use contexts while the runner is executing a job.

For example:

```yaml
steps:
  - name: Show operating system
    run: echo "${{ runner.os }}"
```

Here, the command runs on the runner, but `runner.os` is a context property.

You could also inspect the runner operating system through the `RUNNER_OS` environment variable:

```yaml
steps:
  - name: Show operating system
    run: echo "$RUNNER_OS"
```

Both approaches can work in this example.

The choice depends on the location where you need the information and the mechanism you are using.

## 7.5 The practical rule

Use a context when you need to access information through GitHub Actions expressions.

Use an environment variable when you need to access information through a process environment, such as a Bash or PowerShell command.

Contexts can also be used for many workflow decisions that must be made before a runner starts.

However, not every context is available in every field.

---

# 8. Why Contexts Can Be Used Before a Runner Starts

Consider this workflow:

```yaml
name: Main Branch Only

on:
  push:
    branches:
      - main
      - develop

jobs:
  test:
    if: ${{ github.ref == 'refs/heads/main' }}
    runs-on: ubuntu-latest

    steps:
      - name: Run tests
        run: echo "Tests are running"
```

Suppose you push to `develop`.

GitHub receives the event.

The value of `github.ref` is normally:

```text
refs/heads/develop
```

GitHub evaluates:

```yaml
if: ${{ github.ref == 'refs/heads/main' }}
```

The condition is false.

Therefore, GitHub skips the job.

The job does not need to start on an Ubuntu runner just to discover that it should not run.

Now consider a push to `main`.

The condition becomes true, so GitHub can schedule the job.

This is why contexts are useful during workflow processing.

**Remember:** A context can be used before the runner starts only when the relevant context and property are available in that workflow field. Context availability is not unlimited.

---

# 9. Important GitHub Actions Contexts

GitHub Actions provides several built-in contexts.

| Context | What it provides | Example |
|---|---|---|
| `github` | Information about the workflow run and event | `${{ github.actor }}` |
| `env` | Custom environment variables defined in workflow configuration | `${{ env.APP_NAME }}` |
| `vars` | Configuration variables defined in GitHub settings | `${{ vars.APP_NAME }}` |
| `secrets` | Secrets made available to the workflow | `${{ secrets.API_TOKEN }}` |
| `runner` | Information about the runner | `${{ runner.os }}` |
| `job` | Information about the currently running job | `${{ job.status }}` |
| `steps` | Results and outputs of steps | `${{ steps.build.outcome }}` |
| `needs` | Results and outputs of prerequisite jobs | `${{ needs.build.result }}` |
| `matrix` | The current matrix combination | `${{ matrix.java }}` |
| `strategy` | Information about the job's strategy | `${{ strategy.job-index }}` |
| `inputs` | Inputs provided to a workflow or reusable workflow | `${{ inputs.environment }}` |

Some properties are available only in specific workflow fields or execution situations. Always check the official context-availability reference when an expression is not accepted.

---

# 10. The `github` Context

The `github` context provides information about the current workflow run and the event that triggered it.

Common properties include:

| Property | Meaning |
|---|---|
| `github.actor` | Username of the person or account that triggered the run |
| `github.repository` | Repository name in `owner/repository` format |
| `github.ref` | Fully formed Git ref associated with the event |
| `github.sha` | Commit SHA associated with the event |
| `github.event_name` | Name of the event that triggered the workflow |
| `github.workflow` | Name of the workflow |
| `github.run_id` | Unique identifier for the workflow run |
| `github.run_number` | Run number within the repository |
| `github.job` | ID of the current job; available only in supported contexts |
| `github.workspace` | Default working directory on the runner |
| `github.event` | Event payload associated with the trigger |

Some values depend on the triggering event.

For example, `github.ref` can identify a branch or tag. For a pull request, the value depends on the pull request event and its context.

## Example

```yaml
name: GitHub Context Demo

on:
  push:
  workflow_dispatch:

jobs:
  show-info:
    runs-on: ubuntu-latest

    steps:
      - name: Display GitHub information
        run: |
          echo "Repository: ${{ github.repository }}"
          echo "Actor: ${{ github.actor }}"
          echo "Event: ${{ github.event_name }}"
          echo "Ref: ${{ github.ref }}"
          echo "Commit: ${{ github.sha }}"
```

### What happens?

1. You trigger the workflow.
2. GitHub identifies the event and associated information.
3. GitHub evaluates the expressions.
4. The runner executes the command.
5. The values are printed.

### When is this context useful?

Use it when your workflow needs to know which repository, event, branch or commit is involved.

For example, it can help you create branch-specific CI/CD behavior.

---

# 11. The `env` Context

The `env` context provides access to custom environment variables defined in workflow configuration.

Example:

```yaml
name: Environment Context Demo

on:
  workflow_dispatch:

env:
  APP_NAME: NetRescue
  JAVA_VERSION: "21"

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display configuration
        run: |
          echo "Application: $APP_NAME"
          echo "Java version: $JAVA_VERSION"

      - name: Display through contexts
        run: |
          echo "Application: ${{ env.APP_NAME }}"
          echo "Java version: ${{ env.JAVA_VERSION }}"
```

Here:

```yaml
env:
  APP_NAME: NetRescue
```

defines an environment variable at workflow scope.

The context expression:

```yaml
${{ env.APP_NAME }}
```

accesses that configured value.

## Important distinction

The `env` context is not the same as the runner's entire environment.

For example, this:

```yaml
${{ env.APP_NAME }}
```

accesses a custom variable defined in the applicable workflow `env` configuration.

It does not automatically expose every operating-system environment variable.

The scope of an `env` variable can be:

- Workflow level.
- Job level.
- Step level.

A narrower scope can override a broader scope for the same variable name.

---

# 12. The `vars` Context

The `vars` context provides access to configuration variables stored in GitHub.

You can define configuration variables at supported levels, such as repository, organization or environment.

For example, suppose your repository has a configuration variable named:

```text
APP_NAME = NetRescue
```

You can access it using:

```yaml
${{ vars.APP_NAME }}
```

Example workflow:

```yaml
name: Configuration Variables Demo

on:
  workflow_dispatch:

jobs:
  display:
    runs-on: ubuntu-latest

    steps:
      - name: Show application name
        run: echo "Application: ${{ vars.APP_NAME }}"
```

If `APP_NAME` is configured with the value `NetRescue`, the command displays that value.

## `env` vs. `vars`

| `env` | `vars` |
|---|---|
| Defined in workflow YAML using `env:` | Defined in GitHub configuration settings |
| Has workflow, job or step scope | Has supported configuration scopes |
| Useful for workflow-specific environment variables | Useful for centrally managed configuration |
| Accessed with `${{ env.NAME }}` | Accessed with `${{ vars.NAME }}` |

For example:

```yaml
env:
  APP_NAME: NetRescue
```

and:

```yaml
${{ vars.APP_NAME }}
```

are different mechanisms.

One is defined in workflow configuration; the other reads a GitHub configuration variable.

---

# 13. The `secrets` Context

The `secrets` context provides access to secrets that have been made available to the workflow.

Secrets may contain sensitive values such as:

- API tokens.
- Deployment credentials.
- Passwords.
- Private service keys.

Example:

```yaml
name: Secrets Demo

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Use a secret
        env:
          API_TOKEN: ${{ secrets.API_TOKEN }}
        run: |
          ./deploy.sh
```

Suppose `API_TOKEN` has been configured as an appropriate repository, organization or environment secret.

GitHub makes the secret available to the step's environment.

The shell script can then read the value through its environment.

## Why use `env` here?

This approach separates the expression from the shell command.

It also avoids placing the secret directly into the command text.

**Never print secrets to logs or commit them to your repository.** GitHub attempts to redact registered secret values in logs, but redaction is not a complete security boundary.

Secrets may also be unavailable in certain situations, such as some workflows triggered from forks. Permissions and security rules still apply.

---

# 14. The `runner` Context

The `runner` context provides information about the runner executing the job.

A runner is the machine or execution environment that runs your workflow's steps.

Examples of runner information include:

- Operating system.
- Architecture.
- Temporary directory.
- Workspace directory.
- Runner name.

Common properties include:

| Property | Meaning |
|---|---|
| `runner.os` | Operating system |
| `runner.arch` | Runner architecture |
| `runner.name` | Name of the runner |
| `runner.temp` | Temporary directory |
| `runner.workspace` | Workspace directory |

## Example

```yaml
name: Runner Context Demo

on:
  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Show runner information
        run: |
          echo "OS: ${{ runner.os }}"
          echo "Architecture: ${{ runner.arch }}"
          echo "Workspace: ${{ runner.workspace }}"
```

Possible output:

```text
OS: Linux
Architecture: X64
Workspace: /home/runner/work/...
```

The exact values depend on the runner.

## When should you use it?

Use the `runner` context when your workflow needs information about the machine executing a job.

For example, you might use operating-system information to choose a suitable build or test procedure.

---

# 15. The `job` Context

The `job` context provides information about the currently running job.

One useful property is:

```yaml
${{ job.status }}
```

It can report a job status such as:

```text
success
failure
cancelled
```

Example:

```yaml
name: Job Context Demo

on:
  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Show current job status
        run: echo "Job status: ${{ job.status }}"
```

During ordinary successful execution, this may display:

```text
Job status: success
```

However, the exact status depends on when the value is evaluated and the job's execution state.

The `job` context also contains other properties, including information about the job's container or service containers when applicable.

**Remember:** The `job` context describes the current job. It is different from the `needs` context, which describes prerequisite jobs.

---

# 16. The `steps` Context

The `steps` context provides information about steps that have already executed in the current job.

This context becomes especially useful when one step produces information that a later step needs.

Consider the following workflow:

```yaml
name: Steps Context Demo

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Prepare version
        id: version
        run: echo "number=1.0.0" >> "$GITHUB_OUTPUT"

      - name: Display version
        run: echo "Version: ${{ steps.version.outputs.number }}"
```

Let's understand it carefully.

## Step 1: Give the step an ID

```yaml
id: version
```

This gives the step an identifier.

The identifier allows later expressions to refer to the step.

## Step 2: Create an output

```bash
echo "number=1.0.0" >> "$GITHUB_OUTPUT"
```

GitHub Actions reads this output file and records a step output named `number`.

## Step 3: Read the output

```yaml
${{ steps.version.outputs.number }}
```

Break it down:

- `steps` → context.
- `version` → ID of the earlier step.
- `outputs` → collection of outputs produced by that step.
- `number` → output name.

The resulting value is:

```text
1.0.0
```

The next step can then display it.

**Important:** The output must be produced before a later step can read it. A step cannot obtain a future step's output before that step executes.

---

# 17. The `needs` Context

The `needs` context is used to access information about jobs that the current job depends on.

This is different from the `steps` context.

- `steps` helps you access information from earlier steps in the same job.
- `needs` helps you access information from prerequisite jobs.

Consider this example:

```yaml
name: Needs Context Demo

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Build application
        run: echo "Building application"

  deploy:
    needs: build
    runs-on: ubuntu-latest

    steps:
      - name: Show build result
        run: echo "Build result: ${{ needs.build.result }}"
```

## How does it work?

First, GitHub runs the `build` job.

Then the `deploy` job waits because it declares:

```yaml
needs: build
```

Once the prerequisite job finishes, the dependent job can access its result.

The expression:

```yaml
${{ needs.build.result }}
```

might evaluate to:

```text
success
```

The possible job results include `success`, `failure`, `cancelled`, and `skipped`.

## The important distinction

```yaml
${{ steps.build.outputs.version }}
```

refers to a step output in the current job.

```yaml
${{ needs.build.result }}
```

refers to the result of a prerequisite job named `build`.

The names can be confusing because both examples contain the word `build`, but they refer to different things.

---

# 18. The `matrix` Context

The `matrix` context provides the values for the current combination in a matrix job.

A matrix allows GitHub Actions to run a job using multiple combinations of configuration values.

For example, you might want to test your application using multiple Java versions.

```yaml
name: Matrix Context Demo

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21", "25"]

    steps:
      - name: Display Java version
        run: echo "Testing Java ${{ matrix.java }}"
```

GitHub creates separate job runs for the matrix values.

Conceptually:

```text
Matrix configuration
       |
       |---- java = 21
       |
       |---- java = 25
```

The first job uses:

```yaml
${{ matrix.java }}
```

with the value:

```text
21
```

The second job uses the same expression with the value:

```text
25
```

## Why is this useful?

You can use a single workflow definition to test multiple versions, operating systems, or configurations.

Instead of writing separate jobs manually, the matrix defines the combinations.

**Important:** The `matrix` context is available only in supported situations, such as jobs that use a matrix strategy.

---

# 19. The `strategy` Context

The `strategy` context provides information about the strategy used for a job.

For example, it can expose the current matrix job index and the total number of jobs in the matrix.

Useful properties include:

```yaml
${{ strategy.job-index }}
```

```yaml
${{ strategy.job-total }}
```

Example:

```yaml
name: Strategy Context Demo

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21", "25", "26"]

    steps:
      - name: Display matrix position
        run: |
          echo "Job index: ${{ strategy.job-index }}"
          echo "Total jobs: ${{ strategy.job-total }}"
          echo "Java: ${{ matrix.java }}"
```

The workflow creates three matrix jobs.

Each job has its own matrix value and position in the strategy.

The index is zero-based, so it begins at `0`.

The job total is `3` in this example.

**Remember:** `matrix` tells you the current configuration values. `strategy` tells you information about how the jobs are organized and executed.

---

# 20. The `inputs` Context

The `inputs` context provides access to inputs supplied to supported workflows and reusable workflows.

Inputs allow a workflow to accept values rather than hardcoding everything.

For example, you might want to choose which environment to deploy to.

```yaml
name: Inputs and Contexts Demo

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose deployment environment"
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
      - name: Show selected environment
        run: echo "Environment: ${{ inputs.environment }}"
```

When you manually run the workflow, GitHub asks you to select an environment.

If you select `staging`, the expression:

```yaml
${{ inputs.environment }}
```

provides the value:

```text
staging
```

## Why is this useful?

Inputs make workflows reusable and configurable.

You can use the same workflow for different environments or tasks without rewriting its YAML for every execution.

## A small distinction

For `workflow_dispatch`, GitHub also exposes inputs through `github.event.inputs`.

However, the `inputs` context preserves Boolean values as Booleans, while values accessed through `github.event.inputs` are strings.

For reusable workflows triggered by `workflow_call`, declared inputs are also accessible through the `inputs` context.

---

# 21. The `needs` Context and Job Outputs

Let's build a more realistic example.

Suppose your CI/CD pipeline has two jobs:

1. `build` calculates a version.
2. `deploy` uses that version.

The build job can expose an output, and the deploy job can read it through the `needs` context.

```yaml
name: Job Outputs Demo

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    outputs:
      app-version: ${{ steps.version.outputs.number }}

    steps:
      - name: Generate version
        id: version
        run: echo "number=2.5.0" >> "$GITHUB_OUTPUT"

  deploy:
    needs: build
    runs-on: ubuntu-latest

    steps:
      - name: Display version
        run: echo "Deploying version ${{ needs.build.outputs.app-version }}"
```

Let's follow the information.

### First: The step creates an output

```bash
echo "number=2.5.0" >> "$GITHUB_OUTPUT"
```

The step output is:

```text
number = 2.5.0
```

### Second: The job exposes the output

```yaml
outputs:
  app-version: ${{ steps.version.outputs.number }}
```

The build job publishes a job output named `app-version`.

### Third: The deploy job reads the output

```yaml
${{ needs.build.outputs.app-version }}
```

Break it down:

- `needs` → context containing prerequisite-job information.
- `build` → prerequisite job ID.
- `outputs` → outputs exposed by that job.
- `app-version` → name of the job output.

The resulting value is:

```text
2.5.0
```

The deploy job can use this value without hardcoding the version a second time.

This is an important pattern in real CI/CD workflows.

---

# 22. Context Availability

Not every context is available everywhere.

This is a fundamental rule of GitHub Actions.

For example:

- `matrix` is associated with matrix jobs.
- `steps` is available in supported fields within a job and can refer to steps that have already executed.
- `needs` provides information about prerequisite jobs.
- `runner` provides information about the runner.
- `inputs` is available when the relevant workflow trigger or reusable-workflow configuration provides inputs.

Imagine writing:

```yaml
jobs:
  test:
    if: ${{ matrix.java == '21' }}
    runs-on: ubuntu-latest
```

This condition refers to a matrix value, but the job has not declared a matrix strategy.

The workflow is missing the configuration that provides the intended matrix value.

A correct version would be:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21", "25"]

    steps:
      - name: Display version
        run: echo "${{ matrix.java }}"
```

Now the matrix context has a source.

## What should you do when an expression fails?

Ask these questions:

1. Does this context exist in this workflow?
2. Is the property supported by that context?
3. Is the context allowed in this particular workflow field?
4. Does the value become available only after a step or job has executed?
5. Have I used the correct syntax?

The official context availability table is particularly helpful when debugging these issues.

---

# 23. Practical Workflow Examples

Let's put the concepts together.

## Example 1: Display GitHub and runner information

```yaml
name: Basic Contexts

on:
  workflow_dispatch:

jobs:
  information:
    runs-on: ubuntu-latest

    steps:
      - name: Show workflow information
        run: |
          echo "Repository: ${{ github.repository }}"
          echo "Actor: ${{ github.actor }}"
          echo "Event: ${{ github.event_name }}"
          echo "Operating system: ${{ runner.os }}"
          echo "Architecture: ${{ runner.arch }}"
```

What this teaches:

- `github` gives information about the workflow run.
- `runner` gives information about the execution environment.
- Expressions can be used inside supported command fields.

## Example 2: Run a step only on the main branch

```yaml
name: Branch Condition

on:
  push:
    branches:
      - main
      - develop

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Run only on main
        if: ${{ github.ref == 'refs/heads/main' }}
        run: echo "Building main branch"

      - name: Run on every selected branch
        run: echo "This step runs for both selected branches"
```

What this teaches:

- `github.ref` provides the Git reference.
- `if` evaluates a condition.
- The first step runs only when the condition is true.

## Example 3: Use custom configuration variables

```yaml
name: Custom Environment Variables

on:
  workflow_dispatch:

env:
  APP_NAME: NetRescue

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display application name
        run: echo "Building ${{ env.APP_NAME }}"
```

What this teaches:

- `env:` defines a custom environment variable.
- `env.APP_NAME` accesses it through a context expression.

## Example 4: Pass information between jobs

```yaml
name: Build and Deploy

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    outputs:
      version: ${{ steps.release.outputs.version }}

    steps:
      - name: Create version
        id: release
        run: echo "version=1.2.0" >> "$GITHUB_OUTPUT"

  deploy:
    needs: build
    runs-on: ubuntu-latest

    steps:
      - name: Show version
        run: echo "Deploying ${{ needs.build.outputs.version }}"
```

What this teaches:

- A step can produce an output.
- A job can expose that output.
- A dependent job can read the output through `needs`.

## Example 5: Test multiple Java versions

```yaml
name: Java Matrix

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21", "25"]

    steps:
      - name: Display selected Java version
        run: echo "Testing Java ${{ matrix.java }}"
```

What this teaches:

- A matrix creates multiple job combinations.
- `matrix.java` identifies the current combination's value.

These examples demonstrate different contexts. They are independent examples and can be saved as separate workflow files for practice.

---

# 24. Common Beginner Mistakes

## Mistake 1: Confusing contexts with environment variables

Incorrect assumption:

> Contexts work only before the runner starts.

Correction:

Contexts can be used before the runner starts and during execution, provided the context is available in the relevant field.

Default environment variables are accessed through the process environment.

## Mistake 2: Assuming every context is available everywhere

Incorrect:

```yaml
jobs:
  test:
    if: ${{ matrix.java == '21' }}
    runs-on: ubuntu-latest
```

This example does not define a matrix strategy.

Correction:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21"]

    steps:
      - run: echo "${{ matrix.java }}"
```

## Mistake 3: Confusing `steps` with `needs`

Use:

```yaml
${{ steps.compile.outputs.version }}
```

for a step output in the current job.

Use:

```yaml
${{ needs.build.outputs.version }}
```

for a job output from a prerequisite job.

## Mistake 4: Expecting a future output to exist

A step cannot read an output from a later step that has not executed yet.

Create the output first, then read it from a subsequent step.

## Mistake 5: Confusing `env` and `vars`

Use:

```yaml
${{ env.APP_NAME }}
```

for a custom environment variable defined in workflow YAML.

Use:

```yaml
${{ vars.APP_NAME }}
```

for a GitHub configuration variable.

## Mistake 6: Assuming expression syntax is required everywhere

Many workflow fields support expressions, but not every field supports every context.

Always check the context availability rules.

## Mistake 7: Printing secrets

Avoid commands like:

```yaml
run: echo "${{ secrets.API_TOKEN }}"
```

This exposes sensitive information to the command output and potentially to logs.

Use secrets only where required, pass them securely, and avoid printing them.

---

# 25. Security: Never Trust Every Context Value

GitHub's documentation includes an important warning.

**Some context values may contain untrusted input.**

For example, information in a pull request title, branch name, commit message, or event payload may be influenced by someone outside your trusted development team.

Imagine someone creates a pull request with an unexpected title.

If a workflow inserts that title directly into a shell command, the shell might interpret special characters as commands.

This is known as script injection.

## A risky example

```yaml
- name: Process pull request
  run: |
    echo "${{ github.event.pull_request.title }}"
```

The pull request title is inserted into the generated shell script before execution.

If the title contains malicious shell syntax, it may alter the command's behavior.

## A safer pattern

Pass the value through an environment variable:

```yaml
- name: Process pull request
  env:
    PR_TITLE: ${{ github.event.pull_request.title }}
  run: |
    printf '%s\n' "$PR_TITLE"
```

This keeps the untrusted value out of the shell program's source text. The shell expands the variable as data rather than parsing the title as part of the original script.

However, this is not a universal security solution. You must still handle the value appropriately for its intended use, especially if passing it into another interpreter, query, or executable.

For example, SQL queries and commands passed to other programs may require their own safe parameterization or validation.

## Security rules

- Never assume event payloads are trusted.
- Avoid injecting untrusted context values directly into shell scripts.
- Pass values through environment variables where appropriate.
- Quote shell variables correctly.
- Validate data before using it in sensitive operations.
- Do not expose secrets in logs.
- Apply least-privilege permissions.
- Review workflows triggered by forked pull requests carefully.

Security is part of writing correct CI/CD workflows, not an optional extra.

---

# 26. Best Practices

Follow these practices when working with contexts.

1. **Understand the source of each value.** Know whether information comes from GitHub, a workflow input, a runner, a step, or another job.

2. **Choose the correct mechanism.** Use contexts in supported GitHub Actions expressions and environment variables in process environments.

3. **Respect context availability.** Check whether the context and property are available in the field where you use them.

4. **Use step IDs and job IDs clearly.** They make expressions such as `steps.compile.outputs.version` and `needs.build.outputs.version` easier to understand.

5. **Use outputs to pass information between execution stages.** Use step outputs within a job and job outputs between dependent jobs.

6. **Keep configuration manageable.** Use `env` for workflow-defined environment variables and `vars` for GitHub configuration variables.

7. **Protect sensitive information.** Use the `secrets` context appropriately and never print secret values.

8. **Treat external input carefully.** Event payloads and other externally influenced context values can be dangerous when inserted into executable scripts.

9. **Test workflows with different events.** A workflow triggered by `push` may receive different context values from one triggered by `pull_request` or `workflow_dispatch`.

10. **Read the official documentation when in doubt.** Context properties and availability rules vary depending on the workflow field and event.

---

# 27. Final Revision

Let's summarize the entire topic in simple words.

## What is a context?

A context is a built-in object provided by GitHub Actions that contains information about the workflow run, repository, runner, jobs, steps, or other execution details.

## How do we access a context?

Using expression syntax:

```yaml
${{ github.actor }}
```

## Why do contexts exist?

GitHub Actions needs information to evaluate workflow conditions, configure jobs, access execution details, and pass results between workflow stages.

## Are contexts the same as default environment variables?

No.

A context is accessed through GitHub Actions expressions.

A default environment variable is accessed through a process environment, such as a shell running on a runner.

## Can contexts be used while a runner is executing?

Yes. Contexts can be used during execution wherever they are supported and available.

## Can every context be used everywhere?

No. Each workflow field has context-availability rules.

## Which contexts should you remember first?

| Context | Remember it as |
|---|---|
| `github` | Information about the workflow run |
| `env` | Custom workflow environment variables |
| `vars` | GitHub configuration variables |
| `secrets` | Sensitive values |
| `runner` | Information about the runner |
| `steps` | Information and outputs from steps |
| `job` | Information about the current job |
| `needs` | Information and outputs from prerequisite jobs |
| `matrix` | The current matrix configuration |
| `strategy` | Matrix execution details |
| `inputs` | Values supplied to supported workflows |

## The most important mental model

```text
GitHub Actions needs information
             |
             v
It provides relevant contexts
             |
             v
You access their properties
             |
             v
${{ context.property }}
             |
             v
GitHub evaluates the expression
             |
             v
The resulting value is used
where that expression is supported
```

And remember the difference:

```text
Context:
${{ github.repository }}

Default environment variable:
$GITHUB_REPOSITORY
```

Both can provide repository information, but they use different mechanisms.

**Final takeaway:** Contexts are the information-access mechanism built into GitHub Actions expressions. Once you understand where the information comes from, when it becomes available, and where the expression is allowed, contexts become much easier to understand and use in real CI/CD pipelines.

---

# Official Documentation

For the authoritative reference, consult:

- [GitHub Actions — About contexts](https://docs.github.com/en/actions/concepts/workflows-and-actions/contexts#about-contexts)
- [GitHub Actions — Contexts reference](https://docs.github.com/en/actions/reference/workflows-and-actions/contexts)
- [GitHub Actions — Variables reference](https://docs.github.com/en/actions/reference/workflows-and-actions/variables)
- [GitHub Actions — Evaluate expressions](https://docs.github.com/en/actions/reference/workflows-and-actions/expressions)
- [GitHub Actions — Secure use reference](https://docs.github.com/en/actions/reference/security/secure-use)

Keep this README as your reference while practising contexts in your GitHub Actions repository.
