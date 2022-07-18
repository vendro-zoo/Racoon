#!/usr/bin/env bash
BNAME=$(git branch --show-current)
CHASH=$(git rev-parse --short HEAD)

FCONT=$(cat ./gradle.properties.base)
FCONT="${FCONT/__branch_name__/${BNAME}}"
FCONT="${FCONT/__commit_hash__/${CHASH}}"

touch ./gradle.properties
echo "${FCONT}" > ./gradle.properties

set -x

./gradlew clean build -x test
./gradlew publishToMavenLocal