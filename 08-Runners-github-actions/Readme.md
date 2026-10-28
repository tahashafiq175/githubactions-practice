# GitHub Actions Runners — Complete Beginner-to-Advanced Guide

> **The machine that brings your workflow to life.**

Welcome to the Runners chapter of GitHub Actions.

This guide explains runners from the ground up, using simple language, real YAML examples, practical experiments, and behind-the-scenes explanations.

The goal is not merely to memorize `runs-on`. The goal is to understand what happens when GitHub receives a workflow, how it chooses a machine, where your commands execute, why different runner types exist, how resources affect builds, and how to select runners safely in real CI/CD pipelines.

---

# Table of Contents

1. What Is a Runner?
2. Understand the Workflow → Job → Runner → Step Relationship
3. What Happens After You Push Code?
4. The `runs-on` Keyword
5. GitHub-Hosted Runners
6. Standard Runner Capacity: CPU, RAM, Storage and Software
7. Understanding Runner Environment Variables
8. Operating Systems and Runner Architectures
9. Larger Runners
10. Self-Hosted Runners
11. Why Self-Hosted Runners Are Risky for Public Repositories
12. Runner Isolation and Job Execution
13. How Jobs Share Files Using Artifacts
14. Multiple Jobs and Parallel Execution
15. Runner Labels and Runner Selection
16. GitHub-Hosted vs. Larger vs. Self-Hosted Runners
17. Runner Billing and Usage Limits
18. Practical Workflow 1 — Inspect Your Runner
19. Practical Workflow 2 — Compare Operating Systems
20. Practical Workflow 3 — Understand Job Isolation
21. Practical Workflow 4 — Inspect Runner Resources
22. Practical Workflow 5 — Understand Job Dependencies
23. Practical Workflow 6 — A Java CI Pipeline
24. Runner Security Best Practices
25. Common Runner Errors and Troubleshooting
26. Frequently Asked Questions
27. Final Mental Model
28. Learning Checklist

---

# 1. What Is a Runner?

Imagine that you want to build a Java application.

You have written your source code:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello GitHub Actions!");
    }
}
```

Now you want GitHub Actions to compile your Java code, execute tests, and prepare your application for deployment.

But there is a problem.

**Where will these commands actually execute?**

GitHub needs a computer on which it can run commands such as:

```bash
java -version
mvn clean test
mvn package
docker build -t my-app .
```

That computer environment is called a **runner**.

A runner is the machine or execution environment that runs the steps of a GitHub Actions job.

Think of a runner as a worker.

You give the worker instructions, and the worker performs them.

```text
You write a workflow
        |
        v
GitHub receives the workflow
        |
        v
A runner is selected
        |
        v
The runner executes commands
        |
        v
You receive logs and results
```

The runner does not decide what your entire CI/CD pipeline should accomplish. Your workflow defines the work; the runner executes it.

## A real-life analogy

Imagine a restaurant.

- **Workflow:** The complete order and instructions.
- **Job:** One group of related tasks.
- **Runner:** The kitchen worker and workstation performing those tasks.
- **Step:** One instruction, such as chopping vegetables or cooking rice.
- **Logs:** The record of what happened.

The restaurant needs workers and workstations before the instructions can be carried out.

In the same way, GitHub Actions needs runners to execute workflow jobs.

## The most important definition

**A runner is the execution environment where a GitHub Actions job runs.**

Remember this definition throughout the chapter.

---

# 2. Understand the Workflow → Job → Runner → Step Relationship

Consider this workflow:

```yaml
name: Runner Example

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Print Message
        run: echo "Hello from the runner!"

      - name: Show Java Version
        run: java -version
```

Let's understand every layer.

## Layer 1: Workflow

```yaml
name: Runner Example
```

A workflow is an automated process defined in a YAML file.

It contains the rules describing when the automation should run and what jobs it contains.

## Layer 2: Trigger

```yaml
on:
  workflow_dispatch:
```

This allows you to start the workflow manually from GitHub's Actions interface.

Other triggers include `push`, `pull_request`, and scheduled execution.

## Layer 3: Job

```yaml
jobs:
  build:
```

A job is a group of steps that GitHub executes on a runner.

Here, the job is named `build`.

## Layer 4: Runner selection

```yaml
runs-on: ubuntu-latest
```

This tells GitHub to select an available runner matching the `ubuntu-latest` label.

For a standard GitHub-hosted runner, GitHub provisions and manages the execution environment.

## Layer 5: Steps

```yaml
steps:
  - name: Print Message
    run: echo "Hello from the runner!"
```

The runner executes the command in this step.

The next step runs:

```yaml
- name: Show Java Version
  run: java -version
```

The runner attempts to execute the Java command.

If Java is available in the environment, its version is printed. Otherwise, the step may fail.

The workflow above does not explicitly install Java, so the command relies on the software already available on the selected runner.

## Put everything together

```text
WORKFLOW
   |
   +---- Trigger: workflow_dispatch
   |
   +---- JOB: build
             |
             +---- runs-on: ubuntu-latest
             |         |
             |         v
             |    Select runner
             |
             +---- STEP 1: Print message
             |
             +---- STEP 2: Display Java version
```

**Remember:** `runs-on` belongs to a job, not an individual step.

A job has one selected runner environment for its execution. Each step in that job runs as part of that job's execution.

---

# 3. What Happens After You Push Code?

Suppose you create this workflow:

```yaml
name: Runner Demo

on:
  push:

jobs:
  runner-info:
    runs-on: ubuntu-latest

    steps:
      - name: Print Message
        run: echo "My first runner!"
```

You save the file and execute:

```bash
git add .
git commit -m "Add runner demo"
git push
```

What happens behind the scenes?

## Step 1 — Git sends your commit

Your local Git repository sends the commit to GitHub.

GitHub receives the updated repository content.

## Step 2 — GitHub detects the event

The workflow contains:

```yaml
on:
  push:
```

The push event matches the trigger, assuming the workflow file and event satisfy GitHub's workflow rules.

## Step 3 — GitHub reads the workflow

GitHub identifies the job:

```yaml
runner-info:
```

Then it reads the runner requirement:

```yaml
runs-on: ubuntu-latest
```

## Step 4 — GitHub finds a suitable runner

GitHub arranges an available runner matching the requested environment.

If you use a standard GitHub-hosted runner, GitHub manages the provisioning and execution environment.

## Step 5 — The job executes

The runner starts executing the job's steps.

```bash
echo "My first runner!"
```

The command prints a message.

## Step 6 — Logs become available

You can open the repository's **Actions** tab, select the workflow run, open the job, and inspect the step logs.

## The complete journey

```text
git push
   |
   v
GitHub receives commit
   |
   v
Push event detected
   |
   v
Workflow is triggered
   |
   v
Job is identified
   |
   v
runs-on is evaluated
   |
   v
Suitable runner becomes available
   |
   v
Steps execute
   |
   v
Logs and job result appear
```

## Important distinction

A successful `git push` does not guarantee a successful workflow.

Git and GitHub Actions perform different jobs.

- Git transfers and records source-code changes.
- GitHub Actions runs automation based on workflow definitions.

A file can be committed successfully but still contain invalid YAML or an invalid workflow.

For example, an empty `.yml` file is not a useful workflow definition. It may result in no useful run or a workflow validation problem, depending on its contents and the situation.

Always inspect the actual workflow file when a run does not behave as expected.

---

# 4. The `runs-on` Keyword

The `runs-on` keyword tells GitHub what kind of runner a job requires.

Example:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
```

Here:

- `jobs` contains the jobs in the workflow.
- `build` is the job ID.
- `runs-on` specifies the runner requirement.
- `ubuntu-latest` is the requested runner label.

Other common labels include:

```yaml
runs-on: ubuntu-latest
```

```yaml
runs-on: windows-latest
```

```yaml
runs-on: macos-latest
```

These labels select supported operating-system environments for GitHub-hosted runners.

## Why does the label matter?

Imagine that you want to run:

```bash
ls
```

on Linux, but your script uses a Windows-specific command such as:

```powershell
Get-ChildItem
```

The operating system matters because different environments have different shells, utilities, paths, and behavior.

Selecting a runner is not just selecting a machine with enough power. It also selects an environment in which your commands must work.

## A subtle but important point

`ubuntu-latest` is a moving label. GitHub can update the Ubuntu image associated with that label over time.

Therefore, a workflow using `ubuntu-latest` may use a newer runner image in the future.

If you need a specific supported image, you can select a version-specific label, such as `ubuntu-24.04`, where supported.

Even a version-specific image can receive updates to its installed software and packages.

---

# 5. GitHub-Hosted Runners

GitHub-hosted runners are execution environments provided and managed by GitHub.

You request a supported environment, and GitHub arranges the runner.

For example:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
```

You do not need to purchase and maintain a separate server just to run this job.

GitHub handles the underlying runner infrastructure according to the selected offering.

## What does GitHub manage?

For a standard GitHub-hosted runner, GitHub manages the environment's provisioning, operating-system image, and underlying infrastructure.

The exact environment and available resources depend on the runner offering.

The runner image commonly contains tools such as Git, language runtimes, and build utilities. However, you must not assume that every required tool or exact version will always be installed.

For example, a Java workflow can explicitly select Java:

```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '21'
```

This makes the required Java version an explicit part of your workflow.

## What does the runner do?

A runner can execute tasks such as:

- Checking out source code.
- Compiling Java applications.
- Running unit and integration tests.
- Building Docker images.
- Running code-quality checks.
- Packaging application files.
- Uploading build artifacts.
- Preparing deployment operations.

A runner does not automatically perform all these tasks merely because it exists. Your workflow must define the appropriate steps.

## The temporary-machine mental model

For standard GitHub-hosted jobs, think of the runner as a temporary work environment.

```text
Job begins
    |
    v
Runner environment is prepared
    |
    v
Steps execute
    |
    v
Job completes
    |
    v
Standard hosted environment is discarded
```

The exact lifecycle depends on the runner offering. Standard GitHub-hosted runners generally provide a fresh environment for each job.

This makes them convenient for repeatable CI pipelines.

---

# 6. Standard Runner Capacity: CPU, RAM, Storage and Software

A runner is not an unlimited computer.

It has finite resources.

The four main things to understand are CPU, RAM, storage, and installed software.

## 6.1 CPU — The processing power

CPU stands for Central Processing Unit.

Think of the CPU as the part of the computer that carries out instructions.

A Java build may need to:

1. Read source files.
2. Compile Java code.
3. Resolve build tasks.
4. Run tests.
5. Package output files.

These tasks consume processing resources.

A runner with more available CPU resources may complete a CPU-intensive build faster, although the actual improvement depends on the project and the build process.

## 6.2 RAM — Working memory

RAM is the memory used by running processes.

During a Java build, memory may be used by:

- The Java Virtual Machine.
- The Maven or Gradle process.
- Test processes.
- Build plugins.
- Other running tools.

If a process demands more memory than the environment can provide, it may fail or be terminated.

For example, a Java process can report an `OutOfMemoryError` when it cannot obtain the required JVM memory. Other forms of system memory exhaustion can also terminate processes.

A larger runner can help when the workload genuinely needs more memory.

## 6.3 Storage — Disk space

Storage holds files such as:

- The checked-out repository.
- Downloaded dependencies.
- Build output.
- Docker image layers.
- Test reports.
- Temporary files.
- Cached data.

Suppose your workflow builds a large Docker image.

```text
Source code
    |
    v
Build dependencies
    |
    v
Intermediate image layers
    |
    v
Final Docker image
```

All these operations may consume disk space.

If available storage is exhausted, commands may fail even when the CPU is powerful enough.

The storage visible inside the runner is not necessarily the same as the underlying host's total physical storage.

## 6.4 Installed software

A runner also needs the right tools.

A Java workflow may need:

- JDK.
- Maven or Gradle.
- Git.
- Docker tooling, if building images.
- Test utilities.

A runner may have some of these tools already, but installed versions can change.

For reliable builds, explicitly configure important tool versions.

For example:

```yaml
- name: Set up Java 21
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '21'
```

## Why this matters

Imagine your build fails.

The cause might be:

- Not enough memory.
- Insufficient disk space.
- A missing command.
- An incompatible Java version.
- A network or dependency-download problem.
- A problem in your application code.

These are different problems, and they require different solutions.

**Do not automatically assume that every failed build needs a larger runner.**

First identify the actual cause.

## Are all GitHub-hosted runners equally powerful?

No.

Runner capacity depends on the selected offering, operating system, architecture, and runner configuration.

Standard runners have predefined specifications. Larger runners provide higher resource capacities and additional configuration options, subject to availability and account eligibility.

Consult GitHub's current documentation before relying on exact CPU, RAM, storage, or pricing numbers.

Official documentation:

- https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners
- https://docs.github.com/en/actions/using-github-hosted-runners/about-larger-runners

---

# 7. Understanding Runner Environment Variables

GitHub Actions provides default environment variables that tell you useful information about the current execution environment.

Consider:

```yaml
name: Runner Environment Variables

on:
  workflow_dispatch:

jobs:
  runner-info:
    runs-on: ubuntu-latest

    steps:
      - name: Display Runner Details
        run: |
          echo "Runner OS: $RUNNER_OS"
          echo "Runner Architecture: $RUNNER_ARCH"
          echo "Runner Name: $RUNNER_NAME"
          echo "Runner Temporary Directory: $RUNNER_TEMP"
          echo "Workspace: $GITHUB_WORKSPACE"
```

Let's understand these variables.

## `RUNNER_OS`

Identifies the operating-system family of the runner.

Examples include:

```text
Linux
Windows
macOS
```

## `RUNNER_ARCH`

Identifies the runner architecture.

Examples include:

```text
X64
ARM64
```

The actual architecture depends on the selected runner.

## `RUNNER_NAME`

Identifies the runner by its name.

Do not assume this will always be a friendly, permanent server name. The exact value depends on the runner environment.

## `RUNNER_TEMP`

Identifies a temporary directory available to the runner.

It is useful when a step needs temporary files.

## `GITHUB_WORKSPACE`

Identifies the workspace directory associated with the job.

When the repository is checked out, its files are normally placed in this workspace.

To make the checkout explicit:

```yaml
- name: Checkout Repository
  uses: actions/checkout@v4
```

The checkout step downloads the repository contents into the workspace.

## Environment variables versus contexts

GitHub Actions exposes information through more than one mechanism.

For example:

```yaml
- name: Print Operating System
  run: echo "$RUNNER_OS"
```

Here, `$RUNNER_OS` is shell syntax that reads an environment variable.

You can also use a GitHub Actions context in an expression:

```yaml
- name: Print Operating System
  run: echo "${{ runner.os }}"
```

Here, `${{ runner.os }}` is a GitHub Actions expression evaluated by the workflow engine.

Both can provide runner information, but they are not the same mechanism.

**Remember:**

- `$RUNNER_OS` is an environment-variable reference.
- `${{ runner.os }}` is a context expression.

For the complete list of default variables, see:

https://docs.github.com/en/actions/reference/workflows-and-actions/variables

---

# 8. Operating Systems and Runner Architectures

A runner has an operating system and a processor architecture.

These two things are related, but they are not identical.

## Operating system

Examples:

- Ubuntu Linux.
- Windows Server.
- macOS.

## Architecture

Examples:

- `X64`.
- `ARM64`.

The architecture describes the processor instruction-set family supported by the runner environment.

Your application and tools must be compatible with the environment you choose.

For example, a tool downloaded for an incompatible architecture may fail to execute.

## A practical workflow

```yaml
name: Runner OS Comparison

on:
  workflow_dispatch:

jobs:
  ubuntu-job:
    runs-on: ubuntu-latest

    steps:
      - name: Inspect Ubuntu
        run: |
          echo "Ubuntu runner"
          uname -a
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"

  windows-job:
    runs-on: windows-latest

    steps:
      - name: Inspect Windows
        run: |
          echo "Windows runner"
          systeminfo
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"

  macos-job:
    runs-on: macos-latest

    steps:
      - name: Inspect macOS
        run: |
          echo "macOS runner"
          sw_vers
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"
```

This workflow contains three independent jobs.

Each job selects its own runner environment.

The jobs may run concurrently, depending on availability and concurrency limits.

Notice that the commands differ because each operating system provides different utilities and command behavior.

You are not installing these three operating systems on your personal laptop. GitHub provides the hosted environments.

---

# 9. Larger Runners

A larger runner is a more powerful GitHub-hosted runner configuration.

The important distinction is that **larger runners are still GitHub-hosted runners**.

They are not the same thing as self-hosted runners.

## Why do larger runners exist?

Imagine two Java projects.

### Project A

A small Spring Boot application with a few unit tests.

### Project B

A large monorepo containing many services, extensive tests, and large build operations.

Project A may work perfectly on a standard runner.

Project B may benefit from more CPU, RAM, or storage.

Larger runners exist for workloads that require greater capacity or particular supported configurations.

## The relationship

```text
GitHub-hosted runners
        |
        +---- Standard runners
        |       |
        |       +---- Predefined capacity
        |       +---- Common CI workloads
        |
        +---- Larger runners
                |
                +---- Higher resource capacity
                +---- Additional configuration options
                +---- Heavy or demanding workloads
```

The available capacities and configuration options depend on the larger-runner offering.

## How do you select one?

A larger runner must be available and configured for your repository or organization.

The exact `runs-on` label depends on the configured runner label.

For example, the following is a placeholder:

```yaml
jobs:
  heavy-build:
    runs-on: YOUR-LARGER-RUNNER-LABEL

    steps:
      - name: Build
        run: echo "Running on a configured larger runner"
```

Replace `YOUR-LARGER-RUNNER-LABEL` with the actual label supplied by your configuration.

Otherwise, GitHub cannot find a matching runner.

## Do larger runners automatically make everything faster?

No.

More resources help only when the workload can benefit from them.

For example:

- A CPU-intensive compilation may benefit from more CPU.
- A memory-intensive test suite may benefit from more RAM.
- A large image build may benefit from more available disk space.

A slow dependency server, an inefficient algorithm, or a network problem may not be solved by upgrading the runner.

## Important availability note

Larger runners are subject to current GitHub product availability, account eligibility, configuration, usage limits, and billing.

Do not assume every personal account can use every larger-runner configuration.

Official documentation:

https://docs.github.com/en/actions/using-github-hosted-runners/about-larger-runners

---

# 10. Self-Hosted Runners

A self-hosted runner is a machine that you provide and manage.

It may be:

- Your own computer.
- A company server.
- A virtual machine.
- A cloud VM.
- A dedicated build server.

You install and configure the GitHub Actions runner application, register the runner with GitHub, and keep the machine available for jobs.

GitHub can then route eligible jobs to that runner.

## The self-hosted mental model

With a GitHub-hosted runner:

> GitHub provides and manages the runner environment.

With a self-hosted runner:

> You provide and manage the machine that executes the job.

## Example

Imagine you have an Ubuntu server with:

```text
CPU: 16 cores
RAM: 32 GB
Storage: 500 GB
```

These values are only an example of a machine you might provide.

You install the runner software and configure it for your repository or organization.

A workflow can select an appropriately registered self-hosted runner:

```yaml
jobs:
  build:
    runs-on: self-hosted

    steps:
      - name: Print Message
        run: echo "Running on a self-hosted runner"
```

This example uses the `self-hosted` label, which is automatically applied to self-hosted runners. If you have multiple eligible runners, you can use additional labels to select the intended environment.

For example:

```yaml
runs-on: [self-hosted, linux, x64]
```

This asks GitHub to find a runner matching all the specified labels.

Labels must actually exist on a suitable registered runner.

## Why would an organization use self-hosted runners?

### 1. Special hardware

A company may need a machine with a specific GPU, processor, or connected device.

### 2. Special software

The organization may need licensed tools or custom software that is not conveniently available on a standard hosted image.

### 3. Private network access

The build may need to communicate with systems reachable only from the company's internal network.

### 4. Custom environment

The organization may require particular operating-system settings, dependencies, or tools.

### 5. Infrastructure control

The organization controls the machine's maintenance, updates, network configuration, and security policies.

## What is the trade-off?

You gain control, but you also accept responsibility.

You must consider:

- Operating-system updates.
- Runner software updates.
- Network security.
- Credentials and secrets.
- Disk cleanup.
- Monitoring.
- Machine availability.
- Access control.
- Isolation between jobs.

A self-hosted runner is not automatically safer, cheaper, or faster than a GitHub-hosted runner.

Its suitability depends on the workload and how well the machine is managed.

Official documentation:

https://docs.github.com/en/actions/hosting-your-own-runners/about-self-hosted-runners

---

# 11. Why Self-Hosted Runners Are Risky for Public Repositories

This is one of the most important runner-security lessons.

A public repository allows people outside your organization to inspect its code and potentially submit contributions.

That includes pull requests containing workflow changes.

Suppose your repository uses a self-hosted runner on your personal server.

```text
Public repository
       |
       v
Untrusted pull request
       |
       v
Workflow execution
       |
       v
Your self-hosted machine
```

If an untrusted workflow executes on your machine, it may run malicious commands with the permissions available to that runner.

Depending on your configuration, the attacker could attempt to:

- Read accessible files.
- Steal credentials available to the process.
- Access internal services.
- Modify the machine.
- Abuse permissions or network access.

This can turn a workflow-security mistake into a server-security incident.

## A simple example

Imagine a self-hosted runner has a file containing a sensitive token.

An unsafe workflow could attempt to read it:

```yaml
- name: Unsafe Example
  run: cat /path/to/a/sensitive/file
```

This is an illustrative malicious pattern, not a recommended workflow step.

If the runner process can read the file, an untrusted job might be able to read it too.

A malicious workflow might then try to transmit the data elsewhere.

The fundamental issue is not the YAML syntax itself. It is that untrusted code is being allowed to execute inside an environment containing resources the attacker should not access.

## Why GitHub-hosted runners are often preferred for public contributions

Standard GitHub-hosted runners normally provide a fresh environment for each job.

After the job, the environment is discarded.

This reduces the risk that one job leaves malicious modifications behind for a later job on the same persistent machine.

However, GitHub-hosted runners do not magically make every workflow safe. You still need to protect secrets, permissions, and trusted workflow operations.

## Why persistent self-hosted runners need extra care

A persistent self-hosted machine may retain:

- Files from earlier jobs.
- Local credentials.
- Cached dependencies.
- Tool configurations.
- Network access.
- Other sensitive state.

If jobs from untrusted contributors can access that environment, the risks increase.

For public repositories, do not allow untrusted pull-request code to run on a sensitive, persistent self-hosted runner.

Use isolated ephemeral runners or other carefully designed security controls when self-hosted execution is genuinely required.

## A crucial correction

Self-hosted runners are not universally forbidden for public repositories.

They can be used safely in appropriately designed environments, but the security model requires careful thought.

**The dangerous combination is untrusted code plus a runner that exposes sensitive resources or persists state.**

Also, GitHub-hosted jobs can have access to secrets or permissions if the workflow configuration allows them. A hosted runner is not a substitute for correct workflow permissions.

Official security documentation:

https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions

---

# 12. Runner Isolation and Job Execution

Now let's understand a subtle but extremely important behavior.

Consider this workflow:

```yaml
name: Runner Isolation

on:
  workflow_dispatch:

jobs:
  first-job:
    runs-on: ubuntu-latest

    steps:
      - name: Create File
        run: |
          echo "Hello from Job 1" > message.txt
          cat message.txt

  second-job:
    runs-on: ubuntu-latest

    steps:
      - name: Look for File
        run: |
          if [ -f message.txt ]; then
            cat message.txt
          else
            echo "File does not exist in this job"
          fi
```

Both jobs use:

```yaml
runs-on: ubuntu-latest
```

You might think that both jobs share the same machine because the label is identical.

That assumption is incorrect.

**The same runner label does not mean the same runner instance or shared filesystem.**

For standard GitHub-hosted runners, each job normally gets its own fresh runner environment.

Therefore:

1. `first-job` creates `message.txt` in its workspace.
2. `first-job` finishes.
3. `second-job` executes in a separate job environment.
4. `second-job` cannot normally find the file created by `first-job`.

Expected output from the second job:

```text
File does not exist in this job
```

## Why does this happen?

Because a job's filesystem is part of its execution environment.

The next job does not automatically inherit the previous job's workspace.

Think of two hotel rooms.

Two guests may book rooms of the same type, but they do not share the same room or belongings.

In the same way, two jobs can request the same runner type without sharing their filesystem.

## Does every self-hosted job behave this way?

Not necessarily.

Self-hosted runners can use persistent machines, and their local files may remain between jobs.

Do not depend on this behavior for workflow communication, though. Use artifacts or another deliberate transfer mechanism.

---

# 13. How Jobs Share Files Using Artifacts

If jobs do not automatically share files, how does a CI/CD pipeline transfer build output?

GitHub Actions provides artifacts.

An artifact is a file or collection of files that a workflow can upload and later download.

Imagine this pipeline:

```text
Build job
   |
   | Produces application.jar
   v
Upload artifact
   |
   v
Download artifact
   |
   v
Test or deployment job
```

## Example: Upload and download a file

```yaml
name: Share Files Between Jobs

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Create File
        run: |
          echo "Build output" > message.txt

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: build-message
          path: message.txt

  consume:
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Download Artifact
        uses: actions/download-artifact@v4
        with:
          name: build-message

      - name: Read File
        run: cat message.txt
```

## What happens?

### In the build job

The first job creates:

```text
message.txt
```

The upload action stores the file as a workflow artifact.

### In the consume job

The second job declares:

```yaml
needs: build
```

This means it depends on the successful completion of the `build` job.

The download action retrieves the uploaded artifact into the receiving job's workspace.

Now this command works:

```bash
cat message.txt
```

## Important distinction

Artifacts and caches serve different purposes.

- **Artifacts** transfer and preserve files produced by workflow runs.
- **Caches** reuse suitable files to speed up future work, such as dependency downloads.

For example, a compiled application intended for a later job is a natural artifact.

A Maven dependency cache is a natural cache.

Neither mechanism means that two jobs share the same runner filesystem.

---

# 14. Multiple Jobs and Parallel Execution

A workflow can contain multiple jobs.

For example:

```yaml
name: Parallel Jobs

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Running tests"

  lint:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Checking code style"

  security:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Running security checks"
```

The workflow defines three jobs:

- `test`
- `lint`
- `security`

Because no dependencies are specified between these jobs, GitHub can schedule them independently.

They may run in parallel if sufficient runner capacity and concurrency allowance are available.

## Why is parallel execution useful?

Imagine that three independent jobs take:

```text
Test job:       4 minutes
Lint job:       2 minutes
Security job:   3 minutes
```

If all three can run concurrently, the total elapsed time can be closer to the duration of the longest job than to the sum of all three durations.

The actual result depends on scheduling, setup time, and resource availability.

## How do you create a dependency?

Use `needs`.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Build application"

  test:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - run: echo "Test application"
```

The `test` job waits for `build` to succeed before running under normal success conditions.

The flow becomes:

```text
Build
  |
  v
Test
```

The runner selection remains separate for each job.

The build and test jobs can use the same runner label while still receiving different standard hosted job environments.

---

# 15. Runner Labels and Runner Selection

Runner labels describe the environments that a runner can satisfy.

For example:

```yaml
runs-on: ubuntu-latest
```

selects a supported GitHub-hosted Ubuntu environment.

Self-hosted runners have labels that can be used to target a particular environment.

Example:

```yaml
runs-on: [self-hosted, linux, x64]
```

GitHub looks for an eligible self-hosted runner that matches all requested labels.

A label is not magic. A matching runner must exist and be available.

## What if a label does not exist?

Suppose you write:

```yaml
runs-on: my-special-runner
```

but no eligible runner has that label.

GitHub cannot assign the job to a matching runner.

The job may remain queued and eventually fail if no suitable runner becomes available within the applicable limits.

## Why use labels?

Labels help distinguish runners.

For example, an organization may register:

```text
linux
x64
docker
internal-network
```

A job can request a combination that identifies the environment it needs.

Only use labels that match your actual runner configuration.

## Standard hosted labels versus self-hosted labels

For GitHub-hosted runners, you generally choose a supported operating-system or runner label.

For self-hosted runners, labels help target machines registered to your repository or organization.

Larger runners use the labels configured for those runners.

The exact label depends on the runner configuration, so do not copy a placeholder and expect GitHub to discover your machine automatically.

---

# 16. GitHub-Hosted vs. Larger vs. Self-Hosted Runners

These are related concepts, but they are not interchangeable.

| Feature | Standard GitHub-hosted | Larger GitHub-hosted | Self-hosted |
|---|---|---|---|
| Who provides the machine? | GitHub | GitHub | You or your organization |
| Who manages the underlying machine? | GitHub | GitHub | You or your organization |
| Resource capacity | Predefined by offering | Higher supported capacity/configuration | Depends on your machine |
| Machine customization | Limited to supported environment | Additional supported configuration options | Extensive control |
| Maintenance responsibility | Mostly GitHub | Mostly GitHub | Your responsibility |
| Special internal network access | Limited to supported arrangements | Depends on configuration | Can be configured |
| Typical use | Common CI jobs | Resource-intensive jobs | Specialized or controlled environments |
| Security responsibility | Workflow and access configuration remain yours | Workflow and access configuration remain yours | Workflow, access, and host security remain yours |

## The simplest possible explanation

**Standard GitHub-hosted runner**

GitHub provides a standard managed environment.

**Larger runner**

GitHub provides a more powerful managed environment.

**Self-hosted runner**

You provide and manage the machine.

Do not confuse a larger runner with a self-hosted runner. A larger runner remains GitHub-hosted.

---

# 17. Runner Billing and Usage Limits

Runner usage is subject to GitHub's current product rules, account eligibility, and applicable limits.

Do not assume that all runner types are free or that every plan provides the same amount of usage.

Factors that may affect costs and availability include:

- Your GitHub plan.
- Whether the repository is public or private.
- The runner type and operating system.
- The amount of runner time consumed.
- Included usage and applicable quotas.
- Organization or enterprise policies.
- Storage and other billable services where applicable.

Larger runners can have different pricing from standard runners.

Self-hosted runners avoid the normal GitHub-hosted machine charge for the runner itself in many configurations, but they are not cost-free to operate. You may still pay for the server, cloud VM, network, storage, and maintenance.

GitHub's billing and runner policies can change, so consult the official documentation for current details:

https://docs.github.com/en/billing/concepts/product-billing/github-actions

For usage limits and supported configurations:

https://docs.github.com/en/actions

---

# 18. Practical Workflow 1 — Inspect Your Runner

Let's create your first practical experiment.

File:

`.github/workflows/01-runner-info.yml`

```yaml
name: "01 - Runner Information"

on:
  workflow_dispatch:

jobs:
  runner-info:
    runs-on: ubuntu-latest

    steps:
      - name: Display Runner Details
        run: |
          echo "Hello from GitHub Actions!"
          echo "Runner OS: $RUNNER_OS"
          echo "Runner Architecture: $RUNNER_ARCH"
          echo "Runner Name: $RUNNER_NAME"
          echo "Temporary Directory: $RUNNER_TEMP"
          echo "Workspace: $GITHUB_WORKSPACE"
          echo "Current Directory:"
          pwd
```

## What should you learn?

1. The workflow starts when you trigger it manually.
2. GitHub selects a runner matching `ubuntu-latest`.
3. The job's shell executes the commands.
4. The output appears in the Actions logs.
5. The environment variables provide information about the runner.

## How to run it

1. Create the YAML file.
2. Commit and push it to GitHub.
3. Open the repository's Actions tab.
4. Select the workflow.
5. Click **Run workflow**.
6. Open the run and inspect the job logs.

This is your first direct look at the environment in which a workflow job executes.

---

# 19. Practical Workflow 2 — Compare Operating Systems

File:

`.github/workflows/02-runner-os.yml`

```yaml
name: "02 - Runner Operating Systems"

on:
  workflow_dispatch:

jobs:
  ubuntu-job:
    runs-on: ubuntu-latest

    steps:
      - name: Inspect Ubuntu
        run: |
          echo "I am running on Ubuntu"
          uname -a
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"

  windows-job:
    runs-on: windows-latest

    steps:
      - name: Inspect Windows
        run: |
          echo "I am running on Windows"
          systeminfo
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"

  macos-job:
    runs-on: macos-latest

    steps:
      - name: Inspect macOS
        run: |
          echo "I am running on macOS"
          sw_vers
          echo "OS: $RUNNER_OS"
          echo "Architecture: $RUNNER_ARCH"
```

## Experiment

Run the workflow and open each job.

Compare:

- Operating system.
- Architecture.
- Command output.
- Runner setup and execution logs.

The jobs may run concurrently if your account's available concurrency permits it.

This experiment teaches you that `runs-on` selects an environment, and that different operating systems require different commands.

---

# 20. Practical Workflow 3 — Understand Job Isolation

File:

`.github/workflows/03-runner-isolation.yml`

```yaml
name: "03 - Runner Isolation"

on:
  workflow_dispatch:

jobs:
  create-file:
    runs-on: ubuntu-latest

    steps:
      - name: Create a File
        run: |
          echo "Hello from Job 1" > message.txt
          cat message.txt

  read-file:
    runs-on: ubuntu-latest

    steps:
      - name: Try to Read Job 1 File
        run: |
          if [ -f message.txt ]; then
            cat message.txt
          else
            echo "message.txt does not exist in this job"
          fi
```

## Predict the result

Will `read-file` find the file created by `create-file`?

**Expected answer: No, not on standard GitHub-hosted runners.**

The jobs have separate environments.

This experiment proves that the same runner label does not imply a shared filesystem.

To transfer the file, use artifacts as explained earlier.

---

# 21. Practical Workflow 4 — Inspect Runner Resources

File:

`.github/workflows/04-runner-resources.yml`

```yaml
name: "04 - Runner Resources"

on:
  workflow_dispatch:

jobs:
  inspect:
    runs-on: ubuntu-latest

    steps:
      - name: Inspect CPU
        run: |
          echo "Available processing units:"
          nproc

      - name: Inspect Memory
        run: |
          free -h

      - name: Inspect Disk
        run: |
          df -h /

      - name: Inspect Operating System
        run: |
          cat /etc/os-release
```

## What does this workflow teach?

- `nproc` reports the processing units available to the command.
- `free -h` displays memory statistics.
- `df -h /` reports disk-space statistics for the filesystem containing `/`.
- `/etc/os-release` describes the Linux distribution.

These commands help you inspect the environment.

They do not guarantee that the numbers shown correspond directly to the complete underlying host's hardware specifications.

## Important lesson

Do not memorize a single CPU or RAM number as if it applies to every `ubuntu-latest` runner.

Runner resources can depend on the current GitHub offering and runner configuration.

Use the current documentation when exact specifications matter.

---

# 22. Practical Workflow 5 — Understand Job Dependencies

File:

`.github/workflows/05-runner-dependencies.yml`

```yaml
name: "05 - Runner Job Dependencies"

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Build
        run: echo "Building application..."

  test:
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Test
        run: echo "Testing application..."

  deploy:
    runs-on: ubuntu-latest
    needs: test

    steps:
      - name: Deploy
        run: echo "Deploying application..."
```

## Execution order

```text
Build
  |
  v
Test
  |
  v
Deploy
```

The `needs` keyword creates dependencies between jobs.

Without a dependency, jobs can be scheduled independently.

With a dependency, a downstream job waits for its prerequisite under the normal success rules.

## Important distinction

The job dependency controls **when a job can start**.

The `runs-on` keyword controls **which runner environment the job requires**.

They solve different problems.

This example only prints messages. A real deployment workflow needs actual build, test, and deployment commands.

---

# 23. Practical Workflow 6 — A Java CI Pipeline

Now let's connect runner knowledge to a real Java project.

This workflow checks out a repository, configures Java 21, and runs Maven tests.

File:

`.github/workflows/06-java-ci.yml`

```yaml
name: "06 - Java CI on GitHub-Hosted Runner"

on:
  push:
  pull_request:

permissions:
  contents: read

jobs:
  java-ci:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set Up Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Display Java Version
        run: java -version

      - name: Run Maven Tests
        run: mvn --batch-mode clean verify
```

## Step-by-step explanation

### Step 1 — Trigger

```yaml
on:
  push:
  pull_request:
```

The workflow can run when code is pushed or a pull request event occurs, subject to GitHub's event and repository rules.

### Step 2 — Permissions

```yaml
permissions:
  contents: read
```

This grants the workflow the specified repository-content permission, rather than relying on broader default permissions.

### Step 3 — Select a runner

```yaml
runs-on: ubuntu-latest
```

The job uses a GitHub-hosted Ubuntu environment.

### Step 4 — Check out code

```yaml
uses: actions/checkout@v4
```

This action checks out the repository so that the build commands can access the project files.

### Step 5 — Configure Java

```yaml
uses: actions/setup-java@v4
```

This action configures the requested Java distribution and version.

### Step 6 — Run Maven

```yaml
run: mvn --batch-mode clean verify
```

Maven executes the project's verification lifecycle, including tests configured for that lifecycle.

**Requirement:** This example assumes your repository contains a suitable Maven project and a `pom.xml` at the repository root. For a multi-module project or a project in a subdirectory, you may need to adjust the command or working directory.

## What did the runner contribute?

The runner supplied the environment in which the entire job executed.

The workflow specified what needed to happen, while the runner provided the execution environment.

This is the connection between runners and real CI/CD.

---

# 24. Runner Security Best Practices

Runners execute commands, so they must be treated as security-sensitive infrastructure.

## Rule 1 — Be careful with untrusted pull requests

A public pull request may contain modified code or workflow definitions.

Do not allow untrusted code to execute on a persistent, sensitive self-hosted runner without an appropriate isolation and security design.

## Rule 2 — Protect secrets

Do not print tokens, passwords, or private keys in logs.

Avoid placing sensitive credentials in files or environment variables that untrusted code can access.

GitHub's secret-masking features are useful, but they are not a guarantee against every form of accidental disclosure.

## Rule 3 — Use least privilege

Give the workflow only the permissions it needs.

For example:

```yaml
permissions:
  contents: read
```

Add further permissions only when the workflow requires them.

## Rule 4 — Avoid unnecessary persistence

On self-hosted runners, clean up workspaces and sensitive files as appropriate.

For jobs handling untrusted content or sensitive credentials, isolated ephemeral runners are often preferable to long-lived shared machines.

## Rule 5 — Review third-party actions

A workflow can execute code from actions as well as shell commands.

Review the actions you use and consider pinning actions to full commit SHAs when stronger supply-chain control is required.

## Rule 6 — Do not confuse isolation with trust

A fresh GitHub-hosted runner reduces some risks associated with persistent machines, but it does not make a malicious workflow harmless.

A workflow can still misuse its own permissions, expose accessible secrets, or send data over the network.

## Rule 7 — Keep self-hosted infrastructure updated

If you manage the machine, you are responsible for its security configuration, operating-system updates, runner software, access controls, and network exposure.

Official guidance:

https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions

---

# 25. Common Runner Errors and Troubleshooting

Understanding runners also means understanding what can go wrong.

## Problem 1 — Job remains queued

Possible causes:

- No eligible runner is available.
- The requested label does not match a registered self-hosted runner.
- Concurrency limits prevent the job from starting.
- The runner group or repository access configuration blocks the job.

Check the job's status and runner configuration.

## Problem 2 — Command not found

Example:

```text
mvn: command not found
```

Possible cause:

Maven is not installed or is not available on `PATH`.

Possible solution:

Configure the required tools explicitly, or use a suitable runner image.

For Java projects, configure the JDK with `actions/setup-java`. If Maven is not otherwise available, add an appropriate installation step or use a Maven wrapper committed to the repository.

## Problem 3 — Out of memory

Possible cause:

The build or test suite needs more memory than is available.

Possible solutions:

- Inspect JVM memory settings.
- Investigate memory usage.
- Reduce unnecessary parallelism.
- Optimize the build.
- Consider a larger runner if the workload genuinely needs more memory.

Do not assume that every Java memory error is caused by the runner. The application or build configuration may also be responsible.

## Problem 4 — No space left on device

Possible cause:

The runner has insufficient available disk space.

Possible solutions:

- Remove unnecessary temporary files.
- Avoid downloading redundant dependencies.
- Reduce Docker image size and unnecessary build layers.
- Reduce generated output.
- Consider a runner with more available storage when appropriate.

## Problem 5 — File exists in one job but not another

Possible cause:

Each job has its own execution environment.

Solution:

Upload the file as an artifact and download it in the receiving job.

## Problem 6 — Workflow file appears empty on GitHub

Possible cause:

The file was committed without the expected contents, or the changes were not saved before committing.

Inspect the local file:

```bash
cat .github/workflows/your-workflow.yml
```

Check what Git committed:

```bash
git show HEAD:.github/workflows/your-workflow.yml
```

Inspect the commit summary:

```bash
git show --stat HEAD
```

A new workflow containing several lines of YAML should ordinarily appear as a file with inserted lines in the commit.

If the committed file is empty, fix it, commit the correction, and push again.

## Problem 7 — Workflow fails on one operating system

Possible cause:

The script uses commands or paths specific to another operating system.

For example, a Linux script may use Bash syntax that does not work unchanged in PowerShell.

Solution:

Use the appropriate shell and commands for the selected runner, or design the workflow to use compatible cross-platform tooling.

## Problem 8 — Larger-runner job cannot start

Possible causes:

- The configured larger runner is not available to your account or organization.
- The `runs-on` label is incorrect.
- The runner is not configured or is offline.
- Applicable access or concurrency limits prevent execution.

Solution:

Check runner availability, account eligibility, labels, and the relevant organization settings.

---

# 26. Frequently Asked Questions

## Q1. Is a runner the same thing as a workflow?

No.

A workflow defines the automation. A runner provides the environment that executes a job.

## Q2. Is a runner the same thing as a job?

No.

A job is a group of steps. A runner is the execution environment used by the job.

## Q3. Does `ubuntu-latest` mean the latest Ubuntu release in every case?

It refers to GitHub's supported `ubuntu-latest` runner image label, which can change over time. Consult the official documentation for the current mapping.

## Q4. Does every runner have the same CPU and RAM?

No.

Capacity depends on the runner offering and configuration.

## Q5. Are larger runners self-hosted runners?

No.

Larger runners are GitHub-hosted. Self-hosted runners are provided and managed by you or your organization.

## Q6. Can I use my laptop as a self-hosted runner?

Yes, technically, provided you install and configure the runner and meet the relevant requirements.

However, the laptop must remain available to receive and execute jobs. Consider power, connectivity, security, and maintenance before using it for important pipelines.

## Q7. Can two jobs use the same runner label?

Yes.

But the same label does not guarantee that they use the same machine or share a filesystem.

## Q8. Can two steps in one job share files?

Normally, yes.

Steps in the same job share the job's workspace and execution environment, unless a step or action changes or removes relevant files.

## Q9. How do I transfer files between jobs?

Use workflow artifacts or another deliberate transfer mechanism.

## Q10. Why not always use the most powerful runner?

Because higher capacity can cost more and may provide no benefit for a small or I/O-bound workload.

Choose resources based on the actual requirements.

## Q11. Are GitHub-hosted runners always secure?

No environment is automatically secure.

GitHub-hosted runners provide useful isolation properties, but workflow permissions, secrets, action dependencies, and untrusted code still require security controls.

## Q12. Are self-hosted runners always dangerous?

No.

They can be appropriate for controlled workloads. The risk depends on what code can execute, what resources the machine can access, how jobs are isolated, and how the runner is maintained.

## Q13. Does a successful push mean the runner worked?

No.

A push confirms that Git transferred the commit. The workflow may still fail to validate, find a runner, execute a command, or complete its job.

## Q14. Does the runner decide which steps to execute?

The workflow engine determines the job and step execution according to the workflow definition, dependencies, conditions, and job state. The runner executes the work assigned to it.

## Q15. Do I need to install GitHub Actions on a GitHub-hosted runner?

No. GitHub manages the hosted runner infrastructure. For self-hosted runners, you must install and configure the runner application.

---

# 27. Final Mental Model

Let's connect every major concept.

```text
                  GITHUB ACTIONS
                        |
                        v
                    WORKFLOW
             What should happen?
                        |
                        v
                      JOB
               A group of steps
                        |
                        v
                    RUNS-ON
             Which environment?
                        |
           +------------+------------+
           |            |            |
           v            v            v
       Standard      Larger      Self-hosted
       Hosted        Hosted       Runner
           |            |            |
           v            v            v
       GitHub        GitHub       You manage
       manages       manages      the machine
           |            |            |
           +------------+------------+
                        |
                        v
                 STEPS EXECUTE
                        |
                        v
                  LOGS AND RESULTS
```

## The four distinctions you must remember

### 1. Workflow versus runner

The workflow defines the automation; the runner executes a job.

### 2. Job versus step

A job groups steps. Each step performs a specific operation.

### 3. Standard versus larger runner

Both are GitHub-hosted. A larger runner offers greater supported resource capacity and configuration options.

### 4. GitHub-hosted versus self-hosted

GitHub manages hosted runners. You manage self-hosted machines.

## The most important rule

**Choose a runner based on the environment, resources, security, and network access your workload requires—not simply because one label sounds more powerful.**

A standard runner is often enough for ordinary CI.

A larger runner can help with demanding builds.

A self-hosted runner can help when you need specialized hardware, controlled infrastructure, or particular network access—but it also brings operational and security responsibilities.

---

# 28. Learning Checklist

Use this checklist to verify your understanding.

- [ ] I can explain what a runner is in simple words.
- [ ] I understand the relationship between workflow, job, runner, and step.
- [ ] I know what `runs-on` does.
- [ ] I can trigger a workflow manually using `workflow_dispatch`.
- [ ] I can inspect runner information using default environment variables.
- [ ] I understand the difference between CPU, RAM, storage, and installed software.
- [ ] I can compare Linux, Windows, and macOS runner environments.
- [ ] I understand the difference between standard and larger GitHub-hosted runners.
- [ ] I understand how self-hosted runners are configured and managed.
- [ ] I can explain why untrusted public pull requests are dangerous on sensitive self-hosted runners.
- [ ] I understand why separate jobs do not automatically share files.
- [ ] I can use artifacts to transfer files between jobs.
- [ ] I understand how `needs` controls job dependencies.
- [ ] I can inspect basic runner resource information.
- [ ] I can troubleshoot missing commands, insufficient disk space, and runner-selection failures.
- [ ] I can explain the main security considerations when selecting runners.

If you can explain each point in your own words and complete the practical workflows, you have a strong foundation in GitHub Actions runners.

**Next recommended topic:** GitHub Actions job execution, dependencies, outputs, and artifacts. These concepts build naturally on runner knowledge and help you design complete multi-job CI/CD pipelines.

---

## Official Documentation

1. GitHub-hosted runners: https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners
2. Larger runners: https://docs.github.com/en/actions/using-github-hosted-runners/about-larger-runners
3. Self-hosted runners: https://docs.github.com/en/actions/hosting-your-own-runners/about-self-hosted-runners
4. Workflow syntax: https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax
5. Variables and contexts: https://docs.github.com/en/actions/reference/workflows-and-actions/variables
6. Artifacts: https://docs.github.com/en/actions/using-workflows/storing-workflow-data-as-artifacts
7. Security hardening: https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions
8. Billing: https://docs.github.com/en/billing/concepts/product-billing/github-actions

**End of Runners Guide**

*Learn the machine. Understand the job. Build reliable automation.*