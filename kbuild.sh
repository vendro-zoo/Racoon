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

trap "exit" INT

spinner() {
  local pid=$!

  local spin='-\|/'
  local i=0

  while kill -0 $pid 2>/dev/null; do
    i=$(((i + 1) % 4))
    printf "\r%s - \e[1m\e[30m\e[43mIN PROGRESS\e[0m %s" "$1" "${spin:$i:1}"
    sleep .1
  done &
  if wait $pid; then
    printf "\033[2K\r%s - \e[1m\e[30m\e[42mCOMPLETED\e[0m\n" "$1"
  else
    printf "\033[2K\r%s - \e[1m\e[30m\e[41mFAILED\e[0m\n" "$1"
    exit 1
  fi
}

DYNAMIC_NAMING=false
PUBLISH_LOCAL=false
BUILD=false
SHADOW_BUILD=false
CLEAN=false

while getopts ":hdpbsc" opt; do
  case $opt in
    h)
      echo "Usage: kbuild.sh [-h] [-d] [-p]"
      echo " -h Show this help message and exit."
      echo " -d Use dynamic naming. Overwrites gradle.properties with a generated one starting from gradle.properties.base"
      echo " -p Publish to maven local."
      echo " -b Normal build."
      echo " -s Creates a FatJar using the ShadowJar."
      echo " -c Clean the build."
      exit 0
      ;;
    d)
      DYNAMIC_NAMING=true
      ;;
    p)
      PUBLISH_LOCAL=true
      ;;
    b)
      BUILD=true
      ;;
    s)
      SHADOW_BUILD=true
      ;;
    c)
      CLEAN=true
      ;;
    ?)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done


FCONT=$(cat ./gradle.properties.base)
if $DYNAMIC_NAMING; then
  BNAME=$(git branch --show-current)
  CHASH=$(git rev-parse --short HEAD)

  FCONT="${FCONT/__branch_name__/${BNAME}}"
  FCONT="${FCONT/__commit_hash__/${CHASH}}"

  touch ./gradle.properties
  echo "${FCONT}" > ./gradle.properties
fi
VERSION=$(grep "vers" ./gradle.properties | cut -d '=' -f 2 2> /dev/null)
if [ $? -ne 0 ]; then
  echo "Error: gradle.properties is not valid."
  exit 1
fi

if $CLEAN; then
  ./gradlew clean >& /dev/null & spinner "Cleaning"
fi

if $BUILD; then
  ./gradlew build -x test >& /dev/null & spinner "Building normally"
fi

if $SHADOW_BUILD; then
  ./gradlew shadowJar >& /dev/null & spinner "Building ShadowJar"
fi

if $PUBLISH_LOCAL; then
  ./gradlew publishToMavenLocal >& /dev/null & spinner "Publishing to maven local"
fi

echo ""
echo "Successfully built. Version built: ${VERSION}"

exit 0