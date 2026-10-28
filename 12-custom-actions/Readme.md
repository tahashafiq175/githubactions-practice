# GitHub Actions Custom Actions — From Zero to Practical Understanding

> **The goal:** Understand Custom Actions so clearly that you can design, build, debug, and reuse your own CI/CD building blocks without blindly copying YAML.

---

## Table of Contents

1. [What Is a Custom Action?](#1-what-is-a-custom-action)
2. [Workflow vs Action](#2-workflow-vs-action)
3. [Types of Custom Actions](#3-types-of-custom-actions)
4. [Understanding the Action Directory](#4-understanding-the-action-directory)
5. [Understanding `action.yml`](#5-understanding-actionyml)
6. [Understanding `runs` and `using: composite`](#6-understanding-runs-and-using-composite)
7. [Why Do We Need `shell`?](#7-why-do-we-need-shell)
8. [Inputs: Giving Information to an Action](#8-inputs-giving-information-to-an-action)
9. [Required Inputs and Default Values](#9-required-inputs-and-default-values)
10. [Using Inputs Inside Commands](#10-using-inputs-inside-commands)
11. [Working with Multiple Inputs](#11-working-with-multiple-inputs)
12. [Using Existing Actions Inside Custom Actions](#12-using-existing-actions-inside-custom-actions)
13. [Repository Checkout](#13-repository-checkout)
14. [Environment Variables](#14-environment-variables)
15. [Bash Conditions Inside Custom Actions](#15-bash-conditions-inside-custom-actions)
16. [Outputs: Returning Information](#16-outputs-returning-information)
17. [`$GITHUB_OUTPUT` Explained](#17-github_output-explained)
18. [Step Outputs vs Custom Action Outputs](#18-step-outputs-vs-custom-action-outputs)
19. [Building a Real Java/Maven Custom Action](#19-building-a-real-javamaven-custom-action)
20. [Calling the Java Build Action](#20-calling-the-java-build-action)
21. [Passing Information Between Actions](#21-passing-information-between-actions)
22. [Conditions with Custom Action Inputs](#22-conditions-with-custom-action-inputs)
23. [Common Mistakes and Their Fixes](#23-common-mistakes-and-their-fixes)
24. [Application Dockerfile vs Docker Action Dockerfile](#24-application-dockerfile-vs-docker-action-dockerfile)
25. [The Complete Mental Model](#25-the-complete-mental-model)
26. [Quick Reference Cheat Sheet](#26-quick-reference-cheat-sheet)
27. [Practice Exercises](#27-practice-exercises)

---

# 1. What Is a Custom Action?

Imagine that every time you bake a cake, you repeat these steps:

1. Collect the ingredients.
2. Mix everything.
3. Put the mixture in the oven.
4. Wait for the cake.
5. Take it out.

Now imagine baking a hundred cakes. Would you want to redesign the entire process every time?

Probably not. You would create a reusable process or tool that handles the repeated work.

**A Custom Action does something similar in GitHub Actions.**

Suppose you repeatedly need to:

- Set up Java.
- Configure a build environment.
- Build a Spring Boot application.
- Find the generated JAR.
- Return the JAR path.

Instead of repeating all these steps in every workflow, you can package them into one reusable Custom Action.

Your workflow can then call it whenever needed.

```text
Without a Custom Action

Workflow A → Java setup → Maven build → Find JAR
Workflow B → Java setup → Maven build → Find JAR
Workflow C → Java setup → Maven build → Find JAR


With a Custom Action

Workflow A ──┐
Workflow B ──┼──→ Java Build Custom Action
Workflow C ──┘
```

The three workflows can reuse the same implementation.

## Why do we need Custom Actions?

Custom Actions help us:

- Avoid repeating the same automation logic.
- Organize large CI/CD pipelines.
- Create reusable building blocks.
- Make workflows easier to read.
- Standardize how applications are built and tested.
- Maintain shared automation in one place.

For example, a team might create these actions:

```text
Java Build Action
Docker Build Action
Docker Push Action
Security Scan Action
Deployment Action
```

A workflow can combine them to build a complete pipeline.

---

# 2. Workflow vs Action

This is the first distinction you must understand.

A **workflow** describes the overall automation process.

An **action** performs a reusable piece of work inside that process.

Think of a restaurant:

- The workflow is the complete order process.
- An action is one reusable task, such as preparing a meal.

| Workflow | Custom Action |
|---|---|
| Defines the overall automation process | Packages reusable automation |
| Contains jobs | Contains action implementation steps |
| A job uses `runs-on` to select a runner | Uses the runner supplied by the calling job |
| Can call existing and custom actions | Can wrap existing actions and commands |
| Commonly starts from an event or manual trigger | Is normally invoked by a workflow or another supported action consumer |

Example workflow:

```yaml
name: Java CI

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: ./.github/actions/java-build
```

The workflow decides **when** the build happens.

The Custom Action defines **how** the reusable build process works.

Remember:

```text
Workflow = Orchestrator
Custom Action = Reusable building block
```

---

# 3. Types of Custom Actions

GitHub supports three major types of actions.

| Type | Simple explanation | Best suited for |
|---|---|---|
| Composite | Combines multiple steps | Reusable shell commands and existing actions |
| JavaScript | Runs JavaScript/Node.js action code | Programmatic logic and API interactions |
| Docker | Runs action logic inside a container | Custom runtimes and system dependencies |

## 3.1 Composite Action

A Composite Action combines multiple steps into one reusable action.

For example:

```text
Java Build Action
    │
    ├── Set up Java
    ├── Give Maven Wrapper permission
    ├── Build the application
    └── Find the generated JAR
```

The caller only needs to write:

```yaml
- name: Build Application
  uses: ./.github/actions/java-build
```

Use a Composite Action when you want to combine existing actions, shell commands, and other supported steps.

## 3.2 JavaScript Action

A JavaScript Action uses Node.js to execute the action's program logic.

Its metadata can point to a JavaScript entry file:

```yaml
runs:
  using: node24
  main: index.js
```

The exact supported Node.js runtime should be checked against GitHub's current documentation when creating an action.

A JavaScript Action is useful when you need to write program logic rather than simply combine shell commands.

## 3.3 Docker Action

A Docker Action executes action logic inside a Docker container.

It is useful when your action needs a particular runtime, operating-system environment, or system dependencies.

For example, you might write the action's logic in Python or Go and package the environment in a Docker image.

**Important:** A Docker Action is not the same thing as using a Dockerfile to package your Spring Boot application. We will distinguish these later.

For now, focus on **Composite Actions**. They are an excellent starting point for learning reusable CI/CD automation.

---

# 4. Understanding the Action Directory

Let's create our first action.

A common repository structure is:

```text
repository/
│
├── .github/
│   ├── workflows/
│   │   └── java-ci.yml
│   │
│   └── actions/
│       └── java-build/
│           └── action.yml
│
└── CICDDEMO/
    ├── pom.xml
    ├── mvnw
    └── src/
```

Let's understand the important parts.

### `.github/workflows/`

This directory commonly contains workflow YAML files.

For example:

```text
.github/workflows/java-ci.yml
```

GitHub discovers workflow files in this directory.

### `.github/actions/`

This is a common place to store your own local actions.

It is a convention, not a mandatory location. An action can live elsewhere in the repository.

### `java-build/`

This is the directory containing our particular Custom Action.

Inside it, we put:

```text
action.yml
```

The action metadata file describes the action.

## Why give every action its own directory?

Because a real action may need more than one file.

For example:

```text
.github/actions/
│
├── java-build/
│   └── action.yml
│
├── docker-build/
│   └── action.yml
│
└── environment-check/
    └── action.yml
```

Each directory represents a separate reusable action.

This structure keeps the actions organized and independent.

## Why do we write `./`?

A workflow might call our action like this:

```yaml
uses: ./.github/actions/java-build
```

Break it down:

- `.` means the current directory.
- `./` starts a relative path from the current directory.
- `.github/actions/java-build` identifies the action directory.

For a local action, GitHub resolves this path relative to the repository workspace, not relative to the workflow YAML file's directory.

Compare:

```text
./folder     → folder inside the current directory
../folder    → folder inside the parent directory
```

For a local action, `uses` points to the **directory containing `action.yml`**, not directly to the file.

---

# 5. Understanding `action.yml`

Think of `action.yml` as the action's identity card and instruction manual.

It tells GitHub what the action is, what information it accepts, what information it returns, and how it runs.

A typical Composite Action looks like this:

```yaml
name: "Java Build Action"

description: "Builds a Java application using Maven"

inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"

outputs:
  jar_file:
    description: "Generated JAR file"
    value: ${{ steps.build.outputs.jar_file }}

runs:
  using: composite
  steps:
    - name: Build
      id: build
      shell: bash
      run: echo "Building application"
```

Let's understand each section.

## `name`

```yaml
name: "Java Build Action"
```

The action's name.

## `description`

```yaml
description: "Builds a Java application using Maven"
```

A short explanation of the action's purpose.

This is part of the Custom Action metadata. It is not the same as defining a normal workflow property.

## `inputs`

```yaml
inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"
```

Defines information the action can receive.

## `outputs`

```yaml
outputs:
  jar_file:
    description: "Generated JAR file"
    value: ${{ steps.build.outputs.jar_file }}
```

Defines information the action exposes to its caller.

## `runs`

```yaml
runs:
  using: composite
  steps:
```

Describes how the action executes.

We will examine this section next.

---

# 6. Understanding `runs` and `using: composite`

Consider:

```yaml
runs:
  using: composite
  steps:
    - name: Show Message
      shell: bash
      run: echo "Hello from my Custom Action"
```

The word `runs` tells GitHub how the action should execute.

The value:

```yaml
using: composite
```

tells GitHub that this action packages multiple steps into a reusable unit.

The `steps` list contains the actual work.

A Composite Action can include both:

```yaml
uses: actions/setup-java@v4
```

and:

```yaml
run: mvn clean package
```

The first invokes another action. The second executes a shell command.

## Does a Composite Action have `runs-on`?

No. A Composite Action does not define its own job runner with `runs-on`.

The calling workflow selects the runner:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
```

The Composite Action executes its steps within that job's execution environment.

Think of it this way:

```text
Workflow Job
    │
    ├── Selects runner
    │
    └── Calls Composite Action
             │
             ├── Step 1
             ├── Step 2
             └── Step 3
```

---

# 7. Why Do We Need `shell`?

Consider:

```yaml
- name: Build Application
  shell: bash
  run: ./mvnw clean package
```

The `run` field contains a command.

But who interprets that command?

The shell does.

A shell is a program that understands commands and asks the operating system to execute them.

Bash is one example of a shell.

In a Composite Action, a step using `run` must specify its shell.

```yaml
shell: bash
```

tells GitHub to execute the command using Bash.

For example:

```yaml
- name: Display Message
  shell: bash
  run: |
    echo "Starting build"
    echo "Preparing application"
```

Bash executes these commands in order.

Remember:

```text
run   = What command should execute?
shell = Which shell should execute it?
```

This distinction matters because shell syntax differs. Bash syntax may not work unchanged in PowerShell or other shells.

---

# 8. Inputs: Giving Information to an Action

An input is information that the caller supplies to a Custom Action.

Imagine a Java build action that accepts a Java version.

We do not want to hardcode Java 21 permanently into the action. We want the caller to choose a version.

First, declare the input inside `action.yml`:

```yaml
inputs:
  java-version:
    description: "Java version to use"
    required: true
```

Then supply the value from the workflow:

```yaml
- name: Build Java Application
  uses: ./.github/actions/java-build
  with:
    java-version: "21"
```

Inside the action, read the value:

```yaml
${{ inputs.java-version }}
```

## The complete flow

```text
Workflow
   │
   │ with:
   │   java-version: "21"
   ▼
Custom Action
   │
   │ inputs.java-version
   ▼
"21"
```

There are three different pieces here.

| Syntax | Purpose |
|---|---|
| `inputs:` | Declares the action's input |
| `with:` | Supplies the value when calling the action |
| `${{ inputs.java-version }}` | Reads the value inside the action |

### A Java analogy

Consider this method:

```java
void buildApplication(String javaVersion) {
    System.out.println(javaVersion);
}
```

Calling it:

```java
buildApplication("21");
```

The string `"21"` is passed into the method.

A Custom Action works similarly at a conceptual level:

```text
Caller supplies a value
        ↓
Action receives the value
        ↓
Action uses the value
```

The input makes the action reusable because different callers can supply different values.

---

# 9. Required Inputs and Default Values

Two important input properties are `required` and `default`.

## Required input

```yaml
inputs:
  application-name:
    description: "Name of the application"
    required: true
```

This declares the input as required.

The caller is expected to provide it:

```yaml
with:
  application-name: "NetRescue"
```

If a required input is missing, GitHub Actions generally reports an error when evaluating or running the action.

## Optional input with a default

```yaml
inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"
```

The caller can provide a different value:

```yaml
with:
  java-version: "25"
```

If the caller omits the input, the default value is used.

```text
Caller provides 25
       ↓
Action receives 25

Caller omits input
       ↓
Action receives default 21
```

A useful rule:

- Use `required: true` when the caller must provide the value.
- Use `required: false` with `default` when the action has a sensible fallback.

Do not confuse this with `workflow_dispatch` inputs. Workflow inputs can use `type: string`, while Custom Action input metadata does not use `type: string` in the same way.

---

# 10. Using Inputs Inside Commands

Declaring an input is not enough. The action must actually use it.

Suppose the action declares:

```yaml
inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"
```

We can pass the value to `actions/setup-java`:

```yaml
- name: Setup Java
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: ${{ inputs.java-version }}
```

The expression:

```yaml
${{ inputs.java-version }}
```

asks GitHub Actions to evaluate the input and supply its value.

If the caller provides `"21"`, the setup action receives `"21"`.

If the caller provides `"25"`, it receives `"25"`.

## GitHub expressions vs shell variables

These are two different mechanisms.

GitHub Actions expression:

```yaml
${{ inputs.java-version }}
```

Shell environment variable:

```bash
$JAVA_VERSION
```

The first is evaluated by GitHub Actions. The second is expanded by the shell when the command executes.

For example:

```yaml
- name: Display Java Version Input
  shell: bash
  env:
    JAVA_VERSION: ${{ inputs.java-version }}
  run: |
    echo "Requested Java version: $JAVA_VERSION"
```

Here the input is first mapped to an environment variable, and Bash then reads that variable.

---

# 11. Working with Multiple Inputs

A Custom Action can accept several inputs.

For example:

```yaml
inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"

  application-name:
    description: "Application name"
    required: true

  environment-name:
    description: "Target environment"
    required: false
    default: "development"
```

The caller supplies values:

```yaml
- name: Run Custom Action
  uses: ./.github/actions/example
  with:
    java-version: "21"
    application-name: "NetRescue"
    environment-name: "development"
```

The action can read them:

```yaml
${{ inputs.java-version }}
${{ inputs.application-name }}
${{ inputs.environment-name }}
```

This lets us reuse the same implementation for different applications and environments.

```text
Reusable action logic
        +
Different input values
        =
Flexible automation
```

## A security warning

Be careful about directly executing arbitrary input as a shell command:

```yaml
run: ${{ inputs.maven-command }}
```

This can be dangerous if an untrusted person can influence the input. It can lead to shell injection or unintended command execution.

Prefer fixed commands, validated values, and controlled options. Inputs should not automatically be treated as safe shell code.

---

# 12. Using Existing Actions Inside Custom Actions

A Custom Action does not mean you must write every operation yourself.

You can combine existing actions with your own commands.

For example:

```yaml
runs:
  using: composite
  steps:
    - name: Setup Java
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: "21"

    - name: Build Application
      shell: bash
      run: mvn clean package
```

Here:

- `actions/setup-java@v4` is an existing action.
- `mvn clean package` is a shell command.
- The Composite Action packages both steps into one reusable unit.

```text
Java Build Custom Action
        │
        ├── Existing setup-java action
        │
        ├── Custom Bash commands
        │
        └── Additional reusable steps
```

This is one of the most practical uses of Composite Actions: combining existing tools into a workflow component designed for your project or team.

---

# 13. Repository Checkout

Before building a project, the runner needs access to the repository files.

The common way to do this is:

```yaml
- name: Checkout Repository
  uses: actions/checkout@v4
```

There are two common designs.

## Option A: Checkout in the workflow

```yaml
steps:
  - name: Checkout Repository
    uses: actions/checkout@v4

  - name: Build Application
    uses: ./.github/actions/java-build
```

The repository is checked out before the Custom Action runs.

This is a clear pattern when the workflow owns repository preparation.

## Option B: Checkout inside the Custom Action

```yaml
runs:
  using: composite
  steps:
    - name: Checkout Repository
      uses: actions/checkout@v4

    - name: Build Application
      shell: bash
      run: ./mvnw clean package
```

This packages checkout into the action itself.

**Important:** If you call a local action such as `./.github/actions/java-build`, that action's files must already be available in the workspace. A workflow normally checks out its repository before calling a local action. An action cannot rely on checking out the repository inside itself to make its own local action files available for the initial lookup.

Also, the action may need the application source code to be checked out before its build steps run.

Avoid checking out the same repository twice unnecessarily. Choose a clear design based on what the action promises to do.

---

# 14. Environment Variables

Inputs and environment variables are related, but they are not the same.

An **input** is part of the action's interface.

An **environment variable** is a value made available to a process while it runs.

Suppose the action receives an environment name:

```yaml
inputs:
  environment-name:
    description: "Application environment"
    required: false
    default: "development"
```

Now map it to an environment variable:

```yaml
- name: Display Environment
  shell: bash
  env:
    ENVIRONMENT_NAME: ${{ inputs.environment-name }}
  run: |
    echo "Environment: $ENVIRONMENT_NAME"
```

The complete flow is:

```text
Workflow
   │
   │ with:
   │   environment-name: production
   ▼
Custom Action Input
   │
   │ inputs.environment-name
   ▼
Environment Mapping
   │
   │ ENVIRONMENT_NAME
   ▼
Bash Process
   │
   ▼
Environment: production
```

Why do this?

Because shell commands commonly read environment variables using `$VARIABLE_NAME`.

It also keeps values separate from command text and can help avoid unsafe expression interpolation in shell commands.

---

# 15. Bash Conditions Inside Custom Actions

Sometimes an action must choose what to do based on a value.

For example, the behavior may depend on whether the target environment is `development` or `production`.

Consider:

```bash
if [ "$ENVIRONMENT_NAME" == "development" ]; then
    echo "Running in development"
elif [ "$ENVIRONMENT_NAME" == "production" ]; then
    echo "Running in production"
else
    echo "Unknown environment"
fi
```

Let's understand every part.

### `if`

Starts a condition.

### `[ ... ]`

Checks whether a condition is true or false. In Bash, `[` is a command, which is why spaces around it matter.

Correct:

```bash
if [ "$ENVIRONMENT_NAME" == "development" ]; then
```

Incorrect:

```bash
if["$ENVIRONMENT_NAME"=="development"];then
```

### `"$ENVIRONMENT_NAME"`

Reads the variable's value.

Quoting helps preserve values that contain spaces and prevents many word-splitting problems.

### `==`

Compares strings in this Bash conditional.

### `then`

Marks the commands to execute if the condition is true.

### `elif`

Means "else if." It checks another condition when the previous condition was false.

### `else`

Runs if none of the preceding conditions matched.

### `fi`

Closes the `if` statement.

Yes, `fi` is `if` written backwards.

Compare this with Java:

```java
if (environment.equals("development")) {
    System.out.println("Running in development");
} else {
    System.out.println("Other environment");
}
```

Java uses braces to mark the block. Bash uses `then` and `fi`.

## Using a condition in a Composite Action

GitHub Actions also provides an `if` property for individual steps.

For example:

```yaml
- name: Run Development Step
  if: ${{ inputs.environment-name == 'development' }}
  shell: bash
  run: echo "Development step selected"
```

This is a GitHub Actions expression, not a Bash `if` statement.

Both mechanisms can be useful, but they operate at different levels:

- GitHub Actions `if:` decides whether a step should run.
- Bash `if` decides which commands execute inside a running shell step.

---

# 16. Outputs: Returning Information

So far, the workflow has been giving information to an action.

Now let's reverse the direction.

Suppose a Java Build Action creates this file:

```text
CICDDEMO/target/CICDDEMO-0.0.1-SNAPSHOT.jar
```

We want the action to return the JAR path so that the workflow can use it later.

This is an **output**.

```text
Input:
Workflow ───────→ Custom Action

Output:
Custom Action ──→ Workflow
```

An action can declare an output in `action.yml`:

```yaml
outputs:
  jar_file:
    description: "Path to the generated JAR file"
    value: ${{ steps.build.outputs.jar_file }}
```

This declares an output called `jar_file`.

But notice the `value` property. It maps the action output to an output created by a particular internal step.

Understanding this mapping is essential.

---

# 17. `$GITHUB_OUTPUT` Explained

Consider:

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

This command creates a step output in GitHub Actions.

Let's break it down.

### `echo`

Prints text. In this case, its output is redirected into a special file rather than just appearing in the log.

### `jar_file=$Jar_File`

This text has the form:

```text
name=value
```

- `jar_file` is the output name.
- `$Jar_File` is the Bash variable containing the value.

For example, if:

```bash
Jar_File="./CICDDEMO/target/application.jar"
```

then the text becomes:

```text
jar_file=./CICDDEMO/target/application.jar
```

### `>>`

Appends the text to a file.

A single `>` generally replaces a file's contents; `>>` appends to the file.

### `$GITHUB_OUTPUT`

GitHub Actions provides this special environment variable. It points to a file that the running step can write to in order to define its outputs.

Therefore:

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

means:

> "Create a step output named `jar_file` whose value is the path stored in `Jar_File`."

The syntax must use an equals sign.

Incorrect:

```bash
echo "jar_file: $Jar_File" >> "$GITHUB_OUTPUT"
```

Correct:

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

The output file expects the `name=value` format for a normal single-line output.

---

# 18. Step Outputs vs Custom Action Outputs

This is one of the most important concepts in this entire guide.

There are two levels of outputs.

1. A step inside the Custom Action produces a **step output**.
2. The Custom Action exposes that value as an **action output**.

These are connected, but they are not automatically identical.

## Level 1: Create a step output

Inside `action.yml`:

```yaml
- name: Return Build Output
  id: build
  shell: bash
  run: |
    Jar_File="./CICDDEMO/target/application.jar"
    echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

Two important details:

```yaml
id: build
```

gives the step an identifier.

Then:

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

creates its output.

The step output can now be referenced inside the action as:

```yaml
${{ steps.build.outputs.jar_file }}
```

Break it down:

```text
steps
  └── build
       └── outputs
            └── jar_file
```

## Level 2: Expose the step output

In the top-level `outputs` section of `action.yml`:

```yaml
outputs:
  jar_file:
    description: "Path to the generated JAR"
    value: ${{ steps.build.outputs.jar_file }}
```

This tells GitHub:

> "The Custom Action's `jar_file` output should contain the `jar_file` output created by my internal `build` step."

Without this mapping, declaring the output name alone does not supply its value.

## Level 3: Read the Custom Action output in the workflow

The workflow calls the action:

```yaml
- name: Build Java Application
  id: java-build
  uses: ./.github/actions/java-build
```

Notice:

```yaml
id: java-build
```

This is the identifier of the *caller workflow's step*. It is not the ID of the internal build step.

The workflow can now read the action output:

```yaml
- name: Display Generated JAR
  shell: bash
  run: |
    echo "Generated JAR: ${{ steps.java-build.outputs.jar_file }}"
```

## Complete output flow

```text
Inside Custom Action
        │
        ▼
Internal step: id: build
        │
        │ writes to $GITHUB_OUTPUT
        ▼
steps.build.outputs.jar_file
        │
        │ mapped by action.yml
        ▼
Custom Action output: jar_file
        │
        │ action called with id: java-build
        ▼
steps.java-build.outputs.jar_file
        │
        ▼
Calling workflow
```

Remember:

- `build` identifies the internal step.
- `java-build` identifies the workflow step that calls the action.
- The two identifiers belong to different scopes.

---

# 19. Building a Real Java/Maven Custom Action

Now let's put the concepts together.

Suppose your repository contains a Spring Boot project at:

```text
CICDDEMO/
```

Inside it, you have a Maven Wrapper:

```text
CICDDEMO/mvnw
```

We want to create an action that:

1. Accepts a Java version.
2. Sets up Java.
3. Builds the Spring Boot application.
4. Finds the generated JAR.
5. Returns the JAR path.

Create this file:

```text
.github/actions/java-build/action.yml
```

Use the following implementation:

```yaml
name: "Java Build Action"

description: "Builds a Java application using Maven and returns the JAR path."

inputs:
  java-version:
    description: "Java version to use"
    required: false
    default: "21"

outputs:
  jar_file:
    description: "Path to the generated JAR file"
    value: ${{ steps.get-jar.outputs.jar_file }}

runs:
  using: composite

  steps:
    - name: Set up Java
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: ${{ inputs.java-version }}
        cache: maven

    - name: Give Maven Wrapper Permission
      shell: bash
      run: chmod +x CICDDEMO/mvnw

    - name: Build Spring Boot Application
      shell: bash
      run: |
        cd CICDDEMO
        ./mvnw clean package -DskipTests

    - name: Return Build Output
      id: get-jar
      shell: bash
      run: |
        Jar_File=$(find ./CICDDEMO/target -type f -name "*.jar" \
          ! -name "*.original" | head -n 1)

        if [ -z "$Jar_File" ]; then
          echo "Error: No JAR file was found."
          exit 1
        fi

        echo "Build output JAR file: $Jar_File"
        echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

## Understanding the important pieces

### Java setup

```yaml
- name: Set up Java
  uses: actions/setup-java@v4
```

This uses GitHub's existing Java setup action.

The Java version comes from:

```yaml
java-version: ${{ inputs.java-version }}
```

The value is controlled by the caller.

### Maven dependency caching

```yaml
cache: maven
```

This enables Maven dependency caching to help subsequent builds avoid downloading the same dependencies repeatedly when the cache is available.

It does not mean `setup-java` installs Maven as a separate program. The project uses the Maven Wrapper.

### Maven Wrapper permission

```bash
chmod +x CICDDEMO/mvnw
```

This gives the script executable permission on the Linux runner.

The Maven Wrapper is then invoked with:

```bash
./mvnw clean package -DskipTests
```

The command cleans previous build output and packages the application while skipping test execution.

### Finding the JAR

```bash
Jar_File=$(find ./CICDDEMO/target -type f -name "*.jar" \
  ! -name "*.original" | head -n 1)
```

This command:

1. Searches under `CICDDEMO/target`.
2. Selects regular files ending in `.jar`.
3. Excludes `.jar.original` files.
4. Takes the first matching result.
5. Stores the result in `Jar_File`.

The next lines check whether the variable is empty and fail the step if no JAR was found.

This is more robust than silently returning an empty output. For production pipelines, you should select the exact expected artifact when multiple JARs may be generated.

### Returning the JAR path

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

This creates the internal step output.

The action's top-level mapping:

```yaml
value: ${{ steps.get-jar.outputs.jar_file }}
```

exposes that value to the caller.

**Important:** This example assumes the caller has checked out the repository before invoking the local action, so the `CICDDEMO` directory exists.

---

# 20. Calling the Java Build Action

Create a workflow such as:

```text
.github/workflows/java-ci.yml
```

Use:

```yaml
name: Java Build Using Custom Action

on:
  workflow_dispatch:
    inputs:
      java-version:
        description: "Java version"
        type: string
        required: false
        default: "21"

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Build Java Application
        id: java-build
        uses: ./.github/actions/java-build
        with:
          java-version: ${{ inputs.java-version }}

      - name: Display JAR Path
        shell: bash
        run: |
          echo "Generated JAR: ${{ steps.java-build.outputs.jar_file }}"
```

The workflow:

1. Starts when manually triggered.
2. Checks out the repository.
3. Calls the local Java Build Action.
4. Passes the selected Java version.
5. Waits for the action to complete.
6. Reads the returned JAR path.

The action's output is available because the calling step has:

```yaml
id: java-build
```

and the action exposes the output in `action.yml`.

---

# 21. Passing Information Between Actions

Now imagine that we have two Custom Actions:

- Java Build Action
- Docker Build Action

The Java action returns the JAR path. The Docker action needs to know which JAR to package.

The workflow connects them.

```text
Java Build Action
        │
        │ output: jar_file
        ▼
     Workflow
        │
        │ input: jar-file
        ▼
Docker Build Action
```

The Java action is called like this:

```yaml
- name: Build Java Application
  id: java-build
  uses: ./.github/actions/java-build
```

Suppose the Docker action declares:

```yaml
inputs:
  jar-file:
    description: "Path to the application JAR"
    required: true
```

The workflow can pass the output to that input:

```yaml
- name: Build Docker Image
  uses: ./.github/actions/docker-build
  with:
    jar-file: ${{ steps.java-build.outputs.jar_file }}
```

The value flows through the workflow.

This is the important principle:

**Action A does not automatically send its output directly into Action B. The workflow reads the output and supplies it as Action B's input.**

That makes the workflow the connector between reusable building blocks.

---

# 22. Conditions with Custom Action Inputs

We can make the Java Build Action choose whether to run tests.

Declare:

```yaml
inputs:
  skip-tests:
    description: "Whether to skip tests"
    required: false
    default: "false"
```

Then define two conditional steps:

```yaml
- name: Build Without Tests
  if: ${{ inputs.skip-tests == 'true' }}
  shell: bash
  run: |
    cd CICDDEMO
    ./mvnw clean package -DskipTests

- name: Build With Tests
  if: ${{ inputs.skip-tests == 'false' }}
  shell: bash
  run: |
    cd CICDDEMO
    ./mvnw clean package
```

The workflow decides the behavior by passing a value:

```yaml
- name: Build Java Application
  uses: ./.github/actions/java-build
  with:
    skip-tests: "true"
```

The flow is:

```text
skip-tests = "true"
        │
        ▼
Build Without Tests runs
Build With Tests is skipped
```

If the value is `"false"`, the opposite happens.

Custom Action inputs are string-oriented in this metadata format, so explicit comparisons such as:

```yaml
if: ${{ inputs.skip-tests == 'true' }}
```

make the intended behavior clear.

**Production note:** Make sure the input has an accepted value. If someone supplies an unexpected string, both conditions above could be false and neither build step would run. You can validate the input or use a single build step with controlled shell logic.

---

# 23. Common Mistakes and Their Fixes

These are important because understanding mistakes teaches you how the system actually works.

## Mistake 1: Using `type: string` in Custom Action input metadata

Incorrect:

```yaml
inputs:
  java-version:
    type: string
```

Correct:

```yaml
inputs:
  java-version:
    description: "Java version"
    required: false
    default: "21"
```

The `type` property is used in workflow input definitions such as `workflow_dispatch`. It is not a supported Custom Action input metadata field.

## Mistake 2: Using the wrong `find` option

Incorrect:

```bash
find target -name "*.jar" -f
```

Correct:

```bash
find target -type f -name "*.jar"
```

`-type f` tells `find` to select regular files.

## Mistake 3: Using a colon instead of an equals sign

Incorrect:

```bash
echo "jar_file: $Jar_File" >> "$GITHUB_OUTPUT"
```

Correct:

```bash
echo "jar_file=$Jar_File" >> "$GITHUB_OUTPUT"
```

The normal output format is `name=value`.

## Mistake 4: Declaring an output without mapping its value

Incomplete:

```yaml
outputs:
  jar_file:
    description: "Generated JAR"
```

Correct:

```yaml
outputs:
  jar_file:
    description: "Generated JAR"
    value: ${{ steps.build.outputs.jar_file }}
```

The mapping exposes the output created by the internal step.

## Mistake 5: Forgetting the internal step ID

Incorrect:

```yaml
- name: Return Build Output
  shell: bash
```

Correct:

```yaml
- name: Return Build Output
  id: build
  shell: bash
```

The ID lets other expressions reference the step.

## Mistake 6: Confusing inputs and outputs

```text
Input  = information going into an action
Output = information coming out of an action
```

Declaring an input does not automatically create an output with the same name.

## Mistake 7: Checking out the repository twice

If the workflow checks out the repository and the action checks it out again, the same work may be repeated unnecessarily.

Choose a clear ownership model and remember that a local action must be available before GitHub can load it.

## Mistake 8: Assuming `setup-java` installs Maven

`actions/setup-java` sets up Java. Its Maven cache option enables dependency caching; it does not turn the action into a general Maven installer.

The Maven Wrapper, `./mvnw`, is a project-provided script that invokes the configured Maven version.

## Mistake 9: Forgetting that the JAR search can return nothing

If `find` returns no matching file, `Jar_File` is empty.

Always inspect the build result and fail clearly when the expected artifact is missing.

## Mistake 10: Confusing step IDs

Inside the action:

```yaml
id: get-jar
```

In the workflow:

```yaml
id: java-build
```

These IDs belong to different scopes.

The action uses:

```yaml
steps.get-jar.outputs.jar_file
```

The workflow uses:

```yaml
steps.java-build.outputs.jar_file
```

---

# 24. Application Dockerfile vs Docker Action Dockerfile

Because you are learning Java, Docker, and CI/CD, this distinction matters.

## Application Dockerfile

Your Java application's Dockerfile packages your Spring Boot application into an application image.

```text
Spring Boot Source
       │
       ▼
     Maven
       │
       ▼
    JAR file
       │
       ▼
Application Dockerfile
       │
       ▼
Application Docker Image
       │
       ▼
Container running your app
```

Its purpose is to build an image that runs your application.

## Docker Action Dockerfile

A Docker Action's Dockerfile packages the environment used to execute the action's own logic.

```text
Action Source Code
       │
       ▼
Docker Action Dockerfile
       │
       ▼
Action Image
       │
       ▼
Container executes action logic
```

Its purpose is to provide the execution environment for the action.

These two Dockerfiles can be completely different because they solve different problems.

You do not need a Docker Action just because your Java application already has a Dockerfile. A Composite Action can run the existing Docker build commands, too.

---

# 25. The Complete Mental Model

Let's combine everything.

A developer pushes a Spring Boot application.

The workflow starts and calls the Java Build Action.

```text
Developer Push
      │
      ▼
GitHub Workflow
      │
      ▼
Checkout Repository
      │
      ▼
Java Build Custom Action
      │
      ├── Receive java-version input
      │
      ├── Set up Java
      │
      ├── Configure Maven Wrapper permission
      │
      ├── Build Spring Boot application
      │
      ├── Find generated JAR
      │
      └── Expose jar_file output
                 │
                 ▼
            Workflow
                 │
                 ▼
         Docker Build Action
                 │
                 ▼
          Docker Image
                 │
                 ▼
         Docker Push Action
                 │
                 ▼
             GHCR
```

The Custom Action itself has a simpler internal model:

```text
action.yml
    │
    ├── name
    │
    ├── description
    │
    ├── inputs
    │
    ├── outputs
    │
    └── runs
         │
         └── steps
              ├── uses
              ├── run
              ├── shell
              ├── env
              └── if
```

Every section has a purpose.

- `name` identifies the action.
- `description` explains the action.
- `inputs` defines the data it accepts.
- `outputs` defines the data it exposes.
- `runs` defines how it executes.
- `steps` contains the work.
- `uses` invokes another action.
- `run` executes a command.
- `shell` selects the shell for a run step.
- `env` provides environment variables.
- `if` controls whether a step runs.

---

# 26. Quick Reference Cheat Sheet

| Syntax | Meaning |
|---|---|
| `action.yml` | Defines a Custom Action |
| `name` | Action's name |
| `description` | Action's purpose |
| `inputs` | Declares data the action accepts |
| `with` | Supplies values when calling an action |
| `${{ inputs.name }}` | Reads an action input |
| `required: true` | Declares a required input |
| `default: "21"` | Defines a fallback value |
| `outputs` | Declares values exposed by the action |
| `$GITHUB_OUTPUT` | Special file for setting step outputs |
| `steps.build.outputs.jar_file` | Reads an internal step output |
| `value: ${{ steps.build.outputs.jar_file }}` | Maps a step output to an action output |
| `steps.java-build.outputs.jar_file` | Reads an action output in the workflow |
| `runs` | Describes the action's execution method |
| `using: composite` | Declares a Composite Action |
| `steps` | Lists the action's implementation steps |
| `uses` | Invokes another action |
| `run` | Executes a shell command |
| `shell: bash` | Runs the command using Bash |
| `env` | Sets environment variables |
| `if` | Adds a step condition |
| `runs-on` | Selects the runner for a workflow job |
| `./.github/actions/java-build` | Example path to a local action |

The most important flow to remember:

```text
INPUT:
Workflow → Custom Action

OUTPUT:
Custom Action → Workflow

ACTION-TO-ACTION:
Action A → Workflow → Action B
```

---

# 27. Practice Exercises

Complete these exercises in order. Each one builds on the previous exercise.

## Exercise 1 — Create a simple Composite Action

Create:

```text
.github/actions/hello-action/action.yml
```

The action should print:

```text
Hello from my first Custom Action!
```

Call it from a workflow.

**Goal:** Understand the action directory, `action.yml`, `runs`, `using: composite`, `steps`, `shell`, and `run`.

## Exercise 2 — Add an input

Add an input called `person-name`.

Pass a name from the workflow and print it inside the action.

**Goal:** Understand `inputs`, `with`, and `${{ inputs.person-name }}`.

## Exercise 3 — Add a default value

Add a `java-version` input with a default value of `"21"`.

Try calling the action with and without supplying a value.

**Goal:** Understand required inputs and defaults.

## Exercise 4 — Add an environment variable

Pass the `environment-name` input into an environment variable called `ENVIRONMENT_NAME`.

Print the variable from Bash.

**Goal:** Understand how inputs and environment variables work together.

## Exercise 5 — Add a condition

Make the action display different messages for `development` and `production`.

**Goal:** Understand Bash conditions and GitHub Actions step-level `if`.

## Exercise 6 — Create a step output

Create an internal step with:

```yaml
id: example
```

Write an output called `message` using `$GITHUB_OUTPUT`.

**Goal:** Understand how a step creates an output.

## Exercise 7 — Expose the action output

Map the internal step output to the action's top-level `outputs` section.

Read the output in the calling workflow.

**Goal:** Understand the complete output chain.

## Exercise 8 — Build your Spring Boot application

Use your Java project and Maven Wrapper to build the application and return the JAR path.

**Goal:** Combine inputs, existing actions, shell commands, outputs, and practical Maven automation.

## Exercise 9 — Connect two actions

Pass the JAR path from the Java Build Action to a second action that accepts a `jar-file` input.

**Goal:** Understand how workflows connect reusable actions.

## Exercise 10 — Add Docker image automation

Use a Custom Action to build a Docker image from your existing application Dockerfile.

Keep application image building separate from creating a Docker-based Custom Action.

**Goal:** Connect Java build automation with container image creation.

---

# Final Message

Custom Actions are not mysterious. They are reusable units of automation with a defined interface and an implementation.

Once you understand the following five ideas, the rest becomes much easier:

1. **Action metadata:** `action.yml` tells GitHub what the action is and how it runs.
2. **Inputs:** The caller supplies information to the action.
3. **Steps:** The action performs the work using existing actions and commands.
4. **Outputs:** The action exposes useful results to its caller.
5. **Composition:** A workflow connects multiple actions to create a complete CI/CD pipeline.

Do not memorize the YAML blindly. Follow the information:

```text
Who provides the value?
        ↓
Where does the action receive it?
        ↓
Which step uses it?
        ↓
What result is produced?
        ↓
How does the caller receive that result?
```

If you can answer these questions for every Custom Action you build, you are no longer just writing YAML. You are designing reusable CI/CD components — a useful skill on the path toward DevOps and Platform Engineering.
