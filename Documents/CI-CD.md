# CI/CD Pipeline — GitLab

The project uses a GitLab CI/CD pipeline defined in `.gitlab-ci.yml` at the root of the repository. It runs automatically on every push to `main` and handles building and deploying the Spring Boot backend to the class production server.

---

## Pipeline Overview

```
push to main
      │
      ▼
 ┌─────────┐     ┌──────────┐
 │  build  │────▶│  deploy  │
 └─────────┘     └──────────┘
```

The pipeline has two sequential stages. The `deploy` stage only runs if `build` succeeds, and it depends on the JAR artifact produced by `build`.

---

## Stage: `build`

Compiles the Spring Boot backend into a runnable JAR using Maven.

**What it does:**
- Changes into the `Backend/` directory
- Runs `mvn clean package -DskipTests`
- Tests are skipped here — they run separately via Maven Surefire or manually

**Artifacts produced:**
- `Backend/target/*.jar` — the runnable Spring Boot JAR
- `Backend/src/**` and `Backend/pom.xml` — source files passed to deploy

Artifacts expire after **1 hour**.

**Cache:**
Maven dependencies (`.m2/repository/`) and `Backend/target/` are cached between pipeline runs to avoid re-downloading dependencies on every build.

**Trigger:** Only runs on pushes to `main`.

---

## Stage: `deploy`

Deploys the built JAR to the production server and restarts the application. Sends Discord notifications at the start, on success, and on failure.

**Steps in order:**

### 1. Discord — Deployment Started
Sends a blue embed to the project Discord channel with:
- Branch name
- Short commit SHA
- Author (GitLab username)
- First line of the commit message

### 2. File Sync (rsync)
Copies the `Backend/` directory to `~/app/` on the server using `rsync`, excluding:
- `.git/`
- `node_modules/`
- `.idea/`
- `*.iml`
- `.DS_Store`

The `--delete` flag removes any files in `~/app/` that no longer exist in `Backend/`, keeping the server directory clean.

### 3. Restart Application
Runs inside `~/app/target/` on the server:
1. Kills any existing `demo-0.0.1-SNAPSHOT.jar` process with `pkill` (safe — ignores errors if nothing is running)
2. Waits 3 seconds for the port to free up
3. Starts the JAR in a new detached GNU Screen session named `nerdmarket`
4. Waits 2 seconds for the JVM to initialize
5. Verifies the `nerdmarket` screen session is listed — exits with code 1 if not, failing the pipeline

### 4. Discord — Deployment Successful
Sends a green embed confirming the app is live, including the server URL, app directory, and screen session name.

### After Script (failure only)
If any deploy step exits with a non-zero code, `after_script` sends a red Discord embed with the GitLab pipeline URL so the team can check the logs.

**Trigger:** Only runs on pushes to `main`. Depends on the `build` stage artifact.

---

## Discord Notifications

| Event | Embed Color | When Sent |
|-------|-------------|-----------|
| Deployment Started | Blue (`#3498DB`) | Deploy job begins |
| Deployment Successful | Green (`#2ECC71`) | App verified running in screen |
| Deployment Failed | Red (`#E74C3C`) | Any deploy step fails |

All embeds include: branch, short commit SHA, author, and the first line of the commit message.

---

## Variables

| Variable | Description |
|----------|-------------|
| `MAVEN_OPTS` | Points Maven's local repository to `$CI_PROJECT_DIR/.m2/repository` so it's picked up by the cache |
| `DISCORD_WEBHOOK` | Full Discord webhook URL for the notification channel |

> **Security note:** The `DISCORD_WEBHOOK` URL is currently hardcoded in `.gitlab-ci.yml`. If the repository is ever public (or to rotate the webhook safely), move it to a GitLab masked CI/CD variable under `Settings → CI/CD → Variables` and reference it as `$DISCORD_WEBHOOK` — which is already what the YAML uses.

---

## Production Server

| Detail | Value |
|--------|-------|
| Hostname | `coms-3090-022.class.las.iastate.edu` |
| Port | `8080` |
| App directory | `~/app/` |
| JAR filename | `demo-0.0.1-SNAPSHOT.jar` |
| Process manager | GNU Screen — session named `nerdmarket` |

---

## Useful Server Commands

```bash
# Check if the app is running
screen -list

# Attach to the running app session
screen -r nerdmarket

# Detach from screen without killing it
Ctrl+A, D

# Manually kill and restart the app
pkill -f demo-0.0.1-SNAPSHOT.jar
cd ~/app/target
screen -dmS nerdmarket java -jar demo-0.0.1-SNAPSHOT.jar

# Check screen session exists
screen -list | grep nerdmarket
```

---

## First-Time Setup Notes

- The pipeline runner must have SSH access to the production server with a key registered in GitLab CI/CD variables.
- The `~/app/` directory must exist on the server before the first deploy.
- The first admin user must be created manually (via DB or signup) and then promoted directly in the database, since no admin exists initially to call the promote endpoint.
