#!/usr/bin/env bash

# The MIT License (MIT)
# Copyright (c) 2022 a-vezzoli-02

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

DYNAMIC_NAMING=false
PUBLISH_LOCAL=false
SHADOW_BUILD=false

while getopts ":hdps" opt; do
  case $opt in
    h)
      echo "Usage: kbuild.sh [-h] [-d] [-p]"
      echo " -h Show this help message and exit."
      echo " -d Use dynamic naming. Overwrites gradle.properties with a generated one starting from gradle.properties.base"
      echo " -p Publish to maven local."
      echo " -s Creates a FatJar using the ShadowJar."
      exit 0
      ;;
    d)
      DYNAMIC_NAMING=true
      ;;
    p)
      PUBLISH_LOCAL=true
      ;;
    s)
      SHADOW_BUILD=true
      ;;
    ?)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done

if $DYNAMIC_NAMING; then
  BNAME=$(git branch --show-current)
  CHASH=$(git rev-parse --short HEAD)

  FCONT=$(cat ./gradle.properties.base)
  FCONT="${FCONT/__branch_name__/${BNAME}}"
  FCONT="${FCONT/__commit_hash__/${CHASH}}"

  touch ./gradle.properties
  echo "${FCONT}" > ./gradle.properties
fi

set -x

if $SHADOW_BUILD; then
  ./gradlew clean shadowJar
else
  ./gradlew clean build -x test
fi

if $PUBLISH_LOCAL; then
  ./gradlew publishToMavenLocal
fi