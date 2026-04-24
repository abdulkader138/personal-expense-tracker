#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# Apache Maven Wrapper Script — version 3.2.0

set -e

# Determine script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Read properties file
PROPS="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"
DISTRIBUTION_URL=$(grep "^distributionUrl" "$PROPS" | sed 's/distributionUrl=//')

# Determine local Maven home path
DIST_FILENAME=$(basename "$DISTRIBUTION_URL")
DIST_NAME="${DIST_FILENAME%.zip}"
MVN_CACHE="$HOME/.m2/wrapper/dists/$DIST_NAME"
MVN_BIN="$MVN_CACHE/bin/mvn"

if [ ! -f "$MVN_BIN" ]; then
  echo "Downloading Maven: $DISTRIBUTION_URL"
  mkdir -p "$MVN_CACHE"
  TMP_ZIP="$MVN_CACHE/download.zip"
  if command -v curl > /dev/null 2>&1; then
    curl --fail --silent --location -o "$TMP_ZIP" "$DISTRIBUTION_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget --quiet -O "$TMP_ZIP" "$DISTRIBUTION_URL"
  else
    echo "Error: neither curl nor wget is available." >&2
    exit 1
  fi
  unzip -q "$TMP_ZIP" -d "$HOME/.m2/wrapper/dists/"
  rm -f "$TMP_ZIP"
  # rename extracted folder to expected name
  EXTRACTED=$(find "$HOME/.m2/wrapper/dists/" -maxdepth 1 -name "apache-maven-*" -type d | head -1)
  if [ "$EXTRACTED" != "$MVN_CACHE" ]; then
    mv "$EXTRACTED" "$MVN_CACHE"
  fi
fi

exec "$MVN_BIN" "$@"
