#!/bin/bash -xe

. ./jenkins-scripts/utils.sh

# FHIR artefact GIT repository URL
GITHUB_URL="https://github.com/nhsconnect/STU3-FHIR-Assets.git"
# Branch to publish from
BRANCH="develop"
# Directory within repository containing all artefacts (will be searched recursively)
IN_PATH="."

# URL domains to be treated as local
LOCAL_DOMAINS="https://fhir.nhs.uk/"
# Extensions beginning with any of these prefixes will be displayed as 'Simple' if they are not locally available
PERMITTED_MISSING_EXTENSION_PREFIXES="https://fhir.hl7.org.uk/"
# Allow the items that were successfully rendered (i.e. no error events) to be copied, even if others were not successful
FORCE_COPY="false"

# no replacement required locally
PREPROCESS_ARTEFACTS_OLD_URL=""
PREPROCESS_ARTEFACTS_NEW_URL=""

# No external hosts to pull from or run on
REGISTRY_HOST=""
TARGET_HOST=""

# Directory to put imported artefacts in, inside mounted volume
OUT_PATH="NHSDigital"

# Tag to pull
TAG_NAME="1.3.0"

# Container to create
CONTAINER_NAME="fhir-renderer"

# Build up flags to pass to the renderer application
RENDERER_FLAGS=""
if [ ! -z "$LOCAL_DOMAINS" ]; then
  RENDERER_FLAGS="$RENDERER_FLAGS -l $LOCAL_DOMAINS"
fi
if [ ! -z "$PERMITTED_MISSING_EXTENSION_PREFIXES" ]; then
  RENDERER_FLAGS="$RENDERER_FLAGS -p $PERMITTED_MISSING_EXTENSION_PREFIXES"
fi
if [ "$FORCE_COPY" -eq "true" ]; then
  RENDERER_FLAGS="$RENDERER_FLAGS -f"
fi

# Publish all the profiles
cd jenkins-scripts
./publisher-run.sh "$GITHUB_URL" "$BRANCH" "$IN_PATH" "$PREPROCESS_ARTEFACTS_OLD_URL" "$PREPROCESS_ARTEFACTS_NEW_URL" "$REGISTRY_HOST" "$TARGET_HOST" "$OUT_PATH" "$TAG_NAME" "$RENDERER_FLAGS"

# return to root directory
cd ..
