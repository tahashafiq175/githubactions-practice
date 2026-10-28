# GitHub Actions — From Fundamentals to Practical CI/CD

**A practical, beginner-friendly repository for learning GitHub Actions through deeply explained concepts, commented YAML workflows, and hands-on examples.**

[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-Automation-2088FF?logo=githubactions&logoColor=white)](https://docs.github.com/en/actions)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-Continuous%20Integration%20%26%20Delivery-2E8B57)](https://docs.github.com/en/actions/about-github-actions/understanding-github-actions)
[![Learning](https://img.shields.io/badge/Level-Beginner%20to%20Intermediate-blue)](#learning-roadmap)
[![YAML](https://img.shields.io/badge/Configuration-YAML-CB171E?logo=yaml&logoColor=white)](https://yaml.org/)

Welcome to my GitHub Actions learning repository!

This repository is built to help developers understand **not only how to write a GitHub Actions workflow, but also why each part exists, how it works, and what happens behind the scenes when a workflow runs.**

Instead of memorizing YAML syntax, the goal is to understand the logic behind automation through practical, heavily commented examples.

Whether you are learning your first workflow or preparing to build CI/CD pipelines for Java applications, Docker images, and Kubernetes deployments, this repository provides a structured path from fundamentals to practical implementation.

---

## Table of Contents

- [1. What Is GitHub Actions?](#1-what-is-github-actions)
- [2. What Is CI/CD?](#2-what-is-cicd)
- [3. Repository Objectives](#3-repository-objectives)
- [4. How to Use This Repository](#4-how-to-use-this-repository)
- [5. Learning Roadmap](#5-learning-roadmap)
- [6. Concepts Explained in This Repository](#6-concepts-explained-in-this-repository)
- [7. Practical Workflow Directory](#7-practical-workflow-directory)
- [8. How a Workflow Works](#8-how-a-workflow-works)
- [9. Understanding the YAML Syntax](#9-understanding-the-yaml-syntax)
- [10. From Basic Automation to Real CI/CD](#10-from-basic-automation-to-real-cicd)
- [11. Recommended Learning Order](#11-recommended-learning-order)
- [12. Common Mistakes and Troubleshooting](#12-common-mistakes-and-troubleshooting)
- [13. Who Is This Repository For?](#13-who-is-this-repository-for)
- [14. Contributing](#14-contributing)
- [15. Final Message](#15-final-message)

---

## 1. What Is GitHub Actions?

GitHub Actions is an automation platform that allows developers to execute predefined workflows in response to events such as code pushes, pull requests, scheduled times, and manual requests.

For example, imagine you have a Spring Boot application.

Whenever you push new code to GitHub, you want to:

1. Check out the source code.
2. Set up Java.
3. Compile the application.
4. Run automated tests.
5. Build a Docker image.
6. Publish the image to a container registry.
7. Deploy the application when appropriate.

GitHub Actions can automate these operations through workflows defined in YAML files.

### The basic idea

```text
Developer writes code
        |
        v
   Push to GitHub
        |
        v
 GitHub Actions
        |
        v
  Workflow starts
        |
        v
  Jobs are executed
        |
        v
 Build / Test / Package
        |
        v
 Deliver or Deploy
```

The workflow defines what should happen. GitHub Actions coordinates the execution, and runners execute the jobs.

**The goal is to replace repetitive manual procedures with reliable, repeatable automation.**

## 2. What Is CI/CD?

CI/CD refers to practices used to automate software integration, validation, delivery, and deployment.

### Continuous Integration (CI)

Continuous Integration involves frequently integrating code changes and automatically validating them.

A CI pipeline may:

- Compile the application.
- Run unit and integration tests.
- Perform code quality checks.
- Build the application.
- Produce a deployable artifact or container image.

**CI answers:** Did my code change break anything, and can the application build successfully?

### Continuous Delivery

Continuous Delivery keeps software in a deployable state so it can be released reliably whenever required.

The build, tests, and release preparation can be automated, while production deployment may require approval.

**Continuous Delivery answers:** Is my application ready to be released?

### Continuous Deployment

Continuous Deployment automatically releases successful changes to production after the required checks pass.

**Continuous Deployment answers:** Can the system deploy successful changes without requiring manual approval for each release?

### CI/CD in one picture

```text
              CODE CHANGE
                   |
                   v
             GitHub Events
                   |
                   v
            GitHub Actions
                   |
                   v
             Build and Test
                   |
                   v
             Package Software
                   |
                   v
             Delivery / Release
                   |
                   v
               Production
```

The exact pipeline depends on the project. Not every workflow needs to build a Docker image or deploy to production.

---

## 3. Repository Objectives

This repository aims to make GitHub Actions understandable through practical examples rather than isolated definitions.

### What you will learn

- How GitHub Actions workflows are structured.
- How events trigger workflows.
- How branch and path filters control execution.
- How event activity types determine which events trigger a workflow.
- How jobs and steps execute.
- How runners provide execution environments.
- How contexts and expressions provide dynamic information.
- How variables and environment variables store and pass configuration.
- How workflow inputs accept user-provided values.
- How outputs transfer information between steps and jobs.
- How custom actions package reusable functionality.
- How matrix strategies run jobs with multiple configurations.
- How caching reduces repeated dependency downloads and build work.
- How artifacts preserve and transfer generated files.
- How concurrency controls overlapping workflow runs.
- How the concepts come together in practical CI/CD pipelines.

### The teaching approach

Every concept should answer four questions:

1. **Why does it exist?** What problem does it solve?
2. **How does it work?** What happens behind the scenes?
3. **How do we use it?** What does the YAML syntax mean?
4. **What happens in practice?** How does the workflow behave when it runs?

The examples use explanatory comments and practical scenarios to connect the syntax with its actual behavior.

---

## 4. How to Use This Repository

The recommended approach is to study one concept at a time.

### Step 1 — Read the concept explanation

Understand the problem before learning the syntax.

For example, before learning caching, understand why repeatedly downloading the same Maven dependencies wastes time.

### Step 2 — Open the corresponding YAML workflow

Navigate to the workflow directory:

[Browse all workflow files](.github/workflows/)

Read the comments before executing the example.

### Step 3 — Predict the result

Before running the workflow, ask yourself:

- What event triggers it?
- Which jobs will run?
- What will each step do?
- What information will appear in the logs?
- What happens if a command fails?

### Step 4 — Run the workflow

Execute it in your own repository or a suitable test repository.

Inspect the job summary, individual steps, and logs.

### Step 5 — Experiment

Modify one thing at a time.

For example:

- Change a branch filter.
- Change a workflow input.
- Add an environment variable.
- Introduce a second matrix value.
- Modify a cache key.
- Make one command fail intentionally.

Observe the result and explain why it happened.

**The objective is not merely to make a workflow pass. It is to understand why it passes or fails.**

---

## 5. Learning Roadmap

The repository covers the following learning areas, progressing from introductory concepts to reusable automation and CI/CD techniques.

| Stage | Learning area | Main objective |
|---|---|---|
| 1 | CI/CD fundamentals | Understand the purpose of automation |
| 2 | Workflow structure | Understand events, jobs, steps, and runners |
| 3 | Triggers and filters | Control when workflows execute |
| 4 | Contexts and expressions | Read dynamic information and make decisions |
| 5 | Variables and environments | Manage configuration and environment-specific values |
| 6 | Inputs and outputs | Pass information into workflows and between steps/jobs |
| 7 | Functions and conditions | Control workflow behavior using expressions |
| 8 | Runners | Understand where workflow jobs execute |
| 9 | Matrix strategies | Test multiple configurations efficiently |
| 10 | Artifacts and caching | Preserve generated files and accelerate repeated work |
| 11 | Custom actions | Package reusable automation |
| 12 | Concurrency | Control overlapping workflow runs |
| 13 | Practical CI/CD | Apply the concepts to real applications |

Each stage builds on the concepts introduced earlier.

---

## 6. Concepts Explained in This Repository

This section is the main concept index. Use it to locate the relevant explanations and practical YAML examples.

### 6.1 Workflow Fundamentals

**What problem does it solve?**

Before automating anything, you need to understand how GitHub Actions represents and executes a workflow.

Topics:

- What is a workflow?
- What is a job?
- What is a step?
- What is a runner?
- What does `runs-on` mean?
- What does `run` do?
- What is the purpose of the `.github/workflows/` directory?
- How are workflow logs generated?

The fundamental structure is:

```yaml
name: My Workflow

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Print a message
        run: echo "Hello, GitHub Actions!"
```

The workflow defines when it runs, what jobs exist, where a job executes, and which steps it performs.

### 6.2 Triggers, Events, Filters, and Activity Types

**What problem does it solve?**

A workflow should not necessarily execute after every event. You need to control when automation starts.

Topics:

- `push`
- `pull_request`
- `workflow_dispatch`
- `schedule`
- `workflow_call`
- Branch filters
- Path filters
- Tag filters
- Event activity types
- `types`
- Trigger combinations
- Scheduled workflows using cron syntax

Example:

```yaml
name: Push Filter Demo

on:
  push:
    branches:
      - main
      - "feature/**"

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Explain the trigger
        run: echo "A matching branch received a push."
```

This workflow is configured to run on pushes to `main` and branches matching `feature/**`.

**Key lesson:** The trigger determines which event starts the workflow, while filters and activity-type restrictions narrow down which events qualify.

### 6.3 Contexts and Expressions

**What problem does it solve?**

Workflows often need information about the event, repository, branch, actor, job results, or workflow inputs.

Contexts expose information about the current workflow execution. Expressions allow the workflow to evaluate values and conditions dynamically.

Topics:

- What is a context?
- What is an expression?
- The `${{ ... }}` syntax
- `github`
- `env`
- `vars`
- `inputs`
- `needs`
- `steps`
- `job`
- `runner`
- `matrix`
- `strategy`
- `secrets`
- Expression comparisons
- Conditional execution
- Expression functions

Example:

```yaml
name: Context Demo

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Display repository information
        run: echo "Repository is ${{ github.repository }}"

      - name: Display triggering actor
        run: echo "Actor is ${{ github.actor }}"
```

The expressions are evaluated by GitHub Actions, and their resulting values are inserted into the workflow configuration.

**Important:** Contexts and expressions are related but not identical. A context provides information; an expression evaluates a value or condition and may reference a context.

### 6.4 Variables and Environment Variables

**What problem does it solve?**

Hardcoding the same configuration throughout a workflow makes it harder to maintain and reuse.

Topics:

- Workflow-level environment variables
- Job-level environment variables
- Step-level environment variables
- The `env` keyword
- Repository and organization configuration variables
- The `vars` context
- Environment-scoped configuration
- Variable scope and precedence
- Contexts versus environment variables
- Using values in shell commands
- Passing values between steps

Example:

```yaml
name: Environment Variables Demo

on:
  workflow_dispatch:

env:
  APP_NAME: NetRescue

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Read the application name
        run: echo "Application: $APP_NAME"
```

The `env` declaration makes `APP_NAME` available as an environment variable to the applicable steps.

**Key lesson:** The `env` keyword defines environment variables at a particular scope, while `vars` provides access to supported GitHub configuration variables.

### 6.5 Workflow Inputs

**What problem does it solve?**

Sometimes a workflow needs information from the person starting it instead of using fixed values.

Topics:

- Manual workflow inputs
- The `workflow_dispatch` event
- Input descriptions
- Required and optional inputs
- Default values
- Input types
- Reading input values
- Validating user-provided values
- The `inputs` context

Example:

```yaml
name: Manual Input Demo

on:
  workflow_dispatch:
    inputs:
      app_name:
        description: "Name of the application"
        required: true
        type: string
        default: "NetRescue"

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Display the selected application
        run: echo "Application: ${{ inputs.app_name }}"
```

When a user manually runs the workflow, GitHub presents the configured input.

**Key lesson:** Inputs allow a workflow to receive values when it is triggered. The supported inputs and behavior depend on the trigger and workflow type.

### 6.6 Outputs

**What problem does it solve?**

One step may calculate or discover a value that another step or job needs.

Topics:

- Step outputs
- The `$GITHUB_OUTPUT` environment file
- Reading outputs through `steps`
- Job outputs
- The `needs` context
- Passing information between jobs
- Output dependencies

Example:

```yaml
name: Step Outputs Demo

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Generate a value
        id: app
        run: echo "name=NetRescue" >> "$GITHUB_OUTPUT"

      - name: Read the output
        run: echo "Application: ${{ steps.app.outputs.name }}"
```

The first step writes a named output. The next step reads it using the identifier assigned to the producing step.

**Key lesson:** Outputs are a way to pass generated values between workflow components. They are not the same as ordinary log messages or environment variables.

### 6.7 Functions and Conditional Expressions

**What problem does it solve?**

Workflows need to make decisions instead of executing every operation unconditionally.

Topics:

- The `if` keyword
- Expression comparisons
- `success()`
- `failure()`
- `always()`
- `cancelled()`
- String matching functions
- Status-check behavior
- Conditional jobs and steps

Example:

```yaml
name: Conditional Execution Demo

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Run the successful path
        if: ${{ success() }}
        run: echo "Previous steps have succeeded."
```

Conditional expressions allow workflows to select which jobs or steps should run based on the current execution state and other values.

**Key lesson:** Conditions control execution. They are not merely messages printed to the logs.

### 6.8 Runners

**What problem does it solve?**

Workflow commands need a machine or execution environment in which to run.

Topics:

- What is a runner?
- GitHub-hosted runners
- Self-hosted runners
- `runs-on`
- Operating system selection
- Runner environments
- Job execution
- Runner availability and resource limitations
- Differences between jobs and steps

Example:

```yaml
name: Runner Demo

on:
  workflow_dispatch:

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Display the operating system
        run: uname -a
```

The command executes on the selected runner.

**Key lesson:** GitHub Actions orchestrates workflow execution, while runners execute the job's commands.

### 6.9 Matrix Strategy

**What problem does it solve?**

Testing the same application separately for every supported version or configuration can create repetitive workflow definitions.

A matrix lets you define a set of configurations that GitHub Actions can use to run multiple job variations.

Topics:

- `strategy`
- `matrix`
- Multiple Java versions
- Multiple operating systems
- Multiple runtime versions
- `include`
- `exclude`
- `fail-fast`
- `max-parallel`

Example:

```yaml
name: Matrix Strategy Demo

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ["21", "25"]

    steps:
      - name: Display the selected Java version
        run: echo "Testing Java ${{ matrix.java }}"
```

This configuration creates separate job variations for the specified Java values. This simple example only prints the selected value; a real matrix testing workflow would install the corresponding Java version and run the tests.

**Key lesson:** A matrix generates multiple job variations from one job definition.

### 6.10 Artifacts

**What problem does it solve?**

Files generated during a workflow may need to be preserved after the job finishes or made available to another job.

Topics:

- What is a workflow artifact?
- Uploading artifacts
- Downloading artifacts
- Artifact names and paths
- Sharing build outputs between jobs
- Retention and storage considerations
- Artifacts versus caches
- Using artifacts for reports and packaged files

A common example is preserving a compiled application:

```text
Source code
    |
    v
Build application
    |
    v
Generate JAR file
    |
    v
Upload artifact
    |
    v
Download or inspect later
```

The official `actions/upload-artifact` and `actions/download-artifact` actions provide the standard mechanisms for handling workflow artifacts.

**Key lesson:** Artifacts preserve and transfer generated files. They are not primarily intended to accelerate dependency downloads.

### 6.11 Caching

**What problem does it solve?**

Build tools often download dependencies repeatedly. Downloading the same dependencies during every workflow run can waste time.

Caching allows suitable files to be reused across workflow runs.

Topics:

- What is caching?
- Cache keys
- Cache restore keys
- Cache hits and misses
- Dependency caching
- Maven dependency caching
- Cache invalidation
- Cache versus artifact
- Choosing effective cache keys

Conceptual example:

```text
First workflow run
       |
       v
Download dependencies
       |
       v
Save cache
       |
       v
Later workflow run
       |
       v
Restore matching cache
       |
       v
Less repeated downloading
```

A cache is reusable storage intended to speed up future runs. A cache miss does not necessarily mean the workflow fails; the workflow can download dependencies again.

**Key lesson:** Caching improves performance, while artifacts preserve useful outputs.

### 6.12 Custom Actions

**What problem does it solve?**

If the same automation logic is repeated across multiple workflows, it may be useful to package that logic into a reusable action.

Topics:

- What is a custom action?
- Why custom actions exist
- Action metadata
- `action.yml`
- Inputs and outputs for actions
- JavaScript actions
- Composite actions
- Docker container actions
- Calling a local action
- Reusing automation logic

Conceptual structure:

```text
Repository
├── .github/
│   └── workflows/
│       └── main.yml
└── actions/
    └── greeting/
        └── action.yml
```

A workflow can invoke a local action using the `uses` keyword and a path to the action.

**Key lesson:** A workflow defines automation for a run. A custom action packages reusable functionality that workflows can invoke.

### 6.13 Concurrency

**What problem does it solve?**

Multiple workflow runs may overlap. For example, a developer may push several commits while a previous workflow is still running.

Concurrency controls can help prevent unnecessary overlapping work.

Topics:

- Workflow-level concurrency
- Job-level concurrency
- Concurrency groups
- Static group names
- Dynamic group names
- `cancel-in-progress`
- Canceling outdated runs
- Controlling simultaneous deployments

Example:

```yaml
name: Concurrency Demo

on:
  push:

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  example:
    runs-on: ubuntu-latest

    steps:
      - name: Display a message
        run: echo "This run belongs to the current concurrency group."
```

This defines a concurrency group based on the Git reference. When another run in the same group starts, GitHub can cancel the existing in-progress run because `cancel-in-progress` is enabled.

**Key lesson:** Concurrency controls overlapping workflow runs. It is especially useful for avoiding wasted work and managing deployments.

### 6.14 Secrets and Secure Configuration

**What problem does it solve?**

CI/CD pipelines may need credentials to access registries, cloud services, or deployment environments.

Topics:

- GitHub Actions secrets
- The `secrets` context
- Repository and environment secrets
- Secure authentication
- Avoiding hardcoded credentials
- Secret exposure risks
- Permissions and least privilege

Example:

```yaml
- name: Use a configured secret
  env:
    API_TOKEN: ${{ secrets.API_TOKEN }}
  run: |
    if [ -n "$API_TOKEN" ]; then
      echo "The API token is configured."
    else
      echo "The API token is missing."
    fi
```

This example checks whether the secret is available without printing its value.

**Key lesson:** Store sensitive credentials in GitHub's supported secret mechanisms instead of hardcoding them into YAML or application code.

---

## 7. Practical Workflow Directory

The repository contains practical YAML examples organized around individual concepts. Use this section as a guide for studying the workflows.

[**Open the workflow directory →**](.github/workflows/)

Each workflow should be read independently first, then compared with related examples.

| Workflow category | What to study | What you should understand |
|---|---|---|
| Basic workflow | `name`, `on`, `jobs`, `steps`, `run` | The structure of a workflow |
| CI/CD introduction | Sequential commands and simulated stages | How automation represents the software delivery process |
| Push event | `push` | How a repository event triggers a workflow |
| Branch filtering | `branches` | How to restrict workflow execution to selected branches |
| Push and pull request events | Multiple event triggers | How one workflow can respond to different events |
| Manual workflow | `workflow_dispatch` | How to start a workflow manually |
| Scheduled workflow | `schedule` and cron | How to schedule automated runs |
| Variables and contexts | `env`, `vars`, `github`, and related contexts | How workflow information and configuration are accessed |
| Expressions | `${{ ... }}` | How to evaluate dynamic values and conditions |
| Inputs | `inputs` | How to receive user-provided values |
| Outputs | `$GITHUB_OUTPUT`, `steps`, `needs` | How to transfer values between steps and jobs |
| Runners | `runs-on` | Where workflow commands execute |
| Matrix strategy | `strategy.matrix` | How one job definition can create multiple variations |
| Artifacts | Upload and download actions | How to preserve generated files |
| Caching | Cache keys and restore behavior | How to reuse files to speed up later runs |
| Custom actions | `action.yml` and `uses` | How to package reusable automation |
| Concurrency | `concurrency` and concurrency groups | How overlapping runs are managed |

### How each workflow should be documented

For every practical workflow, the accompanying explanation should cover:

1. **Purpose:** What problem does this workflow solve?
2. **Trigger:** What causes it to run?
3. **YAML walkthrough:** What does each important line mean?
4. **Execution flow:** What happens first, second, and next?
5. **Expected behavior:** What should you observe in the Actions logs?
6. **Important details:** What rules, limitations, or special behaviors matter?
7. **Experiment:** What can you change to deepen your understanding?

This approach keeps the examples educational rather than turning the repository into a collection of unexplained YAML files.

---

## 8. How a Workflow Works

Consider a simple workflow:

```yaml
name: Example Workflow

on:
  workflow_dispatch:

jobs:
  greet:
    runs-on: ubuntu-latest

    steps:
      - name: Print greeting
        run: echo "Hello from GitHub Actions!"

      - name: Print completion
        run: echo "The workflow has reached its final step."
```

Let's understand the execution from beginning to end.

### Step 1 — GitHub detects the trigger

The `workflow_dispatch` configuration allows the workflow to be manually started.

### Step 2 — GitHub schedules the job

The `greet` job requests an Ubuntu runner.

### Step 3 — The runner executes the first step

The shell executes:

```bash
echo "Hello from GitHub Actions!"
```

The message appears in the workflow logs.

### Step 4 — The runner executes the second step

The next command runs after the first step succeeds.

### Step 5 — GitHub reports the result

GitHub displays the job status and logs.

The complete relationship is:

```text
Event
  |
  v
Workflow
  |
  v
Job
  |
  v
Runner
  |
  v
Step 1
  |
  v
Step 2
  |
  v
Result
```

A workflow may contain several jobs. Jobs can run in parallel unless dependencies or other constraints determine their execution order.

---

## 9. Understanding the YAML Syntax

GitHub Actions uses YAML to describe workflow configuration.

The following example demonstrates the main structural elements:

```yaml
# Workflow name
name: Basic YAML Example

# Trigger configuration
on:
  workflow_dispatch:

# Job definitions
jobs:
  example:

    # Execution environment
    runs-on: ubuntu-latest

    # Individual tasks
    steps:
      - name: Print a message
        run: echo "Learning GitHub Actions"
```

### Syntax reference

| Syntax | Purpose |
|---|---|
| `name` | Names a workflow or a step |
| `on` | Defines workflow trigger events |
| `jobs` | Contains job definitions |
| `runs-on` | Selects a runner |
| `steps` | Lists tasks inside a job |
| `run` | Executes shell commands |
| `uses` | Uses an action or reusable workflow, depending on context |
| `with` | Supplies inputs to an action or reusable workflow |
| `env` | Defines environment variables |
| `if` | Controls conditional execution |
| `needs` | Defines job dependencies |
| `strategy` | Configures job execution strategies |
| `matrix` | Defines combinations of job configuration values |
| `concurrency` | Controls overlapping workflow runs |

### YAML indentation matters

Correct indentation defines which fields belong to which objects.

For example:

```yaml
jobs:
  example:
    runs-on: ubuntu-latest
    steps:
      - name: Print message
        run: echo "Hello"
```

If indentation is incorrect, YAML may fail to parse or may describe a different structure than intended.

**Important:** YAML defines the configuration. GitHub Actions interprets that configuration and orchestrates execution.

---

## 10. From Basic Automation to Real CI/CD

The introductory workflows use `echo` commands to demonstrate syntax and execution order. They intentionally avoid introducing unnecessary complexity.

The next step is to replace simulated operations with real commands.

### Stage 1 — Print messages

```yaml
- name: Introduction
  run: echo "Starting CI"
```

This demonstrates how a step executes a command.

### Stage 2 — Check out the source code

```yaml
- name: Checkout repository
  uses: actions/checkout@v4
```

This uses a prebuilt action to make the repository's files available to the job.

For new workflows, use a maintained, appropriate action version and follow its current documentation and security guidance.

### Stage 3 — Set up the required runtime

For a Java project, the workflow may use a Java setup action before executing Maven commands.

### Stage 4 — Run tests

```yaml
- name: Run Maven tests
  run: mvn test
```

This executes actual project tests, assuming the Maven project and required environment are available.

### Stage 5 — Build and package

```yaml
- name: Build application
  run: mvn package
```

This performs a real Maven package build.

### Stage 6 — Build and publish a Docker image

A containerized application can be built and published using Docker commands and suitable registry authentication.

### Stage 7 — Deploy

A deployment job can release the application to a configured environment or update a GitOps repository so that a deployment controller can synchronize the desired state.

Each stage introduces additional requirements, such as dependencies, credentials, permissions, and environment configuration.

---

## 11. Recommended Learning Order

To avoid confusion, study the concepts in this order:

**Phase 1 — Foundations**

- What is CI/CD?
- What is GitHub Actions?
- Workflow structure
- Events
- Jobs
- Steps
- Runners
- Basic YAML syntax

**Phase 2 — Workflow Triggers**

- Push events
- Pull request events
- Manual triggers
- Scheduled workflows
- Branch filters
- Path filters
- Activity types

**Phase 3 — Dynamic Information**

- Contexts
- Expressions
- Variables
- Environment variables
- Workflow inputs
- Step outputs
- Job outputs

**Phase 4 — Workflow Control**

- Conditions
- Expression functions
- Job dependencies
- Concurrency
- Matrix strategies

**Phase 5 — Reusability and Performance**

- Custom actions
- Artifacts
- Caching
- Reusable workflows
- Workflow inputs and outputs in reusable workflows

**Phase 6 — Practical CI/CD**

- Build a Java application
- Run automated tests
- Package a JAR
- Build a Docker image
- Publish an image to a registry
- Manage secrets and permissions
- Deploy an application
- Integrate with Kubernetes and GitOps tools

The purpose of this order is to make advanced concepts easier to understand by building on the foundations.

---

## 12. Common Mistakes and Troubleshooting

### Workflow does not run

Check:

- Is the YAML file inside `.github/workflows/`?
- Does the trigger match the event that occurred?
- Is the branch filter correct?
- Does the event satisfy any path filters or activity-type restrictions?
- Is the workflow configured and available to run?

### YAML parsing error

Check:

- Indentation
- Colons and quotation marks
- List markers such as `-`
- Whether keys are nested under the correct parent
- Whether special characters are represented correctly

### A step fails

Open the workflow run and inspect the first failed step.

Read the actual error message before changing unrelated configuration.

### A context or expression returns an unexpected value

Check:

- Whether the context is available in that location
- Whether the expression uses the correct syntax
- Whether the value is empty or different from what you expected
- Whether shell expansion is being confused with GitHub expression evaluation

### A cache does not restore

Check:

- The cache key
- The restore-key configuration
- The dependency path
- Whether a compatible cache exists
- Whether the workflow has the appropriate permissions and supported action configuration

### A workflow prints success even though the intended operation did not happen

Remember that printing a success message does not verify the real operation.

For example:

```yaml
- name: Simulate deployment
  run: echo "Deployment successful"
```

This only prints a message. It does not deploy an application or verify that a service is running.

**Always distinguish between a simulated example and a real implementation.**

---

## 13. Who Is This Repository For?

This repository is intended for:

- Beginners learning GitHub Actions for the first time.
- Developers learning CI/CD concepts.
- Java and Spring Boot developers building automated pipelines.
- Students preparing for DevOps and cloud engineering roles.
- Engineers learning workflow inputs, outputs, contexts, expressions, and runners.
- Developers preparing to automate Docker builds and application releases.
- Learners progressing toward Kubernetes and GitOps tools such as Argo CD.

You do not need to understand every advanced feature before starting. Begin with the basic workflow examples and progress through the concepts at your own pace.

---

## 14. Contributing

Contributions, corrections, and suggestions are welcome.

If you find an issue or have an improvement:

1. Identify the concept or workflow involved.
2. Explain the issue or proposed improvement.
3. Provide a reproducible example where possible.
4. Keep explanations accurate, beginner-friendly, and practical.
5. Distinguish clearly between simulated commands and real operations.

The goal is to maintain a resource that helps learners understand not just what to write, but why it works.

---

## 15. Final Message

GitHub Actions becomes much easier when its concepts are learned in the right order.

Start by understanding the workflow structure. Then learn how events trigger workflows, how contexts and expressions provide dynamic information, how variables and outputs transfer values, and how runners execute jobs.

After that, explore matrix strategies, caching, artifacts, custom actions, and concurrency before combining these concepts into real CI/CD pipelines.

This repository follows that philosophy: **learn the concept, read the workflow, understand the execution, and then experiment.**

If this repository helps you understand GitHub Actions, consider starring it and sharing it with other developers who are learning automation and DevOps.

**Happy learning, and keep automating!**
