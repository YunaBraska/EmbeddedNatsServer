#!/usr/bin/env bash
"$(dirname "$0")/tag.sh"
mvn clean package deploy -P release -DskipTests