# GitHub Actions Concurrency: `cancel-in-progress: true` vs `false`

## A Complete Beginner-Friendly Guide

**Topic:** Workflow-level concurrency, shared concurrency groups, cancellation, pending runs, and mixed settings  
**Goal:** Understand what GitHub Actions does when multiple workflow runs compete for the same concurrency group.

---

## 1. The problem concurrency solves

Imagine a small team has one production environment. Two deployment workflows try to update it at the same time.

- Deployment A starts updating production.
- Deployment B starts a moment later.
- If both deployments change production simultaneously, they might interfere with each other.

Concurrency lets GitHub Actions coordinate workflow runs that use the same concurrency group. You give the group a name, and runs with that same evaluated name compete for the same concurrency slot.

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Read this as:

> “Runs using the group `production-deployment` must be coordinated. If one is already running, do not automatically cancel it just because another run arrives.”

Concurrency is not a general-purpose queue that promises every run will eventually execute. GitHub allows **at most one running run and one pending run per concurrency group**. A newer run can replace an older pending run.

## 2. The two important settings

### `cancel-in-progress: true`

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

This means a newly arriving run that competes for this group can cancel the currently running run. The new run can then proceed, subject to GitHub's concurrency handling.

Think of it as:

> “If a newer run arrives for this group, it is okay to stop the current run and let the newer one take over.”

This can be useful for CI checks where the latest commit matters more than an older run. It requires care for deployments, database migrations, releases, or any task that makes external changes: cancellation does not roll back actions already completed.

### `cancel-in-progress: false`

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

This means a newly arriving competing run does **not automatically cancel the currently running run**.

Think of it as:

> “Let the current run continue. The new run may wait as pending.”

There is only one pending slot for the group. If another run arrives while a run is already running and a different run is pending, the newer run can replace the pending run. Therefore, `false` protects the current run from automatic cancellation by this setting; it does **not** guarantee that every waiting run will execute.

## 3. The basic mental model: one running slot, one pending slot

For a given concurrency group, imagine two slots:

```text
Concurrency group: production-deployment

┌─────────────────────────┐
│ RUNNING SLOT            │  At most one run
│ Run A                   │
└─────────────────────────┘

┌─────────────────────────┐
│ PENDING SLOT            │  At most one waiting run
│ Run B                   │
└─────────────────────────┘
```

If Run C arrives, GitHub does not create an unlimited line of pending runs. The newer pending run can replace the older pending run. With `cancel-in-progress: true`, the currently running run can also be cancelled when a new competing run arrives.

**Important:** GitHub concurrency is not guaranteed FIFO ordering. Do not rely on every run being processed in the order it was triggered.

## 4. The `group` name is what connects workflows

Consider two different files.

**`.github/workflows/deploy-app.yml`**

```yaml
name: Deploy Application

on:
  workflow_dispatch:

concurrency:
  group: production-deployment
  cancel-in-progress: false

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - run: |
          echo "Application deployment started"
          sleep 120
          echo "Application deployment finished"
```

**`.github/workflows/deploy-api.yml`**

```yaml
name: Deploy API

on:
  workflow_dispatch:

concurrency:
  group: production-deployment
  cancel-in-progress: false

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - run: |
          echo "API deployment started"
          sleep 120
          echo "API deployment finished"
```

The workflow names and job IDs are different, but both files use the exact same group name: `production-deployment`.

Therefore, the two workflow runs share the same concurrency group. The group name—not the workflow display name—is the key that makes them compete.

If the second file instead used `api-deployment`, it would belong to a different group and would not be coordinated with the first by this concurrency setting.

## 5. What happens when every workflow uses `false`?

Suppose Workflow A and Workflow B both have:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

Sequence:

1. Run A starts and occupies the running slot.
2. Run B arrives and becomes pending.
3. Run A continues; Run B does not automatically cancel it.
4. If Run A finishes while Run B is still pending, Run B can start.
5. If another run arrives while B is pending, the newer pending run can replace B.

**Result:** the active run is protected from automatic cancellation by this setting, but pending runs can be replaced. This is not a durable queue.

## 6. What happens when every workflow uses `true`?

Suppose Workflow A and Workflow B both have:

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

Sequence:

1. Run A starts.
2. Run B arrives with the same group.
3. Run B can cancel Run A.
4. Run B proceeds, subject to GitHub's concurrency handling.
5. If Run C arrives while B is running, C can cancel B as well.

**Result:** newer competing runs can take over from running ones. This is often useful when only the newest version matters, but risky if cancellation can leave external work partly completed.

## 7. What if one workflow says `true` and another says `false`?

This is the subtle case.

For example:

**Workflow A**

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: false
```

**Workflow B**

```yaml
concurrency:
  group: production-deployment
  cancel-in-progress: true
```

Both workflows share `production-deployment`. The setting is written in each workflow file, but it is **not a single shared group-level configuration** that is permanently set to either `true` or `false`.

The important distinction is that the newly arriving run's concurrency configuration controls whether it is allowed to cancel a currently running run in the group. Therefore:

- If a new run using `cancel-in-progress: true` arrives while a run is already active in the shared group, it can cancel that running run—even if the active run came from a workflow configured with `false`.
- If a new run using `cancel-in-progress: false` arrives while another run is active, it does not automatically cancel that active run—even if the active run came from a workflow configured with `true`.

In short, the incoming run's setting matters for the cancellation decision. The existing run's `false` does not protect it from a competing incoming run configured with `true`.

### Mixed-settings table

| Currently running run | Newly arriving run | Expected cancellation behavior |
|---|---|---|
| A uses `false` | B uses `false` | B does not automatically cancel A |
| A uses `true` | B uses `true` | B can cancel A |
| A uses `false` | B uses `true` | B can cancel A |
| A uses `true` | B uses `false` | B does not automatically cancel A |

These rows assume both runs resolve to the **same concurrency group** and the new run is competing with the running one.

**Practical takeaway:** do not mix settings casually for workflows that can affect the same production environment. A workflow set to `true` can cancel another workflow's running job or run when they share a group.

## 8. What if the group names are different?

Example:

```yaml
# Workflow A
concurrency:
  group: deploy-production
  cancel-in-progress: true
```

```yaml
# Workflow B
concurrency:
  group: deploy-staging
  cancel-in-progress: false
```

These are different groups. Their runs do not cancel or block each other through these concurrency settings, even though one uses `true` and the other uses `false`.

The `true` setting does not cancel every workflow in the repository. It applies to competing runs in the same concurrency group.

## 9. Workflow-level versus job-level concurrency

### Workflow-level concurrency

```yaml
name: Example

on:
  workflow_dispatch:

concurrency:
  group: production-deployment
  cancel-in-progress: false

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Build"

  deploy:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploy"
```

The concurrency setting is at the top level, alongside `on` and `jobs`. It coordinates **whole workflow runs**. If a run is cancelled by concurrency, jobs in that workflow run can be cancelled too.

### Job-level concurrency

```yaml
name: Example

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: production-deployment
      cancel-in-progress: false

    steps:
      - run: echo "Deploy"
```

The concurrency setting is inside the `deploy` job. It coordinates executions of that job that share the group; other jobs in the workflow are not directly governed by this job-level setting.

Do not confuse `needs:` with concurrency. `needs:` expresses a dependency/order between jobs in one workflow run; `concurrency:` coordinates runs or jobs that share a group.

## 10. Dynamic concurrency groups

The group can be built from a context or input.

### Separate groups by branch

```yaml
concurrency:
  group: deploy-${{ github.ref_name }}
  cancel-in-progress: true
```

For example:

- `main` → `deploy-main`
- `dev` → `deploy-dev`

Runs on different branches use different groups, so they do not compete with each other through this setting.

### Share a group by selected environment

```yaml
on:
  workflow_dispatch:
    inputs:
      environment:
        description: Choose environment
        type: choice
        options:
          - staging
          - production
        required: true

concurrency:
  group: deploy-${{ inputs.environment }}
  cancel-in-progress: false
```

If two workflows use the same group formula and both are run with `production`, they resolve to the same group, `deploy-production`. They can therefore compete across workflow files.

When using dynamic groups, make sure the group includes the dimensions you intend to isolate (such as environment or branch). A group that is too broad can make unrelated runs cancel or block one another.

## 11. A safe way to experiment

Use harmless test workflows first. The following workflow waits for 120 seconds so you can start a competing run while it is active.

```yaml
name: Concurrency Experiment

on:
  workflow_dispatch:

concurrency:
  group: concurrency-learning-lab
  cancel-in-progress: false

jobs:
  demo:
    runs-on: ubuntu-latest
    steps:
      - name: Show run identity
        run: |
          echo "Run ID: ${{ github.run_id }}"
          echo "Commit: ${{ github.sha }}"

      - name: Wait so another run can start
        run: |
          echo "Waiting for 120 seconds"
          sleep 120
          echo "Finished"
```

Try these controlled experiments:

1. Run the workflow once. Start it again while the first run is sleeping. Observe that the first run is not automatically cancelled when the new run uses `false`.
2. Change `cancel-in-progress` to `true`, commit the change, and run it again. Start a competing run while the first is active.
3. Create a second workflow with the same group name to observe that concurrency groups can be shared across workflow files.
4. Give the second workflow a different group name and observe that the two groups no longer compete through this setting.

For a clean comparison, remember that changing a workflow file in a new commit may itself trigger a `push` workflow if the workflow listens for pushes to that branch. A manual `workflow_dispatch` experiment can make the trigger sequence easier to control.

## 12. Common misunderstandings

**Misunderstanding: “`false` means all runs wait their turn.”**  
Correction: only one run can be pending in a group. A newer pending run can replace an older pending run.

**Misunderstanding: “`true` cancels every other workflow.”**  
Correction: it affects competing runs that use the same concurrency group.

**Misunderstanding: “Different workflow names mean different concurrency groups.”**  
Correction: group names determine which runs compete. Two differently named workflows can share a group.

**Misunderstanding: “If Workflow A uses `false`, Workflow B cannot cancel it.”**  
Correction: a newly arriving competing run configured with `true` can cancel the currently running run in the shared group.

**Misunderstanding: “Cancelling a run undoes the deployment.”**  
Correction: GitHub cancels the run, but does not automatically roll back cloud resources, database changes, published artifacts, or other side effects already completed.

**Misunderstanding: “Concurrency is a strict FIFO queue.”**  
Correction: it is not. There is one running slot and one pending slot per group, and newer pending runs can replace older pending runs.

## 13. Choosing the right setting

| Use case | Typical choice | Why |
|---|---|---|
| CI checks where only the latest commit matters | `true` | Older running checks can be cancelled when a newer run arrives |
| Production deployment that should not be interrupted automatically | `false` | Protects the currently running run from automatic cancellation by a new run |
| Independent environments such as staging and production | Dynamic group per environment | Keeps unrelated environments from competing |
| Two workflows that must coordinate access to the same resource | Same group name in both workflows | Makes their runs share concurrency restrictions |

Neither setting is universally better. Choose based on whether an in-progress operation can safely be interrupted and whether every run needs to complete. If every deployment must be processed reliably and in order, GitHub concurrency alone is not a durable job queue; use an appropriate deployment or queueing design.

## 14. Final mental model

Remember these three rules:

1. **`group` decides who competes.** Same evaluated group name means the runs share concurrency restrictions.
2. **`cancel-in-progress` controls interruption of the running run.** `true` lets a new competing run cancel the current run; `false` does not automatically cancel it.
3. **Pending runs are not a guaranteed queue.** A group has at most one pending run, and a newer run can replace the pending one.

Once these rules are clear, you can predict the behavior of `true`, `false`, multiple workflow files, and dynamic concurrency groups without memorizing separate special cases.
