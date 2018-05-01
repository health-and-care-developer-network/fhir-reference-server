#!/bin/bash -xe
# Usage:
# prepareAndBuildDockerImage.sh project_name tag_name mvn_version [registry_host]

. ./utils.sh

if (( $# < 1 )); then
  echo "Exiting: no project name supplied"
  exit 1
fi

PROJECT="$1"
REFERENCE_SERVER_MVN_VERSION="$2"
DEPLOYABLE_EXTENSION="$3"
DEPLOYABLE_TARGET_NAME="$4"
REGISTRY_HOST="$5"

# e.g. 1.2.3
IMAGE_NAME="nhsd/fhir-$PROJECT"
REFERENCE_SERVER_TRIMMED_VERSION=${REFERENCE_SERVER_MVN_VERSION%-SNAPSHOT}
TAG_NAME=${TAG_NAME:-${REFERENCE_SERVER_TRIMMED_VERSION}}

# Resolve image name
IMAGE_AND_TAG=$(qualifiedImage $IMAGE_NAME $TAG_NAME)
DOCKER_CMD=$(dockerCmd $REGISTRY_HOST)

./buildDockerImage.sh "$PROJECT" "$IMAGE_AND_TAG" "$REFERENCE_SERVER_MVN_VERSION" "$DOCKER_CMD" "$DEPLOYABLE_EXTENSION" "$DEPLOYABLE_TARGET_NAME"

if [ ! -z $REGISTRY_HOST ]; then
  ./publishDockerImage.sh "$IMAGE_AND_TAG" "$REGISTRY_HOST" "$DOCKER_CMD"
fi
