Dynamic Concurrency Groups in GitHub Actions
A professional, beginner-friendly guide to creating concurrency group names dynamically using workflow inputs, branch names, workflow names, and matrix values.
1. What is a concurrency group?
A concurrency group is a name GitHub Actions uses to identify workflow runs or jobs that must coordinate with one another.

For example:
concurrency:
  group: production-deployment
  cancel-in-progress: false
This says: runs or jobs using the group name production-deployment share concurrency restrictions. GitHub coordinates them so that they do not all run at the same time within that group.

Think of a concurrency group as a named lane. Runs assigned to the same lane compete for the same running and pending slots. Runs assigned to different lanes do not compete through this concurrency setting.
2. What does dynamic naming mean?
A fixed group name always stays the same:
concurrency:
  group: production-deployment
  cancel-in-progress: false
Every run using this configuration joins production-deployment.

A dynamic group name is built from a value that GitHub Actions evaluates when the workflow runs:
concurrency:
  group: deploy-${{ github.ref_name }}
  cancel-in-progress: false
The expression ${{ github.ref_name }} is replaced with the branch or tag name associated with the run.

For example:
Run's branch	Evaluated group name
main	deploy-main
dev	deploy-dev
feature/login	deploy-feature/login


Now runs from main share deploy-main, while runs from dev share deploy-dev. They do not compete with each other through this setting because their group names differ.

Core idea: dynamic naming decides which concurrency group a run joins by building the group name from runtime context or input values.
3. How GitHub evaluates a dynamic group
Consider this configuration:
concurrency:
  group: deploy-${{ inputs.environment }}
  cancel-in-progress: false
If a user starts the workflow manually and chooses production, GitHub evaluates the expression and uses the group name:
deploy-production
If the user chooses staging, the group becomes:
deploy-staging
The expression is not a command that creates a new resource. It is a way to calculate the name GitHub uses for concurrency coordination.

The sequence is:
1. A workflow is triggered.
2. GitHub reads the available context and input values.
3. GitHub evaluates the expression inside group.
4. The resulting group name identifies the concurrency group.
5. GitHub applies the concurrency rules to other runs or jobs with the same group name.
4. Example 1 — Dynamic group based on a manual input
Save as .github/workflows/deploy-app.yml.
# Name shown in the Actions tab.
name: Deploy Application

# Allow a user to start this workflow manually.
on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose the deployment environment"
        type: choice
        options:
          - development
          - staging
          - production
        required: true
        default: development

jobs:
  deploy:
    runs-on: ubuntu-latest

    # The user's input becomes part of the concurrency group name.
    concurrency:
      group: deploy-${{ inputs.environment }}
      cancel-in-progress: false

    steps:
      - name: Show the selected environment
        run: |
          echo "Selected environment: ${{ inputs.environment }}"
          echo "Simulating deployment..."
          sleep 30
          echo "Deployment simulation completed"
What happens?
When you select production, the job joins deploy-production. When you select staging, it joins deploy-staging.

This is useful when deployments to different environments should not block one another, but two deployments to the same environment should be coordinated.
Note: this example demonstrates group naming. It simulates a deployment; it does not actually deploy an application.

5. Example 2 — Two workflow files sharing a dynamic group
Dynamic group naming becomes especially useful when multiple workflow files need to coordinate access to the same environment.
File A: .github/workflows/deploy-app.yml
name: Deploy Application

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose environment"
        type: choice
        options:
          - staging
          - production
        required: true

jobs:
  deploy:
    runs-on: ubuntu-latest

    concurrency:
      group: deploy-${{ inputs.environment }}
      cancel-in-progress: false

    steps:
      - name: Simulate application deployment
        run: |
          echo "Application environment: ${{ inputs.environment }}"
          sleep 60
          echo "Application deployment finished"
File B: .github/workflows/deploy-api.yml
name: Deploy API

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Choose environment"
        type: choice
        options:
          - staging
          - production
        required: true

jobs:
  deploy:
    runs-on: ubuntu-latest

    # This formula matches the one in deploy-app.yml.
    concurrency:
      group: deploy-${{ inputs.environment }}
      cancel-in-progress: false

    steps:
      - name: Simulate API deployment
        run: |
          echo "API environment: ${{ inputs.environment }}"
          sleep 60
          echo "API deployment finished"
How the two workflows coordinate
Application workflow input	API workflow input	Evaluated group	Behavior
production	production	deploy-production	They share concurrency restrictions
staging	production	Different groups	They do not compete through these settings
staging	staging	deploy-staging	They share concurrency restrictions


The workflow names are different, but that does not prevent them from sharing a concurrency group. The evaluated group name is what matters.

With cancel-in-progress: false, a new competing run does not automatically cancel the currently running run. It can become pending, but remember that GitHub allows at most one pending run per group, and a newer pending run can replace an older pending run.
6. Example 3 — Dynamic group based on the branch name
name: Branch-Based Concurrency

on:
  push:
    branches:
      - main
      - dev
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    concurrency:
      group: build-${{ github.ref_name }}
      cancel-in-progress: true

    steps:
      - name: Show branch
        run: |
          echo "Branch or tag: $GITHUB_REF_NAME"
          echo "Build started"
          sleep 30
          echo "Build completed"
The group changes according to the branch or tag name:
- main → build-main
- dev → build-dev
With cancel-in-progress: true, a newer competing run can cancel the currently running run in the same group. A run in build-dev does not cancel a run in build-main through this setting because they are different groups.

This pattern is useful when each branch should have its own concurrency behavior.
7. Example 4 — Dynamic group based on a workflow name and branch
name: Build and Test

on:
  push:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest

    concurrency:
      group: ${{ github.workflow }}-${{ github.ref_name }}
      cancel-in-progress: true

    steps:
      - name: Show run details
        run: |
          echo "Workflow: $GITHUB_WORKFLOW"
          echo "Branch: $GITHUB_REF_NAME"
          sleep 30
The group combines two values:
- ${{ github.workflow }} — the workflow name.
- ${{ github.ref_name }} — the branch or tag name.
For a workflow named Build and Test running on main, the group is effectively:
Build and Test-main
Including the workflow name can separate different workflows. Including the branch name can separate runs from different branches.

Be careful: if you want two different workflows to coordinate with each other, adding different workflow names to their group expressions may unintentionally give them different groups.
8. Example 5 — Dynamic group based on a matrix value
A matrix creates multiple job executions from one job definition. Each matrix execution can use its own value to calculate a group name.
name: Environment Matrix

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        environment:
          - development
          - staging
          - production

    concurrency:
      group: deploy-${{ matrix.environment }}
      cancel-in-progress: false

    steps:
      - name: Show matrix environment
        run: |
          echo "Environment: ${{ matrix.environment }}"
          sleep 30
The matrix creates executions with these group names:
- deploy-development
- deploy-staging
- deploy-production
Each environment has a separate concurrency group. This is useful when each environment should be coordinated independently.
9. Dynamic group names and cancel-in-progress
Dynamic naming and cancellation answer two different questions:
- Dynamic group: Which runs compete with one another?
- cancel-in-progress: Can a newly arriving competing run cancel the currently running run?
cancel-in-progress: true
concurrency:
  group: deploy-${{ inputs.environment }}
  cancel-in-progress: true
A new competing run can cancel the current run in the matching group.
cancel-in-progress: false
concurrency:
  group: deploy-${{ inputs.environment }}
  cancel-in-progress: false
A new competing run does not automatically cancel the current run. It may wait as the group's pending run.

The setting applies to the run or job whose concurrency configuration is being evaluated. When multiple workflows share a group but use different cancellation settings, the incoming run's setting matters for whether it can cancel the currently running run. A new run configured with true can cancel a currently running run in that group even if the running workflow was configured with false.
Important concurrency limitations
- A concurrency group allows at most one running run and one pending run.
- A newer pending run can replace an older pending run.
- Concurrency does not guarantee FIFO order or that every triggered run will eventually execute.
- Cancellation stops workflow execution; it does not automatically undo changes already made to external systems.
- Use caution with production deployments, database migrations, and other operations that may not be safe to interrupt.
10. Common mistakes to avoid
Mistake 1: Thinking the workflow name controls the group
name: Deploy Application

concurrency:
  group: production
The workflow display name is Deploy Application, but the group name is production. Another workflow can share the group by also specifying group: production.
Mistake 2: Accidentally separating workflows that should coordinate
Workflow A:
concurrency:
  group: deploy-${{ inputs.environment }}
Workflow B:
concurrency:
  group: ${{ github.workflow }}-${{ inputs.environment }}
Even if both users select production, these formulas produce different group names. The workflows will not coordinate with each other through these settings.
Mistake 3: Making one group too broad
concurrency:
  group: deployment
This puts all deployments using this literal group into the same concurrency group, regardless of branch or environment. If production and staging should run independently, include the environment in the group name.
Mistake 4: Assuming false creates a permanent queue
cancel-in-progress: false protects the currently running run from automatic cancellation by a new competing run. It does not guarantee that every pending run will execute, because a newer pending run can replace an older pending run.
11. Choosing a naming strategy
Requirement	Example group	Why
One shared production lane	production-deployment	All runs using this group coordinate
Independent environments	deploy-${{ inputs.environment }}	Staging and production use separate groups
Independent branches	build-${{ github.ref_name }}	Each branch gets its own group
Separate workflows and branches	${{ github.workflow }}-${{ github.ref_name }}	Group includes both workflow and branch
Independent matrix environments	deploy-${{ matrix.environment }}	Each matrix environment gets its own group


Choose the parts of the group name based on what should be isolated and what should coordinate. If two workflows must serialize access to the same production environment, they need to resolve to the same group name.
12. Final mental model
Remember these three rules:
1. A concurrency group is identified by its evaluated name. Different workflow files can share a group.
2. Dynamic naming calculates the group name. It can use inputs, branch names, workflow names, and matrix values.
3. The group decides who competes; cancellation decides what can be interrupted. true allows a new competing run to cancel the running run; false does not automatically cancel it.
Once these rules are clear, you can design dynamic concurrency groups for CI pipelines, deployment environments, and matrix workflows with confidence.