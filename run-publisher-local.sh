#!/bin/bash -xe

. ./jenkins-scripts/utils.sh

# Set the below to the FHIR artefact GIT repository URL
export GITHUB_URL="https://github.com/nhsconnect/STU3-FHIR-Assets.git"
# Set the below to the branch you want to publish from
export BRANCH="develop"
# URL domains to be treated as local
LOCAL_DOMAINS="https://fhir.nhs.uk/"
PERMITTED_MISSING_EXTENSION_PREFIXES="https://fhir.hl7.org.uk/"
FORCE_COPY="false"

export IN_PATH="."	# path within fhir artefact repository
export PREPROCESS_ARTEFACTS_OLD_URL=""
export PREPROCESS_ARTEFACTS_NEW_URL=""

# no replacement required locally
export REGISTRY_HOST=""
export TARGET_HOST=""

export OUT_PATH="NHSDigital"        # DSTU2 = NHSDigital and STU3 = NHSDigitalSTU3
export TAG_NAME="1.3.0"
export CONTAINER_NAME="fhir-server" # fhir-server = NHSD, hl7-fhir-server = HL7

export RENDERER_FLAGS=""

if [ ! -z "$LOCAL_DOMAINS" ]; then
  export RENDERER_FLAGS="$RENDERER_FLAGS -l $LOCAL_DOMAINS"
fi
if [ ! -z "$PERMITTED_MISSING_EXTENSION_PREFIXES" ]; then
  export RENDERER_FLAGS="$RENDERER_FLAGS -p $PERMITTED_MISSING_EXTENSION_PREFIXES"
fi
if [ "$FORCE_COPY" -eq "true" ]; then
  export RENDERER_FLAGS="$RENDERER_FLAGS -f"
fi

# Publish all the profiles
cd jenkins-scripts
./publisher-run.sh "$GITHUB_URL" "$BRANCH" "$IN_PATH" "$PREPROCESS_ARTEFACTS_OLD_URL" "$PREPROCESS_ARTEFACTS_NEW_URL" "$REGISTRY_HOST" "$TARGET_HOST" "$OUT_PATH" "$TAG_NAME" "$RENDERER_FLAGS"
