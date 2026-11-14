#!/usr/bin/env bash
set -euo pipefail

# Build & export script for Weed Guru / Cyber Tarot
# Default target directory: ~/CTV2
# Default APK export path:  ~/apkexport/WeedGuru.apk

PROJECT_DIR="${PROJECT_DIR:-$HOME/CTV2}"
EXPORT_DIR="${EXPORT_DIR:-$HOME/apkexport}"
EXPORT_APK="$EXPORT_DIR/WeedGuru.apk"
REPO_URL="${REPO_URL:-https://github.com/PerfectFriend/WEED-GURU-TAROT.git}"
BRANCH="${BRANCH:-main}"
FORCE_GIT_SYNC=false

for arg in "$@"; do
  case "$arg" in
    --force-git-sync)
      FORCE_GIT_SYNC=true
      ;;
    --help|-h)
      echo "Usage: bash build_weedguru.sh [--force-git-sync]"
      echo "  Builds :app:assembleDebug from \$PROJECT_DIR (default: ~/CTV2)"
      echo "  and copies APK to ~/apkexport/WeedGuru.apk"
      echo "  Env overrides: PROJECT_DIR, EXPORT_DIR, REPO_URL, BRANCH"
      exit 0
      ;;
  esac
done

# If executed from inside an unpacked project folder without ~/CTV2/.git, use current dir if matching
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ ! -d "$PROJECT_DIR" ] && [ -f "$SCRIPT_DIR/settings.gradle.kts" ]; then
  PROJECT_DIR="$SCRIPT_DIR"
fi

echo "[1/5] Preparing project in $PROJECT_DIR..."
if [ -d "$PROJECT_DIR/.git" ]; then
  git -C "$PROJECT_DIR" fetch origin
  if git -C "$PROJECT_DIR" show-ref --verify --quiet "refs/remotes/origin/$BRANCH"; then
    git -C "$PROJECT_DIR" pull --ff-only origin "$BRANCH"
  else
    git -C "$PROJECT_DIR" pull --ff-only
  fi
elif [ ! -d "$PROJECT_DIR" ] || [ -z "$(ls -A "$PROJECT_DIR" 2>/dev/null)" ]; then
  mkdir -p "$(dirname "$PROJECT_DIR")"
  git clone "$REPO_URL" "$PROJECT_DIR"
elif [ "$FORCE_GIT_SYNC" = true ]; then
  echo "Initializing git remote $REPO_URL and syncing $BRANCH..."
  git -C "$PROJECT_DIR" init
  git -C "$PROJECT_DIR" remote remove origin 2>/dev/null || true
  git -C "$PROJECT_DIR" remote add origin "$REPO_URL"
  git -C "$PROJECT_DIR" fetch origin
  if git -C "$PROJECT_DIR" show-ref --verify --quiet "refs/remotes/origin/$BRANCH"; then
    git -C "$PROJECT_DIR" checkout -f -B "$BRANCH" "origin/$BRANCH"
  else
    DEFAULT_BRANCH="$(git -C "$PROJECT_DIR" remote show origin | awk '/HEAD branch/ {print $NF}')"
    git -C "$PROJECT_DIR" checkout -f -B "$DEFAULT_BRANCH" "origin/$DEFAULT_BRANCH"
  fi
elif [ -f "$PROJECT_DIR/settings.gradle.kts" ]; then
  echo "Detected unpacked project (no .git); building local files."
  echo "(Tip: pass --force-git-sync to attach and pull from $REPO_URL)"
else
  echo "Error: $PROJECT_DIR exists, is not empty, and is neither a git repo nor a Gradle project."
  exit 1
fi

cd "$PROJECT_DIR"

echo "[2/5] Checking Android SDK..."
if [ -z "${ANDROID_SDK_ROOT:-}" ] && [ -z "${ANDROID_HOME:-}" ]; then
  if [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
    export ANDROID_HOME="$HOME/Android/Sdk"
  elif [ -d "/opt/android-sdk" ]; then
    export ANDROID_SDK_ROOT="/opt/android-sdk"
    export ANDROID_HOME="/opt/android-sdk"
  fi
fi

if [ -z "${ANDROID_SDK_ROOT:-}" ] && [ -z "${ANDROID_HOME:-}" ] && [ ! -f "$PROJECT_DIR/local.properties" ]; then
  echo "Error: ANDROID_SDK_ROOT / ANDROID_HOME is not set and local.properties is missing."
  echo "Run: export ANDROID_SDK_ROOT=\"\$HOME/Android/Sdk\" (or your SDK path) and retry."
  exit 1
fi

echo "[3/5] Checking debug.keystore..."
if [ ! -s "$PROJECT_DIR/debug.keystore" ]; then
  if [ -f "$PROJECT_DIR/debug.keystore.base64" ]; then
    base64 -d "$PROJECT_DIR/debug.keystore.base64" > "$PROJECT_DIR/debug.keystore" || rm -f "$PROJECT_DIR/debug.keystore"
  fi
  if [ ! -s "$PROJECT_DIR/debug.keystore" ]; then
    keytool -genkey -v \
      -keystore "$PROJECT_DIR/debug.keystore" \
      -storepass android \
      -alias androiddebugkey \
      -keypass android \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -dname "CN=Android Debug,O=Android,C=US"
  fi
fi

echo "[4/5] Compiling Debug APK..."
if [ -x "$PROJECT_DIR/gradlew" ]; then
  GRADLE_CMD="$PROJECT_DIR/gradlew"
elif command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
else
  echo "Error: Neither ./gradlew nor system 'gradle' was found."
  echo "Install Gradle 9.x (AGP 9.1.1 requires Gradle 9+) and JDK 17+."
  exit 1
fi

"$GRADLE_CMD" :app:assembleDebug

BUILT_APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$BUILT_APK" ]; then
  echo "Error: Build succeeded, but APK not found at $BUILT_APK"
  exit 1
fi

echo "[5/5] Copying APK to $EXPORT_APK..."
mkdir -p "$EXPORT_DIR"
cp -f "$BUILT_APK" "$EXPORT_APK"

echo "Done! APK ready at: $EXPORT_APK"
ls -lh "$EXPORT_APK"
