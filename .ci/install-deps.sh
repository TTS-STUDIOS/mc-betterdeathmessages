#!/usr/bin/env bash
# Installs the internal TTS artifacts that are not on any public Maven repo.
set -euo pipefail
LOCALREPO="${MVN_LOCAL_REPO:+-Dmaven.repo.local=$MVN_LOCAL_REPO}"
cd "$(dirname "$0")"
mvn -B -q org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file $LOCALREPO -Dfile=tts-sdk-0.16.0.jar -DgroupId=com.ttsstudio -DartifactId=tts-sdk -Dversion=0.16.0 -Dpackaging=jar -DpomFile=tts-sdk-0.16.0.pom
