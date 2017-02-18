#!/bin/sh
set -e
LAUNCHER=$(readlink -f "$0")
ROOT=$(dirname "$LAUNCHER")
java -jar "$ROOT/plooze-cli-full.jar" "$@"
