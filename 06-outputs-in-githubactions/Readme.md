# GitHub Actions Matrix Strategy — From Zero to Complete Understanding

> **The goal of this repository is simple:** Understand GitHub Actions Matrix Strategy so deeply that you never need to memorize it blindly again.

This repository contains four practical GitHub Actions workflows demonstrating how Matrix Strategy works, why it exists, how GitHub generates job combinations, and how to control those combinations.

The examples use a Spring Boot application built with Maven and multiple Java versions.

---

## Table of Contents

1. [The Problem Before Matrix Strategy](#1-the-problem-before-matrix-strategy)
2. [What Is Matrix Strategy?](#2-what-is-matrix-strategy)
3. [Your First Matrix](#3-your-first-matrix)
4. [How Matrix Variables Work](#4-how-matrix-variables-work)
5. [Multiple Matrix Dimensions](#5-multiple-matrix-dimensions)
6. [Understanding Cartesian Product](#6-understanding-cartesian-product)
7. [The Include Strategy](#7-the-include-strategy)
8. [The Exclude Strategy](#8-the-exclude-strategy)
9. [Fail-Fast](#9-fail-fast)
10. [Max-Parallel](#10-max-parallel)
11. [Include vs Exclude vs Fail-Fast vs Max-Parallel](#11-include-vs-exclude-vs-fail-fast-vs-max-parallel)
12. [Understanding the Four Practice Workflows](#12-understanding-the-four-practice-workflows)
13. [Matrix Execution and Independent Runners](#13-matrix-execution-and-independent-runners)
14. [Matrix and Expressions](#14-matrix-and-expressions)
15. [Matrix and Artifacts](#15-matrix-and-artifacts)
16. [Matrix and Caching](#16-matrix-and-caching)
17. [Common Beginner Mistakes](#17-common-beginner-mistakes)
18. [Best Practices](#18-best-practices)
19. [Practice Exercises](#19-practice-exercises)
20. [Final Mental Model](#20-final-mental-model)

---

# 1. The Problem Before Matrix Strategy

Imagine that you have developed a Spring Boot application.

You want to check whether it builds successfully with:

- Java 21
- Java 25
- Java 26

You also want to verify it on:

- Ubuntu
- Windows

Why would you do this?

Because an application that works in one environment does not automatically prove that it will work in every other environment.

A different Java version might expose a compatibility issue. A different operating system might introduce differences in commands, file paths, permissions, or installed tools.

You want to test these combinations automatically whenever code is pushed to GitHub.

## 1.1 The manual approach

Without Matrix Strategy, you might create separate jobs:

```yaml
jobs:
  java-21:
    runs-on: ubuntu-latest
    # Checkout, setup Java 21, and build

  java-25:
    runs-on: ubuntu-latest
    # Checkout, setup Java 25, and build

  java-26:
    runs-on: ubuntu-latest
    # Checkout, setup Java 26, and build
```

Now imagine adding Windows and macOS.

You would need more jobs, and each would repeat much of the same configuration.

This creates three problems:

1. **Duplication:** You write nearly identical YAML repeatedly.
2. **Maintenance:** Changing the build steps means updating multiple jobs.
3. **Human error:** You might update one job but accidentally forget another.

## 1.2 The solution

Instead of writing the same job repeatedly, describe the values that should change.

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

GitHub Actions uses those values to generate multiple executions of the same job definition.

**The important idea: Write the job once; let GitHub generate the variations.**

---

# 2. What Is Matrix Strategy?

Matrix Strategy is a feature of GitHub Actions that allows a job to run multiple times with different combinations of specified values.

For example:

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

Conceptually, GitHub creates:

```text
Java 21 → Job execution 1
Java 25 → Job execution 2
Java 26 → Job execution 3
```

The steps are defined once, but each execution receives a different Java version.

## 2.1 Understand it like a child

Imagine a teacher who wants three students to solve the same mathematics exercise.

Instead of writing three different exercises, the teacher writes one exercise and gives it to each student.

The exercise remains the same, but each student works independently.

Matrix Strategy works similarly:

- The job definition is the exercise.
- The matrix values are the variations.
- Each matrix execution is an independent attempt.

## 2.2 What Matrix Strategy does and does not do

| Matrix Strategy does | Matrix Strategy does not |
|---|---|
| Generate job executions from combinations | Automatically create separate workflow files |
| Supply different values to each execution | Guarantee that every job runs simultaneously |
| Reduce repeated YAML definitions | Automatically share files between executions |
| Support compatibility testing | Guarantee that every combination is valid |
| Work with expressions and other Actions features | Replace the need for testing and debugging |

---

# 3. Your First Matrix

Consider this workflow:

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

Let's understand the important parts.

### `jobs`

Defines the jobs that belong to the workflow.

### `test`

The name of the job definition.

### `runs-on`

```yaml
runs-on: ubuntu-latest
```

Specifies the runner environment for each generated job execution.

### `strategy`

```yaml
strategy:
```

Configures how the job should be executed, including its matrix and related settings.

### `matrix`

```yaml
matrix:
  java: [21, 25, 26]
```

Defines a matrix variable called `java` with three possible values.

### `java-version`

```yaml
java-version: ${{ matrix.java }}
```

Uses the value associated with the current matrix execution.

The resulting executions use Java 21, Java 25, and Java 26, respectively.

**Important:** These versions must be supported by the selected action and runner environment, and your application and build tooling must be compatible with them.

---

# 4. How Matrix Variables Work

Consider:

```yaml
matrix:
  java: [21, 25, 26]
```

You have defined a matrix variable named `java`.

GitHub exposes the current combination's value through the `matrix` context.

For example:

```yaml
${{ matrix.java }}
```

The expression resolves to the current execution's value.

| Execution | `matrix.java` |
|---|---:|
| First | 21 |
| Second | 25 |
| Third | 26 |

The value is not always 21. It depends on which matrix combination is being executed.

## 4.1 Why use `${{ }}`?

GitHub Actions expressions use the `${{ ... }}` syntax in many workflow fields.

For example:

```yaml
java-version: ${{ matrix.java }}
```

This tells GitHub Actions to evaluate the expression and provide its value.

Without the expression syntax, the field would not be interpreted in the same way.

## 4.2 Matrix variables are not ordinary environment variables

These are different concepts:

```yaml
${{ matrix.java }}
```

Accesses the matrix context.

Meanwhile:

```yaml
env:
  JAVA_VERSION: ${{ matrix.java }}
```

creates an environment variable named `JAVA_VERSION` for the applicable scope.

You can then use it in a shell command:

```yaml
- name: Display Java Version
  env:
    JAVA_VERSION: ${{ matrix.java }}
  run: echo "Java version is $JAVA_VERSION"
```

The matrix provides the value; the environment variable passes that value to the process.

---

# 5. Multiple Matrix Dimensions

A matrix becomes more powerful when multiple variables are defined.

Consider:

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

There are now two dimensions:

- `os`
- `java`

You can access them using:

```yaml
${{ matrix.os }}
${{ matrix.java }}
```

The operating system can be used here:

```yaml
runs-on: ${{ matrix.os }}
```

The Java version can be used here:

```yaml
java-version: ${{ matrix.java }}
```

GitHub generates every combination of the two dimensions.

| Job | Operating system | Java |
|---:|---|---:|
| 1 | Ubuntu | 21 |
| 2 | Ubuntu | 25 |
| 3 | Ubuntu | 26 |
| 4 | Windows | 21 |
| 5 | Windows | 25 |
| 6 | Windows | 26 |

Therefore:

**2 operating systems × 3 Java versions = 6 combinations.**

---

# 6. Understanding Cartesian Product

The mathematical idea behind a standard matrix is called a Cartesian product.

Do not worry about the name. The idea is simple.

Suppose you have two shirts:

```text
Blue
Black
```

And two trousers:

```text
Jeans
Chinos
```

If you want to try every possible outfit, you combine each shirt with each pair of trousers.

You get:

```text
Blue + Jeans
Blue + Chinos
Black + Jeans
Black + Chinos
```

Matrix Strategy does something similar with configuration values.

For example:

```yaml
matrix:
  os: [ubuntu-latest, windows-latest]
  java: [21, 25]
```

The combinations are:

```text
Ubuntu + Java 21
Ubuntu + Java 25
Windows + Java 21
Windows + Java 25
```

The total number of combinations is:

\[
N = n_1 \times n_2 \times \cdots \times n_k
\]

Where each \(n\) represents the number of values in a matrix dimension.

For three operating systems, four Java versions, and two database versions:

\[
3 \times 4 \times 2 = 24
\]

A standard matrix would generate 24 combinations before exclusions are applied.

This is why adding matrix dimensions can increase the number of jobs very quickly.

---

# 7. The Include Strategy

## 7.1 Why do we need `include`?

Suppose your original matrix is:

```yaml
matrix:
  os: [ubuntu-latest, windows-latest]
  java: [21, 25, 26]
```

You get six combinations.

Now imagine that you want to attach extra information to one combination.

For example:

```text
Ubuntu + Java 26 → environment = production
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

The additional `environment` property is available through:

```yaml
${{ matrix.environment }}
```

## 7.2 What actually happens?

The original combinations remain the starting point.

The `include` entry can add properties to compatible combinations or introduce additional combinations when it cannot be applied without overwriting existing original matrix values.

For this example, `environment: production` can be attached to the Ubuntu + Java 26 combination.

Conceptually:

```text
Ubuntu + Java 21
Ubuntu + Java 25
Ubuntu + Java 26 + production
Windows + Java 21
Windows + Java 25
Windows + Java 26
```

There are still six combinations.

## 7.3 Can `include` create a new combination?

Yes.

Consider:

```yaml
strategy:
  matrix:
    java: [21, 25]

    include:
      - java: 26
```

The original matrix contains Java 21 and Java 25.

The include entry introduces Java 26 as an additional combination.

Conceptually:

```text
Java 21
Java 25
Java 26
```

There are now three combinations.

## 7.4 An important rule

`include` is not simply an unconditional command to append a row. GitHub attempts to merge include properties into compatible original combinations without overwriting original matrix values. If an include entry cannot be merged into an original combination, GitHub adds an additional combination.

When several include entries are used, their order can affect how properties are merged.

**Remember:** `include` can enrich existing combinations or add new ones.

---

# 8. The Exclude Strategy

## 8.1 Why do we need `exclude`?

A normal matrix generates every combination.

But sometimes a combination is unnecessary, unsupported, or invalid.

Suppose you have:

```yaml
matrix:
  os: [ubuntu-latest, windows-latest]
  java: [17, 21, 25]
```

GitHub generates six combinations.

But imagine that you do not want Windows + Java 25.

You can remove that combination with `exclude`.

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    java: [17, 21, 25]

    exclude:
      - os: windows-latest
        java: 25
```

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

Only the specified combination is removed.

## 8.4 What if you exclude a single property?

An exclusion entry can match multiple combinations.

For example:

```yaml
exclude:
  - os: windows-latest
```

This excludes every original combination using `windows-latest`.

With the previous matrix, that would remove three combinations.

You can also exclude several specific combinations by adding more entries.

**Remember:** `exclude` removes matching combinations from the matrix generated by the original dimensions. If you use `include` as well, understand how it interacts with the resulting matrix.

---

# 9. Fail-Fast

## 9.1 The problem

Suppose a matrix runs three Java versions:

```text
Java 21 → PASS
Java 25 → FAIL
Java 26 → RUNNING
```

What should happen after one combination fails?

Should GitHub cancel other in-progress matrix jobs, or should they continue so you can see all the results?

This is the problem addressed by `fail-fast`.

## 9.2 Fail-fast enabled

```yaml
strategy:
  fail-fast: true
  matrix:
    java: [21, 25, 26]
```

`fail-fast` defaults to `true`.

When a matrix job fails, GitHub cancels in-progress and queued matrix jobs for that matrix job group, subject to the workflow's execution state.

This can save time and resources when one failure makes the remaining tests less useful.

However, it does not mean that every other job in the entire workflow is automatically cancelled.

## 9.3 Fail-fast disabled

```yaml
strategy:
  fail-fast: false
  matrix:
    java: [21, 25, 26]
```

Now other matrix executions can continue even if one fails.

For example:

```text
Java 21 → PASS
Java 25 → FAIL
Java 26 → PASS
```

This is useful when you want to collect results from every supported version.

## 9.4 When should you use each?

| Setting | Useful when |
|---|---|
| `fail-fast: true` | You want to stop unnecessary matrix work after a failure |
| `fail-fast: false` | You want results from all matrix combinations |

**Important:** `fail-fast` controls cancellation behavior, not the order in which matrix jobs start.

---

# 10. Max-Parallel

## 10.1 Matrix size and concurrency are different

Suppose your matrix creates six combinations.

That does not mean all six must execute simultaneously.

GitHub Actions can run jobs concurrently depending on available runners, dependencies, and applicable limits.

`max-parallel` lets you set a limit on how many matrix jobs can run simultaneously.

```yaml
strategy:
  max-parallel: 2

  matrix:
    java: [21, 25, 26]
```

This matrix has three combinations, but at most two matrix jobs run at the same time.

## 10.2 Visual example

Imagine these three jobs:

```text
Java 21
Java 25
Java 26
```

With `max-parallel: 2`, the execution might look like:

```text
Time ───────────────────────────────→

Job 1: [ Java 21 ]
Job 2: [ Java 25 ]

        Job 1 finishes

Job 3: [ Java 26 ]
```

The exact scheduling and durations depend on the runners and jobs.

## 10.3 Why limit concurrency?

- To control the number of jobs running at once.
- To reduce pressure on external services.
- To avoid overwhelming shared test environments.
- To manage workloads and execution capacity.
- To make large matrices easier to control.

`max-parallel` does not change the total number of combinations. It limits concurrent matrix execution.

---

# 11. Include vs Exclude vs Fail-Fast vs Max-Parallel

| Feature | Main purpose | Changes combinations? |
|---|---|---|
| `include` | Adds properties to compatible combinations or adds combinations | Potentially |
| `exclude` | Removes matching original matrix combinations | Yes |
| `fail-fast` | Controls cancellation after a matrix job fails | No |
| `max-parallel` | Limits simultaneous matrix job executions | No |

Think of the four features this way:

- **Include:** Add or attach information.
- **Exclude:** Remove combinations.
- **Fail-fast:** Decide what happens after a failure.
- **Max-parallel:** Decide how many matrix jobs may run concurrently.

These features solve four different problems.

---

# 12. Understanding the Four Practice Workflows

This repository contains four workflows that demonstrate Matrix Strategy in a practical Spring Boot Maven project.

## Workflow 1 — Single Matrix

**Purpose:** Understand the foundation.

```yaml
strategy:
  matrix:
    java: [21, 25, 26]
```

This creates three Java-version combinations on Ubuntu.

It teaches:

- How to define a matrix.
- How to access `matrix.java`.
- How one job definition runs with different values.
- How to build the same application against multiple Java versions.

## Workflow 2 — Multiple Matrix Dimensions

**Purpose:** Understand combinations across multiple dimensions.

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    java: [21, 25, 26]
```

This creates six combinations.

It teaches:

- How to define multiple matrix variables.
- How to access `matrix.os` and `matrix.java`.
- How the Cartesian product works.
- How the selected runner changes between executions.

**Windows-specific note:** The Maven Wrapper command and permission-handling steps may need to differ between Windows and Ubuntu. Linux commands such as `chmod` and `./mvnw` should not be copied blindly into a Windows runner. Use platform-appropriate commands or explicitly run the build through a compatible shell.

## Workflow 3 — Include

**Purpose:** Understand how additional properties can be attached to a matrix combination.

```yaml
include:
  - os: ubuntu-latest
    java: 26
    environment: production
```

This demonstrates how additional matrix information can be exposed to steps through `matrix.environment`.

To display it reliably in a shell step, use:

```yaml
- name: Display Matrix Information
  run: |
    echo "OS: ${{ matrix.os }}"
    echo "Java: ${{ matrix.java }}"
    echo "Environment: ${{ matrix.environment }}"
```

If the `environment` property exists only on the Ubuntu + Java 26 combination, other combinations may not have a value for it.

## Workflow 4 — Exclude, Fail-Fast, and Max-Parallel

**Purpose:** Learn how to control the matrix.

```yaml
strategy:
  fail-fast: false
  max-parallel: 2

  matrix:
    java: [17, 21, 25]
    os: [windows-latest, ubuntu-latest]

    exclude:
      - os: windows-latest
        java: 25
```

The original matrix contains six combinations.

The exclusion removes one:

```text
Windows + Java 25
```

Five combinations remain.

Meanwhile:

- `fail-fast: false` allows the remaining matrix executions to continue after a failure.
- `max-parallel: 2` limits concurrent matrix executions to two.

The exclusion changes the combination count; the other two properties control execution behavior.

---

# 13. Matrix Execution and Independent Runners

A very important concept: matrix executions are separate job executions.

Imagine two combinations:

```text
Ubuntu + Java 21
Windows + Java 21
```

Each job execution has its own runner environment.

That means:

- Each execution checks out the repository independently.
- Each runner has its own filesystem.
- Files created by one execution are not automatically available to another.
- Installed dependencies and local state are not automatically shared.
- Each execution runs its own steps.

Think of two separate computers working on the same assignment. They receive the same instructions, but they do not automatically share their desks, files, or installed tools.

## How can jobs share files?

Use artifacts when one job needs to make files available to another.

For example, a build job might upload a JAR file, and a later job might download it.

## How can jobs share values?

Use job outputs when one job needs to pass a small value to another job.

Matrix outputs require additional care because several executions share the same job definition. Do not assume that every matrix execution produces one independently addressable output under the same output name.

---

# 14. Matrix and Expressions

Matrix Strategy works naturally with GitHub Actions expressions.

For example:

```yaml
runs-on: ${{ matrix.os }}
```

Selects the runner for the current combination.

```yaml
java-version: ${{ matrix.java }}
```

Selects the Java version.

You can also use matrix values in a step:

```yaml
- name: Show Configuration
  run: |
    echo "Operating system: ${{ matrix.os }}"
    echo "Java version: ${{ matrix.java }}"
```

Or in an artifact name:

```yaml
- name: Upload Build Artifact
  uses: actions/upload-artifact@v4
  with:
    name: build-java-${{ matrix.java }}
    path: CICDDEMO/target/*.jar
```

This makes the artifact name specific to the matrix execution.

You can also use conditional expressions. For example:

```yaml
- name: Ubuntu-only step
  if: ${{ matrix.os == 'ubuntu-latest' }}
  run: echo "This step runs only for Ubuntu combinations."
```

The condition is evaluated for each matrix execution.

---

# 15. Matrix and Artifacts

Suppose three matrix executions build your application:

```text
Java 21 → application JAR
Java 25 → application JAR
Java 26 → application JAR
```

If every execution uploads an artifact using the same name, you can create naming conflicts or unintended interactions.

Instead, include the matrix value:

```yaml
- name: Upload Build Artifact
  uses: actions/upload-artifact@v4
  with:
    name: spring-boot-java-${{ matrix.java }}
    path: CICDDEMO/target/*.jar
```

The artifact names become:

```text
spring-boot-java-21
spring-boot-java-25
spring-boot-java-26
```

This makes it easier to identify which execution produced each artifact.

**Important:** The build must actually produce the file matching the specified path. Uploading an artifact does not build the application; it only uploads files that exist.

---

# 16. Matrix and Caching

Caching helps avoid repeatedly downloading dependencies or rebuilding reusable data.

For Maven, you might use a cache key such as:

```yaml
key: maven-${{ runner.os }}-java-${{ matrix.java }}-${{ hashFiles('**/pom.xml') }}
```

Understand the components:

- `maven` identifies the cache's purpose.
- `runner.os` distinguishes runner operating systems.
- `matrix.java` distinguishes Java-version combinations.
- `hashFiles('**/pom.xml')` changes when matching Maven project files change.

A cache key identifies the cache entry to restore or create.

Using matrix values in the key can help separate caches when the environments differ.

However, a separate cache for every combination is not always necessary. Design cache keys according to the files and dependencies that actually need to be cached.

---

# 17. Common Beginner Mistakes

## Mistake 1 — Confusing Matrix With Multiple Workflows

**Wrong assumption:** Six combinations mean six workflow files.

**Correct understanding:** One workflow can contain one matrix job definition that generates six job executions.

## Mistake 2 — Forgetting the Matrix Context

**Wrong:**

```yaml
java-version: matrix.java
```

**Correct:**

```yaml
java-version: ${{ matrix.java }}
```

The correct expression accesses the matrix context.

## Mistake 3 — Thinking Matrix Always Runs Everything Simultaneously

A matrix defines combinations, not guaranteed simultaneous execution.

Use `max-parallel` when you need to limit concurrent matrix jobs.

## Mistake 4 — Confusing Include and Exclude

- `include` can add properties or additional combinations.
- `exclude` removes matching original matrix combinations.

They solve opposite kinds of problems.

## Mistake 5 — Assuming Files Are Shared Between Matrix Jobs

Every matrix execution has a separate job environment.

Use artifacts or another explicit data-transfer mechanism to share files.

## Mistake 6 — Ignoring Operating System Differences

This command is Linux-specific:

```bash
chmod +x CICDDEMO/mvnw
```

Windows runners generally need a different command strategy, such as invoking the Maven Wrapper through PowerShell and `mvnw.cmd`.

Always make your build steps compatible with the selected runner.

## Mistake 7 — Misunderstanding Fail-Fast

`fail-fast: false` does not mean failures are ignored.

A failed job still fails. The setting only controls whether one failure causes cancellation of other in-progress or queued matrix executions.

## Mistake 8 — Creating Too Many Combinations

A matrix with five operating systems, four Java versions, and three databases generates:

```text
5 × 4 × 3 = 60 combinations
```

Before adding another dimension, ask whether every combination provides meaningful test coverage.

---

# 18. Best Practices

1. **Start small.** Begin with one matrix variable, then add more dimensions.
2. **Use meaningful job names.** Include operating system and runtime information when useful.
3. **Avoid unnecessary combinations.** More jobs do not automatically mean better testing.
4. **Use include intentionally.** Attach extra properties or introduce carefully selected combinations.
5. **Use exclude intentionally.** Remove combinations that are not supported or needed.
6. **Choose fail-fast based on your goal.** Decide whether you prefer early cancellation or complete test results.
7. **Control concurrency.** Use `max-parallel` where resource or service limits matter.
8. **Make artifact names unique.** Include relevant matrix values.
9. **Use platform-specific commands.** Linux and Windows shells are not interchangeable.
10. **Treat matrix jobs as independent.** Transfer files and values explicitly when required.
11. **Keep cache keys meaningful.** Include values that actually affect the cached dependencies or build environment.
12. **Validate the actual environment.** A matrix value alone does not guarantee that the runner, runtime, or application supports that combination.

---

# 19. Practice Exercises

Complete these exercises to prove that you understand Matrix Strategy.

### Exercise 1 — Single Matrix

Create a workflow that builds a Spring Boot application using Java 21, 25, and 26 on Ubuntu.

**Expected result:** Three matrix combinations.

### Exercise 2 — Multiple Dimensions

Add Windows to the matrix.

**Expected result:** Six combinations.

### Exercise 3 — Exclude

Remove Windows + Java 25.

**Expected result:** Five combinations.

### Exercise 4 — Include

Attach `environment: production` to Ubuntu + Java 26.

**Expected result:** The compatible combination receives the additional property. The combination count remains five if the exclusion from Exercise 3 is retained and no new combinations are added.

### Exercise 5 — Fail-Fast

Set:

```yaml
fail-fast: false
```

Observe what happens when one matrix execution fails.

**Expected result:** Other matrix executions are not cancelled merely because that execution failed.

### Exercise 6 — Max-Parallel

Set:

```yaml
max-parallel: 2
```

Observe the workflow's execution.

**Expected result:** At most two matrix jobs run concurrently.

### Exercise 7 — Unique Artifacts

Upload an artifact whose name includes the Java version.

**Expected result:** Each successful execution can upload a separately named artifact, provided its configured files exist.

### Exercise 8 — Display Matrix Values

Print the operating system, Java version, and environment for every combination.

**Expected result:** Each execution displays its own matrix values; combinations without an `environment` property may display an empty value.

---

# 20. Final Mental Model

Imagine you need to test a Spring Boot application on different Java versions and operating systems.

Without Matrix Strategy:

```text
Write Job 1
Write Job 2
Write Job 3
Write Job 4
Write Job 5
Write Job 6
```

You repeat similar configuration and maintain it in multiple places.

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
The same steps run using those values
```

And the advanced controls:

```text
matrix
  = Defines the dimensions and values

include
  = Adds compatible properties or additional combinations

exclude
  = Removes matching original combinations

fail-fast
  = Controls cancellation after a matrix failure

max-parallel
  = Limits concurrent matrix executions
```

The central lesson is:

**Matrix Strategy separates the job's logic from the values that vary between executions.**

You define the steps once, describe the variations, and let GitHub Actions generate the combinations.

That is why Matrix Strategy is such a valuable feature for automated testing, compatibility checks, and professional CI/CD pipelines.

---

## Official Documentation

Use these references to verify syntax and explore advanced features:

- [GitHub Actions — Running variations of jobs in a workflow](https://docs.github.com/en/actions/how-tos/writing-workflows/choosing-what-your-workflow-does/running-variations-of-jobs-in-a-workflow)
- [GitHub Actions — Workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [GitHub Actions — Contexts reference](https://docs.github.com/en/actions/reference/workflows-and-actions/contexts)
- [GitHub Actions — Store and share data with workflow artifacts](https://docs.github.com/en/actions/tutorials/store-and-share-data)

**Final takeaway:** Matrix Strategy is not just a YAML feature. It is a practical way to describe and automate testing across multiple environments without duplicating the same job definition.
