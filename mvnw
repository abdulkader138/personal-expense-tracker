#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# -----------------
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then

  if [ -f /usr/local/etc/mavenrc ]; then
    . /usr/local/etc/mavenrc
  fi

  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi

  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi

fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  Darwin*) darwin=true ;;
esac

## Path to script
if [ -n "$BASH_VERSION" ]; then
  # shellcheck disable=SC3011
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
else
  SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

# For Mingw, ensure paths are in UNIX format before anything is touched
if $mingw; then
  [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ] &&
    JAVA_HOME="$(cd "$JAVA_HOME" || (echo "cannot cd into $JAVA_HOME."; exit 1); pwd)"
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac)"
  if [ -n "$javaExecutable" ] && ! [ "$(expr "$javaExecutable" : '\([^ ]*\)')" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=$(which readlink)
    if [ ! "$(expr "$readLink" : '\([^ ]*\)')" = "no" ]; then
      if $darwin; then
        javaHome="$(dirname "$javaExecutable")"
        javaExecutable="$(readlink "$javaHome"/../javac)"
        javaHome="$(dirname "$javaExecutable")"
        javaHome=$(expr "$javaHome" : '\(.*\)/bin')
        JAVA_HOME="$javaHome"
        export JAVA_HOME
      else
        javaHome="$(dirname "$javaExecutable")"
        javaExecutable="$(readlink -f "$javaExecutable")"
        javaHome="$(dirname "$javaExecutable")"
        javaHome=$(expr "$javaHome" : '\(.*\)/bin')
        JAVA_HOME="$javaHome"
        export JAVA_HOME
      fi
    fi
  fi
fi

if [ -z "$JAVA_HOME" ] && [ -r /etc/gentoo-release ]; then
  JAVA_HOME=$(java-config --jre-home)
fi

if [ -z "$JAVACMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="$(
      \unset -f command 2>/dev/null
      \command -v java
    )"
  fi
fi

if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$JAVA_HOME" ]; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher

# traverses directory structure from process work directory to filesystem root
# first directory with .mvn subdirectory is considered project base directory
find_maven_basedir() {
  if [ -z "$1" ]; then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while true; do
    if [ -d "$wdir"/.mvn ]; then
      basedir=$wdir
      break
    fi
    # workaround for JBEAP-8937 (on Solaris 10/Sparc)
    if [ -d "${wdir}" ]; then
      wdir=$(
        cd "$wdir/.." || exit 1
        pwd
      )
    fi
    # end of workaround
    if [ "$wdir" = '/' ]; then
      break
    fi
  done
  echo "${basedir}"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]; then
    # Remove \r in case we run on Windows within Git Bash
    # shellcheck disable=SC2001
    sed 's/\r//' <"$1"
  fi
}

log() {
  if [ "$MVNW_VERBOSE" = true ]; then
    printf '%s\n' "$1"
  fi
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
if [ -z "$BASE_DIR" ]; then
  exit 1
fi

MVNW_REPOURL="${MVNW_REPOURL:-https://repo.maven.apache.org/maven2}"
export MVNW_REPOURL

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
# This allows using the Maven Wrapper in environments where the maven-wrapper.jar is not
# available (e.g. GitHub Codespaces)
#
# By default, the download is performed from repo.maven.apache.org, but it can be changed
# by setting MVNW_REPOURL env variable to the desired URL
##########################################################################################
if [ -r "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" ]; then
  log "Found .mvn/wrapper/maven-wrapper.jar"
else
  log "Couldn't find .mvn/wrapper/maven-wrapper.jar, downloading it ..."
  if [ -n "$MVNW_REPOURL" ]; then
    wrapperUrl="$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  else
    wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  fi
  while IFS="=" read -r key value; do
    # Remove '\r' character that is found on Windows machines
    # shellcheck disable=SC2001
    value=$(echo "$value" | sed 's/\r//')
    case "$key" in wrapperUrl)
      wrapperUrl="$value"
      ;;
    esac
  done <"$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
  log "Downloading from: $wrapperUrl"

  wrapperJarPath="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
  if $cygwin; then
    wrapperJarPath=$(cygpath --path --windows "$wrapperJarPath")
  fi

  if command -v wget > /dev/null; then
    log "Found wget ... using wget"
    [ "$MVNW_VERBOSE" = true ] && QUIET="" || QUIET="--quiet"
    if [ -z "$MVNW_USERNAME" ] || [ -z "$MVNW_PASSWORD" ]; then
      wget $QUIET "$wrapperUrl" -O "$wrapperJarPath" || rm -f "$wrapperJarPath"
    else
      wget $QUIET --http-user="$MVNW_USERNAME" --http-password="$MVNW_PASSWORD" "$wrapperUrl" -O "$wrapperJarPath" || rm -f "$wrapperJarPath"
    fi
  elif command -v curl > /dev/null; then
    log "Found curl ... using curl"
    [ "$MVNW_VERBOSE" = true ] && QUIET="" || QUIET="--silent"
    if [ -z "$MVNW_USERNAME" ] || [ -z "$MVNW_PASSWORD" ]; then
      curl $QUIET -o "$wrapperJarPath" "$wrapperUrl" -f || rm -f "$wrapperJarPath"
    else
      curl $QUIET --user "$MVNW_USERNAME:$MVNW_PASSWORD" -o "$wrapperJarPath" "$wrapperUrl" -f || rm -f "$wrapperJarPath"
    fi
  else
    log "Falling back to using Java to download"
    javaSource="$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.java"
    javaClass="$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader"
    # For Cygwin, switch paths to Windows format before running javac
    if $cygwin; then
      javaSource=$(cygpath --path --windows "$javaSource")
      javaClass=$(cygpath --path --windows "$javaClass")
    fi
    if [ -f "$javaSource" ]; then
      if [ ! -f "$javaClass.class" ]; then
        log " - Compiling MavenWrapperDownloader.java ..."
        ("$JAVA_HOME/bin/javac" "$javaSource")
      fi
      if [ -f "$javaClass.class" ]; then
        log " - Running MavenWrapperDownloader.java ..."
        ("$JAVA_HOME/bin/java" -cp .mvn/wrapper MavenWrapperDownloader "$wrapperUrl" "$wrapperJarPath") || true
      fi
    fi
  fi
fi
##########################################################################################
# End of extension
##########################################################################################

export MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
log "$MAVEN_PROJECTBASEDIR"
MAVEN_OPTS="$(concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config") $MAVEN_OPTS"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=$(cygpath --path --windows "$JAVA_HOME")
  [ -n "$HOME" ] &&
    HOME=$(cygpath --path --windows "$HOME")
  [ -n "$MAVEN_PROJECTBASEDIR" ] &&
    MAVEN_PROJECTBASEDIR=$(cygpath --path --windows "$MAVEN_PROJECTBASEDIR")
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=$(cygpath --path --windows "$CLASSPATH")
fi

# Provide a "temporary" Gradle-like way to disable the Maven Wrapper (0.5+ only)
# just set the env var MVNW_DISABLE_DOWNLOAD_FROM_REMOTE=true and the jar won't be
# downloaded.
# You _must_ have a local copy of the Maven Wrapper JAR in that case.

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

LAUNCH_EXP=exec
exec "$JAVACMD" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${WRAPPER_LAUNCHER} $MAVEN_CONFIG "$@"
