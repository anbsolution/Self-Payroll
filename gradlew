#!/bin/sh
# Lightweight project launcher. GitHub Actions installs Gradle 9.6 explicitly.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed. Install Gradle 9.6+ or use the included GitHub Actions workflow." >&2
exit 1
