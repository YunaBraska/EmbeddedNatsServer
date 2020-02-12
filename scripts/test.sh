#!/usr/bin/env bash
"$(dirname "$0")/build.sh"
mvn clean -Dsurefire.skipAfterFailureCount=1 verify