# GitHub Actions Matrix Strategy — The Complete Guide

> From absolute beginner to advanced understanding, with simple explanations, visual diagrams, practical YAML workflows, and real-world CI/CD examples.

## Introduction

Imagine that you have developed a Spring Boot application.

You want to verify that it builds successfully with Java 21, Java 25, and Java 26. You also want to test it on Ubuntu and Windows.

You could write six separate jobs, but most of those jobs would contain almost identical instructions.

Wouldn't it be better to write the job once and tell GitHub Actions which combinations to test?

**That is exactly what Matrix Strategy helps you do.**

Matrix Strategy is a GitHub Actions feature that allows a job to run multiple times using different combinations of specified values.

This guide explains not only how to write matrix YAML, but also why Matrix Strategy exists, how combinations are generated, how execution is controlled, and how to use it in professional CI/CD pipelines.

---

# Table of Contents

- [1. The Problem Before Matrix Strategy](#1-the-problem-before-matrix-strategy)
- [2. What Is Matrix Strategy?](#2-what-is-matrix-strategy)
- [3. Your First Matrix](#3-your-first-matrix)
- [4. Understanding the Matrix Context](#4-understanding-the-matrix-context)
- [5. Multiple Matrix Dimensions](#5-multiple-matrix-dimensions)
- [6. Understanding Cartesian Product](#6-understanding-cartesian-product)
- [7. The Include Strategy](#7-the-include-strategy)
- [8. The Exclude Strategy](#8-the-exclude-strategy)
- [9. Fail-Fast](#9-fail-fast)
- [10. Max-Parallel](#10-max-parallel)
- [11. A Complete Matrix Workflow](#11-a-complete-matrix-workflow)
- [12. Matrix and Job Names](#12-matrix-and-job-names)
- [13. Matrix and Expressions](#13-matrix-and-expressions)
- [14. Matrix and Environment Variables](#14-matrix-and-environment-variables)
- [15. Matrix and Artifacts](#15-matrix-and-artifacts)
- [16. Matrix and Caching](#16-matrix-and-caching)
- [17. Matrix and Job Dependencies](#17-matrix-and-job-dependencies)
- [18. Matrix and Job Outputs](#18-matrix-and-job-outputs)
- [19. Static and Dynamic Matrices](#19-static-and-dynamic-matrices)
- [20. Matrix and Reusable Workflows](#20-matrix-and-reusable-workflows)
- [21. Matrix and Docker](#21-matrix-and-docker)
- [22. Matrix Explosion](#22-matrix-explosion)
- [23. Matrix vs Multiple Jobs](#23-matrix-vs-multiple-jobs)
- [24. Common Beginner Mistakes](#24-common-beginner-mistakes)
- [25. Best Practices](#25-best-practices)
- [26. Hands-On Exercises](#26-hands-on-exercises)
- [27. Quick Reference Cheat Sheet](#27-quick-reference-cheat-sheet)
- [28. Final Mental Model](#28-final-mental-model)

---

# 1. The Problem Before Matrix Strategy

## 1.1 Imagine you are testing a Spring Boot application

You have a Java application and want to build it using three Java versions:

- Java 21
- Java 25
- Java 26

Without Matrix Strategy, you might create three separate jobs.

```yaml
jobs:
  java-21:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      # Setup Java 21 and build

  java-25:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      # Setup Java 25 and build

  java-26:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      # Setup Java 26 and build
```

Notice something?

All three jobs perform nearly the same work. The primary difference is the Java version.

Now imagine testing those three Java versions on three operating systems.

You would have to maintain nine combinations.

The workflow would become longer, harder to maintain, and more likely to contain mistakes.

## 1.2 The problem of duplication

Suppose you want to add a new build step.

Without Matrix Strategy, you might have to add that step to every job.

If you forget one, the jobs no longer behave consistently.

This creates three problems:

1. **Duplication:** Similar YAML is repeated many times.
2. **Maintenance:** Changes must be made in multiple places.
3. **Consistency:** One job might accidentally behave differently from the others.

## 1.3 The solution

Instead of writing the same job repeatedly, define the values that should change.

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

GitHub Actions uses these values to create multiple executions of the same job definition.

```text
ONE JOB DEFINITION
        |
        v
     MATRIX
        |
        +------ Java 21
        |
        +------ Java 25
        |
        +------ Java 26
```

The steps remain the same, but the Java version changes.

**The central idea: Write the job once, define the variations, and let GitHub Actions generate the combinations.**

---

# 2. What Is Matrix Strategy?

## 2.1 Simple definition

Matrix Strategy allows one GitHub Actions job definition to run multiple times with different combinations of predefined values.

## 2.2 Technical definition

A matrix is a set of variables and values defined under `jobs.<job_id>.strategy.matrix`. GitHub Actions expands the matrix into job executions for the generated combinations, subject to the matrix configuration and applicable limits.

## 2.3 Understand it like a child

Imagine a teacher who gives the same exercise to three students.

The exercise remains the same, but each student works independently.

Matrix Strategy works similarly:

- The job definition is the exercise.
- The matrix values are the variations.
- Each generated combination is a separate job execution.
- The steps are the instructions each execution follows.

## 2.4 Normal job vs matrix job

A normal job:

```text
Job
 |
 +-- Checkout
 |
 +-- Build
 |
 +-- Test
```

A matrix job:

```text
             MATRIX
                |
       +--------+--------+
       |        |        |
       v        v        v
    Job 1    Job 2     Job 3
    Java 21  Java 25   Java 26
       |        |        |
       v        v        v
    Checkout Checkout  Checkout
       |        |        |
       v        v        v
     Build    Build     Build
       |        |        |
       v        v        v
      Test     Test      Test
```

The matrix multiplies job executions, not the number of steps in a single job.

---

# 3. Your First Matrix

Let's create a practical GitHub Actions workflow.

```yaml
name: First Matrix Workflow

on: push

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: [21, 25, 26]

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ matrix.java }}

      - name: Build Spring Boot Application
        run: |
          cd CICDDEMO
          ./mvnw clean package -DskipTests
```

## 3.1 Understand every important line

### `on: push`

The workflow runs when a push event triggers it, subject to any additional event configuration.

### `jobs`

Defines the jobs in the workflow.

### `test`

This is the job ID. It identifies the job definition.

### `runs-on`

```yaml
runs-on: ubuntu-latest
```

Specifies the runner environment used for each generated job execution.

### `strategy`

```yaml
strategy:
```

Configures how the job is executed, including the matrix.

### `matrix`

```yaml
matrix:
  java: [21, 25, 26]
```

Defines a matrix variable named `java` with three possible values.

### `java-version`

```yaml
java-version: ${{ matrix.java }}
```

Tells the setup action to use the Java version for the current matrix combination.

The executions use Java 21, Java 25, and Java 26.

### `./mvnw clean package -DskipTests`

This runs the Maven Wrapper to clean the previous build output and package the application while skipping test execution.

Remember that `-DskipTests` skips running tests; it does not necessarily skip compiling test source code.

Also, the Java versions and build tools must actually support your application. A matrix can request a version, but it cannot guarantee compatibility.

## 3.2 Expected matrix

```text
Execution 1 → Java 21
Execution 2 → Java 25
Execution 3 → Java 26
```

Total: **3 matrix combinations.**

---

# 4. Understanding the Matrix Context

This is one of the most important concepts.

Consider:

```yaml
matrix:
  java: [21, 25, 26]
```

You have defined a matrix variable named `java`.

GitHub Actions exposes the current combination through the `matrix` context.

```yaml
${{ matrix.java }}
```

During different executions, it resolves to different values.

| Execution | `matrix.java` |
|---|---:|
| First | 21 |
| Second | 25 |
| Third | 26 |

The expression doesn't always return 21. It returns the value belonging to the current execution.

## 4.1 Why do we need `${{ }}`?

GitHub Actions uses expressions to access dynamic values.

For example:

```yaml
java-version: ${{ matrix.java }}
```

The expression tells GitHub Actions to evaluate the matrix value and provide it to the action.

## 4.2 Matrix variables are not automatically environment variables

This:

```yaml
${{ matrix.java }}
```

accesses the matrix context.

This:

```yaml
env:
  JAVA_VERSION: ${{ matrix.java }}
```

creates an environment variable using the matrix value.

For example:

```yaml
- name: Display Java Version
  env:
    JAVA_VERSION: ${{ matrix.java }}
  run: echo "Java version is $JAVA_VERSION"
```

The matrix supplies the value, and the environment variable makes it available to the shell command.

---

# 5. Multiple Matrix Dimensions

A matrix can have multiple variables.

```yaml
strategy:
  matrix:
    os:
      - ubuntu-latest
      - windows-latest

    java:
      - 21
      - 25
      - 26
```

We now have two dimensions:

- `os`
- `java`

We can access them through:

```yaml
${{ matrix.os }}
${{ matrix.java }}
```

For example:

```yaml
runs-on: ${{ matrix.os }}
```

selects the runner.

And:

```yaml
java-version: ${{ matrix.java }}
```

selects the Java version.

## 5.1 Generated combinations

GitHub generates all combinations of the two dimensions.

| Job | Operating system | Java |
|---:|---|---:|
| 1 | Ubuntu | 21 |
| 2 | Ubuntu | 25 |
| 3 | Ubuntu | 26 |
| 4 | Windows | 21 |
| 5 | Windows | 25 |
| 6 | Windows | 26 |

The total is:

```text
2 operating systems × 3 Java versions = 6 combinations
```

## 5.2 Why this matters

One job definition can now test six environments without writing six separate job definitions.

This is useful when validating:

- Language and runtime compatibility.
- Operating system compatibility.
- Database compatibility.
- Framework compatibility.
- Different build configurations.

**Practical warning:** Linux and Windows use different shell commands. A Windows job generally needs Windows-compatible Maven Wrapper commands, such as `.\mvnw.cmd`, rather than assuming that Linux commands such as `chmod +x` and `./mvnw` will work unchanged.

---

# 6. Understanding Cartesian Product

The mathematical idea behind the standard matrix is called a Cartesian product.

The name sounds difficult, but the idea is simple.

Imagine you have two shirts:

```text
Blue
Black
```

And two pairs of trousers:

```text
Jeans
Chinos
```

If you want to try every possible combination, you get:

```text
Blue + Jeans
Blue + Chinos
Black + Jeans
Black + Chinos
```

Matrix Strategy works in the same way.

For example:

```yaml
matrix:
  os: [ubuntu-latest, windows-latest]
  java: [21, 25]
```

Produces:

```text
Ubuntu + Java 21
Ubuntu + Java 25
Windows + Java 21
Windows + Java 25
```

## 6.1 The formula

For a standard matrix with independent dimensions:

```text
Total combinations =
Number of values in dimension 1
× Number of values in dimension 2
× ...
× Number of values in the final dimension
```

For example:

```text
2 operating systems
× 3 Java versions
× 2 database versions
= 12 combinations
```

Exclusions can reduce the original combination count. Include entries can add properties or additional combinations.

---

# 7. The Include Strategy

## 7.1 Why do we need `include`?

Suppose your original matrix is:

```yaml
matrix:
  os: [ubuntu-latest, windows-latest]
  java: [21, 25, 26]
```

You have six combinations.

Now suppose you want to attach additional information to Ubuntu + Java 26:

```text
Ubuntu + Java 26 → environment: production
```

You can use `include`.

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    java: [21, 25, 26]

    include:
      - os: ubuntu-latest
        java: 26
        environment: production
```

Now the compatible combination can access:

```yaml
${{ matrix.environment }}
```

## 7.2 What happens?

The original combinations are:

```text
Ubuntu + Java 21
Ubuntu + Java 25
Ubuntu + Java 26
Windows + Java 21
Windows + Java 25
Windows + Java 26
```

After the include entry:

```text
Ubuntu + Java 21
Ubuntu + Java 25
Ubuntu + Java 26 + production
Windows + Java 21
Windows + Java 25
Windows + Java 26
```

The number of combinations remains six because the include entry attaches an additional property to an existing combination.

## 7.3 Can `include` add a new combination?

Yes.

```yaml
strategy:
  matrix:
    java: [21, 25]

    include:
      - java: 26
```

The original combinations are:

```text
Java 21
Java 25
```

The include entry adds Java 26 as an additional combination.

```text
Java 21
Java 25
Java 26
```

## 7.4 The important merging rule

GitHub attempts to merge include properties into original combinations without overwriting original matrix values.

If an include entry cannot be merged into any original combination without overwriting those original values, GitHub adds a new combination instead.

With multiple include entries, their order can affect how additional properties are merged.

**Remember:** `include` can add properties to compatible combinations or create additional combinations.

---

# 8. The Exclude Strategy

## 8.1 Why do we need `exclude`?

A standard matrix creates all combinations.

But perhaps some combinations are not supported or are unnecessary.

Consider:

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    java: [17, 21, 25]

    exclude:
      - os: windows-latest
        java: 25
```

The exclusion removes the Windows + Java 25 combination.

## 8.2 Before exclusion

```text
Ubuntu + Java 17
Ubuntu + Java 21
Ubuntu + Java 25

Windows + Java 17
Windows + Java 21
Windows + Java 25
```

Total: six combinations.

## 8.3 After exclusion

```text
Ubuntu + Java 17
Ubuntu + Java 21
Ubuntu + Java 25

Windows + Java 17
Windows + Java 21
```

Total: five combinations.

## 8.4 Excluding an entire dimension value

You can also write:

```yaml
exclude:
  - os: windows-latest
```

This removes every original combination whose `os` value is `windows-latest`.

For the previous matrix, all three Windows combinations are removed.

## 8.5 Include vs exclude

| Include | Exclude |
|---|---|
| Adds properties or additional combinations | Removes matching original combinations |
| Can increase the number of combinations | Reduces the original combination count |
| Useful for special configurations | Useful for invalid or unnecessary combinations |

---

# 9. Fail-Fast

## 9.1 The problem

Suppose your matrix tests three Java versions:

```text
Java 21 → PASS
Java 25 → FAIL
Java 26 → RUNNING
```

Should GitHub cancel the remaining in-progress and queued matrix jobs when one fails?

That is the problem `fail-fast` addresses.

## 9.2 Fail-fast enabled

```yaml
strategy:
  fail-fast: true
  matrix:
    java: [21, 25, 26]
```

`fail-fast` defaults to `true`.

When a matrix job fails, GitHub cancels in-progress and queued matrix jobs in that matrix group, subject to the workflow's execution state.

This can save time when the remaining tests are no longer useful.

## 9.3 Fail-fast disabled

```yaml
strategy:
  fail-fast: false
  matrix:
    java: [21, 25, 26]
```

Other matrix executions can continue even if one execution fails.

For example:

```text
Java 21 → PASS
Java 25 → FAIL
Java 26 → PASS
```

This is useful when you want to know the result for every supported Java version.

## 9.4 Important distinction

`fail-fast: false` does not ignore failures. A failed matrix job still fails.

It only changes the cancellation behavior of other matrix executions.

| Setting | Behavior |
|---|---|
| `true` | A matrix failure can cancel in-progress and queued matrix jobs |
| `false` | Other matrix jobs are allowed to continue |

---

# 10. Max-Parallel

## 10.1 Matrix size is not concurrency

Suppose your matrix generates six combinations.

That does not mean all six must run at the same time.

GitHub Actions schedules jobs according to runner availability, dependencies, concurrency limits, and other applicable constraints.

You can limit the number of simultaneous matrix jobs:

```yaml
strategy:
  max-parallel: 2
  matrix:
    java: [21, 25, 26]
```

This matrix has three combinations, but no more than two of its matrix jobs run concurrently.

## 10.2 Visual example

```text
Time ------------------------------------->

Job 1: [ Java 21 ]
Job 2: [ Java 25 ]

             Job 1 finishes

Job 3:       [ Java 26 ]
```

The actual execution order and timing depend on GitHub's scheduler.

## 10.3 Why limit parallelism?

- To reduce pressure on shared test environments.
- To avoid overwhelming external services.
- To control how many jobs execute simultaneously.
- To manage limited execution capacity.
- To keep large workflows under control.

Remember:

```text
Matrix = How many combinations exist

Max-parallel = How many matrix jobs can run simultaneously
```

---

# 11. A Complete Matrix Workflow

This example combines multiple dimensions, include, exclude, fail-fast, and max-parallel.

```yaml
name: Complete Matrix Demonstration

on: push

jobs:
  matrix-build:
    name: Build (${{ matrix.os }}, Java ${{ matrix.java }})

    runs-on: ${{ matrix.os }}

    strategy:
      fail-fast: false
      max-parallel: 2

      matrix:
        os: [ubuntu-latest, windows-latest]
        java: [17, 21, 25]

        exclude:
          - os: windows-latest
            java: 25

        include:
          - os: ubuntu-latest
            java: 25
            environment: production

    steps:
      - name: Display Matrix Information
        run: |
          echo "OS: ${{ matrix.os }}"
          echo "Java: ${{ matrix.java }}"
          echo "Environment: ${{ matrix.environment }}"

      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ matrix.java }}

      - name: Build on Ubuntu
        if: ${{ runner.os == 'Linux' }}
        run: |
          chmod +x CICDDEMO/mvnw
          cd CICDDEMO
          ./mvnw clean package -DskipTests

      - name: Build on Windows
        if: ${{ runner.os == 'Windows' }}
        shell: pwsh
        run: |
          cd CICDDEMO
          .\mvnw.cmd clean package -DskipTests
```

## 11.1 How many combinations?

The original matrix creates:

```text
2 operating systems × 3 Java versions = 6 combinations
```

Exclude removes Windows + Java 25.

Five combinations remain.

The include entry attaches `environment: production` to Ubuntu + Java 25, which already exists.

Therefore:

```text
Original combinations: 6
Excluded combinations: 1
Additional combinations: 0

Final total: 5
```

The workflow also:

- Displays matrix information.
- Sets up the selected Java version.
- Uses platform-specific Maven Wrapper commands.
- Limits concurrent matrix jobs to two.
- Allows other matrix jobs to continue after one fails.

The example assumes the selected Java versions are supported by the project and build tools.

---

# 12. Matrix and Job Names

When a matrix creates many executions, identifying the failed combination becomes important.

You can use:

```yaml
name: Java ${{ matrix.java }} on ${{ matrix.os }}
```

GitHub can display names such as:

```text
Java 21 on ubuntu-latest
Java 25 on ubuntu-latest
Java 21 on windows-latest
```

Meaningful job names help you:

- Find failed combinations quickly.
- Read workflow logs.
- Compare different runtime results.
- Understand which environment caused a failure.

---

# 13. Matrix and Expressions

Matrix values can be used with GitHub Actions expressions.

## 13.1 Use the selected operating system

```yaml
runs-on: ${{ matrix.os }}
```

## 13.2 Use the selected Java version

```yaml
java-version: ${{ matrix.java }}
```

## 13.3 Use conditional execution

```yaml
- name: Ubuntu-only step
  if: ${{ matrix.os == 'ubuntu-latest' }}
  run: echo "This step runs only for Ubuntu combinations."
```

## 13.4 Display multiple values

```yaml
- name: Show Configuration
  run: |
    echo "OS: ${{ matrix.os }}"
    echo "Java: ${{ matrix.java }}"
```

The expression is evaluated for each matrix execution.

This is how Matrix Strategy connects to other GitHub Actions concepts, including contexts, expressions, conditions, and environment variables.

---

# 14. Matrix and Environment Variables

Matrix values and environment variables serve different purposes.

Consider:

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

The matrix variable is accessed through:

```yaml
${{ matrix.java }}
```

You can pass that value into an environment variable:

```yaml
- name: Display Java Version
  env:
    JAVA_VERSION: ${{ matrix.java }}
  run: echo "Java version is $JAVA_VERSION"
```

The distinction is:

- **Matrix variable:** Identifies the value for the current combination.
- **Environment variable:** Passes a value into the environment of a step, job, or workflow, depending on its scope.

Environment variables are useful when a command or application expects a particular variable name.

---

# 15. Matrix and Artifacts

Suppose each matrix execution creates a JAR file.

You want the uploaded artifacts to be easy to identify.

```yaml
- name: Upload Build Artifact
  uses: actions/upload-artifact@v4
  with:
    name: spring-boot-java-${{ matrix.java }}
    path: CICDDEMO/target/*.jar
```

The resulting artifact names could be:

```text
spring-boot-java-21
spring-boot-java-25
spring-boot-java-26
```

This helps you identify which matrix execution produced each artifact.

**Important:** The specified path must match files produced by the build. If the files do not exist, the upload action cannot upload them.

Artifacts can also be downloaded by later jobs when you explicitly configure artifact transfer.

---

# 16. Matrix and Caching

Maven downloads dependencies into its local repository.

Caching can help avoid downloading the same dependencies repeatedly.

Example cache key:

```yaml
key: maven-${{ runner.os }}-java-${{ matrix.java }}-${{ hashFiles('**/pom.xml') }}
```

This key includes:

- The operating system.
- The Java version.
- A hash derived from matching Maven project files.

The key helps identify the appropriate cache.

Matrix values can be used to separate cache entries when the environment matters.

However, do not automatically create a separate cache for every matrix variable. Choose a key based on what actually affects the cached content.

For example, if the dependencies do not differ by Java version, separating caches by Java version may create unnecessary duplication.

---

# 17. Matrix and Job Dependencies

Matrix jobs can participate in normal GitHub Actions job dependencies using `needs`.

Consider:

```text
Build
  |
  v
Matrix Tests
  |
  v
Deploy
```

Example:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Build completed"

  test:
    needs: build
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: [21, 25, 26]

    steps:
      - run: echo "Testing Java ${{ matrix.java }}"

  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying application"
```

The matrix test job depends on the build job.

The deploy job depends on the matrix test job. By default, the deploy job does not run if a required job fails or is skipped.

In this example, deployment normally waits for the required matrix executions to finish successfully.

For real deployment workflows, ensure the build artifacts, deployment credentials, environments, and release conditions are configured correctly.

---

# 18. Matrix and Job Outputs

Outputs allow jobs to pass values to dependent jobs.

For example, a normal job can write an output to `$GITHUB_OUTPUT`, and a dependent job can read it through `needs`.

Matrix outputs require additional care.

A matrix creates several executions of the same job definition, so you should not assume that each execution automatically produces a separately addressable output under the same output name.

If multiple matrix executions set the same output name, their results can overwrite or otherwise produce nondeterministic final values depending on completion order.

For reliable designs:

- Use artifacts to transfer files.
- Use unique identifiers for data associated with separate matrix executions.
- Use JSON and dynamic matrices when passing structured configuration between jobs.
- Avoid relying on one shared matrix output to preserve every individual result.

The key idea is that matrix executions are separate jobs, but their outputs do not automatically become a convenient indexed collection.

---

# 19. Static and Dynamic Matrices

## 19.1 Static Matrix

A static matrix is written directly in the workflow.

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

The values are known from the YAML configuration.

This is the easiest matrix to learn and maintain.

## 19.2 Dynamic Matrix

A dynamic matrix is generated from data produced at runtime.

Imagine:

```text
Job A
  |
  | Produces a JSON list
  v
Job B
  |
  | Reads the JSON list
  v
Dynamic Matrix
  |
  +--- Combination 1
  +--- Combination 2
  +--- Combination 3
```

For example, one job might discover a list of modules that need testing. A later job can turn that list into a matrix.

GitHub Actions expressions include `fromJSON()`, which converts a JSON string into a value that can be used in workflow expressions.

Conceptually:

```yaml
strategy:
  matrix: ${{ fromJSON(needs.prepare.outputs.matrix) }}
```

This requires a preceding job named `prepare` that defines an output named `matrix`, along with the appropriate job dependency.

Dynamic matrices are useful when combinations are not known until the workflow runs.

---

# 20. Matrix and Reusable Workflows

A reusable workflow allows you to reuse a workflow definition from another workflow.

Matrix Strategy can be used with reusable workflows to execute the same reusable workflow with different input values.

Conceptually:

```text
Calling Workflow
      |
      v
Matrix Combinations
      |
      +--- Reusable Workflow with Java 21
      |
      +--- Reusable Workflow with Java 25
      |
      +--- Reusable Workflow with Java 26
```

This can help large CI/CD systems avoid duplicating entire workflow definitions.

Remember the difference:

- Matrix Strategy varies values across executions.
- Reusable workflows reuse workflow logic.
- They can work together.

---

# 21. Matrix and Docker

Matrix Strategy is not limited to programming language versions.

It can also be used to test different container configurations.

For example:

```yaml
strategy:
  matrix:
    java: [21, 25]
    base-image: [ubuntu, alpine]
```

This creates combinations of Java versions and base-image choices.

Other possible dimensions include:

- Docker image variants.
- CPU architectures such as `amd64` and `arm64`.
- Database versions.
- Dependency versions.
- Framework versions.
- Browser versions.

For architecture-specific builds, the workflow must also configure the appropriate build tooling and runner capabilities. Adding `arm64` to a matrix does not automatically provide an ARM runner or configure a cross-platform Docker build.

---

# 22. Matrix Explosion

One of the biggest mistakes is creating too many combinations.

Imagine this matrix:

```text
5 operating systems
× 4 Java versions
× 3 database versions
× 2 architectures
```

The result is:

```text
5 × 4 × 3 × 2 = 120 combinations
```

That means 120 matrix job executions before exclusions.

## Why is this a problem?

- More runner capacity is required.
- More jobs may increase workflow duration.
- External test services may be overloaded.
- The workflow may become harder to debug.
- Additional execution can increase cost where applicable.

## How can you control it?

- Remove unnecessary combinations with `exclude`.
- Add only useful configurations.
- Use `max-parallel` to limit concurrency.
- Choose representative combinations when exhaustive testing is unnecessary.
- Use dynamic matrices when the test set depends on discovered data.

The objective is not to create the largest possible matrix. The objective is to create a matrix that gives meaningful test coverage.

---

# 23. Matrix vs Multiple Jobs

| Multiple manual jobs | Matrix Strategy |
|---|---|
| Each job is defined separately | One job definition describes multiple variations |
| Repeated YAML can accumulate | Reduces duplicated configuration |
| Every job can have completely different steps | Best when the core steps are similar |
| Each job can be configured independently | Matrix values can control differences |
| Useful for fundamentally different tasks | Useful for variations of the same task |

Matrix Strategy is not always the correct choice.

If three jobs perform completely different operations, separate job definitions may be clearer.

If the same build or test process needs to run with different values, Matrix Strategy is often a better fit.

---

# 24. Common Beginner Mistakes

## Mistake 1 — Thinking Matrix Creates Multiple Workflows

A matrix does not create multiple workflow files. It generates multiple job executions within the workflow run.

## Mistake 2 — Forgetting the Expression Syntax

Incorrect:

```yaml
java-version: matrix.java
```

Correct:

```yaml
java-version: ${{ matrix.java }}
```

## Mistake 3 — Thinking All Combinations Run Simultaneously

Matrix defines combinations, not guaranteed concurrency.

Use `max-parallel` to limit concurrent matrix executions.

## Mistake 4 — Confusing Include and Exclude

- `include` adds compatible properties or additional combinations.
- `exclude` removes matching original matrix combinations.

## Mistake 5 — Assuming Files Are Shared Between Jobs

Each matrix execution has its own job environment.

Use artifacts or another explicit transfer mechanism to share files.

## Mistake 6 — Ignoring Operating System Differences

Linux commands such as:

```bash
chmod +x ./mvnw
./mvnw clean package
```

should not be copied blindly into a Windows job.

Windows can use the Maven Wrapper command:

```powershell
.\mvnw.cmd clean package
```

Choose the shell and command that match the selected runner.

## Mistake 7 — Assuming Failures Are Ignored

`fail-fast: false` does not turn a failure into a success. It allows other matrix executions to continue.

## Mistake 8 — Creating Unnecessary Combinations

A larger matrix is not automatically a better matrix.

Every combination should have a testing or operational purpose.

## Mistake 9 — Reusing Artifact Names Carelessly

Give artifacts unique names when separate matrix executions need to upload separate results.

## Mistake 10 — Assuming a Matrix Value Guarantees Compatibility

A matrix can request a Java version or operating system, but your application, dependencies, build tools, and runner must still support that configuration.

---

# 25. Best Practices

1. Start with a single matrix variable.
2. Add dimensions only when they provide meaningful test coverage.
3. Use clear job names containing relevant matrix values.
4. Use `include` for additional configuration or deliberate extra combinations.
5. Use `exclude` to remove invalid or unnecessary combinations.
6. Use `fail-fast` according to whether you need early cancellation or complete test results.
7. Use `max-parallel` when concurrency needs to be controlled.
8. Make commands compatible with the selected operating system.
9. Give per-matrix artifacts unique names.
10. Use cache keys that reflect the actual differences in cached content.
11. Treat matrix jobs as independent execution environments.
12. Use reusable workflows when you need to reuse more than a few steps.
13. Use dynamic matrices when the combination list must be generated at runtime.
14. Keep the workflow understandable and maintainable.

---

# 26. Hands-On Exercises

Use your Spring Boot Maven project to practice the following.

### Exercise 1 — Single Matrix

Test Java 21, 25, and 26 on Ubuntu.

Expected combinations: **3**.

### Exercise 2 — Multiple Dimensions

Add Windows.

Expected combinations: **6**.

### Exercise 3 — Exclude

Remove Windows + Java 25.

Expected combinations: **5**.

### Exercise 4 — Include

Attach `environment: production` to Ubuntu + Java 25.

Expected combinations: **5**, because this property is added to an existing combination.

### Exercise 5 — Fail-Fast

Set:

```yaml
fail-fast: false
```

Observe what happens if one matrix job fails.

### Exercise 6 — Max-Parallel

Set:

```yaml
max-parallel: 2
```

Observe how the workflow limits concurrent matrix executions.

### Exercise 7 — Unique Artifacts

Upload the JAR with an artifact name containing `${{ matrix.java }}`.

Expected result: Separately named artifacts for successful executions whose build output exists.

### Exercise 8 — Display the Matrix Values

Print the operating system and Java version in every matrix execution.

### Exercise 9 — Dynamic Matrix

Create a preparation job that produces a JSON list of Java versions. Pass the output to a dependent job and use `fromJSON()` to generate the matrix.

### Exercise 10 — Matrix Explosion

Create a matrix with three operating systems, three Java versions, and two database versions.

Calculate the number of combinations before writing the workflow.

Expected result:

```text
3 × 3 × 2 = 18 combinations
```

---

# 27. Quick Reference Cheat Sheet

## Basic matrix

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

## Multiple dimensions

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    java: [21, 25, 26]
```

## Access matrix values

```yaml
runs-on: ${{ matrix.os }}
```

```yaml
java-version: ${{ matrix.java }}
```

## Include

```yaml
include:
  - os: ubuntu-latest
    java: 25
    environment: production
```

## Exclude

```yaml
exclude:
  - os: windows-latest
    java: 25
```

## Fail-fast

```yaml
fail-fast: false
```

Allows other matrix executions to continue after a matrix job fails.

## Max-parallel

```yaml
max-parallel: 2
```

Limits the number of matrix jobs that can run simultaneously.

## Dynamic matrix

```yaml
matrix: ${{ fromJSON(needs.prepare.outputs.matrix) }}
```

Uses JSON produced by a dependent job to define the matrix.

The preparation job must define the corresponding output.

---

# 28. Final Mental Model

Without Matrix Strategy:

```text
Write Job 1
    |
Write Job 2
    |
Write Job 3
    |
Write Job 4
    |
Repeated configuration
```

With Matrix Strategy:

```text
Write ONE job definition
          |
          v
Define matrix dimensions
          |
          v
GitHub generates combinations
          |
          v
Each combination becomes a job execution
          |
          v
Each execution receives its own matrix values
          |
          v
The same steps run with different values
```

The advanced controls:

```text
matrix
  = Defines the dimensions and values

include
  = Adds properties or additional combinations

exclude
  = Removes matching original combinations

fail-fast
  = Controls cancellation after a matrix failure

max-parallel
  = Limits concurrent matrix executions
```

The most important lesson is this:

**Matrix Strategy separates the job's logic from the values that vary between executions.**

Instead of manually repeating similar jobs, you describe the variations once and let GitHub Actions generate the combinations.

That is why Matrix Strategy is a fundamental feature for compatibility testing, automated builds, and professional CI/CD pipelines.

---

## Official Documentation

- [Running variations of jobs in a workflow](https://docs.github.com/en/actions/how-tos/writing-workflows/choosing-what-your-workflow-does/running-variations-of-jobs-in-a-workflow)
- [GitHub Actions workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [GitHub Actions contexts](https://docs.github.com/en/actions/reference/workflows-and-actions/contexts)
- [Storing and sharing workflow artifacts](https://docs.github.com/en/actions/tutorials/store-and-share-data)
