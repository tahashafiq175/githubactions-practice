

# GitHub Actions Environments — Complete Beginner-to-Advanced Guide

> **Learn GitHub Actions Environments from zero to production.**
>
> A simple, step-by-step guide to environment-specific configuration, secrets, deployment protection, approvals, and real-world CI/CD pipelines.

---

## Table of Contents

1. [Start With a Real-World Problem](#1-start-with-a-real-world-problem)
2. [What Is a GitHub Actions Environment?](#2-what-is-a-github-actions-environment)
3. [The Three-Room Analogy](#3-the-three-room-analogy)
4. [Where Do Environments Exist?](#4-where-do-environments-exist)
5. [Create Your First Environment](#5-create-your-first-environment)
6. [How the `environment` Keyword Works](#6-how-the-environment-keyword-works)
7. [Environment Variables](#7-environment-variables)
8. [Environment Secrets](#8-environment-secrets)
9. [Variables vs Secrets](#9-variables-vs-secrets)
10. [Repository Secrets vs Environment Secrets](#10-repository-secrets-vs-environment-secrets)
11. [Deployment Protection Rules](#11-deployment-protection-rules)
12. [Required Reviewers and Manual Approval](#12-required-reviewers-and-manual-approval)
13. [Wait Timers](#13-wait-timers)
14. [Deployment Branch and Tag Restrictions](#14-deployment-branch-and-tag-restrictions)
15. [An Environment Does Not Automatically Deploy Anything](#15-an-environment-does-not-automatically-deploy-anything)
16. [Environment vs Server vs Container](#16-environment-vs-server-vs-container)
17. [The Environment URL](#17-the-environment-url)
18. [Development → Staging → Production](#18-development--staging--production)
19. [Using `needs` With Environments](#19-using-needs-with-environments)
20. [A Complete Practical Workflow](#20-a-complete-practical-workflow)
21. [Matrix Strategy With Environments](#21-matrix-strategy-with-environments)
22. [GitHub Actions Environments and Docker](#22-github-actions-environments-and-docker)
23. [Environments and GitHub Container Registry](#23-environments-and-github-container-registry)
24. [Environments and Kubernetes](#24-environments-and-kubernetes)
25. [Branch Protection vs Environment Protection](#25-branch-protection-vs-environment-protection)
26. [The Difference Between `env:` and `environment:`](#26-the-difference-between-env-and-environment)
27. [How Environment Secrets Are Protected](#27-how-environment-secrets-are-protected)
28. [Common Mistakes](#28-common-mistakes)
29. [Real-World Scenarios](#29-real-world-scenarios)
30. [Interview Questions and Answers](#30-interview-questions-and-answers)
31. [Scenario-Based Questions](#31-scenario-based-questions)
32. [Production-Style Example](#32-production-style-example)
33. [The Complete Mental Model](#33-the-complete-mental-model)
34. [Quick Revision Cheat Sheet](#34-quick-revision-cheat-sheet)

---

# 1. Start With a Real-World Problem

Imagine you are developing a Java Spring Boot application.

Your application needs to run in three places:

- Development
- Staging
- Production

Each place has different configuration.

| Configuration | Development | Staging | Production |
|---|---|---|---|
| Application URL | `dev.example.com` | `staging.example.com` | `example.com` |
| Database | Development DB | Testing DB | Production DB |
| Deployment target | Dev server | Staging server | Production server |
| Credentials | Dev credentials | Staging credentials | Production credentials |
| Approval | Usually not required | May be required | Often required |

Now imagine writing a separate workflow for every environment.

You would need to maintain repeated YAML, manage credentials carefully, and remember which deployment is allowed to run where.

That becomes difficult as the application grows.

**GitHub Actions Environments help organize deployment targets, environment-specific configuration, secrets, and deployment protection rules.**

They let you define a named environment in your repository and reference it from a workflow job.

## The problem in one picture

```text
                 GitHub Repository
                         |
                  GitHub Actions
                         |
              Which environment?
                         |
          +--------------+--------------+
          |              |              |
     Development       Staging       Production
          |              |              |
      Dev config      Test config    Live config
      Dev secrets     Test secrets   Live secrets
      Dev rules       Test rules     Approval rules
```

The important idea is simple:

**The same application may be deployed to different targets, but each target may need different configuration, credentials, and safety rules.**

---

# 2. What Is a GitHub Actions Environment?

A GitHub Actions Environment is a named configuration and protection context associated with a deployment job.

For example:

- `development`
- `staging`
- `production`

You can configure an environment with:

- Environment-specific variables
- Environment-specific secrets
- Deployment protection rules
- Deployment history and status information

A workflow job can reference the environment by name.

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Deploy application
        run: echo "Deploying to production"
```

Let's understand each part.

```yaml
jobs:
```

This starts the section where workflow jobs are defined.

```yaml
deploy:
```

This is the job ID. We have named the job `deploy`.

```yaml
runs-on: ubuntu-latest
```

GitHub Actions selects an eligible Ubuntu runner to execute the job.

```yaml
environment: production
```

This associates the job with the GitHub repository environment named `production`.

```yaml
steps:
```

This begins the list of tasks the job will execute.

```yaml
run: echo "Deploying to production"
```

This command prints a message. It does **not** actually deploy an application.

That distinction is important: the environment provides deployment context and protection; your workflow must still perform the real deployment.

---

# 3. The Three-Room Analogy

Imagine a company has three rooms.

## Room 1: Development

Developers experiment here. Mistakes are expected, and changes can be tested safely.

## Room 2: Staging

This room resembles the real production environment. The team checks whether the application behaves correctly before releasing it.

## Room 3: Production

Real users depend on this room. A mistake may interrupt the service or affect real data.

GitHub Actions can associate deployment jobs with corresponding environments.

```yaml
jobs:
  deploy-dev:
    runs-on: ubuntu-latest
    environment: development
    steps:
      - run: echo "Deploying to development"

  deploy-staging:
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - run: echo "Deploying to staging"

  deploy-production:
    runs-on: ubuntu-latest
    environment: production
    steps:
      - run: echo "Deploying to production"
```

These jobs illustrate environment association. Because no `needs` relationships are defined, the jobs are not automatically ordered from development to production.

The environment names do not themselves create three servers. You configure the environments in GitHub and separately configure the infrastructure where your application will run.

**Remember:** An environment is a named deployment context, not a physical room or server.

---

# 4. Where Do Environments Exist?

GitHub repository environments are configured in the repository settings.

To create or manage one:

1. Open your GitHub repository.
2. Select **Settings**.
3. Find **Environments** in the repository settings.
4. Select **New environment**.
5. Enter a name, such as `development`.
6. Configure variables, secrets, or protection rules as needed.
7. Save the environment.

Repeat the process for `staging` and `production` if you need them.

Your repository may then contain environments such as:

```text
GitHub Repository
|
+-- Environments
    |
    +-- development
    |   +-- Variables
    |   +-- Secrets
    |   +-- Protection rules
    |
    +-- staging
    |   +-- Variables
    |   +-- Secrets
    |   +-- Protection rules
    |
    +-- production
        +-- Variables
        +-- Secrets
        +-- Protection rules
```

The available settings and protection options depend on your repository visibility, GitHub plan, and organization configuration.

A workflow can refer to an environment name, but it does not create all the associated infrastructure for you.

---

# 5. Create Your First Environment

Suppose you create an environment named `development` in GitHub.

You can reference it in your workflow:

```yaml
name: Development Deployment

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: development

    steps:
      - name: Show deployment target
        run: echo "Deploying to development"
```

## What happens when you run this workflow?

1. You manually start the workflow using **Run workflow**.
2. GitHub Actions identifies the `deploy` job.
3. GitHub Actions evaluates the job's environment association and any applicable protection rules.
4. If the job is permitted to proceed, a runner executes its steps.
5. The command prints the message.

If the environment has a required approval rule, the job may need approval before it proceeds.

If the environment does not exist, referencing its name can cause GitHub to create an environment with that name when the workflow runs. Do not rely on this behavior for production: configure the environment and its protection rules deliberately before using it.

**Production lesson:** Create and secure production environments in repository settings instead of relying on an unconfigured environment name.

---

# 6. How the `environment` Keyword Works

The `environment` keyword associates a **job** with an environment.

Correct example:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Deploy
        run: echo "Deploying application"
```

You can also specify the environment name and its URL:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: production
      url: https://example.com

    steps:
      - name: Deploy
        run: echo "Deploying application"
```

The `name` identifies the environment. The `url` provides a deployment-related URL that GitHub can display in deployment information.

The URL does not automatically create a website or deploy an application.

## Why does the environment belong to a job?

A job represents a unit of work that runs on a runner.

For example, your workflow might contain:

```text
build
  |
  v
test
  |
  v
deploy-staging
  |
  v
deploy-production
```

Only the deployment jobs may need environment-specific credentials or approvals.

The build job can build the application without being associated with production.

This is useful because it keeps the release process organized and limits the scope of deployment-specific access.

---

# 7. Environment Variables

Different environments often need different non-sensitive configuration.

For example:

```text
development → https://dev.example.com
staging     → https://staging.example.com
production  → https://example.com
```

Instead of writing these URLs directly into your workflow, configure an environment variable for each GitHub environment.

For example, in the `production` environment, create a variable:

```text
APP_URL = https://example.com
```

Then read it in a workflow:

```yaml
name: Production Deployment

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Display application URL
        run: echo "Application URL is $APP_URL"
        env:
          APP_URL: ${{ vars.APP_URL }}
```

Let's understand this.

```yaml
environment: production
```

The job is associated with the `production` environment.

```yaml
${{ vars.APP_URL }}
```

This reads the GitHub Actions configuration variable named `APP_URL`.

```yaml
env:
  APP_URL: ${{ vars.APP_URL }}
```

This maps the GitHub Actions variable into the process environment of the step.

```yaml
run: echo "Application URL is $APP_URL"
```

The shell reads the environment variable and prints its value.

## Why use variables?

Imagine your application URL changes.

Without a variable, you may need to edit the workflow file.

With a GitHub environment variable, you can update the value in the environment's settings without hardcoding that value into the workflow.

Use variables for configuration that is not secret, such as:

- Application URLs
- Deployment region names
- Non-sensitive service identifiers
- Feature configuration
- Environment labels

**Important:** A variable is not a secure storage location for passwords, tokens, or private keys.

---

# 8. Environment Secrets

Some configuration must remain confidential.

Examples include:

- Database passwords
- Cloud deployment credentials
- API tokens
- Private deployment keys
- Credentials for a production server

These values should not be hardcoded into workflow YAML.

Instead, create an environment secret in GitHub.

For example, in the `production` environment, create:

```text
DATABASE_PASSWORD
```

Then reference it:

```yaml
name: Production Deployment

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Connect to production service
        env:
          DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
        run: |
          echo "Deployment step can access the configured credential."
          ./deploy.sh
```

Here:

```yaml
${{ secrets.DATABASE_PASSWORD }}
```

References a secret available to the job.

The secret value is passed into the step's environment.

The example assumes that `deploy.sh` exists in the runner's working directory and knows how to use the credential. As written, the snippet does not check out a repository or create that script, so it illustrates credential wiring rather than a complete deployment.

## Why should we not write passwords directly in YAML?

Bad practice:

```yaml
steps:
  - name: Deploy
    run: |
      echo "Connecting with password MyProductionPassword123"
```

Problems:

1. The password becomes part of the repository's file history.
2. Other people with repository access may see it.
3. It may be copied into logs, screenshots, or documentation.
4. Removing the line later does not erase the secret from Git history.

Better practice:

```yaml
steps:
  - name: Deploy
    env:
      DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
    run: ./deploy.sh
```

Even when using GitHub secrets, be careful not to print them or pass them to untrusted scripts.

**A secret is protected storage, not permission to expose its value.**

---

# 9. Variables vs Secrets

Variables and secrets both provide values to workflows, but they serve different purposes.

| Feature | Variables | Secrets |
|---|---|---|
| Main purpose | Non-sensitive configuration | Sensitive credentials |
| Example | `APP_URL` | `DATABASE_PASSWORD` |
| Expression context | `vars` | `secrets` |
| Designed to protect confidentiality | No | Yes |
| Typical values | URLs, labels, regions | Passwords, tokens, keys |

Examples:

```yaml
env:
  APP_URL: ${{ vars.APP_URL }}
  DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

The two expressions come from different contexts.

```text
vars.APP_URL
      |
      v
Non-sensitive configuration

secrets.DATABASE_PASSWORD
      |
      v
Sensitive credential
```

Do not place sensitive information in variables simply because the variable is associated with a production environment.

**Rule:** If exposure of the value could compromise an account, system, or data, treat it as a secret.

---

# 10. Repository Secrets vs Environment Secrets

GitHub Actions supports different scopes for configuration.

Two important scopes are repository-level and environment-level.

## Repository-level secret

A repository secret is configured for the repository and can be made available to eligible workflows and jobs according to GitHub's access and security rules.

Example:

```yaml
${{ secrets.GLOBAL_API_TOKEN }}
```

Use this scope when the same credential is legitimately needed across several workflows or deployment contexts.

## Environment-level secret

An environment secret is configured for a specific environment.

For example:

```text
development:
  DATABASE_PASSWORD = development credential

staging:
  DATABASE_PASSWORD = staging credential

production:
  DATABASE_PASSWORD = production credential
```

Each environment can have a different value for the same secret name.

The workflow can use the same expression:

```yaml
env:
  DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

When the job is associated with the corresponding environment and is permitted to access it, GitHub makes the appropriate environment secret available.

## Why is this powerful?

You do not need to invent different variable names just to distinguish environments.

The same workflow can use:

```yaml
${{ secrets.DATABASE_PASSWORD }}
```

while the actual credential depends on the selected environment.

This makes it easier to maintain one deployment process for multiple environments.

**Security principle:** Use the narrowest scope that supports the task. A production-only credential should not be broadly available to unrelated jobs.

---

# 11. Deployment Protection Rules

Deploying to production is different from running a unit test.

A failed test may waste time. A bad production release can interrupt service or damage data.

GitHub environments can apply protection rules before a deployment job proceeds.

Depending on availability and configuration, these can include:

- Required reviewers
- Wait timers
- Deployment branch and tag restrictions
- Custom deployment protection rules where supported

Imagine a production job:

```yaml
jobs:
  deploy-production:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Deploy
        run: ./deploy.sh
```

If `production` has required reviewers configured, the job may pause until an authorized review is completed.

The protection rule is configured in GitHub's environment settings, not by writing `environment: production` alone.

The environment reference tells GitHub which environment's rules to apply.

## The protected deployment flow

```text
Workflow starts
      |
      v
Production job reaches environment gate
      |
      v
Protection rules evaluated
      |
      v
Approval required?
   /         \
 Yes          No
  |            |
Wait for       Continue when
approval       otherwise eligible
  |
  v
Approved?
 /     \
No      Yes
|        |
Stop     Job proceeds
         |
         v
    Deployment steps
```

An approval does not guarantee that the application will deploy successfully. It means the required protection gate was satisfied.

---

# 12. Required Reviewers and Manual Approval

A production release may require a responsible person to review it.

For example, a team might require a release manager to approve every production deployment.

Configure required reviewers in the environment's settings where the feature is available.

The workflow can remain simple:

```yaml
jobs:
  deploy-production:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - name: Deploy application
        run: ./deploy.sh
```

The workflow YAML references the environment. GitHub evaluates the protection rules configured for that environment.

## What does the reviewer actually approve?

The reviewer authorizes the protected job to proceed.

The reviewer should consider questions such as:

- Have tests passed?
- Is the correct version being released?
- Is the change safe for production?
- Is the deployment happening at the right time?
- Is there a rollback plan?

The approval gate does not replace testing, code review, monitoring, or rollback planning.

## Important distinction

The `environment` keyword does not mean that every deployment requires approval.

Approval is required only when the environment has a relevant protection rule configured.

Also, do not assume that every GitHub plan or repository configuration supports every protection feature. Check the options available in your repository settings.

---

# 13. Wait Timers

A wait timer introduces a configured delay before a protected deployment can proceed.

For example, a team may want to create a delay between authorizing a production deployment and allowing the job to continue.

A conceptual flow:

```text
Deployment job requests access
             |
             v
      Protection checks
             |
             v
      Wait timer starts
             |
             v
      Required delay passes
             |
             v
      Job may proceed if all rules pass
```

A wait timer is not the same as:

```yaml
run: sleep 300
```

A shell sleep merely pauses a running step. An environment wait timer is a deployment protection setting managed by GitHub.

Use the protection setting when the goal is to control deployment eligibility rather than simply delay a command.

---

# 14. Deployment Branch and Tag Restrictions

Not every branch should be allowed to deploy to production.

Imagine these branches:

```text
main
feature/login
feature/payment
experimental
```

You may want production deployments to originate only from an approved branch or tag pattern.

GitHub environment settings can restrict which branches or tags may deploy, subject to the available rule options.

For example, your policy might be:

```text
production:
  allowed deployment source = main
```

The exact policy should match your release process.

Why does this matter?

A developer may create a feature branch containing incomplete or experimental code. Even if the workflow file includes a production deployment job, the environment's deployment restrictions can prevent an unauthorized branch or tag from deploying through that environment.

Remember that **branch and tag restrictions configured on an environment are separate from branch protection rules**.

Use both where appropriate:

- Branch protection helps control changes to important branches.
- Environment protection helps control access to deployment targets.

Neither should be treated as a replacement for the other.

---

# 15. An Environment Does Not Automatically Deploy Anything

This is one of the most important concepts in this guide.

Consider:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - run: echo "Hello production"
```

What does this workflow do?

It associates the job with the `production` environment and prints a message.

It does not automatically:

- Upload a JAR file to a server
- Build a Docker image
- Push an image to GHCR
- Connect to Kubernetes
- Update a Deployment
- Restart a server
- Create a database
- Publish an application

Those actions need explicit steps, scripts, or deployment tools.

Think of an environment as the **deployment gate and configuration context**, not the deployment engine.

```text
GitHub Environment
      |
      +-- Supplies eligible environment configuration
      |
      +-- Applies configured deployment protection
      |
      +-- Records deployment-related information
      |
      v
Your workflow's actual deployment commands
      |
      v
Server / cloud / Kubernetes / another target
```

---

# 16. Environment vs Server vs Container

These concepts have different jobs.

| Concept | What it means |
|---|---|
| GitHub Actions Environment | Deployment context, configuration, and protection |
| Server | A machine or compute instance where software runs |
| Docker container | An isolated process environment created from an image |
| Kubernetes namespace | A logical grouping of Kubernetes resources |
| Git branch | A line of development in Git |
| GitHub Actions runner | The machine or execution environment that runs a workflow job |

Example:

```text
GitHub Actions job
        |
        | environment: production
        |
        v
Production deployment configuration
        |
        v
Deploy command
        |
        v
Production Kubernetes cluster
        |
        v
Namespace: production
        |
        v
Application Pods
```

Here, `production` appears as both a GitHub environment name and a Kubernetes namespace name.

They are still separate resources. Giving them the same name does not connect them automatically.

---

# 17. The Environment URL

You can associate a URL with an environment in the workflow.

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: production
      url: https://example.com

    steps:
      - name: Deploy
        run: echo "Deployment completed"
```

The URL helps GitHub display a useful destination in deployment-related information.

It does not prove that the website is healthy or that the deployment succeeded.

If the URL is dynamic, a workflow may set an environment URL using a supported expression, such as a job output, provided that output is available when GitHub evaluates the deployment information.

The important point is that a URL is metadata about the deployment target, not the deployment itself.

---

# 18. Development → Staging → Production

A common CI/CD pipeline moves through environments in stages.

## Stage 1: Development

The application is deployed to a development target.

The team checks basic functionality.

## Stage 2: Staging

The application is deployed to a test-like target.

The team checks whether the release behaves as expected.

## Stage 3: Production

The approved release is deployed to the live target.

Production may have stricter protection rules and more sensitive credentials.

```text
Code change
    |
    v
Build
    |
    v
Automated tests
    |
    v
Development deployment
    |
    v
Staging deployment
    |
    v
Validation
    |
    v
Production approval, if configured
    |
    v
Production deployment
```

A sensible pipeline should not treat the diagram as proof that each stage is configured correctly. You must define the dependencies, tests, protection rules, and actual deployment commands.

The environment names organize the deployment targets; the workflow determines how the stages relate to one another.

---

# 19. Using `needs` With Environments

The `needs` keyword defines job dependencies.

Suppose you want staging to run after the build, and production to run after staging.

```yaml
name: Staged Deployment

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build
        run: echo "Building application"

  deploy-staging:
    runs-on: ubuntu-latest
    needs: build
    environment: staging
    steps:
      - name: Deploy to staging
        run: echo "Deploying to staging"

  deploy-production:
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment: production
    steps:
      - name: Deploy to production
        run: echo "Deploying to production"
```

## How does the workflow run?

1. `build` runs first.
2. `deploy-staging` waits for `build`.
3. `deploy-production` waits for `deploy-staging`.
4. If the required earlier jobs succeed and no other condition prevents execution, the next job can proceed.
5. If production has a required approval, the production job may wait at its protection gate.

The `needs` keyword controls job ordering. The `environment` keyword associates a job with an environment and its applicable deployment protections.

They solve different problems.

---

# 20. A Complete Practical Workflow

Let's create a small workflow that uses a GitHub environment variable and secret.

First, configure the following in GitHub's `staging` environment:

```text
Variable:
APP_URL = https://staging.example.com

Secret:
DEPLOY_TOKEN = your-staging-deployment-token
```

Then use this workflow:

```yaml
name: Deploy to Staging

on:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  deploy:
    runs-on: ubuntu-latest

    environment:
      name: staging
      url: ${{ vars.APP_URL }}

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Show deployment target
        env:
          APP_URL: ${{ vars.APP_URL }}
        run: |
          echo "Preparing deployment to staging"
          echo "Target URL: $APP_URL"

      - name: Run deployment command
        env:
          APP_URL: ${{ vars.APP_URL }}
          DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
        run: |
          ./deploy.sh
```

This is an illustrative workflow. For it to work as a real deployment:

- The `staging` environment must be configured appropriately.
- The referenced secret must exist and have the correct value.
- The repository must contain an executable `deploy.sh`.
- The script must implement the actual deployment using the supplied configuration and credential.
- Any required tools and permissions must be available.

The token should not be printed in the logs.

## What did we learn?

The workflow uses:

```yaml
environment:
  name: staging
```

to associate the job with the environment.

It uses:

```yaml
${{ vars.APP_URL }}
```

to access a non-sensitive configuration variable.

It uses:

```yaml
${{ secrets.DEPLOY_TOKEN }}
```

to access a sensitive credential.

It uses `actions/checkout` to check out the repository so that the deployment script can be available to the job.

The actual deployment is performed by the script, not by the environment declaration.

---

# 21. Matrix Strategy With Environments

A matrix lets one job definition run multiple times with different input values.

Suppose you want to run the same demonstration for development and staging.

```yaml
name: Environment Matrix Demo

on:
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        target: [development, staging]

    environment: ${{ matrix.target }}

    steps:
      - name: Show target
        run: echo "Target environment is ${{ matrix.target }}"
```

GitHub creates a job run for each matrix value:

```text
matrix.target = development
        |
        v
environment: development

matrix.target = staging
        |
        v
environment: staging
```

Each job run is associated with the corresponding named environment.

The relevant environment must be configured with the variables, secrets, and protection rules you intend to use.

## Why is this useful?

A matrix can reduce repeated YAML when the deployment process is the same across several targets.

However, it does not automatically make production safe. If you add `production` to the matrix, you must account for its credentials, deployment restrictions, and approval requirements.

For important release pipelines, separate deployment jobs may be easier to audit than a matrix that combines development and production.

---

# 22. GitHub Actions Environments and Docker

Suppose your Spring Boot application is packaged as a JAR and then built into a Docker image.

A simplified pipeline may look like this:

```text
Java source code
      |
      v
Build Spring Boot JAR
      |
      v
Build Docker image
      |
      v
Push image to registry
      |
      v
Deploy image to staging
      |
      v
Validate release
      |
      v
Deploy image to production
```

GitHub environments can help manage deployment configuration and access for the staging and production jobs.

For example:

```yaml
jobs:
  deploy-staging:
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - run: echo "Deploy the selected image to staging"

  deploy-production:
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment: production
    steps:
      - run: echo "Deploy the selected image to production"
```

These commands are placeholders. A real pipeline must pull or reference the intended image and execute the deployment process.

**Important:** The Docker image itself does not become a GitHub environment. Docker images and GitHub deployment environments are separate concepts.

---

# 23. Environments and GitHub Container Registry

GitHub Container Registry (GHCR) stores container images.

For example, a Java application image may be published under:

```text
ghcr.io/OWNER/REPOSITORY:VERSION
```

A deployment workflow can use an image that has already been built and published.

A sound release strategy is to identify the image using a unique tag or immutable digest, test that image in staging, and promote the same artifact to production when possible.

```text
Build application once
        |
        v
Publish versioned image to GHCR
        |
        v
Deploy that image to staging
        |
        v
Validate it
        |
        v
Deploy the same image to production
```

GitHub environments can control deployment-specific configuration and protection rules.

They do not store the image, build it, or publish it to GHCR.

For production, prefer a deliberate permissions model and avoid giving deployment jobs unnecessary registry or cloud access.

---

# 24. Environments and Kubernetes

Imagine you have a Kubernetes cluster with two namespaces:

```text
Kubernetes Cluster
|
+-- staging namespace
|   +-- Application Deployment
|   +-- Service
|
+-- production namespace
    +-- Application Deployment
    +-- Service
```

Separately, your GitHub repository may contain:

```text
GitHub Environments
|
+-- staging
|
+-- production
```

The names are similar, but the resources are different.

The GitHub `staging` environment can provide the configuration and protection rules used by the workflow that deploys into the Kubernetes `staging` namespace.

The deployment command or tool must still tell Kubernetes which cluster and namespace to use.

A simplified example:

```yaml
jobs:
  deploy-staging:
    runs-on: ubuntu-latest
    environment: staging

    steps:
      - name: Deploy to Kubernetes staging
        run: |
          echo "Run the configured Kubernetes deployment here"
```

The example does not actually connect to Kubernetes. A real implementation needs cluster access, authentication, appropriate permissions, and a command such as `kubectl apply`, Helm, or another deployment mechanism.

## Security lesson

Do not assume that naming a GitHub environment `production` automatically protects your Kubernetes cluster.

You must separately configure the cluster's authentication, authorization, network access, and deployment permissions.

GitHub environment protection is one layer of the overall security design.

---

# 25. Branch Protection vs Environment Protection

These features solve different problems.

| Branch protection | Environment protection |
|---|---|
| Controls rules around changes to a Git branch | Controls eligibility to deploy through an environment |
| Can require reviews or checks before merging | Can require deployment reviewers or other supported protection rules |
| Helps protect source-code changes | Helps protect deployment targets |
| Example: protect `main` | Example: protect `production` |

Consider this situation:

A developer opens a pull request to change the application.

Branch protection can require the pull request to pass checks before it is merged.

Later, a workflow attempts to deploy the application.

Environment protection can require an authorized reviewer before the production deployment job proceeds.

```text
Pull request
    |
    v
Branch rules and code review
    |
    v
Merge to main
    |
    v
Build and test
    |
    v
Production environment protection
    |
    v
Actual deployment
```

Both mechanisms may be valuable because code review and deployment authorization are not the same thing.

---

# 26. The Difference Between `env:` and `environment:`

This is a common beginner confusion.

Although the names look similar, they serve different purposes.

## `env:`

The `env` keyword defines environment variables for a workflow, job, or step.

Example:

```yaml
jobs:
  example:
    runs-on: ubuntu-latest

    env:
      APP_NAME: NetRescue

    steps:
      - name: Show app name
        run: echo "$APP_NAME"
```

Here, `APP_NAME` is a variable available to the job's steps.

## `environment:`

The `environment` keyword associates a job with a GitHub deployment environment.

Example:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    steps:
      - run: echo "Deploying"
```

Here, `production` is the name of the GitHub environment.

## Compare them

| Question | `env:` | `environment:` |
|---|---|---|
| Defines or associates what? | Environment variables | GitHub deployment environment |
| Example | `APP_NAME: NetRescue` | `production` |
| Main purpose | Supply values to processes | Select deployment context and protections |
| Automatically deploys the app? | No | No |

You can use both in the same job:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest

    environment: production

    env:
      APP_NAME: NetRescue

    steps:
      - name: Show configuration
        run: echo "Deploying $APP_NAME"
```

**Remember:** `env:` supplies variables. `environment:` selects a GitHub deployment environment.

---

# 27. How Environment Secrets Are Protected

Secrets require careful handling throughout the workflow.

GitHub's secret mechanism helps protect secret values, but it does not make every use of those values safe.

For example:

```yaml
- name: Deploy
  env:
    DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
  run: ./deploy.sh
```

This passes the secret to the script.

If the script prints the token, sends it to an untrusted destination, or exposes it in an unsafe command, the workflow may leak the credential.

Follow these practices:

1. Store sensitive credentials in GitHub secrets or an appropriate external secret manager.
2. Use environment-specific credentials when the deployment target requires them.
3. Limit each job's permissions to what it needs.
4. Never print tokens, passwords, or private keys.
5. Avoid passing production credentials into jobs that only build or test code.
6. Be cautious with third-party actions and scripts that can access credentials.
7. Use short-lived credentials or identity federation where supported and practical.
8. Rotate credentials if they are exposed.
9. Protect the branches and workflow files that control deployments.
10. Treat untrusted pull request code as a potential security risk.

## A subtle but important point

A GitHub environment can have a required approval gate, but this does not mean that every job in the workflow is automatically safe.

If another job has broad credentials, or a workflow executes untrusted code with access to sensitive information, the overall pipeline may still be insecure.

**Security comes from the complete workflow design, not from one YAML keyword.**

---

# 28. Common Mistakes

## Mistake 1: Thinking the environment performs the deployment

Incorrect assumption:

```yaml
environment: production
```

means that GitHub deploys the application.

Correction: The environment associates the job with deployment configuration and protection. The workflow must perform the actual deployment.

## Mistake 2: Thinking every environment requires approval

An environment does not automatically require manual approval merely because it is named `production`.

Configure the relevant protection rules.

## Mistake 3: Putting passwords in variables

Bad:

```yaml
${{ vars.DATABASE_PASSWORD }}
```

for a sensitive password.

Better:

```yaml
${{ secrets.DATABASE_PASSWORD }}
```

## Mistake 4: Confusing `env` and `environment`

`env` defines variables. `environment` associates a job with a GitHub deployment environment.

## Mistake 5: Assuming the environment creates a server

Creating `production` in GitHub does not provision a virtual machine, cloud service, or Kubernetes cluster.

## Mistake 6: Assuming a Kubernetes namespace is the same as a GitHub environment

They are separate resources that can be connected by your deployment workflow.

## Mistake 7: Forgetting to create production secrets

A workflow may reference a secret that is missing or unavailable to that job.

Verify the environment configuration and permissions.

## Mistake 8: Assuming `needs` means approval

`needs` defines job dependencies. Approval comes from a configured protection rule.

## Mistake 9: Using a production secret in the build job

Build and test jobs should not receive production credentials unless there is a clearly justified need.

## Mistake 10: Printing a secret during debugging

Never print sensitive credentials to verify them. Check whether the credential is configured without exposing its value.

## Mistake 11: Deploying different image versions accidentally

A staging deployment may use one image while production uses another.

Identify artifacts precisely and promote the intended version.

## Mistake 12: Relying on an unconfigured environment

A referenced environment may be created automatically if it does not already exist. Do not rely on that behavior to establish production protection.

Create and configure production environments deliberately.

## Mistake 13: Treating an environment URL as a health check

A configured URL is deployment metadata. It does not prove that the application is healthy.

Use a real health check or smoke test.

## Mistake 14: Assuming approval means deployment success

Approval only satisfies a protection requirement. The deployment can still fail.

## Mistake 15: Giving every job broad permissions

Grant only the permissions required for each job. Protect deployment credentials and limit access to them.

---

# 29. Real-World Scenarios

## Scenario 1: Development and production use different database passwords

**Problem:** One workflow needs to deploy the same application to two environments.

**Solution:** Store the appropriate database credential as a secret in each GitHub environment and use the same secret expression in the deployment job.

```yaml
env:
  DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

The environment associated with the job determines which eligible environment secret is available.

## Scenario 2: Production must be manually approved

**Problem:** Developers should not be able to release to production without authorization.

**Solution:** Configure required reviewers on the `production` environment, where available.

Then associate the deployment job with:

```yaml
environment: production
```

## Scenario 3: A feature branch must not deploy to production

**Problem:** Experimental code should not reach the live application.

**Solution:** Configure environment deployment branch or tag restrictions, and protect important branches separately.

## Scenario 4: Staging must succeed before production

**Problem:** The release process must follow a specific order.

**Solution:** Use `needs` to express the job dependency.

```yaml
deploy-production:
  needs: deploy-staging
  environment: production
```

## Scenario 5: The same Docker image should be used in staging and production

**Problem:** Rebuilding independently for each environment could produce different artifacts.

**Solution:** Build and publish a uniquely identified image, test it in staging, and promote the same image to production.

## Scenario 6: Production deployment requires a cloud credential

**Problem:** The deployment job needs permission to update cloud resources.

**Solution:** Use an appropriately scoped credential or supported short-lived identity mechanism. Store or obtain it securely, and restrict it to the deployment job.

## Scenario 7: A deployment is approved but fails

**Problem:** The production approval succeeds, but the deployment command fails.

**Solution:** Inspect the deployment logs, identify the failure, and use a tested recovery or rollback procedure. Approval is not a guarantee of technical success.

## Scenario 8: A developer changes the production URL

**Problem:** A deployment needs a new URL without hardcoding it into YAML.

**Solution:** Update the appropriate environment variable in GitHub settings and reference it through `vars`.

---

# 30. Interview Questions and Answers

### Q1. What is a GitHub Actions Environment?

It is a named deployment context that can provide environment-specific variables and secrets and apply deployment protection rules to jobs associated with it.

### Q2. Why do we use environments?

To organize deployment targets, separate configuration, manage sensitive deployment credentials, and control when deployments may proceed.

### Q3. Where do you create an environment?

In the repository's GitHub **Settings → Environments** area, subject to the repository's available settings.

### Q4. How do you associate a job with an environment?

Use the `environment` keyword inside the job.

```yaml
environment: production
```

### Q5. Can one workflow use multiple environments?

Yes. Different jobs can reference different environments.

### Q6. What is the difference between `vars` and `secrets`?

`vars` provides non-sensitive configuration values; `secrets` provides sensitive values.

### Q7. What is the difference between repository secrets and environment secrets?

Repository secrets are configured at repository scope. Environment secrets are configured for a specific environment and are subject to that environment's access and protection behavior.

### Q8. Does naming an environment `production` automatically require approval?

No. Configure required reviewers or other relevant protection rules.

### Q9. Does `environment: production` deploy an application?

No. It associates the job with the environment. Actual deployment commands must be implemented separately.

### Q10. What is the difference between `needs` and `environment`?

`needs` controls job dependencies. `environment` associates a job with a GitHub deployment environment.

### Q11. Can a GitHub environment be named `staging` while the Kubernetes namespace is named `test`?

Yes. They are independent resources. Your deployment commands determine the actual Kubernetes target.

### Q12. What is the purpose of an environment URL?

It associates a URL with deployment information so the target can be identified more conveniently. It does not automatically validate application health.

### Q13. What is a deployment protection rule?

A rule that controls whether a deployment job may proceed, such as a required review or wait timer when supported and configured.

### Q14. What is the difference between branch protection and environment protection?

Branch protection controls changes to a Git branch. Environment protection controls deployment eligibility through a GitHub environment.

### Q15. Can environment secrets have the same name in different environments?

Yes. Each environment can define its own value for a secret name, allowing a shared workflow expression to resolve to the appropriate eligible credential.

### Q16. Does a GitHub environment create a virtual machine?

No. Infrastructure provisioning is a separate task.

### Q17. Can a matrix use different environments?

Yes. A matrix value can be used as the environment name, provided the intended environments and protections are configured appropriately.

### Q18. Why should build jobs avoid production secrets?

A build job generally does not need live deployment credentials. Limiting credential access reduces the impact of compromised code or an unsafe action.

---

# 31. Scenario-Based Questions

Try answering these questions before reading the explanations.

### Scenario 1

You have one `DATABASE_PASSWORD` secret in `development` and another secret with the same name in `production`.

Which expression can a deployment job use?

```yaml
${{ secrets.DATABASE_PASSWORD }}
```

**Answer:** The same expression can be used. The job must be associated with the appropriate environment and permitted to access its secrets.

### Scenario 2

Your YAML contains:

```yaml
environment: production
```

but the workflow prints a message instead of updating the application.

Is GitHub Actions broken?

**Answer:** No. The environment declaration does not implement the deployment. Add the required deployment command or action.

### Scenario 3

Your production job has:

```yaml
needs: deploy-staging
```

Does this guarantee that a human approved production?

**Answer:** No. It establishes a dependency on the staging job. Configure a production protection rule if human approval is required.

### Scenario 4

A developer changes a GitHub environment variable named `APP_URL`. Does that automatically update the application's running configuration?

**Answer:** No. The variable is available to eligible workflow jobs. A workflow must use it during deployment, and the deployment mechanism must apply the change to the running application.

### Scenario 5

You create a GitHub environment called `production`. Does Kubernetes automatically create a `production` namespace?

**Answer:** No. GitHub and Kubernetes manage separate resources.

### Scenario 6

A production deployment passes the approval gate but the deployment command fails.

What does that tell you?

**Answer:** The required protection gate was satisfied, but the technical deployment failed. Investigate the logs and follow the recovery process.

### Scenario 7

You want to prevent deployment from feature branches.

What should you consider?

**Answer:** Configure the environment's deployment branch or tag restrictions where available, and protect important Git branches separately.

### Scenario 8

Your workflow sends a production token to a third-party action that you do not trust.

Is the environment enough to make that safe?

**Answer:** No. You must assess the action's trustworthiness, credential exposure, permissions, and overall workflow security.

---

# 32. Production-Style Example

The following example demonstrates a staged workflow with a build job, a staging deployment, and a production deployment.

It focuses on job dependencies and environment association. The deployment commands are placeholders because every infrastructure target requires its own deployment implementation.

```yaml
name: Application Release Pipeline

on:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Build application
        run: |
          echo "Build the application here"

      - name: Run tests
        run: |
          echo "Run automated tests here"

  deploy-staging:
    runs-on: ubuntu-latest
    needs: build

    environment:
      name: staging
      url: ${{ vars.APP_URL }}

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Deploy to staging
        env:
          DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
          APP_URL: ${{ vars.APP_URL }}
        run: |
          echo "Run the staging deployment command here"

  deploy-production:
    runs-on: ubuntu-latest
    needs: deploy-staging

    environment:
      name: production
      url: ${{ vars.APP_URL }}

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Deploy to production
        env:
          DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
          APP_URL: ${{ vars.APP_URL }}
        run: |
          echo "Run the production deployment command here"
```

## How should you read this workflow?

### Build job

The `build` job prepares the application and runs tests.

In a real workflow, the commands must actually compile and test the application. If deployment jobs need the built artifact, the workflow must also transfer or retrieve it correctly.

### Staging job

```yaml
needs: build
```

means staging waits for the build job.

```yaml
environment:
  name: staging
```

associates the job with the `staging` environment.

### Production job

```yaml
needs: deploy-staging
```

means production waits for the staging job to complete successfully under the normal dependency rules.

```yaml
environment:
  name: production
```

associates the job with the production environment.

If production has a required approval rule, the job may wait for approval before proceeding.

## Important production improvements

Before using a workflow like this in a real project:

- Replace placeholder commands with working build, test, and deployment steps.
- Build the release artifact and transfer it to the deployment job correctly.
- Deploy the intended, versioned artifact.
- Configure environment variables and secrets in GitHub.
- Configure production protection rules.
- Limit credentials and job permissions.
- Add smoke tests and health checks.
- Define a rollback strategy.
- Review the permissions of third-party actions.
- Consider using short-lived cloud credentials where supported.

This is the difference between understanding the structure of a CI/CD pipeline and having a fully operational production pipeline.

---

# 33. The Complete Mental Model

Remember the purpose of each component.

```text
GitHub Actions Workflow
        |
        | Defines the automation
        v
Jobs
        |
        | Define units of work
        v
Build / Test / Deploy
        |
        | A deployment job references an environment
        v
GitHub Environment
        |
        +-- Variables
        |
        +-- Secrets
        |
        +-- Protection rules
        |
        +-- Deployment information
        |
        v
Actual deployment steps
        |
        v
Server / Cloud / Kubernetes
        |
        v
Running application
```

The most important distinctions:

- **Workflow:** What automation should run?
- **Job:** What group of work should execute?
- **`needs`:** Which job must complete before another can proceed?
- **Environment:** Which GitHub deployment context and protection rules apply?
- **Variables:** Which non-sensitive configuration values should be used?
- **Secrets:** Which sensitive credentials can an eligible job access?
- **Protection rules:** What conditions must be satisfied before a deployment job proceeds?
- **Deployment command:** How does the application actually reach its target?
- **Infrastructure:** Where does the application run?

When these responsibilities are clear, GitHub Actions Environments become much easier to understand.

---

# 34. Quick Revision Cheat Sheet

| Requirement | Concept |
|---|---|
| Select the deployment context for a job | `environment:` |
| Define a process environment variable | `env:` |
| Read a GitHub Actions variable | `${{ vars.NAME }}` |
| Read a GitHub Actions secret | `${{ secrets.NAME }}` |
| Make staging wait for build | `needs: build` |
| Make production wait for staging | `needs: deploy-staging` |
| Associate a URL with an environment | `environment.url` |
| Require a production reviewer | Configure environment protection rules |
| Restrict deployment branches | Configure environment deployment branch/tag rules where available |
| Build a Docker image | Use build steps or an appropriate action |
| Publish an image to GHCR | Use registry authentication and a push step |
| Deploy to Kubernetes | Use Kubernetes credentials and deployment commands |
| Check application health | Add a real health or smoke test |

## Final revision

A GitHub Actions Environment is not a server, container, or Kubernetes namespace.

It is a named deployment context that can provide environment-specific configuration, secrets, and protection rules to jobs associated with it.

The `environment` keyword associates the job with that context. The `vars` and `secrets` contexts provide configuration and credentials. Protection rules control deployment eligibility. The workflow's deployment steps perform the actual release.

**The one-sentence rule to remember:**

> GitHub Actions Environments help you control *where a job is deploying, which environment-specific configuration it can use, and which deployment protection rules apply* — while your workflow still performs the actual deployment.

---

**End of Guide — GitHub Actions Environments**

