# GitHub Actions — Complete Guide to Concurrency

**A practical, beginner-friendly, and technically comprehensive guide to controlling simultaneous workflow runs and jobs in GitHub Actions.**

> **The central idea:** Concurrency controls how GitHub Actions handles workflow runs or jobs that compete for the same named concurrency group. It helps prevent overlapping work, coordinate deployments, and decide whether newer work may cancel older work.

---

## Table of Contents

1. [The Problem Concurrency Solves](#1-the-problem-concurrency-solves)
2. [Understanding Workflows, Runs, Jobs, and Steps](#2-understanding-workflows-runs-jobs-and-steps)
3. [What Is Concurrency?](#3-what-is-concurrency)
4. [Concurrency Groups](#4-concurrency-groups)
5. [The `cancel-in-progress` Setting](#5-the-cancel-in-progress-setting)
6. [Workflow-Level Concurrency](#6-workflow-level-concurrency)
7. [Job-Level Concurrency](#7-job-level-concurrency)
8. [Workflow-Level vs Job-Level Concurrency](#8-workflow-level-vs-job-level-concurrency)
9. [The Mixed-Settings Rule](#9-the-mixed-settings-rule)
10. [Running, Pending, and Cancelled Work](#10-running-pending-and-cancelled-work)
11. [Static vs Dynamic Concurrency Groups](#11-static-vs-dynamic-concurrency-groups)
12. [Concurrency with Branches](#12-concurrency-with-branches)
13. [Concurrency with Environments](#13-concurrency-with-environments)
14. [Concurrency with Matrix Jobs](#14-concurrency-with-matrix-jobs)
15. [Concurrency with `needs`](#15-concurrency-with-needs)
16. [Concurrency Across Different Workflows](#16-concurrency-across-different-workflows)
17. [Real-World Deployment Scenarios](#17-real-world-deployment-scenarios)
18. [Common Mistakes](#18-common-mistakes)
19. [Best Practices](#19-best-practices)
20. [Complete Revision Table](#20-complete-revision-table)

---

# 1. The Problem Concurrency Solves

Imagine that you have a Java Spring Boot application running in production.

Your GitHub Actions workflow automatically deploys the application when you push code.

Now imagine this sequence:

1. You push commit A.
2. GitHub starts deployment A.
3. Before deployment A finishes, you push commit B.
4. GitHub starts deployment B.
5. Both deployments attempt to update the same production environment.

Depending on the deployment process, the two deployments could interfere with each other. One might overwrite files or configuration changed by the other. A deployment could also finish with an older version after a newer version has already been deployed.

The underlying problem is that **multiple operations are competing to modify the same resource at the same time**.

We need a mechanism that lets us tell GitHub Actions:

> For this particular resource, control which workflow runs or jobs are allowed to execute simultaneously.

GitHub Actions provides **concurrency groups** for this purpose.

Concurrency is especially useful for:

- Production deployments.
- Staging deployments.
- Infrastructure changes.
- Release workflows.
- Jobs that modify shared resources.
- Avoiding unnecessary work when newer changes arrive.
- Coordinating automation spread across different workflow files.

However, concurrency is a scheduling mechanism. It does not automatically make a deployment atomic, roll back completed changes, or guarantee that every release will run.

---

# 2. Understanding Workflows, Runs, Jobs, and Steps

Before learning concurrency, we must understand four basic terms.

Consider this workflow:

```yaml
name: Spring Boot CI/CD

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Compile application
        run: echo "Compiling Java application"

  test:
    runs-on: ubuntu-latest
    steps:
      - name: Run tests
        run: echo "Testing application"

  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy application
        run: echo "Deploying application"
```

## 2.1 Workflow

A **workflow** is the YAML file that defines your automation.

For example:

`.github/workflows/deploy.yml`

It describes when the automation should run and which jobs it contains.

## 2.2 Workflow run

A **workflow run** is one execution of that workflow.

If you click **Run workflow** three times, you can create three separate workflow runs.

Think of the YAML file as a recipe and each workflow run as one attempt to follow that recipe.

## 2.3 Job

A **job** is a unit of work inside a workflow.

In our example, the jobs are:

- `build`
- `test`
- `deploy`

Each job runs on a runner and contains steps.

## 2.4 Step

A **step** is an individual operation inside a job.

For example:

```yaml
steps:
  - name: Print message
    run: echo "Hello"
```

### Why do these distinctions matter?

Because concurrency can be configured at two different scopes:

- **Workflow-level concurrency** controls whole workflow runs.
- **Job-level concurrency** controls individual jobs.

These scopes are related, but they are not interchangeable.

---

# 3. What Is Concurrency?

Concurrency means allowing or controlling multiple operations that may execute at the same time.

In GitHub Actions, concurrency configuration identifies workflow runs or jobs that belong to the same group and controls how they compete for execution.

Consider this configuration:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

It contains two important properties:

- `group` identifies the work that must share a concurrency rule.
- `cancel-in-progress` controls whether newly arriving competing work can cancel work that is already running.

Think of a production environment as a room with one controlled entrance.

- The group name identifies the room.
- The running job is already inside.
- Another competing job arrives at the entrance.
- GitHub applies the concurrency rules to determine what happens next.

This is only a mental model, but it helps explain the basic behavior.

**Important:** concurrency is not a general-purpose queue. GitHub allows at most one running and one pending member per concurrency group. A newer pending member can replace an older pending member.

---

# 4. Concurrency Groups

A concurrency group is a name shared by workflow runs or jobs that should follow the same concurrency rule.

Example:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Here, the group name is:

`production-deployment`

If another workflow or job uses the same group name, it competes with the first one under the applicable concurrency rules.

## 4.1 Same group name

Workflow A:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Workflow B:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

Both use `production-deployment`.

Therefore, they share the same group even though they have different cancellation settings.

The settings do not create separate groups. The `group` value identifies the group.

## 4.2 Different group names

Workflow A:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Workflow B:

```yaml
concurrency:
  group: staging-deployment
  cancel-in-progress: false
```

These workflows use different groups.

They do not compete with each other through these concurrency groups.

This is useful when production and staging should be managed independently.

## 4.3 Why group naming matters

Suppose three workflow files can deploy to the same production environment.

If each file uses a different group name, they will not coordinate through those groups.

If they all use the same group name, they can share the concurrency restriction.

Therefore, group names should reflect the resource being protected—not merely the name of the YAML file.

---

# 5. The `cancel-in-progress` Setting

This setting determines whether newly arriving competing work can cancel the currently running member of the same group.

There are two primary choices.

## 5.1 `cancel-in-progress: true`

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

This allows a newly competing workflow run or job to cancel the currently running member of the same group.

### Example

1. Deployment A starts.
2. Deployment A is still running.
3. Deployment B arrives in the same group.
4. Deployment B has `cancel-in-progress: true`.
5. GitHub can cancel Deployment A so Deployment B can proceed.

### When is this useful?

It can be useful when newer work should replace older work, such as certain validation or preview workflows.

For production deployments, use it only when interruption is acceptable and your deployment process can safely handle partial execution.

## 5.2 `cancel-in-progress: false`

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

A newly competing job does not automatically cancel the currently running member of the group.

### Example

1. Deployment A starts.
2. Deployment B arrives in the same group.
3. Deployment B has `cancel-in-progress: false`.
4. Deployment A is not automatically cancelled by Deployment B.
5. Deployment B may become pending.

This is often more suitable when you want a running deployment to finish rather than being interrupted by a newer request.

However, it does **not** mean every pending deployment is guaranteed to run.

## 5.3 Cancellation is not rollback

Suppose a deployment performs these actions:

1. Uploads a new application image.
2. Updates a cloud resource.
3. Changes a database.
4. Restarts a service.

If the job is cancelled after step 2, GitHub does not automatically undo step 2.

Cancellation stops the job's execution, but completed external changes may remain.

Production workflows should therefore be designed for interruption, recovery, and safe retries.

---

# 6. Workflow-Level Concurrency

Workflow-level concurrency is configured at the top level of the workflow file, alongside `name`, `on`, and `jobs`.

Example:

```yaml
name: Workflow-Level Concurrency Demo

on:
  workflow_dispatch:

# This block is outside the jobs section.
# It controls concurrency for whole workflow runs.
concurrency:
  group: complete-workflow
  cancel-in-progress: false

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build
        run: |
          echo "Build started"
          sleep 30
          echo "Build finished"

  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy
        run: |
          echo "Deployment started"
          sleep 30
          echo "Deployment finished"
```

## What happens?

The concurrency block applies to the **whole workflow run**.

If another workflow run competes for the same group, GitHub applies the concurrency rules at the workflow-run level.

This means the restriction is not limited to only the `deploy` job.

## When should you use it?

Use workflow-level concurrency when you want to coordinate entire workflow runs.

For example, you may want to avoid overlapping runs of an automation that performs a sequence of related operations against one shared resource.

---

# 7. Job-Level Concurrency

Job-level concurrency is configured inside a specific job.

Example:

```yaml
name: Job-Level Concurrency Demo

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build
        run: |
          echo "Build started"
          sleep 30
          echo "Build finished"

  deploy:
    runs-on: ubuntu-latest

    # This concurrency rule applies to deploy only.
    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - name: Deploy
        run: |
          echo "Deployment started"
          sleep 60
          echo "Deployment finished"
```

## What happens?

The `deploy` job participates in `production-deployment`.

The `build` job does not participate in that group because it has no concurrency block.

The build and deploy jobs may start in parallel because no dependency is defined, subject to runner availability and other scheduling constraints.

If another deployment job arrives in the same group, the deployment jobs compete under that group's rules.

## Why is job-level concurrency useful?

Imagine a workflow that contains:

- Code checkout.
- Compilation.
- Unit tests.
- Integration tests.
- Package creation.
- Production deployment.

Perhaps builds and tests can safely run in parallel across different workflow runs, but production deployments should not overlap.

You can apply concurrency to the deployment job without automatically applying the same restriction to every other job.

---

# 8. Workflow-Level vs Job-Level Concurrency

| Feature | Workflow-level concurrency | Job-level concurrency |
|---|---|---|
| Where is it declared? | Top level of the workflow file | Inside an individual job |
| What does it control? | Whole workflow runs | The configured job |
| Can it coordinate across workflow files? | Yes, when group names match | Yes, when group names match |
| Does it automatically control every job in a workflow? | It controls the workflow run as a whole | No |
| Typical use | Prevent overlapping workflow runs | Prevent overlapping deployment jobs |
| Can it use a dynamic group? | Yes | Yes |

### Simple decision rule

Choose **workflow-level concurrency** when the whole workflow run should share one concurrency restriction.

Choose **job-level concurrency** when only a particular job needs that restriction.

They solve similar scheduling problems at different scopes.

---

# 9. The Mixed-Settings Rule

This is one of the most important details to understand.

When two jobs share a group but use different `cancel-in-progress` values, the **incoming job's setting** determines whether it can cancel the running job.

| Currently running job | Incoming job | Can the incoming job cancel the running job? |
|---|---|---|
| `false` | `false` | No |
| `true` | `false` | No |
| `false` | `true` | Yes |
| `true` | `true` | Yes |

Assume all jobs use the same group.

## Scenario A — A job with `false` arrives

- Job A is running.
- Job B arrives in the same group.
- Job B has `cancel-in-progress: false`.

Job B does not automatically cancel Job A, even if Job A was configured with `cancel-in-progress: true`.

## Scenario B — A job with `true` arrives

- Job A is running.
- Job B arrives in the same group.
- Job B has `cancel-in-progress: true`.

Job B can cancel Job A, even if Job A was configured with `cancel-in-progress: false`.

### The rule to memorize

**Look at the incoming job's cancellation setting—not only the running job's setting.**

A different group means no competition through that group, regardless of these settings.

---

# 10. Running, Pending, and Cancelled Work

Concurrency is not an unlimited waiting line.

For a particular concurrency group, GitHub permits at most:

- One running workflow run or job.
- One pending workflow run or job.

When new work arrives, GitHub applies the group's concurrency and cancellation rules.

## Example sequence

Imagine three jobs use the same group.

1. Job A starts running.
2. Job B arrives and becomes pending.
3. Job C arrives while Job A is still running.
4. Job C can replace Job B as the pending member.
5. Job B is cancelled, and Job C becomes pending.

Conceptually:

```text
Job A: RUNNING
Job B: PENDING

Job C arrives in the same group

Job A: RUNNING
Job B: REPLACED / CANCELLED
Job C: PENDING
```

The precise outcome depends on the incoming job's cancellation configuration and GitHub's scheduling behavior.

## Why does this matter?

Suppose you trigger five production deployments in quick succession.

Do not assume GitHub will preserve all five as a durable queue and execute them in order.

Concurrency is designed to control simultaneous execution, not to guarantee that every request will eventually run.

If every release must be processed, use a release process designed to preserve and execute every required release.

---

# 11. Static vs Dynamic Concurrency Groups

A concurrency group can be a fixed string or a value calculated from a GitHub Actions expression.

## 11.1 Static group

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Every run or job using this exact group competes with other members of that group.

This is simple and useful when all work targets the same resource.

## 11.2 Dynamic group

```yaml
concurrency:
  group: deploy-${{ github.ref }}
  cancel-in-progress: false
```

Here, `${{ github.ref }}` contributes the current Git reference.

For example, different branches normally produce different group names.

This lets you isolate concurrency by branch or reference rather than making all branches compete for one group.

## 11.3 Group by workflow and branch

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: false
```

This creates a group using both the workflow name and Git reference.

It can help keep separate workflows and branches from competing unnecessarily.

However, if two different workflows must coordinate access to the same production resource, including different workflow names in their group expressions may separate them unintentionally.

**Design principle:** include only the dimensions that should make jobs compete. Use a common group for work that must share the same concurrency restriction.

---

# 12. Concurrency with Branches

Suppose your repository has:

- `main`
- `develop`
- `feature/login`

You may want work on each branch to be coordinated independently.

Example:

```yaml
name: Branch Concurrency Demo

on:
  push:
  workflow_dispatch:

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Run tests
        run: |
          echo "Testing this Git reference"
          sleep 60
```

Because the group contains `github.ref`, different Git references normally produce different groups.

For example, repeated workflow runs for the same branch can compete with one another, while runs for different branches normally use different groups.

With `cancel-in-progress: true`, a new competing run can cancel an older running run in the same group.

This pattern can be useful for CI workflows where the latest commit on a branch matters more than completing every older test run.

It may be inappropriate for workflows that must execute every event or perform irreversible operations.

---

# 13. Concurrency with Environments

Production and staging often need different concurrency rules.

For example:

- Staging deployments may be allowed independently of production deployments.
- Two production deployments should not overlap.
- Two staging deployments should not overlap.

You can use a manual input to choose an environment and construct the group dynamically.

```yaml
name: Deploy by Environment

on:
  workflow_dispatch:
    inputs:
      environment:
        description: Select the deployment environment
        type: choice
        required: true
        options:
          - staging
          - production

jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: deploy-${{ inputs.environment }}
      cancel-in-progress: false

    steps:
      - name: Show selected environment
        run: echo "Deployment job started"
```

If the selected environment is `production`, the group becomes:

`deploy-production`

If the selected environment is `staging`, the group becomes:

`deploy-staging`

Jobs targeting production compete with other jobs using `deploy-production`. Staging jobs compete with other jobs using `deploy-staging`.

### Important production consideration

Concurrency does not replace GitHub environment protection rules, approval requirements, deployment permissions, or secure secrets handling.

Use these mechanisms together when protecting a production environment.

---

# 14. Concurrency with Matrix Jobs

A matrix lets GitHub Actions create multiple job executions from a set of values.

Example:

```yaml
name: Matrix Concurrency Demo

on:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: ['21', '25']

    concurrency:
      group: java-test-${{ matrix.java }}
      cancel-in-progress: false

    steps:
      - name: Show Java version
        run: echo "Testing Java ${{ matrix.java }}"
```

The matrix creates separate job executions for the selected Java versions.

The group expression includes the matrix value.

Therefore:

- The Java 21 job uses `java-test-21`.
- The Java 25 job uses `java-test-25`.

These jobs use different groups and do not compete with each other through those groups.

If you instead used the same fixed group for every matrix execution, those executions would compete within that group.

### When is this useful?

Dynamic matrix groups are useful when you want to control concurrency separately for each version, region, environment, or other matrix dimension.

Choose the group carefully: adding a matrix value makes that value part of the group identity, which can reduce coordination between jobs that would otherwise share a group.

---

# 15. Concurrency with `needs`

`needs` and `concurrency` solve different problems.

## `needs` controls dependencies

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Build completed"

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying application"
```

The deploy job depends on the build job.

Normally, deployment waits for the build to succeed.

## `concurrency` controls competing work

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploying application"
```

The deployment job shares a concurrency group with other jobs using that group.

## Use both when needed

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Build completed"

  deploy:
    needs: build
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploying after a successful build"
```

This means:

1. Build the application.
2. Wait for the build to succeed.
3. Schedule the deployment job under its concurrency rule.

Remember:

- `needs` establishes dependencies.
- `concurrency` coordinates competing work.

---

# 16. Concurrency Across Different Workflows

A common misconception is that each workflow file has its own completely separate concurrency groups.

That is not correct.

Consider two files.

Workflow A:

```yaml
name: Deploy Application A

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: shared-production
      cancel-in-progress: false

    steps:
      - run: echo "Deploying application A"
```

Workflow B:

```yaml
name: Deploy Application B

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: shared-production
      cancel-in-progress: false

    steps:
      - run: echo "Deploying application B"
```

Both jobs use `shared-production`.

Therefore, they compete under the same group even though they are defined in different workflow files.

This can be useful if both workflows modify the same resource.

However, it can also be a problem if the applications are unrelated and should deploy independently.

The correct group depends on the resource and coordination requirement.

---

# 17. Real-World Deployment Scenarios

## Scenario A — Run only the latest CI work for each branch

```yaml
name: Branch CI

on:
  push:
  pull_request:

concurrency:
  group: ci-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Run CI checks"
```

This pattern lets newer competing runs cancel older running work within the same group.

It can reduce wasted CI resources when newer commits supersede earlier ones.

Be careful with workflows that must process every event or perform important side effects.

## Scenario B — Do not automatically cancel a running production deployment

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploy to production"
```

A new competing deployment does not automatically cancel the currently running deployment.

However, only one pending member is permitted, so this does not guarantee that every requested deployment will run.

## Scenario C — Separate staging and production

```yaml
concurrency:
  group: deploy-${{ inputs.environment }}
  cancel-in-progress: false
```

Each environment gets its own group.

This avoids forcing staging and production deployments to compete with one another through a single group.

## Scenario D — Protect a shared infrastructure operation

Suppose multiple workflows update the same infrastructure.

Use a common group for the operation that must not overlap:

```yaml
jobs:
  terraform:
    runs-on: ubuntu-latest

    concurrency:
      group: shared-infrastructure-production
      cancel-in-progress: false

    steps:
      - run: echo "Run the infrastructure operation"
```

This is a scheduling control, not a substitute for Terraform state locking or other infrastructure safety mechanisms.

---

# 18. Common Mistakes

## Mistake 1 — Thinking the group is just a label

The group name identifies which jobs or workflow runs compete for concurrency.

## Mistake 2 — Thinking different workflow files cannot share a group

They can. Matching evaluated group names allow them to compete.

## Mistake 3 — Assuming `cancel-in-progress: false` guarantees every job will run

It does not. A group allows one running and one pending member, and newer pending work can replace older pending work.

## Mistake 4 — Assuming `cancel-in-progress: true` cancels all jobs

It only affects competing work in the same group.

## Mistake 5 — Putting concurrency on one job and expecting every job to be controlled

Job-level concurrency applies only to the job where it is declared.

## Mistake 6 — Confusing `needs` with concurrency

`needs` establishes dependencies. Concurrency controls competing runs or jobs.

## Mistake 7 — Assuming cancellation rolls back changes

Cancellation does not automatically undo completed actions.

## Mistake 8 — Assuming concurrency guarantees FIFO order

Concurrency is not a guaranteed first-in, first-out queue.

## Mistake 9 — Creating different groups for workflows that modify the same resource

Different groups do not coordinate with each other through concurrency. If the workflows must share a restriction, use a common group.

## Mistake 10 — Creating one global group unnecessarily

If every job uses the same group, unrelated work may block one another. Use separate groups when the underlying resources can safely be handled independently.

---

# 19. Best Practices

1. **Choose a group based on the resource being protected.** For example, `production-deployment`.
2. **Use the same group across workflows that modify the same resource.** Otherwise, they may not coordinate.
3. **Use `cancel-in-progress: true` only when interruption is acceptable.** It can be useful for superseded CI work but needs care in deployment workflows.
4. **Use `cancel-in-progress: false` when a running job should normally finish.** Remember that pending jobs can still be replaced.
5. **Do not use concurrency as a guaranteed queue.** Design an explicit release process if every deployment must run.
6. **Use `needs` to establish job order.** Do not expect concurrency to enforce build-before-deploy dependencies.
7. **Use dynamic groups intentionally.** Branch, environment, workflow, and matrix values can isolate or combine concurrency scopes.
8. **Avoid accidental group collisions.** Two unrelated jobs with the same group name may compete.
9. **Use production protection mechanisms in addition to concurrency.** Include appropriate permissions, approvals, environment rules, and deployment safeguards.
10. **Make external operations recoverable.** Cancellation may happen after some side effects have occurred.
11. **Test with harmless jobs first.** Confirm behavior before applying concurrency to production.
12. **Read the scope carefully.** A top-level concurrency block and a job-level concurrency block control different units of work.

---

# 20. Complete Revision Table

| Question | Answer |
|---|---|
| What is concurrency? | A mechanism for coordinating competing workflow runs or jobs. |
| What is a concurrency group? | A named group used to identify work that shares a concurrency restriction. |
| What does `cancel-in-progress: true` do? | Allows incoming competing work to cancel the running member of the same group. |
| What does `cancel-in-progress: false` do? | Does not automatically cancel the running member when new competing work arrives. |
| Which setting matters in mixed scenarios? | The incoming job or run's cancellation setting. |
| What is workflow-level concurrency? | Concurrency control applied to whole workflow runs. |
| What is job-level concurrency? | Concurrency control applied to a specific job. |
| Can different workflow files share a group? | Yes, if their evaluated group names match. |
| Can branches have separate groups? | Yes, by using a branch or Git reference in the group expression. |
| Can environments have separate groups? | Yes, by including the selected environment in the group expression. |
| Can matrix jobs have separate groups? | Yes, by including matrix values in the group expression. |
| What does `needs` do? | Establishes job dependencies and execution order. |
| Is concurrency an unlimited queue? | No. Each group allows at most one running and one pending member. |
| Can newer pending work replace older pending work? | Yes. |
| Does cancellation roll back deployment changes? | No. |
| Does concurrency replace infrastructure state locking? | No. |
| Can concurrency and `needs` be used together? | Yes. |

---

# The Mental Model to Remember

Imagine a controlled entrance to a production environment.

- **The group name** tells GitHub which jobs are competing for the same entrance.
- **The running job** is already inside.
- **The pending job** is waiting.
- **`cancel-in-progress: true` on incoming work** means GitHub can cancel the running member of that same group.
- **`cancel-in-progress: false` on incoming work** means it does not automatically cancel the running member.
- **The group is not an unlimited queue.** There can be one running and one pending member, and newer pending work can replace older pending work.
- **Workflow-level concurrency** controls whole workflow runs.
- **Job-level concurrency** controls the job where it is declared.
- **`needs`** controls dependencies and execution order.
- **Cancellation is not rollback.** Completed external changes may remain.

Once you understand these rules, you can use concurrency to coordinate CI pipelines, deployments, infrastructure automation, and other workflows without confusing it with job dependencies or a guaranteed release queue.
