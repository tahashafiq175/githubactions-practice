# GitHub Actions — Lecture 1: Introduction to CI/CD and Workflow Fundamentals

> **Learning objective:** Understand CI/CD, the role of GitHub Actions, the basic structure of a workflow, and how GitHub automatically executes commands in response to events.

---

## Table of Contents

1. [Introduction to CI/CD](#1-introduction-to-cicd)
2. [Why Do We Need CI/CD?](#2-why-do-we-need-cicd)
3. [Continuous Integration (CI)](#3-continuous-integration-ci)
4. [Continuous Delivery](#4-continuous-delivery)
5. [Continuous Deployment](#5-continuous-deployment)
6. [Advantages of CI/CD](#6-advantages-of-cicd)
7. [What Is GitHub Actions?](#7-what-is-github-actions)
8. [Understanding the Basic Workflow Structure](#8-understanding-the-basic-workflow-structure)
9. [Understanding the YAML Syntax](#9-understanding-the-yaml-syntax)
10. [Workflow 1 — Your First Workflow](#10-workflow-1--your-first-workflow)
11. [Workflow 2 — Simulating Continuous Integration](#11-workflow-2--simulating-continuous-integration)
12. [Workflow 3 — Simulating the Complete CI/CD Journey](#12-workflow-3--simulating-the-complete-cicd-journey)
13. [Understanding the Workflow Execution Process](#13-understanding-the-workflow-execution-process)
14. [GitHub Actions and Argo CD](#14-github-actions-and-argo-cd)
15. [Important Concepts to Remember](#15-important-concepts-to-remember)
16. [Conclusion](#16-conclusion)

---

## 1. Introduction to CI/CD

CI/CD is a software engineering practice that automates important stages of software delivery, including building, testing, packaging, and delivering or deploying applications.

CI/CD stands for:

- **CI — Continuous Integration**
- **CD — Continuous Delivery**
- **CD — Continuous Deployment**

The term CD can refer to either Continuous Delivery or Continuous Deployment, depending on the context.

### The problem without CI/CD

Imagine developing a Spring Boot application. Every time you modify the code, you might need to perform several tasks manually:

1. Build the application.
2. Run automated tests.
3. Package the application.
4. Create a Docker image.
5. Push the image to a container registry.
6. Deploy the application.
7. Verify that the deployment works.

Repeatedly performing these steps consumes time and increases the risk of human error.

### The solution: CI/CD

CI/CD automates appropriate parts of this process.

```text
Developer changes code
        |
        v
   Push to GitHub
        |
        v
   CI/CD automation
        |
        v
   Build and test
        |
        v
   Package application
        |
        v
   Deliver or deploy
```

**Definition:** CI/CD is the practice of automating software integration, validation, and delivery or deployment to make releases more consistent, efficient, and reliable.

---

## 2. Why Do We Need CI/CD?

Consider a team of developers working on the same application.

```text
Developer A ----\
Developer B -----\
Developer C ------> Shared Git repository
Developer D -----/
Developer E ----/
```

Each developer changes part of the application. Those changes must eventually work together.

Without automated checks, a developer could introduce a bug that breaks an existing feature. The team might discover the problem only after manually building or deploying the application.

CI/CD helps address these problems by automating checks and repeatable delivery tasks.

For example:

```text
Code change
    |
    v
Automated build
    |
    v
Automated tests
    |
    +---- Failure ----> Stop the required pipeline
    |
    +---- Success ---> Continue to the next stage
```

The pipeline's actual behavior depends on how its steps and failure-handling rules are configured.

---

## 3. Continuous Integration (CI)

Continuous Integration is the practice of frequently integrating code changes into a shared codebase and automatically validating those changes.

### What happens during CI?

A typical CI workflow may perform the following operations:

```text
Code pushed
    |
    v
Checkout repository
    |
    v
Set up required environment
    |
    v
Install dependencies
    |
    v
Compile or build
    |
    v
Run tests
    |
    v
Report results
```

For a Java application built with Maven, real CI commands might include:

```bash
mvn test
mvn package
```

The exact commands and order depend on the project.

### Example

Suppose you modify the login functionality of a Spring Boot application.

The CI workflow runs automated tests to check whether the login functionality and other tested behaviors still work.

If a required test fails, the workflow can report a failure and prevent subsequent deployment steps from running.

**Remember:** CI helps answer this question:

> Did my code change introduce a problem, and can the application build successfully?

Creating a Docker image is also common in a CI pipeline, but it is not a mandatory requirement of CI itself.

---

## 4. Continuous Delivery

Continuous Delivery means keeping software in a deployable state so that it can be released reliably whenever the organization chooses.

A typical process looks like this:

```text
Code
  |
  v
Build
  |
  v
Test
  |
  v
Package
  |
  v
Ready for release
  |
  v
Optional manual approval
  |
  v
Production
```

The build, testing, packaging, and release preparation can be automated. The final production release may require human approval.

**Remember:**

> Continuous Delivery means the application is continuously kept ready for release.

---

## 5. Continuous Deployment

Continuous Deployment extends automation to the production release itself.

When a change passes the required checks, the pipeline automatically deploys it to production without requiring a separate human approval for each release.

```text
Code change
    |
    v
Build and test
    |
    v
Package
    |
    v
Required checks pass
    |
    v
Automatic production deployment
```

Organizations may still have safeguards such as security checks, deployment policies, monitoring, and automatic rollback mechanisms.

### Continuous Delivery vs. Continuous Deployment

| Continuous Delivery | Continuous Deployment |
|---|---|
| Software is kept ready for release. | Software is automatically released after required checks pass. |
| Production release may require human approval. | Each successful release does not require manual production approval. |
| Delivery preparation is automated. | Delivery and production deployment are automated. |

**Easy way to remember:**

- Continuous Delivery: "The application is ready. We can release it."
- Continuous Deployment: "The application passed the required checks, so the system releases it automatically."

---

## 6. Advantages of CI/CD

### 6.1 Standardized Process

Every release can follow the same predefined pipeline instead of relying on different manual procedures.

### 6.2 Reduced Repetitive Engineering Work

Tasks such as building, testing, packaging, and publishing can be automated, allowing engineers to focus on development and problem-solving.

### 6.3 Improved Deployment Consistency

Repeatable automated steps reduce differences between releases and help minimize human error.

### 6.4 Faster Software Delivery

Automated pipelines reduce the time spent manually preparing and releasing software.

### 6.5 Safer Releases

Automated tests and validation checks can detect problems before deployment.

### 6.6 Improved Reliability

Standardized, repeatable processes make software delivery more predictable.

**In summary:** CI/CD helps make software delivery faster, safer, more consistent, and more reliable.

---

## 7. What Is GitHub Actions?

GitHub Actions is a GitHub automation platform that allows you to define and execute workflows in response to events.

It can automate tasks such as:

- Building applications.
- Running tests.
- Checking code quality.
- Publishing artifacts.
- Creating and publishing Docker images.
- Deploying applications.
- Running scheduled tasks.

For example, a workflow can run automatically whenever code is pushed to a particular branch.

```text
Developer
    |
    v
Push code to GitHub
    |
    v
GitHub detects the event
    |
    v
GitHub Actions starts a workflow
    |
    v
Workflow executes configured tasks
```

### CI/CD vs. GitHub Actions

These terms describe different things.

| Term | Meaning |
|---|---|
| CI/CD | Software development and delivery practices |
| GitHub Actions | A platform used to automate workflows, including CI/CD |
| Workflow | A defined sequence of automated tasks |
| YAML | The configuration language commonly used to define workflows |

**Important:** GitHub Actions is not CI/CD itself. It is a tool that can implement CI/CD practices.

---

## 8. Understanding the Basic Workflow Structure

GitHub Actions workflow files are stored in the following directory:

```text
your-repository/
└── .github/
    └── workflows/
        ├── 01-basic-cicd.yml
        ├── 02-ci-demo.yml
        └── 03-cicd-journey.yml
```

Each YAML file defines a workflow.

A workflow can specify:

- Its name.
- The events that trigger it.
- The jobs to execute.
- The machines on which jobs run.
- The steps inside each job.
- The commands or actions performed by those steps.

### Basic structure

```yaml
name: My First Workflow

on:
  workflow_dispatch:

jobs:
  hello:
    runs-on: ubuntu-latest

    steps:
      - name: Say hello
        run: echo "Hello, GitHub Actions!"
```

This is a small but valid workflow structure.

When manually triggered, GitHub runs the job on an Ubuntu runner and executes the command in its step.

---

## 9. Understanding the YAML Syntax

Before learning advanced GitHub Actions features, understand the basic syntax used in a workflow.

### 9.1 `name`

```yaml
name: My First Workflow
```

The `name` field gives the workflow a readable name.

It helps you identify the workflow in the GitHub Actions interface.

### 9.2 `on`

```yaml
on:
  workflow_dispatch:
```

The `on` field specifies which event or events trigger the workflow.

Here, `workflow_dispatch` enables manual triggering through GitHub's interface.

Another example:

```yaml
on:
  push:
```

This configures the workflow to run when a push event occurs, subject to any additional filters.

### 9.3 `jobs`

```yaml
jobs:
  hello:
```

The `jobs` section contains the jobs that make up the workflow.

In this example, `hello` is a job identifier chosen by the workflow author.

A workflow can contain one or multiple jobs.

### 9.4 `runs-on`

```yaml
runs-on: ubuntu-latest
```

This specifies the type of runner used to execute the job.

A runner is the machine or execution environment where the job's commands run.

`ubuntu-latest` selects a GitHub-hosted Ubuntu runner.

### 9.5 `steps`

```yaml
steps:
  - name: Say hello
    run: echo "Hello!"
```

A step is an individual task inside a job.

Steps normally execute in the order listed. If a step fails, subsequent steps are generally skipped unless the workflow specifies conditions that allow them to continue.

### 9.6 `name` inside a step

```yaml
- name: Print greeting
```

This gives the individual step a readable label.

The step name helps you understand the workflow logs.

### 9.7 `run`

```yaml
run: echo "Hello!"
```

The `run` keyword executes a shell command on the runner.

In this example, `echo` prints a message.

It does not test, build, package, or deploy an application.

### 9.8 Comments

```yaml
# This is a YAML comment.
# GitHub does not execute this line.
run: echo "This command executes."
```

A `#` begins a YAML comment when used outside a quoted string.

Comments explain the configuration to human readers. They do not perform actions.

### 9.9 Indentation

YAML uses indentation to express structure.

Correct:

```yaml
jobs:
  hello:
    runs-on: ubuntu-latest
    steps:
      - name: Print message
        run: echo "Hello!"
```

Incorrect indentation can change the structure or cause a parsing error.

Use spaces consistently. Do not use tabs for YAML indentation.

### 9.10 The complete hierarchy

Read this structure from the outside inward:

```text
Workflow
  |
  +-- name
  |
  +-- on
  |
  +-- jobs
        |
        +-- Job ID
              |
              +-- runs-on
              |
              +-- steps
                    |
                    +-- name
                    +-- run
```

This hierarchy is one of the most important foundations of GitHub Actions.

---

## 10. Workflow 1 — Your First Workflow

**File:** `.github/workflows/01-basic-cicd.yml`

```yaml
# The name shown in the GitHub Actions interface.
name: "01 - My First CI/CD Workflow"

# Allow the workflow to be started manually.
on:
  workflow_dispatch:

# Define the work to perform.
jobs:

  # "hello" is the identifier for this job.
  hello:

    # Execute this job on an Ubuntu runner.
    runs-on: ubuntu-latest

    # Define the steps that the job executes.
    steps:

      # Print a greeting.
      - name: Say hello
        run: echo "Hello! My CI/CD journey starts today."

      # Print an introductory CI message.
      - name: Explain CI
        run: echo "CI helps automate building and testing code."

      # Print an introductory CD message.
      - name: Explain CD
        run: echo "CD helps prepare and deliver software."
```

### Execution sequence

```text
Manually start workflow
         |
         v
GitHub starts the runner
         |
         v
Execute "Say hello"
         |
         v
Execute "Explain CI"
         |
         v
Execute "Explain CD"
         |
         v
Workflow completes
```

### What you learn

- How to define a workflow.
- How to enable manual triggering.
- How to define a job.
- How to select a runner.
- How to define and execute steps.

**Note:** This workflow only prints messages. It is a syntax demonstration, not a functional CI/CD pipeline.

---

## 11. Workflow 2 — Simulating Continuous Integration

**File:** `.github/workflows/02-ci-demo.yml`

```yaml
# Give the workflow a readable name.
name: "02 - Simulating Continuous Integration"

# Run when code is pushed to the main branch.
on:
  push:
    branches:
      - main

jobs:
  ci-demo:
    runs-on: ubuntu-latest

    steps:

      # Announce the start of the simulated CI process.
      - name: Start CI
        run: echo "CI has started because new code was pushed."

      # Print a message representing code validation.
      - name: Simulate code validation
        run: echo "Checking whether the code is valid..."

      # Print a message representing automated tests.
      - name: Simulate tests
        run: echo "Running automated tests..."

      # Print a simulated success message.
      - name: Show result
        run: echo "All simulated checks passed!"
```

### How it works

When a push to `main` triggers this workflow:

1. GitHub detects the push event.
2. GitHub starts the workflow.
3. The runner executes the first step.
4. The remaining steps execute in order.
5. The workflow finishes if the commands succeed.

For example:

```bash
git add .
git commit -m "Learn CI basics"
git push origin main
```

### Important distinction

This workflow does not actually validate code or run tests. It only prints messages.

To perform real Java testing, you could later introduce:

```yaml
- name: Run Maven tests
  run: mvn test
```

That command would execute the project's Maven tests, assuming Maven and the required project files are available in the runner.

A real workflow commonly checks out the repository and sets up the required Java environment before running Maven.

---

## 12. Workflow 3 — Simulating the Complete CI/CD Journey

**File:** `.github/workflows/03-cicd-journey.yml`

```yaml
# Name of the workflow.
name: "03 - Simulating the CI/CD Journey"

# Allow manual execution.
on:
  workflow_dispatch:

jobs:
  pipeline:
    runs-on: ubuntu-latest

    steps:

      # Announce the start of the pipeline.
      - name: 1 - Start pipeline
        run: echo "A developer has requested a pipeline run."

      # Simulate code validation.
      - name: 2 - Validate code
        run: echo "CI: Checking the code..."

      # Simulate automated testing.
      - name: 3 - Run tests
        run: echo "CI: Running automated tests..."

      # Simulate building the application.
      - name: 4 - Build application
        run: echo "CI: Building the application..."

      # Simulate creating a Docker image.
      - name: 5 - Create image
        run: echo "Imagine creating hotel-service:v1."

      # Simulate deployment.
      - name: 6 - Deploy application
        run: echo "Imagine deploying the application."

      # Display the final message.
      - name: 7 - Finish
        run: echo "The simulated pipeline has finished!"
```

### Execution sequence

```text
Start
  |
  v
Validate code
  |
  v
Run tests
  |
  v
Build application
  |
  v
Create Docker image
  |
  v
Deploy application
  |
  v
Finish
```

Every stage is represented by an `echo` command.

No actual Docker image is created, and no application is deployed.

This example teaches you how a sequence of steps can represent a larger automated process.

### What a real pipeline would do

| Simulated step | Possible real implementation |
|---|---|
| Validate code | Run a linter or static analysis tool |
| Run tests | Execute `mvn test` |
| Build application | Execute a Maven build |
| Create image | Execute `docker build` |
| Publish image | Authenticate and push to a registry |
| Deploy application | Use an approved deployment mechanism |

The actual commands will be introduced in later lessons.

---

## 13. Understanding the Workflow Execution Process

A GitHub Actions workflow consists of configuration and executable operations.

Consider this example:

```yaml
name: Example Workflow

on:
  workflow_dispatch:

jobs:
  demo:
    runs-on: ubuntu-latest

    steps:
      - name: First step
        run: echo "Step 1"

      - name: Second step
        run: echo "Step 2"
```

The execution process is:

**Step 1 — GitHub identifies the trigger.**

The workflow is configured to start when a user manually runs it.

**Step 2 — GitHub schedules the job.**

GitHub assigns the job to a suitable runner.

**Step 3 — The runner executes the steps.**

The first command prints `Step 1`.

**Step 4 — The next step executes.**

The second command prints `Step 2`.

**Step 5 — GitHub reports the result.**

The Actions interface shows the job and step statuses, along with logs.

This example has only one job. More complex workflows can contain multiple jobs with dependencies, conditions, and different runners.

---

## 14. GitHub Actions and Argo CD

In a Kubernetes GitOps environment, GitHub Actions and Argo CD can work together while performing different responsibilities.

### GitHub Actions

A common responsibility is building and validating application changes.

```text
Application code
      |
      v
GitHub Actions
      |
      +--> Build
      |
      +--> Test
      |
      +--> Create Docker image
      |
      +--> Push image
```

### Argo CD

Argo CD focuses on synchronizing the desired Kubernetes configuration stored in Git with the cluster.

```text
Git repository
      |
      v
Desired Kubernetes configuration
      |
      v
Argo CD compares states
      |
      v
Synchronization
      |
      v
Kubernetes
```

### Desired state and actual state

Suppose Git specifies:

```yaml
replicas: 5
```

But Kubernetes currently has three replicas.

Git represents the desired state, while Kubernetes represents the actual state.

Argo CD can detect this difference and synchronize the cluster according to its configured policies.

```text
Git:        replicas = 5
Kubernetes: replicas = 3
                  |
                  v
             State differs
                  |
                  v
             Argo CD sync
                  |
                  v
Kubernetes: replicas = 5
```

This is an introductory example of GitOps synchronization. Argo CD does not simply compare every possible production setting for contradictions; it evaluates the desired configuration against the live cluster state and the applicable synchronization rules.

**Remember:** GitHub Actions can build and publish the application, while Argo CD can manage GitOps synchronization. The exact responsibilities depend on the architecture.

---

## 15. Important Concepts to Remember

| Concept | Meaning |
|---|---|
| CI/CD | Automation practices for integrating, validating, and delivering software |
| CI | Continuously integrate and automatically validate code changes |
| Continuous Delivery | Keep software ready for release |
| Continuous Deployment | Automatically release successful changes to production |
| GitHub Actions | Platform for automating workflows |
| Workflow | A configured automation process |
| YAML | Configuration language used to define the workflow |
| Event | Something that triggers a workflow |
| Job | A group of steps executed by a runner |
| Runner | The machine or environment executing a job |
| Step | An individual task within a job |
| `run` | Executes a shell command |
| `echo` | Prints text to the terminal or workflow log |
| GitOps | An approach that uses Git as the source of desired system configuration |
| Argo CD | A GitOps continuous delivery tool for Kubernetes |

### Five foundational rules

1. A workflow is defined in a YAML file inside `.github/workflows/`.
2. The `on` field specifies which events trigger the workflow.
3. The `jobs` section defines the work to perform.
4. A job uses `runs-on` to select its execution environment.
5. The `steps` section defines tasks, and `run` can execute shell commands.

---

## 16. Conclusion

This lecture establishes the foundation for learning GitHub Actions.

Before moving to advanced topics, understand the distinction between the automation concept and its implementation:

- **CI/CD** explains what we want to automate.
- **GitHub Actions** provides a platform to implement that automation.
- **YAML** describes the workflow configuration.
- **Events** determine when the workflow runs.
- **Jobs and steps** define what happens.
- **Runners** execute the configured work.

The three introductory workflows progress from printing basic messages, to responding to a push event, to simulating a complete CI/CD sequence.

They deliberately use `echo` commands so that the YAML structure is easy to understand without introducing application builds, Docker, secrets, or deployment configuration.

**Final mental model:**

> An event triggers a workflow, GitHub schedules its jobs on runners, and the runners execute the configured steps. By replacing introductory commands with real build, test, packaging, and deployment commands, you can gradually turn a simple workflow into a practical CI/CD pipeline.
