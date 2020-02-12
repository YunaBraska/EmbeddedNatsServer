#!/usr/bin/env bash
"$(dirname "$0")/build.sh"
mvn clean verify -DskipTests=true -P build -Dtag