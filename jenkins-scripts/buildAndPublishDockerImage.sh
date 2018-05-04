#!/bin/bash -xe
# Usage:
# prepareAndBuildDockerImage.sh project_name [tag_name] [registry_host] [deployable_target_name]

. ./utils.sh

if (( $# < 1 )); then
  echo "Exiting: no project name supplied"
  exit 1
fi

PROJECT="$1"
TAG_NAME="$2"
REGISTRY_HOST="$3"
DEPLOYABLE_TARGET_NAME="$4"

# preserve docker tag name if it has been supplied, otherwise default to trimmed mvn version
if [ -z "$TAG_NAME" ]; then
  TAG_NAME=$(trimmedMavenVersion)
fi

IMAGE_NAME="nhsd/fhir-$PROJECT"

./buildDockerImage.sh "$PROJECT" "$IMAGE_NAME" "$TAG_NAME" "$REGISTRY_HOST" "$DEPLOYABLE_TARGET_NAME"

if [ ! -z $REGISTRY_HOST ]; then
  ./publishDockerImage.sh "$IMAGE_NAME" "$TAG_NAME" "$REGISTRY_HOST"
fi

