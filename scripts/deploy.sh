#!/usr/bin/env
sh "$(dirname "$0")/build.sh"
mvn deploy -P release -DskipTests