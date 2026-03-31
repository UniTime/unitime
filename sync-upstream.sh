#!/bin/bash
# =============================================================================
# UniTime Upstream Sync Script
# DigiPen R&D — Automated fork maintenance
#
# Usage:
#   ./sync-upstream.sh              # Check + merge + build
#   ./sync-upstream.sh --deploy     # Check + merge + build + deploy to Docker
#   ./sync-upstream.sh --dry-run    # Only check, don't change anything
# =============================================================================

set -e

# ── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ── Config ───────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

UPSTREAM_REMOTE="upstream"
UPSTREAM_BRANCH="master"
ARCHIVE_DIR="$SCRIPT_DIR/archives"
LOG_DIR="$SCRIPT_DIR/logs"
DOCKER_DIR="$SCRIPT_DIR/../New folder"
LOG_FILE="$LOG_DIR/sync-$(date +%Y-%m-%d_%H-%M-%S).log"

# ── Parse flags ──────────────────────────────────────────────────────────────
DEPLOY=false
DRY_RUN=false

for arg in "$@"; do
    case "$arg" in
        --deploy)  DEPLOY=true ;;
        --dry-run) DRY_RUN=true ;;
        --help|-h)
            echo "Usage: ./sync-upstream.sh [--deploy] [--dry-run]"
            echo ""
            echo "  --deploy    After successful build, deploy WAR to Docker and restart"
            echo "  --dry-run   Only check for updates, don't merge or build"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown flag: $arg${NC}"
            echo "Run ./sync-upstream.sh --help for usage"
            exit 1
            ;;
    esac
done

# ── Setup logging ────────────────────────────────────────────────────────────
mkdir -p "$LOG_DIR"

log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $1"
    echo "$msg" >> "$LOG_FILE"
    echo -e "$2$1${NC}"
}

log "========== UniTime Upstream Sync ==========" "$BOLD"
log "Mode: $(if $DRY_RUN; then echo 'DRY RUN'; elif $DEPLOY; then echo 'SYNC + DEPLOY'; else echo 'SYNC + BUILD'; fi)" "$CYAN"

# ── Step 1: Get current version info ─────────────────────────────────────────
log "" ""
log "--- Step 1: Checking current version ---" "$BOLD"

OUR_BRANCH=$(git branch --show-current)
OUR_COMMIT=$(git rev-parse HEAD)
OUR_COMMIT_SHORT=$(git rev-parse --short HEAD)
OUR_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
OUR_BUILD=$(grep 'build.number=' build.number 2>/dev/null | cut -d= -f2 || echo "?")

log "Branch:  $OUR_BRANCH" "$NC"
log "Version: $OUR_VERSION (build $OUR_BUILD)" "$NC"
log "Commit:  $OUR_COMMIT_SHORT" "$NC"

# ── Step 2: Check upstream ───────────────────────────────────────────────────
log "" ""
log "--- Step 2: Checking upstream ($UPSTREAM_REMOTE/$UPSTREAM_BRANCH) ---" "$BOLD"

# Fetch upstream refs without downloading objects yet
UPSTREAM_COMMIT=$(git ls-remote "$UPSTREAM_REMOTE" "refs/heads/$UPSTREAM_BRANCH" | cut -f1)

if [ -z "$UPSTREAM_COMMIT" ]; then
    log "ERROR: Could not reach upstream remote '$UPSTREAM_REMOTE'" "$RED"
    log "Check your internet connection and that the remote is configured:" "$RED"
    log "  git remote -v" "$NC"
    exit 1
fi

UPSTREAM_COMMIT_SHORT="${UPSTREAM_COMMIT:0:7}"

# Get latest upstream tag (compatible with Windows Git Bash — no -P flag)
UPSTREAM_TAG=$(git ls-remote --tags "$UPSTREAM_REMOTE" | grep -o 'refs/tags/v[0-9][0-9.]*$' | sed 's|refs/tags/||' | sort -V | tail -1)
UPSTREAM_TAG="${UPSTREAM_TAG:-unknown}"

log "Upstream commit: $UPSTREAM_COMMIT_SHORT" "$NC"
log "Latest tag:      $UPSTREAM_TAG" "$NC"

# ── Step 3: Compare ─────────────────────────────────────────────────────────
log "" ""
log "--- Step 3: Comparing versions ---" "$BOLD"

if [ "$OUR_COMMIT" = "$UPSTREAM_COMMIT" ]; then
    log "Already up to date! No changes needed." "$GREEN"
    log "Our commit and upstream/$UPSTREAM_BRANCH are identical." "$GREEN"
    exit 0
fi

# Check if upstream is an ancestor (we're ahead) or if there are new changes
git fetch "$UPSTREAM_REMOTE" "$UPSTREAM_BRANCH" --quiet

BEHIND=$(git rev-list --count "HEAD..$UPSTREAM_REMOTE/$UPSTREAM_BRANCH")
AHEAD=$(git rev-list --count "$UPSTREAM_REMOTE/$UPSTREAM_BRANCH..HEAD")

log "We are $BEHIND commit(s) BEHIND upstream" "$YELLOW"
log "We are $AHEAD commit(s) AHEAD of upstream (our customizations)" "$NC"

if [ "$BEHIND" -eq 0 ]; then
    log "We have all upstream changes (we're ahead with local commits). No update needed." "$GREEN"
    exit 0
fi

log "UPDATE AVAILABLE: $BEHIND new commit(s) from upstream" "$YELLOW"

# ── Show what changed ────────────────────────────────────────────────────────
log "" ""
log "--- Changes summary ---" "$BOLD"

CHANGED_FILES=$(git diff --stat "HEAD...$UPSTREAM_REMOTE/$UPSTREAM_BRANCH" | tail -1)
log "$CHANGED_FILES" "$NC"

log "" ""
log "Files changed:" "$NC"
git diff --name-only "HEAD...$UPSTREAM_REMOTE/$UPSTREAM_BRANCH" | head -30 | while read -r file; do
    log "  $file" "$NC"
done

TOTAL_CHANGED=$(git diff --name-only "HEAD...$UPSTREAM_REMOTE/$UPSTREAM_BRANCH" | wc -l | tr -d ' ')
if [ "$TOTAL_CHANGED" -gt 30 ]; then
    log "  ... and $((TOTAL_CHANGED - 30)) more files" "$CYAN"
fi

# ── Dry run stops here ──────────────────────────────────────────────────────
if $DRY_RUN; then
    log "" ""
    log "=== DRY RUN COMPLETE ===" "$CYAN"
    log "Run without --dry-run to apply the update." "$NC"
    exit 0
fi

# ── Step 4: Archive old version ──────────────────────────────────────────────
log "" ""
log "--- Step 4: Archiving current version ---" "$BOLD"

ARCHIVE_SUBDIR="$ARCHIVE_DIR/v${OUR_VERSION}.${OUR_BUILD}_$(date +%Y-%m-%d)"
mkdir -p "$ARCHIVE_SUBDIR"

# Save old WAR
if [ -f "target/UniTime.war" ]; then
    cp target/UniTime.war "$ARCHIVE_SUBDIR/UniTime.war"
    log "Saved WAR to $ARCHIVE_SUBDIR/UniTime.war" "$NC"
else
    log "No existing WAR file to archive" "$YELLOW"
fi

# Save version metadata
cat > "$ARCHIVE_SUBDIR/version-info.txt" <<EOF
Version:        $OUR_VERSION (build $OUR_BUILD)
Branch:         $OUR_BRANCH
Commit:         $OUR_COMMIT
Archived:       $(date)
Reason:         Upstream update to $UPSTREAM_TAG ($UPSTREAM_COMMIT_SHORT)
Changes behind: $BEHIND commit(s)
EOF

log "Version info saved to $ARCHIVE_SUBDIR/version-info.txt" "$NC"
log "Old version archived successfully" "$GREEN"

# ── Step 5: Merge upstream ──────────────────────────────────────────────────
log "" ""
log "--- Step 5: Merging upstream changes ---" "$BOLD"

# Attempt merge
MERGE_MSG="Merge upstream UniTime $UPSTREAM_TAG ($UPSTREAM_COMMIT_SHORT)"

if ! git merge "$UPSTREAM_REMOTE/$UPSTREAM_BRANCH" --no-commit --no-ff 2>> "$LOG_FILE"; then
    # Check for conflicts
    CONFLICT_FILES=$(git diff --name-only --diff-filter=U)

    if [ -n "$CONFLICT_FILES" ]; then
        log "" ""
        log "MERGE CONFLICTS DETECTED!" "$RED"
        log "The following files have conflicts:" "$RED"
        echo "$CONFLICT_FILES" | while read -r file; do
            log "  CONFLICT: $file" "$RED"
        done

        log "" ""
        log "To resolve manually:" "$YELLOW"
        log "  1. Open each conflicted file and resolve the <<<< ==== >>>> markers" "$NC"
        log "  2. git add <resolved-file>" "$NC"
        log "  3. git commit -m \"$MERGE_MSG\"" "$NC"
        log "  4. Re-run: ./sync-upstream.sh $(if $DEPLOY; then echo '--deploy'; fi)" "$NC"
        log "" ""
        log "To abort the merge:" "$YELLOW"
        log "  git merge --abort" "$NC"

        # Abort the merge so we leave the repo clean
        git merge --abort
        log "" ""
        log "Merge aborted — repo is back to its original state." "$YELLOW"
        log "Old version preserved in: $ARCHIVE_SUBDIR" "$NC"
        exit 1
    fi
fi

# Commit the merge
git commit -m "$MERGE_MSG" >> "$LOG_FILE" 2>&1
NEW_COMMIT_SHORT=$(git rev-parse --short HEAD)
log "Merge successful! New commit: $NEW_COMMIT_SHORT" "$GREEN"

# ── Step 6: Build ────────────────────────────────────────────────────────────
log "" ""
log "--- Step 6: Building UniTime ---" "$BOLD"
log "Running: mvn package -DskipTests (this may take a few minutes...)" "$NC"

if mvn package -DskipTests >> "$LOG_FILE" 2>&1; then
    log "Build SUCCESSFUL!" "$GREEN"

    NEW_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
    NEW_BUILD=$(grep 'build.number=' build.number 2>/dev/null | cut -d= -f2 || echo "?")
    log "New version: $NEW_VERSION (build $NEW_BUILD)" "$GREEN"
    log "WAR file: target/UniTime.war" "$NC"
else
    log "BUILD FAILED!" "$RED"
    log "Check the full log: $LOG_FILE" "$RED"
    log "Old version preserved in: $ARCHIVE_SUBDIR" "$YELLOW"
    log "You may want to revert the merge: git reset --hard HEAD~1" "$YELLOW"
    exit 1
fi

# ── Step 7: Deploy (optional) ───────────────────────────────────────────────
if $DEPLOY; then
    log "" ""
    log "--- Step 7: Deploying to Docker ---" "$BOLD"

    if [ ! -d "$DOCKER_DIR" ]; then
        log "ERROR: Docker directory not found at: $DOCKER_DIR" "$RED"
        log "WAR file is ready at target/UniTime.war — deploy manually." "$YELLOW"
        exit 1
    fi

    cp target/UniTime.war "$DOCKER_DIR/web/UniTime.war"
    log "WAR copied to Docker volume" "$NC"

    cd "$DOCKER_DIR"
    docker-compose down >> "$LOG_FILE" 2>&1
    docker-compose build unitime-web >> "$LOG_FILE" 2>&1
    docker-compose up -d >> "$LOG_FILE" 2>&1
    cd "$SCRIPT_DIR"

    log "Docker containers restarted!" "$GREEN"
    log "Verify at: http://localhost:8888 -> Help -> About" "$CYAN"
fi

# ── Done ─────────────────────────────────────────────────────────────────────
log "" ""
log "========== Sync Complete ==========" "$GREEN"
log "Previous: v${OUR_VERSION}.${OUR_BUILD} ($OUR_COMMIT_SHORT)" "$NC"
log "Current:  v${NEW_VERSION:-$OUR_VERSION}.${NEW_BUILD:-$OUR_BUILD} ($NEW_COMMIT_SHORT)" "$NC"
log "Archive:  $ARCHIVE_SUBDIR" "$NC"
log "Log:      $LOG_FILE" "$NC"
