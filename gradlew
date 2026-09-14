#!/bin/sh
# Android/Termux-friendly Gradle bootstrap launcher for this project.
# If Gradle is already installed, use it. Otherwise download the pinned
# Gradle distribution into ~/.gradle/wrapper-bootstrap and run it.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.6.1
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
BOOTSTRAP_DIR="$GRADLE_USER_HOME/wrapper-bootstrap/gradle-$GRADLE_VERSION"

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

if [ ! -x "$BOOTSTRAP_DIR/bin/gradle" ]; then
  command -v curl >/dev/null 2>&1 || { echo "curl is required to bootstrap Gradle $GRADLE_VERSION." >&2; exit 1; }
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required to bootstrap Gradle $GRADLE_VERSION." >&2; exit 1; }
  mkdir -p "$GRADLE_USER_HOME/wrapper-bootstrap"
  tmp="$GRADLE_USER_HOME/wrapper-bootstrap/gradle-$GRADLE_VERSION-bin.zip"
  if [ ! -f "$tmp" ]; then
    curl -fL --retry 3 -o "$tmp" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  fi
  rm -rf "$BOOTSTRAP_DIR" "$GRADLE_USER_HOME/wrapper-bootstrap/gradle-$GRADLE_VERSION"
  unzip -q "$tmp" -d "$GRADLE_USER_HOME/wrapper-bootstrap"
fi

exec "$BOOTSTRAP_DIR/bin/gradle" "$@"
