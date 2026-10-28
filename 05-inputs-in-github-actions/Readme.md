# GitHub Actions Inputs — CI/CD Part 7

## The Complete Beginner-to-Advanced Guide

> **The central idea:** An input is information that a workflow receives from someone or something. The workflow uses that information to decide what to do.

Imagine that you are ordering food at a restaurant. The waiter asks:

- What would you like to eat?
- What size would you like?
- Would you like a drink?

You provide the answers, and the restaurant prepares your order according to those answers.

GitHub Actions Inputs work in a similar way. A workflow can ask for information such as:

- Which application version should we build?
- Should tests run?
- Which environment should receive the deployment?
- Which operation should the workflow perform?

The workflow receives the answers and uses them during execution.

By the end of this guide, you will understand inputs from the basics to their practical use in CI/CD pipelines.

---

# Table of Contents

1. [The Problem Inputs Solve](#1-the-problem-inputs-solve)
2. [What Exactly Is an Input?](#2-what-exactly-is-an-input)
3. [Understanding Inputs Through a Java Example](#3-understanding-inputs-through-a-java-example)
4. [Your First GitHub Actions Input](#4-your-first-github-actions-input)
5. [Understanding Every YAML Line](#5-understanding-every-yaml-line)
6. [The Inputs Context](#6-the-inputs-context)
7. [Types of Inputs](#7-types-of-inputs)
8. [Required Inputs and Default Values](#8-required-inputs-and-default-values)
9. [Using Multiple Inputs](#9-using-multiple-inputs)
10. [Using Inputs in Conditions](#10-using-inputs-in-conditions)
11. [Inputs vs. Environment Variables](#11-inputs-vs-environment-variables)
12. [Inputs vs. Variables vs. Secrets](#12-inputs-vs-variables-vs-secrets)
13. [Using Inputs in a Docker CI/CD Workflow](#13-using-inputs-in-a-docker-cicd-workflow)
14. [Inputs in Reusable Workflows](#14-inputs-in-reusable-workflows)
15. [Understanding the `with` Keyword](#15-understanding-the-with-keyword)
16. [`workflow_dispatch` vs. `workflow_call`](#16-workflow_dispatch-vs-workflow_call)
17. [A Complete Practice Workflow](#17-a-complete-practice-workflow)
18. [Common Beginner Mistakes](#18-common-beginner-mistakes)
19. [Security and Production Considerations](#19-security-and-production-considerations)
20. [Practice Challenges](#20-practice-challenges)
21. [Quick Revision](#21-quick-revision)
22. [Official Documentation](#22-official-documentation)

---

# 1. The Problem Inputs Solve

Before understanding inputs, we need to understand the problem they solve.

Imagine that you have a Spring Boot application. You want GitHub Actions to build it and prepare a Docker image.

Your workflow contains this command:

```yaml
- name: Build Docker image
  run: docker build -t myapp:v1.0.0 .
```

This works, but there is a problem.

The image version is fixed at `v1.0.0`.

Tomorrow, you want to build `v1.0.1`. You must edit the workflow file.

Next week, you want `v1.1.0`. You must edit it again.

The workflow is not flexible because the version is hardcoded.

## The solution: Inputs

Instead of fixing the version inside the workflow, let the person running the workflow provide it.

```yaml
name: Build Application

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Which version should we build?"
        required: true
        type: string

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Show selected version
        run: echo "Building version ${{ inputs.version }}"
```

Now, when you manually run the workflow, GitHub provides an input field for `version`.

You could enter:

```text
v1.0.0
```

On the next run, you could enter:

```text
v1.0.1
```

On another run, you could enter:

```text
v2.0.0
```

You can reuse the same workflow without editing its YAML for every new version.

**The important lesson:** Inputs allow a workflow to receive configurable information instead of relying entirely on hardcoded values.

### Without inputs vs. with inputs

| Without inputs | With inputs |
|---|---|
| The value is fixed in the workflow. | The value can be supplied when the workflow runs. |
| Changing the value may require editing YAML. | Different runs can use different values. |
| Less flexible for manual operations. | More flexible for configurable operations. |

Inputs do not automatically make a workflow safe or intelligent. They simply give it information that you can use in your workflow logic.

---

# 2. What Exactly Is an Input?

An **input is a named piece of information supplied to a workflow or reusable automation.**

Consider this input declaration:

```yaml
inputs:
  version:
    description: "Application version"
    required: true
    type: string
```

Let's understand it like a beginner.

- `inputs`: We are declaring the information that can be received.
- `version`: The name of one input.
- `description`: Explains what information is expected.
- `required: true`: The input must be provided when running the workflow, unless a default value makes it unnecessary to enter one.
- `type: string`: The input is text.

If someone enters `v2.0.0`, the input has a name and a value:

| Part | Value |
|---|---|
| Input name | `version` |
| Supplied value | `v2.0.0` |
| Input type | `string` |

The name identifies the information. The value is the actual information supplied.

Another example:

```yaml
inputs:
  environment:
    description: "Where should the application be deployed?"
    required: true
    type: choice
    options:
      - development
      - staging
      - production
```

Here, the input is named `environment`, and the person running the workflow chooses one of the listed options.

The workflow can then use that choice to decide which deployment logic to execute.

Remember: **declaring an input does not itself deploy an application.** It only defines information that the workflow can receive.

---

# 3. Understanding Inputs Through a Java Example

Since GitHub Actions is easier to understand when compared with familiar programming concepts, let's use Java.

Consider this Java method:

```java
public void buildApplication(String version) {
    System.out.println("Building version: " + version);
}
```

The method accepts a parameter named `version`.

You can call it with different values:

```java
buildApplication("v1.0.0");
buildApplication("v1.0.1");
buildApplication("v2.0.0");
```

The method stays the same. Only the supplied value changes.

GitHub Actions inputs follow a similar idea.

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

The input declaration defines what information the workflow accepts.

Then:

```yaml
run: echo "Building version ${{ inputs.version }}"
```

uses the supplied value.

The comparison is:

| Java | GitHub Actions |
|---|---|
| Method parameter | Workflow input |
| Parameter name | Input name |
| Argument passed to a method | Value supplied when running a workflow |
| Method body | Workflow jobs and steps |
| Method call | Starting the workflow with supplied values |

This is a conceptual comparison, not an exact implementation match. A workflow is not a Java method, but the comparison helps explain why inputs exist.

**Think of inputs as parameters for configurable automation.**

---

# 4. Your First GitHub Actions Input

Let's create your first working example.

Create this file in your repository:

```text
.github/
└── workflows/
    └── inputs-demo.yml
```

Put the following code inside `inputs-demo.yml`:

```yaml
# The name displayed in the GitHub Actions interface.
name: CI/CD Part 7 - First Input

# Define when the workflow can run.
on:
  # Allow a user to start the workflow manually.
  workflow_dispatch:
    inputs:
      # Declare an input named "environment".
      environment:
        description: "Where should we deploy?"
        required: true
        type: string

jobs:
  show-input:
    # GitHub provides a hosted Ubuntu runner for this job.
    runs-on: ubuntu-latest

    steps:
      - name: Display the supplied input
        run: echo "Selected environment: ${{ inputs.environment }}"
```

Commit and push the file to GitHub.

Open your repository and navigate to:

**Actions → CI/CD Part 7 - First Input → Run workflow**

You should see a field for the environment input.

Try entering:

```text
staging
```

Then start the workflow.

The output should contain something similar to:

```text
Selected environment: staging
```

Try running it again with:

```text
development
```

The workflow uses the new value on the second run.

## What happened?

1. You declared an input in the workflow YAML.
2. You pushed the workflow to GitHub.
3. You opened the workflow in the Actions interface.
4. GitHub displayed the input field.
5. You supplied a value.
6. GitHub started the workflow.
7. The expression `inputs.environment` provided the supplied value to the step.
8. The step printed the value.

Nothing magical happened. You declared what information the workflow accepts, supplied that information, and used it in a step.

**Important:** This example only displays the selected environment. It does not deploy anything.

---

# 5. Understanding Every YAML Line

Let's examine the most important parts of the example.

## `on`

```yaml
on:
```

This section defines the events that can trigger a workflow.

A workflow can run when code is pushed, when a pull request is opened, on a schedule, or when a user manually starts it.

## `workflow_dispatch`

```yaml
on:
  workflow_dispatch:
```

This enables manual execution of the workflow through GitHub's Actions interface or the GitHub API.

When you manually run a workflow, you may need to provide information such as an application version or deployment target.

That is where `workflow_dispatch.inputs` becomes useful.

## `inputs`

```yaml
inputs:
  environment:
```

This declares an input named `environment`.

The input's name is important because you use it later to retrieve its value.

## `description`

```yaml
description: "Where should we deploy?"
```

This describes the information you expect the person running the workflow to provide.

A useful description makes the workflow easier to understand.

## `required`

```yaml
required: true
```

This indicates that the input is required.

For a manual workflow, GitHub generally requires the user to provide a value unless a default value is available.

## `type`

```yaml
type: string
```

This declares the input's data type.

Here, the value is text.

## `jobs`

```yaml
jobs:
  show-input:
```

This defines a job named `show-input`.

The input is declared at the workflow trigger level, but you can use it in the job's steps.

## `runs-on`

```yaml
runs-on: ubuntu-latest
```

This tells GitHub which runner environment should execute the job.

A runner is the machine that executes the workflow's commands.

## `steps`

```yaml
steps:
  - name: Display the supplied input
```

A job consists of steps. Each step performs an operation.

## `${{ inputs.environment }}`

```yaml
run: echo "Selected environment: ${{ inputs.environment }}"
```

This expression accesses the value of the input named `environment`.

If the supplied value is `staging`, the command becomes conceptually similar to:

```bash
echo "Selected environment: staging"
```

The input declaration defines the information. The expression reads it. The step uses it.

---

# 6. The Inputs Context

You will often see this expression in GitHub Actions:

```yaml
${{ inputs.version }}
```

Let's understand it carefully.

## What is a context?

A context is a collection of information that GitHub Actions makes available to expressions.

Different contexts provide different kinds of information.

For example:

```yaml
${{ github.ref }}
```

can provide information about the Git reference associated with a workflow run.

```yaml
${{ runner.os }}
```

provides information about the runner's operating system.

```yaml
${{ inputs.version }}
```

accesses an input named `version`.

The `inputs` context is the part that matters in this lesson.

## Example

Suppose your workflow declares:

```yaml
inputs:
  version:
    description: "Application version"
    required: true
    type: string
```

A user supplies:

```text
v3.5.0
```

You can read that value using:

```yaml
${{ inputs.version }}
```

The parts mean:

- `inputs`: Access the inputs context.
- `version`: Select the input named `version`.
- `${{ ... }}`: GitHub Actions expression syntax.

## Why not write only `version`?

Because GitHub needs to know which value you are referring to.

The name `version` might appear in different places in a workflow. The expression `inputs.version` explicitly identifies the input named `version`.

## Accessing multiple inputs

If your workflow declares:

```yaml
inputs:
  version:
    type: string
    required: true

  environment:
    type: choice
    options:
      - development
      - staging
      - production
    required: true
```

You can access the values separately:

```yaml
${{ inputs.version }}
```

```yaml
${{ inputs.environment }}
```

For example:

```yaml
- name: Display configuration
  env:
    APP_VERSION: ${{ inputs.version }}
    TARGET_ENVIRONMENT: ${{ inputs.environment }}
  run: |
    echo "Application version: $APP_VERSION"
    echo "Target environment: $TARGET_ENVIRONMENT"
```

Here, `inputs` provides the supplied values, and `env` makes those values available as environment variables to the step's process.

We will explore this distinction in detail later.

---

# 7. Types of Inputs

Different situations require different kinds of information.

Sometimes you need text. Sometimes you need a number. Sometimes you want a user to choose from a list.

GitHub Actions supports different input types depending on the trigger.

## 7.1 String

A string is text.

Examples:

```text
v1.0.0
staging
NetRescue
```

Example declaration:

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

Use a string when you want to accept text such as a version, a name, or a label.

Remember that a string input does not automatically validate whether the supplied text is a valid application version.

## 7.2 Choice

A choice input lets the user select one option from a predefined list.

```yaml
on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose a deployment environment"
        required: true
        type: choice
        options:
          - development
          - staging
          - production
```

Instead of allowing arbitrary text, GitHub presents a list of choices.

This helps reduce accidental typing mistakes.

For example, a user can select `staging` instead of accidentally entering `stagng`.

**Important:** A choice list limits the options presented by the interface. It is not a replacement for authorization checks or deployment safeguards.

## 7.3 Boolean

A Boolean represents a true-or-false value.

Imagine a workflow that can optionally run tests.

```yaml
on:
  workflow_dispatch:
    inputs:
      run_tests:
        description: "Should the workflow run tests?"
        required: true
        type: boolean
        default: true
```

The user can select whether tests should run.

The Boolean value can then control a job:

```yaml
jobs:
  tests:
    if: ${{ inputs.run_tests }}
    runs-on: ubuntu-latest

    steps:
      - name: Run tests
        run: echo "Running tests..."
```

If `run_tests` is `true`, the job runs.

If `run_tests` is `false`, the job is skipped.

This example demonstrates conditional behavior; the command only prints a message and does not execute actual tests.

## 7.4 Number

A number input is appropriate for numeric values.

For example:

```yaml
on:
  workflow_dispatch:
    inputs:
      replicas:
        description: "How many replicas should be requested?"
        required: true
        type: number
        default: 2
```

The user can supply a number such as `3`.

The workflow can access it using:

```yaml
${{ inputs.replicas }}
```

You could use the value in a deployment command, but you should validate acceptable ranges before changing production infrastructure.

For example, a production workflow might allow only values from 1 to 10.

## 7.5 Environment

For `workflow_dispatch`, GitHub Actions also supports the `environment` input type.

This input allows the user to select a GitHub Environment.

```yaml
on:
  workflow_dispatch:
    inputs:
      target:
        description: "Select the GitHub Environment"
        required: true
        type: environment
```

GitHub Environments can be configured in your repository's settings.

They can contain deployment protection rules, environment-specific secrets, variables, and other deployment controls.

Do not confuse the `environment` input type with the job-level `environment:` property. The input gathers a selection; the job-level property associates a job with a GitHub Environment.

## Input types at a glance

| Type | Purpose | Example |
|---|---|---|
| `string` | Accept text | `v1.2.0` |
| `choice` | Select from predefined options | `staging` |
| `boolean` | Represent true or false | `true` |
| `number` | Accept a numeric value | `3` |
| `environment` | Select a GitHub Environment | A configured environment |

**Important distinction:** The `workflow_dispatch` trigger supports these five types. The `workflow_call` trigger, used for reusable workflows, supports `string`, `number`, and `boolean` inputs.

---

# 8. Required Inputs and Default Values

Two important properties are:

- `required`
- `default`

They answer different questions.

## 8.1 Required input

```yaml
inputs:
  version:
    description: "Application version"
    required: true
    type: string
```

This says that the workflow expects a version to be supplied.

## 8.2 Optional input

```yaml
inputs:
  message:
    description: "Optional build message"
    required: false
    type: string
```

This input is optional. A user does not have to supply it.

If you do not provide a default value, your workflow must account for the possibility that the value is empty.

For example:

```yaml
- name: Display message
  if: ${{ inputs.message != '' }}
  run: echo "Message provided."
```

This step runs only when the message is not empty.

## 8.3 Default value

A default value is the value GitHub uses when the caller does not supply another value.

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
        default: "v1.0.0"
```

The default is `v1.0.0`.

The user can accept that value or provide a different one.

## Required vs. default

Think about ordering food.

- `required: true` means you must provide the requested information.
- `default: "v1.0.0"` means a suggested value is already available.

A required input can have a default value. When a default exists, the user normally does not need to type a replacement value.

A default is not a security control, and it does not mean the workflow should automatically deploy the default version to production.

---

# 9. Using Multiple Inputs

Real CI/CD workflows often need more than one input.

For example, you may want to select:

1. An application version.
2. A target environment.
3. Whether tests should run.

Here is a complete example:

```yaml
name: CI/CD Part 7 - Multiple Inputs

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
        default: "v1.0.0"

      environment:
        description: "Deployment target"
        required: true
        type: choice
        options:
          - development
          - staging
          - production

      run_tests:
        description: "Run tests before deployment preparation?"
        required: true
        type: boolean
        default: true

jobs:
  show-configuration:
    runs-on: ubuntu-latest

    steps:
      - name: Display selected configuration
        env:
          APP_VERSION: ${{ inputs.version }}
          TARGET_ENVIRONMENT: ${{ inputs.environment }}
          RUN_TESTS: ${{ inputs.run_tests }}
        run: |
          echo "Application version: $APP_VERSION"
          echo "Target environment: $TARGET_ENVIRONMENT"
          echo "Run tests: $RUN_TESTS"
```

When you start this workflow manually, GitHub displays the three inputs.

For example:

```text
Application version: v2.0.0
Target environment: staging
Run tests: true
```

The workflow receives all three values and makes them available to the step.

Notice that the workflow is still only displaying configuration. Receiving a deployment target is not the same as performing a deployment.

---

# 10. Using Inputs in Conditions

Inputs become especially useful when they control what a workflow does.

Suppose you want a workflow to run tests only when a user selects `true`.

```yaml
name: Conditional Input Demo

on:
  workflow_dispatch:
    inputs:
      run_tests:
        description: "Should tests run?"
        required: true
        type: boolean
        default: true

jobs:
  tests:
    if: ${{ inputs.run_tests }}
    runs-on: ubuntu-latest

    steps:
      - name: Run tests
        run: echo "Tests would run here."
```

Let's examine the logic.

The expression is:

```yaml
if: ${{ inputs.run_tests }}
```

It means:

> Run this job only when the supplied `run_tests` input is true.

If the user selects `true`, the job runs.

If the user selects `false`, the job is skipped.

## Another example: production-specific behavior

Suppose you want to display a warning when production is selected.

```yaml
jobs:
  deployment-preview:
    runs-on: ubuntu-latest

    steps:
      - name: Show selected environment
        run: echo "Selected environment: ${{ inputs.environment }}"

      - name: Show production warning
        if: ${{ inputs.environment == 'production' }}
        run: echo "Production safeguards must be checked."
```

The second step runs only if the selected environment equals `production`.

This is a conditional expression.

It lets your workflow choose different actions based on supplied information.

**Production lesson:** A condition can control workflow execution, but a production deployment should also use suitable permissions, environment protection rules, validation, and other safeguards.

---

# 11. Inputs vs. Environment Variables

This is one of the most important distinctions in GitHub Actions.

Consider:

```yaml
environment:
  name: ${{ inputs.environment }}

env:
  IMAGE_VERSION: ${{ inputs.version }}
```

Both properties contain the word `environment` or refer to environment-related information, but they do different jobs.

## 11.1 What is `inputs`?

`inputs` provides access to input values.

For example:

```yaml
${{ inputs.version }}
```

This reads the value of the input named `version`.

## 11.2 What is `env`?

`env` defines environment variables.

Environment variables are named values that can be made available to processes running on the runner.

For example:

```yaml
env:
  IMAGE_VERSION: v1.2.0
```

A shell step can read the value using:

```bash
echo "$IMAGE_VERSION"
```

## 11.3 Connecting an input to an environment variable

Consider:

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Display version
        env:
          IMAGE_VERSION: ${{ inputs.version }}
        run: echo "Building version $IMAGE_VERSION"
```

The flow is:

1. The user supplies a version.
2. `inputs.version` accesses that value.
3. The `env` section assigns it to `IMAGE_VERSION` for the step.
4. The shell command reads `$IMAGE_VERSION`.

For example, if the user supplies `v2.0.0`, the step receives an environment variable named `IMAGE_VERSION` whose value is `v2.0.0`.

## 11.4 What does `environment:` mean?

At the job level, this is different:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: staging
```

The `environment:` property associates the job with a GitHub Environment named `staging`.

GitHub Environments can be used to manage deployment protection rules, environment-specific secrets, and variables.

You can combine the concepts:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: ${{ inputs.environment }}
```

Here, the input chooses the environment associated with the job.

By contrast:

```yaml
env:
  IMAGE_VERSION: ${{ inputs.version }}
```

creates an environment variable from the input value.

## Final comparison

| Property | Meaning |
|---|---|
| `inputs.version` | Reads the input named `version`. |
| `env:` | Defines environment variables. |
| `environment:` | Associates a job or deployment with a GitHub Environment. |

Do not mix these concepts up. They solve different problems.

---

# 12. Inputs vs. Variables vs. Secrets

GitHub Actions has several ways to work with values. They are related, but they are not interchangeable.

## 12.1 Inputs

Inputs provide configurable information to a workflow or reusable workflow.

Example:

```yaml
${{ inputs.version }}
```

Use them when a workflow needs to receive information such as a version or selected operation.

## 12.2 Variables

GitHub Actions variables can store configuration values that are not sensitive.

For example, a repository variable might contain an application name.

You can reference a GitHub Actions configuration variable using:

```yaml
${{ vars.APP_NAME }}
```

Variables are useful when a value should be configured in repository, organization, or environment settings rather than repeatedly written into workflow files.

## 12.3 Secrets

Secrets store sensitive values such as tokens, passwords, and credentials.

You can reference a secret with:

```yaml
${{ secrets.DEPLOY_TOKEN }}
```

Never ask users to supply passwords or access tokens through ordinary text inputs when a proper secret-management mechanism is available.

## Comparison

| Feature | Inputs | Variables | Secrets |
|---|---|---|---|
| Main purpose | Receive configurable information | Store configuration | Store sensitive values |
| Example | `inputs.version` | `vars.APP_NAME` | `secrets.DEPLOY_TOKEN` |
| Typical content | Version, choice, Boolean | App name, configuration | Tokens, passwords |
| Designed to hide sensitive values? | No | No | Yes, subject to platform protections |

### Example combining them

```yaml
name: Configuration Demo

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string

jobs:
  show-config:
    runs-on: ubuntu-latest

    steps:
      - name: Display configuration
        env:
          APP_VERSION: ${{ inputs.version }}
          APP_NAME: ${{ vars.APP_NAME }}
          DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
        run: |
          echo "Application: $APP_NAME"
          echo "Version: $APP_VERSION"

          # Never print the secret.
          echo "Deployment credential is available to this step."
```

The application name comes from a variable, the version comes from an input, and the token comes from a secret.

The example deliberately does not print the token.

For production workflows, only expose secrets to the jobs and steps that actually need them.

---

# 13. Using Inputs in a Docker CI/CD Workflow

Now let's connect inputs to a practical DevOps task.

Suppose you want to build a Docker image and allow the user to select its version.

Create this workflow:

```yaml
name: Docker Image Version Input

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Docker image version"
        required: true
        type: string
        default: "v1.0.0"

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Show selected version
        env:
          IMAGE_VERSION: ${{ inputs.version }}
        run: |
          echo "Selected image version: $IMAGE_VERSION"

      - name: Build Docker image
        env:
          IMAGE_VERSION: ${{ inputs.version }}
        run: |
          docker build -t myapp:"$IMAGE_VERSION" .
```

## What happens?

### Step 1: The user supplies a version

For example:

```text
v2.0.0
```

### Step 2: GitHub makes the input available

The workflow accesses the value using:

```yaml
${{ inputs.version }}
```

### Step 3: The value becomes an environment variable

```yaml
env:
  IMAGE_VERSION: ${{ inputs.version }}
```

The shell receives the selected value as `IMAGE_VERSION`.

### Step 4: Docker builds the image

```bash
docker build -t myapp:"$IMAGE_VERSION" .
```

If the selected version is `v2.0.0`, the command builds an image named:

```text
myapp:v2.0.0
```

The tag changes based on the input.

The workflow remains reusable for different versions.

## Important notes

- This example assumes the repository contains a valid `Dockerfile`.
- It builds an image on the runner but does not push it to a registry.
- It does not deploy the image.
- A real CI/CD pipeline should validate version values and apply appropriate permissions.
- For production use, pin third-party actions to verified commit SHAs according to your organization's security policy rather than relying blindly on mutable tags.

This is how inputs begin to make a CI/CD pipeline configurable.

---

# 14. Inputs in Reusable Workflows

So far, we have used inputs with `workflow_dispatch`, which lets a person manually start a workflow.

Now consider a different situation.

You have several applications, and each one needs a similar deployment process.

You could copy the same deployment YAML into every repository. However, that creates duplication.

If you need to update the deployment process, you may have to update many files.

A reusable workflow helps solve this problem.

Think of a reusable workflow as a shared automation recipe that another workflow can call.

The calling workflow supplies the required information, and the reusable workflow uses it.

For reusable workflows, GitHub Actions uses `workflow_call`.

## 14.1 Create the reusable workflow

Create:

```text
.github/workflows/reusable-deploy.yml
```

Add:

```yaml
name: Reusable Deployment

on:
  workflow_call:
    inputs:
      environment:
        description: "Deployment target"
        required: true
        type: string

      version:
        description: "Application version"
        required: true
        type: string

jobs:
  show-deployment:
    runs-on: ubuntu-latest

    steps:
      - name: Show received values
        env:
          TARGET_ENVIRONMENT: ${{ inputs.environment }}
          APP_VERSION: ${{ inputs.version }}
        run: |
          echo "Environment: $TARGET_ENVIRONMENT"
          echo "Version: $APP_VERSION"
```

This workflow declares two inputs:

- `environment`
- `version`

It expects another workflow to call it and supply those values.

Unlike a `workflow_dispatch` workflow, this reusable workflow is designed to be called by another workflow.

## 14.2 Create the calling workflow

Create another file:

```text
.github/workflows/call-reusable-deploy.yml
```

Add:

```yaml
name: Call Reusable Deployment

on:
  workflow_dispatch:

jobs:
  deployment:
    uses: ./.github/workflows/reusable-deploy.yml

    with:
      environment: staging
      version: v2.1.0
```

Now let's understand the flow.

1. A user manually starts `call-reusable-deploy.yml`.
2. GitHub starts the `deployment` job.
3. The job calls `reusable-deploy.yml`.
4. The `with` section supplies `environment` and `version`.
5. The reusable workflow receives those values through its `inputs` context.
6. Its step prints the values.

Expected output:

```text
Environment: staging
Version: v2.1.0
```

This example demonstrates reusable workflow input passing. It does not perform a real deployment.

## Why is this useful?

Imagine you have three applications:

- UserService
- HotelService
- RatingService

Each application needs a similar deployment process.

Instead of maintaining three copies of the same deployment workflow, you can create one reusable workflow and call it from each application's workflow.

Each caller supplies its own configuration.

This makes automation easier to maintain and reduces duplication.

---

# 15. Understanding the `with` Keyword

You have now seen two important pieces of syntax:

```yaml
inputs:
```

and:

```yaml
with:
```

They are connected, but they are not the same.

## `inputs` declares what can be received

In a reusable workflow:

```yaml
on:
  workflow_call:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

This declares an input named `version`.

It is similar to defining a parameter that a reusable workflow expects.

## `with` supplies the value

In the calling workflow:

```yaml
jobs:
  deployment:
    uses: ./.github/workflows/reusable-deploy.yml

    with:
      version: v2.1.0
```

This supplies the value `v2.1.0` to the reusable workflow's `version` input.

The names must match.

If the reusable workflow expects `version`, the caller must supply the corresponding `version` input.

## Follow the complete path

```yaml
# Reusable workflow: declare the input.
on:
  workflow_call:
    inputs:
      version:
        required: true
        type: string
```

```yaml
# Calling workflow: supply the value.
jobs:
  deployment:
    uses: ./.github/workflows/reusable-deploy.yml
    with:
      version: v2.1.0
```

```yaml
# Reusable workflow: read the received value.
- name: Display version
  run: echo "Version: ${{ inputs.version }}"
```

The flow is:

**Declaration → Value supplied by caller → Value received → Value used.**

### What about actions?

The `with` keyword is also used to pass inputs to actions.

For example:

```yaml
- name: Check out repository
  uses: actions/checkout@v4
```

This action supports its own documented inputs. You can provide them with `with`:

```yaml
- name: Check out a specific branch
  uses: actions/checkout@v4
  with:
    ref: main
```

Here, `ref` is an input defined by the `actions/checkout` action, not an input declared under your workflow's `workflow_dispatch`.

This distinction matters:

- Workflow inputs are declared in your workflow's trigger configuration.
- Reusable workflow inputs are declared under `workflow_call`.
- Action inputs are defined by the action itself.
- `with` supplies values to a reusable workflow or action.

The keyword is similar, but the recipient determines which inputs are valid.

---

# 16. `workflow_dispatch` vs. `workflow_call`

These two triggers are important, and beginners often confuse them.

## `workflow_dispatch`

This trigger allows a workflow to be started manually.

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

Typical use case:

A developer opens the Actions interface, enters a version, and starts the workflow.

## `workflow_call`

This trigger allows a workflow to be called by another workflow.

```yaml
on:
  workflow_call:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

Typical use case:

A main CI/CD workflow calls a shared deployment workflow and passes it a version.

## Comparison

| Feature | `workflow_dispatch` | `workflow_call` |
|---|---|---|
| Main purpose | Manual workflow execution | Reusing a workflow from another workflow |
| Typical caller | Person or API | Another workflow |
| Where inputs are declared | Under `on.workflow_dispatch.inputs` | Under `on.workflow_call.inputs` |
| Supports `choice` input type | Yes | No |
| Supports `environment` input type | Yes | No |
| Supports `string` | Yes | Yes |
| Supports `number` | Yes | Yes |
| Supports `boolean` | Yes | Yes |

## A subtle but important detail: Boolean values

GitHub Actions provides the `inputs` context for workflow inputs.

For `workflow_dispatch`, the `inputs` context preserves Boolean values as Booleans. The older `github.event.inputs` representation converts those input values to strings.

For example:

```yaml
if: ${{ inputs.run_tests }}
```

is appropriate when `run_tests` is a Boolean input.

Be careful when working with strings such as `"false"`: a non-empty string is not the same thing as the Boolean value `false`.

Prefer the correctly typed `inputs` context when using workflow inputs.

---

# 17. A Complete Practice Workflow

Now let's combine several concepts into one practical workflow.

This example allows a user to choose:

- An application version.
- A deployment target.
- Whether tests should run.

It then displays the selected configuration.

**This is a learning workflow. It does not deploy an application or build a Docker image.**

Create:

```text
.github/workflows/inputs-complete-demo.yml
```

Add the following:

```yaml
name: CI/CD Part 7 - Complete Inputs Demo

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
        default: "v1.0.0"

      environment:
        description: "Choose the target environment"
        required: true
        type: choice
        options:
          - development
          - staging
          - production

      run_tests:
        description: "Should the workflow run tests?"
        required: true
        type: boolean
        default: true

jobs:
  tests:
    # Run this job only when the user selects true.
    if: ${{ inputs.run_tests }}
    runs-on: ubuntu-latest

    steps:
      - name: Announce test execution
        run: echo "Tests would run here."

  preview:
    # Always show the selected configuration,
    # even if the optional tests job is skipped.
    if: ${{ always() }}
    runs-on: ubuntu-latest

    steps:
      - name: Display configuration
        env:
          APP_VERSION: ${{ inputs.version }}
          TARGET_ENVIRONMENT: ${{ inputs.environment }}
          RUN_TESTS: ${{ inputs.run_tests }}
        run: |
          echo "Application version: $APP_VERSION"
          echo "Target environment: $TARGET_ENVIRONMENT"
          echo "Run tests: $RUN_TESTS"

      - name: Show production safeguard reminder
        if: ${{ inputs.environment == 'production' }}
        run: |
          echo "Production selected."
          echo "Verify approvals, permissions, and deployment safeguards."
```

## Understanding the execution

### Scenario A: Development

Inputs:

```text
version: v1.0.0
environment: development
run_tests: true
```

Expected behavior:

- The `tests` job runs.
- The `preview` job displays the selected configuration.
- The production warning is skipped.

### Scenario B: Staging without tests

Inputs:

```text
version: v1.2.0
environment: staging
run_tests: false
```

Expected behavior:

- The `tests` job is skipped.
- The `preview` job displays the selected configuration.
- The production warning is skipped.

### Scenario C: Production

Inputs:

```text
version: v2.0.0
environment: production
run_tests: true
```

Expected behavior:

- The `tests` job runs.
- The `preview` job displays the selected configuration.
- The production safeguard reminder runs.

## Why use `always()` here?

Normally, a job that depends on another job can be affected by that dependency's result.

In this example, `preview` has no explicit `needs` dependency on `tests`, but its condition uses `always()` to demonstrate an always-evaluated status-check function.

For this simple workflow, you could omit the `if: ${{ always() }}` condition and the preview job would still run independently of the tests job.

In a more complex workflow where jobs depend on one another, status-check functions require careful use. `always()` should not be added blindly to deployment jobs because it can allow steps to run after failures or cancellations.

**The main learning objective is the input flow:** define the inputs, receive their values, and use those values in conditions and steps.

---

# 18. Common Beginner Mistakes

Let's examine mistakes that can make inputs confusing.

## Mistake 1: Declaring an input but never using it

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string

jobs:
  example:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Hello"
```

The input is declared, but the workflow never reads it.

Declaring an input does not automatically change the workflow's behavior.

Correct example:

```yaml
- name: Show version
  run: echo "Version: ${{ inputs.version }}"
```

## Mistake 2: Using the wrong input name

Suppose you declared:

```yaml
inputs:
  version:
    required: true
    type: string
```

But you wrote:

```yaml
${{ inputs.app_version }}
```

The input names do not match.

Use:

```yaml
${{ inputs.version }}
```

Input names must match their declarations.

## Mistake 3: Confusing `inputs` with `env`

Incorrect mental model:

> `inputs.version` and `$VERSION` are the same thing.

They are different ways of accessing values.

This reads the input through the expression context:

```yaml
${{ inputs.version }}
```

This reads a shell environment variable:

```bash
echo "$VERSION"
```

To connect them:

```yaml
env:
  VERSION: ${{ inputs.version }}
```

Then:

```yaml
run: echo "$VERSION"
```

## Mistake 4: Confusing `environment:` with `env:`

This:

```yaml
env:
  VERSION: ${{ inputs.version }}
```

defines an environment variable.

This:

```yaml
environment:
  name: staging
```

associates the job with a GitHub Environment.

They are not interchangeable.

## Mistake 5: Using an unsupported input type

For `workflow_dispatch`, `choice` is supported.

For `workflow_call`, the supported input types are `string`, `number`, and `boolean`.

If you need a choice-like value in a reusable workflow, use a `string` input and validate the value in the workflow.

## Mistake 6: Thinking `with` declares the input

In a reusable workflow, this:

```yaml
with:
  version: v1.0.0
```

supplies the value.

The reusable workflow must declare the input under `on.workflow_call.inputs`.

## Mistake 7: Assuming an input performs a deployment

This:

```yaml
inputs:
  environment:
    type: choice
    options:
      - staging
      - production
```

only defines a choice.

It does not deploy to staging or production.

You must write the actual deployment logic and configure suitable permissions, credentials, and safeguards.

## Mistake 8: Treating user-provided input as trusted

A string input can contain unexpected characters or malicious text.

Validate values before passing them to commands, deployment tools, file paths, or scripts.

## Mistake 9: Printing sensitive information

Inputs are not a secret-management mechanism.

Never pass passwords, access tokens, or other credentials through ordinary workflow inputs when GitHub Secrets or another secure credential mechanism is appropriate.

---

# 19. Security and Production Considerations

Inputs make workflows configurable, but configuration can influence powerful operations.

Imagine that a workflow can deploy to production.

If anyone who can run that workflow can select `production`, you must consider whether that person should be allowed to trigger a production deployment.

The existence of a dropdown is not authorization.

## 19.1 Validate input values

If an input is supposed to contain a version, validate that it follows the format your project expects.

If an input is supposed to contain an environment name, allow only the expected values.

For a `workflow_dispatch` choice input, use predefined options when they fit the requirement. For other input types, add validation.

## 19.2 Avoid unsafe shell interpolation

Be careful when putting user-controlled text directly inside shell commands.

For example, this directly inserts the input into a shell command:

```yaml
- name: Risky pattern
  run: echo "${{ inputs.message }}"
```

For user-controlled text, a safer pattern is to pass it through an environment variable:

```yaml
- name: Safer handling pattern
  env:
    MESSAGE: ${{ inputs.message }}
  run: |
    printf '%s\n' "$MESSAGE"
```

This reduces the risk of the value being interpreted as part of the generated shell program. It does not remove every possible risk; you must still validate inputs according to how you use them.

## 19.3 Use secrets for credentials

Do not define a password as a normal input:

```yaml
# Do not use an ordinary input for credentials.
inputs:
  password:
    description: "Deployment password"
    required: true
    type: string
```

Prefer GitHub Secrets or an appropriate secret-management service.

## 19.4 Use environment protection for production

GitHub Environments can help enforce deployment controls.

For example, a production environment can be configured with required reviewers or other available protection rules.

A job can reference it:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: production
```

The exact protection behavior depends on how the environment is configured.

## 19.5 Apply least privilege

A workflow should receive only the permissions it needs.

Where possible, declare minimal `GITHUB_TOKEN` permissions:

```yaml
permissions:
  contents: read
```

Add other permissions only when the workflow genuinely needs them.

## 19.6 Never assume that a selected environment is safe

This:

```yaml
environment:
  name: ${{ inputs.environment }}
```

selects a GitHub Environment based on the input.

It does not automatically make every input value valid, and it does not replace authorization checks.

For production workflows, use trusted configuration, explicit validation, environment protection, and appropriate credentials.

---

# 20. Practice Challenges

The best way to understand inputs is to write workflows yourself.

Try these exercises in order.

## Challenge 1 — Your name

Create a workflow that accepts a string input named `student_name`.

When the workflow runs, print:

```text
Welcome to GitHub Actions, Taha!
```

The name should come from the input, not be hardcoded.

**Concepts:** `workflow_dispatch`, `inputs`, `string`.

## Challenge 2 — Application version

Create an input named `version` with a default value of `v1.0.0`.

Print the selected version.

Run the workflow twice with different values.

**Concepts:** Defaults and configurable values.

## Challenge 3 — Select an environment

Create a `choice` input with these options:

- development
- staging
- production

Print the selected environment.

**Concepts:** Choice inputs and predefined options.

## Challenge 4 — Optional tests

Create a Boolean input named `run_tests`.

Create a job that runs only when the input is `true`.

**Concepts:** Boolean inputs and job conditions.

## Challenge 5 — Docker image version

Create a workflow that accepts an image version and uses it to tag a Docker image.

Do not push the image to a registry yet.

**Concepts:** Inputs, environment variables, shell commands, Docker tags.

## Challenge 6 — Reusable workflow

Create two files:

1. `reusable-build.yml`
2. `call-reusable-build.yml`

The reusable workflow should accept an input named `version`.

The calling workflow should pass a value using `with`.

The reusable workflow should print the received version.

**Concepts:** `workflow_call`, reusable workflows, `with`, and the `inputs` context.

## Challenge 7 — Input validation

Create a string input named `environment`.

Accept only these values in your validation logic:

- development
- staging
- production

If the value is invalid, make the workflow fail before doing any deployment-related work.

**Concepts:** Input validation and safe automation.

## Challenge 8 — Production safeguards

Create a workflow that lets the user select a deployment environment.

For production, associate the job with the appropriate GitHub Environment and configure protection rules in repository settings.

Do not treat the input selection alone as permission to deploy.

**Concepts:** Inputs, GitHub Environments, and deployment security.

---

# 21. Quick Revision

If you understand the following points, you have learned the foundation of GitHub Actions inputs.

### What is an input?

A named piece of information supplied to a workflow or reusable automation.

### Why do inputs exist?

They allow a workflow to receive different values without requiring the YAML to be edited for every run.

### How do you declare a manual workflow input?

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

### How do you read the input?

```yaml
${{ inputs.version }}
```

### How do you pass a value to a reusable workflow?

```yaml
with:
  version: v1.0.0
```

### How does a reusable workflow declare its input?

```yaml
on:
  workflow_call:
    inputs:
      version:
        description: "Application version"
        required: true
        type: string
```

### What is `env:`?

It defines environment variables available to the relevant steps or jobs.

### What is `environment:`?

It associates a job or deployment with a GitHub Environment.

### Are inputs secrets?

No. Use GitHub Secrets or an appropriate secret-management system for credentials.

### Does selecting `production` automatically deploy an application?

No. You must implement the deployment logic and apply the required permissions and protection rules.

### What is the complete flow?

**Declare the input → Supply a value → Read the value → Use it in workflow logic.**

That is the central idea of GitHub Actions inputs.

---

# 22. Official Documentation

Use the official documentation to verify syntax and explore advanced capabilities.

1. [Manually running a workflow](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow)
2. [Workflow syntax for GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
3. [Reusing workflows](https://docs.github.com/en/actions/how-tos/reuse-automations/reuse-workflows)
4. [Expressions in GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/expressions)
5. [Using variables in GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-variables)
6. [Using secrets in GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets)
7. [Managing deployment environments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments)

---

## Final Message

Imagine that GitHub Actions is a worker.

You give the worker a task, but the worker sometimes needs additional information:

- Which version?
- Which environment?
- Should tests run?
- Which operation should be performed?

**Inputs are the way you provide that information.**

The workflow declares which information it accepts. The person or calling workflow supplies the values. Expressions access those values, and jobs or steps use them to decide what happens next.

Once you understand this flow, inputs stop looking like confusing YAML syntax and start looking like what they really are: **a way to make automation configurable and reusable.**

*CI/CD Learning Series — Part 5: Inputs in GitHub Actions*
