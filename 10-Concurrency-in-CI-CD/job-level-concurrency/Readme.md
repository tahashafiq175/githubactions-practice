# GitHub Actions — Job-Level Concurrency

A comprehensive, beginner-friendly guide to understanding how GitHub Actions controls the execution of individual jobs, prevents competing jobs from running simultaneously, and handles cancellation and pending work.

> **The central idea:** Job-level concurrency allows you to group jobs under a shared scheduling rule. GitHub can then control which job runs, which job waits, and whether a newly arriving job can cancel a currently running job in that group.

---

## Table of Contents

1. [The Problem Concurrency Solves](#1-the-problem-concurrency-solves)
2. [Understanding Job-Level Concurrency](#2-understanding-job-level-concurrency)
3. [The Three Important Components](#3-the-three-important-components)
4. [Example 1 — Basic Job-Level Concurrency](#4-example-1--basic-job-level-concurrency)
5. [Example 2 — Independent Build and Deploy Jobs](#5-example-2--independent-build-and-deploy-jobs)
6. [How Both Workflows Interact](#6-how-both-workflows-interact)
7. [The Mixed-Settings Rule](#7-the-mixed-settings-rule)
8. [Running, Pending, and Cancelled Jobs](#8-running-pending-and-cancelled-jobs)
9. [Job-Level vs Workflow-Level Concurrency](#9-job-level-vs-workflow-level-concurrency)
10. [Dynamic Concurrency Groups](#10-dynamic-concurrency-groups)
11. [Concurrency and Job Dependencies](#11-concurrency-and-job-dependencies)
12. [Common Mistakes](#12-common-mistakes)
13. [Best Practices](#13-best-practices)
14. [Complete Revision Table](#14-complete-revision-table)

---

## 1. The Problem Concurrency Solves

Imagine that your team maintains an application running in a production environment.

Two developers trigger deployments at almost the same time.

- Deployment A begins updating production.
- Deployment B begins updating production before Deployment A finishes.
- Both jobs attempt to modify the same environment.

Depending on the deployment process, one deployment could overwrite the other's changes or leave the environment in an unexpected state.

The problem is not necessarily that either deployment is incorrect. The problem is that **two operations are attempting to modify the same resource at the same time**.

We need a mechanism that tells GitHub Actions:

> Only allow one deployment job at a time for this production environment. Decide what to do when another deployment arrives.

GitHub Actions provides **concurrency groups** to help manage this situation.

Concurrency is a scheduling mechanism. It is not a complete replacement for deployment locks, environment protection rules, or recovery procedures.

## 2. Understanding Job-Level Concurrency

Before learning concurrency, understand the structure of a GitHub Actions workflow.

```yaml
name: My Application Pipeline

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Building application"

  test:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Testing application"

  deploy:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying application"
```

Think of the workflow as a company, jobs as departments, and steps as the tasks performed by each department.

Here, the workflow contains three jobs:

- `build`
- `test`
- `deploy`

Each job is a separate unit of work that GitHub Actions schedules.

### Where does job-level concurrency go?

It is written **inside the job**, at the same indentation level as `runs-on` and `steps`.

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: true

    steps:
      - run: echo "Deploying application"
```

Let's understand every part:

- `deploy` identifies the job.
- `concurrency` enables concurrency control for that job.
- `group` identifies the shared concurrency group.
- `cancel-in-progress` specifies whether a newly arriving competing job can cancel the currently running job.

The most important rule is:

**Job-level concurrency controls the job in which it is declared. It does not automatically control every job in the workflow.**

---

## 3. The Three Important Components

### 3.1 `concurrency`

This tells GitHub Actions that the job must follow a concurrency rule.

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

It is configuration, not a shell command.

You do not need to install a package, write a script, or create a special runner to use it.

### 3.2 `group`

```yaml
group: production-deployment
```

Think of a concurrency group as a shared waiting-and-running area.

Any jobs or workflow runs that resolve to the same group name compete under that group's concurrency rules.

For example, Workflow A could contain:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

Workflow B could contain:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Both use the exact same group name:

`production-deployment`

Therefore, they compete with one another even though they are in different workflow files.

Now consider a third job:

```yaml
concurrency:
  group: staging-deployment
  cancel-in-progress: false
```

This job belongs to a different group. It does not compete with `production-deployment` through that group.

**Remember:** the group name determines which jobs compete. The workflow filename does not automatically isolate them.

### 3.3 `cancel-in-progress`

This setting answers one specific question:

> If a new job arrives while another job in the same group is running, can the new job cancel the running job?

There are two main choices.

**Option A — `true`**

```yaml
cancel-in-progress: true
```

A newly arriving competing job can cancel the currently running job in the same group.

**Option B — `false`**

```yaml
cancel-in-progress: false
```

A newly arriving competing job does not automatically cancel the currently running job. It may become pending instead.

An important detail: in mixed-settings scenarios, the **incoming job's setting** determines whether that incoming job can cancel the current running job.

We will examine this carefully later.

---

## 4. Example 1 — Basic Job-Level Concurrency

Create the file:

`.github/workflows/job-concurrency-1.yml`

```yaml
name: "Job Concurrency 1 - Basic"

on:
  workflow_dispatch: # Allows you to start the workflow manually.

jobs:
  deploy: # This is the job we want to control.
    runs-on: ubuntu-latest # GitHub provides an Ubuntu runner.

    concurrency: # Apply concurrency rules to this job.
      group: production-deployment # Jobs with this group name compete.
      cancel-in-progress: true # A new competing job can cancel the running job.

    steps:
      - name: Start deployment
        run: |
          echo "Deployment started"
          sleep 60
          echo "Deployment finished"
```

### What does this workflow do?

The workflow has one job: `deploy`.

When you manually start the workflow, the job prints a message, waits for 60 seconds, and then prints another message.

The `sleep 60` command gives you enough time to start another workflow run and observe concurrency behavior.

### Scenario A — The first run starts

1. You click **Run workflow**.
2. GitHub schedules the `deploy` job.
3. The job enters the `production-deployment` group.
4. No other job is currently running in that group.
5. The job starts executing its steps.
6. It prints `Deployment started`.
7. It waits for 60 seconds.
8. It prints `Deployment finished`.

Nothing needs to be cancelled because there is no competing running job.

### Scenario B — A second run arrives while the first is running

1. The first deployment is still running.
2. You click **Run workflow** again.
3. The second deployment uses the same group: `production-deployment`.
4. GitHub recognizes that the two jobs compete for the same group.
5. The incoming job has `cancel-in-progress: true`.
6. GitHub can cancel the currently running deployment.
7. The incoming deployment can then proceed.

The new job is not allowed to cancel the first job merely because it is newer. It can cancel it because **both jobs share a group and the incoming job permits cancellation**.

### Why would we want this?

Suppose every deployment should use the latest version of your code.

If an older deployment is still running and a newer deployment arrives, you might prefer to stop the older job and prioritize the newer one.

However, this is not appropriate for every deployment process. Some jobs perform database migrations or other operations that should not be interrupted casually.

**Important:** cancelling a job does not automatically reverse actions it has already completed. If a deployment has already changed cloud resources, cancellation does not roll those changes back.

---

## 5. Example 2 — Independent Build and Deploy Jobs

Create another file:

`.github/workflows/job-concurrency-2.yml`

```yaml
name: "Job Concurrency 2 - Independent Jobs"

on:
  workflow_dispatch:

jobs:
  build: # This job builds the application.
    runs-on: ubuntu-latest

    # No concurrency block is defined here.
    # Therefore, build does not participate in the production-deployment group.

    steps:
      - name: Build application
        run: |
          echo "Build started"
          sleep 60
          echo "Build finished"

  deploy: # This is a separate job.
    runs-on: ubuntu-latest

    concurrency: # Concurrency applies only to this deploy job.
      group: production-deployment # Same group as Example 1.
      cancel-in-progress: false # Do not automatically cancel a running deployment.

    steps:
      - name: Deploy application
        run: |
          echo "Deployment started"
          sleep 90
          echo "Deployment finished"
```

This example introduces an important distinction: **a workflow can contain several jobs, but only some of them may use concurrency control.**

### What happens to `build`?

The `build` job has no concurrency block.

It does not participate in the `production-deployment` group.

Therefore, the deployment group's concurrency rule does not directly control or cancel this build job.

### What happens to `deploy`?

The `deploy` job belongs to the `production-deployment` group.

If another job using that group is already running, the incoming deployment will not automatically cancel it because this job specifies:

```yaml
cancel-in-progress: false
```

Instead, the incoming job may become pending.

### Will build finish before deploy starts?

No such dependency is defined.

Because there is no `needs` relationship, GitHub Actions may run `build` and `deploy` in parallel, subject to runner availability and scheduling constraints.

If deployment must happen only after a successful build, add a dependency:

```yaml
  deploy:
    needs: build
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - name: Deploy application
        run: echo "Deploying application"
```

Now `needs: build` tells GitHub Actions that the deploy job depends on the build job.

This is different from concurrency:

- `needs` controls job dependency and execution order.
- `concurrency` controls competition among jobs or runs that share a group.

---

## 6. How Both Workflows Interact

Now imagine both example files exist in the same repository.

Workflow 1 uses:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

Workflow 2 uses:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

The group names are identical.

Therefore, the two `deploy` jobs compete with one another even though they belong to different workflow files.

### Scenario A — Workflow 1 is running; Workflow 2 arrives

1. Workflow 1's deployment is running.
2. Workflow 2's deployment arrives.
3. Workflow 2's incoming job uses `cancel-in-progress: false`.
4. It does not automatically cancel the running Workflow 1 deployment.
5. Workflow 2's deployment may become pending.

### Scenario B — Workflow 2 is running; Workflow 1 arrives

1. Workflow 2's deployment is running.
2. Workflow 1's deployment arrives.
3. Workflow 1's incoming job uses `cancel-in-progress: true`.
4. It can cancel the running Workflow 2 deployment.
5. The new deployment can proceed.

### The crucial lesson

Do not look only at the job that is already running.

Look at the **incoming job's** cancellation setting to determine whether it can automatically cancel the current running job.

The existing job's own `cancel-in-progress` setting does not prevent an incoming job with `cancel-in-progress: true` from cancelling it.

---

## 7. The Mixed-Settings Rule

Here is the complete comparison.

| Running job's setting | Incoming job's setting | Can the incoming job automatically cancel the running job? |
|---|---|---|
| `false` | `false` | No |
| `true` | `false` | No |
| `false` | `true` | Yes |
| `true` | `true` | Yes |

This table assumes both jobs use the same concurrency group.

Why does the incoming job's setting matter?

Because the new job is the one requesting entry into a group that already has a running job. Its configuration determines whether it allows that running job to be cancelled.

A useful way to remember the rule:

- Incoming job says `true` → it can cancel the running job in the same group.
- Incoming job says `false` → it does not automatically cancel the running job.
- Different groups → no competition through that group.

---

## 8. Running, Pending, and Cancelled Jobs

GitHub Actions concurrency is not an unlimited queue.

For a particular concurrency group, GitHub permits at most:

- One running job or workflow run.
- One pending job or workflow run.

This has an important consequence: a pending job can be replaced by a newer pending job in the same group.

Imagine this sequence:

1. Job A is running.
2. Job B arrives and becomes pending.
3. Job C arrives in the same group.
4. Job B may be cancelled and replaced by Job C.

Conceptually:

```text
Job A starts
    |
    v
Job A: RUNNING

Job B arrives
    |
    v
Job A: RUNNING       Job B: PENDING

Job C arrives
    |
    v
Job A: RUNNING       Job B: REPLACED/CANCELLED
                      Job C: PENDING
```

The exact behavior depends on the concurrency configuration and GitHub's scheduling rules.

### What does this mean in practice?

Do not treat concurrency as a durable queue that guarantees every deployment will run in arrival order.

If your release process requires every deployment to run, in a strict order, you need a process designed to guarantee those requirements rather than relying on concurrency alone.

---

## 9. Job-Level vs Workflow-Level Concurrency

These two configurations look similar, but they control different scopes.

### Job-level concurrency

The concurrency block is inside an individual job.

```yaml
name: Job-Level Example

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Building"

  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploying"
```

Only the `deploy` job uses this concurrency rule. The `build` job is not automatically placed in that group.

### Workflow-level concurrency

The concurrency block is at the top level, alongside `on` and `jobs`.

```yaml
name: Workflow-Level Example

on:
  workflow_dispatch:

concurrency:
  group: whole-workflow-${{ github.ref }}
  cancel-in-progress: false

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Building"

  deploy:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying"
```

This applies concurrency control to the entire workflow run rather than only one particular job.

### Which one should you choose?

Use job-level concurrency when you want to coordinate one job, such as production deployment, while allowing unrelated jobs to operate independently.

Use workflow-level concurrency when the entire workflow run needs to share the concurrency rule.

---

## 10. Dynamic Concurrency Groups

A concurrency group does not have to be a fixed string.

You can use GitHub Actions expressions to create group names dynamically.

This is useful when different branches or environments should have separate concurrency rules.

### Example A — One group per Git reference

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: deploy-${{ github.ref }}
      cancel-in-progress: false

    steps:
      - run: echo "Deploying this Git reference"
```

The expression `${{ github.ref }}` contributes the current Git reference.

For example, different branches normally produce different group names.

This means jobs deploying different references will not necessarily compete with one another.

### Example B — One group per selected environment

```yaml
name: Deploy by Environment

on:
  workflow_dispatch:
    inputs:
      environment:
        description: Environment to deploy
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

When you manually start the workflow, GitHub asks you to select an environment.

If you choose `production`, the group becomes:

`deploy-production`

If you choose `staging`, the group becomes:

`deploy-staging`

Therefore:

- Two production deployment jobs use the same group and compete.
- Two staging deployment jobs use the same group and compete.
- A production deployment and a staging deployment use different groups and do not compete through these groups.

**Design rule:** choose the group according to the real resource that must be protected.

If two different workflows can modify the same production environment, ensure their group expressions resolve to the same group name.

---

## 11. Concurrency and Job Dependencies

Concurrency and dependencies solve different problems.

### `needs` answers: “What must finish first?”

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Building"

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying"
```

Here, deployment depends on the build job. Normally, `deploy` waits for `build` to complete successfully before running.

### `concurrency` answers: “Which jobs are allowed to compete for execution?”

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploying"
```

Here, the job follows the concurrency rule for its group.

You can use both together:

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
      - run: echo "Deploying after build"
```

This says:

1. Build the application.
2. Wait for the build to succeed.
3. Apply the concurrency rule when the deployment job is scheduled.

---

## 12. Common Mistakes

### Mistake 1 — Thinking the group is only a label

The group is how GitHub identifies jobs or workflow runs that compete for the same concurrency restriction.

### Mistake 2 — Thinking different workflow files cannot share a group

They can. Matching evaluated group names allow jobs in different workflow files to compete.

### Mistake 3 — Assuming `cancel-in-progress: false` creates a permanent queue

It does not. A group supports at most one running and one pending member. New pending work can replace older pending work.

### Mistake 4 — Assuming `cancel-in-progress: true` cancels every job

It affects competing jobs or runs in the same group, not unrelated jobs in different groups.

### Mistake 5 — Putting concurrency on `deploy` and expecting it to control `build`

Job-level concurrency applies only to the job where it is declared.

### Mistake 6 — Confusing concurrency with dependencies

`needs: build` controls job dependency order. `concurrency` controls scheduling competition within a group.

### Mistake 7 — Assuming cancellation means rollback

Cancellation stops work, but it does not automatically undo changes that have already been made.

### Mistake 8 — Assuming concurrency guarantees FIFO order

It does not guarantee that every pending job runs in arrival order.

### Mistake 9 — Using a generic group name without thinking

If unrelated workflows all use the same group name, they may unintentionally compete. Use meaningful names and expressions.

### Mistake 10 — Assuming a deployment is safe merely because it has concurrency

Concurrency reduces overlapping work within the configured scope. It does not guarantee that a deployment is correct, atomic, recoverable, or protected against every external process.

---

## 13. Best Practices

1. **Group by the resource being protected.** Use a meaningful name such as `production-deployment`.
2. **Use consistent groups across workflows that modify the same resource.** Otherwise, those workflows may not coordinate with one another.
3. **Choose cancellation behavior deliberately.** Set `true` when newer work should be able to replace running work; set `false` when a running job should normally be allowed to finish.
4. **Do not rely on concurrency as a guaranteed queue.** If every release must run, design an appropriate release process.
5. **Use `needs` for dependencies.** If deployment must follow a successful build, express that dependency explicitly.
6. **Protect production through multiple layers.** Consider GitHub environment protection rules, permissions, secrets management, and deployment-tool safeguards.
7. **Design for interruption.** A job may be cancelled after it has already changed external resources.
8. **Test with harmless demonstration jobs first.** Confirm the behavior before using cancellation rules in a production pipeline.
9. **Avoid accidental shared groups.** Make sure the group name represents the resource whose work needs coordination.
10. **Remember the scope.** Job-level concurrency controls the configured job, not every job in the workflow.

---

## 14. Complete Revision Table

| Question | Answer |
|---|---|
| Where is job-level concurrency declared? | Inside the job, alongside `runs-on` and `steps`. |
| What does `group` do? | Identifies jobs or runs that share the same concurrency restriction. |
| What does `cancel-in-progress: true` mean? | A newly competing job can cancel the running member of the same group. |
| What does `cancel-in-progress: false` mean? | The incoming job does not automatically cancel the running member; it may become pending. |
| Which setting matters when the two jobs use different cancellation settings? | The incoming job's setting determines whether it can cancel the running job. |
| Can different workflow files share a group? | Yes, when their evaluated group names match. |
| Does job-level concurrency control every job in a workflow? | No, only the job where it is declared. |
| What does `needs` do? | Establishes job dependencies and execution order. |
| Is concurrency a guaranteed queue? | No. There can be one running and one pending member per group, and pending work can be replaced. |
| Does cancellation roll back completed deployment changes? | No. Cancellation is not rollback. |
| Can staging and production use separate groups? | Yes, if their group names are different. |
| Can concurrency and `needs` be used together? | Yes. They solve different problems. |

---

## The Mental Model to Remember

Imagine a shared door to a production room.

- **The group name** tells GitHub which jobs are trying to use the same door.
- **The running job** is already inside.
- **The pending job** is waiting.
- **`cancel-in-progress: true` on the incoming job** means GitHub can stop the running job in that same group to let the incoming job proceed.
- **`cancel-in-progress: false` on the incoming job** means it does not automatically stop the running job.
- **The door is not an unlimited queue.** Only one member can be running and one pending in a group, and newer pending work can replace older pending work.
- **A concurrency block inside `deploy` controls `deploy`.** It does not automatically control `build`.
- **`needs` controls dependencies.** Concurrency controls competition among jobs or runs that share a group.

Once these distinctions are clear, job-level concurrency becomes a practical tool for coordinating shared work—especially deployments—without confusing concurrency with workflow dependencies, workflow-level concurrency, or a guaranteed release queue.
