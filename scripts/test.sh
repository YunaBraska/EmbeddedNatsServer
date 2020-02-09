#!/usr/bin/env
sh "$(dirname "$0")/build.sh"
mvn clean verify