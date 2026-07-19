#!/usr/bin/env bash
# Run the test suite through the repository-pinned Gradle wrapper.
set -euo pipefail
cd "$(dirname "$0")/../.."

./gradlew test
