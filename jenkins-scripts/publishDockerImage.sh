#!/bin/bash -xe

# Usage:
# build.sh imagename [registryhostname]

. ./utils.sh

# assign arguments
IMAGE_NAME="$1"
TAG_NAME="$2"
REGISTRY_HOST="$3"

DOCKER_CMD=$(dockerCmd "$REGISTRY_HOST")
IMAGE_AND_TAG=$(qualifiedImage "$IMAGE_NAME" "$TAG_NAME")
QUALIFIED_IMAGE=$(qualifiedImage "$IMAGE_NAME" "$TAG_NAME" "$REGISTRY_HOST")

# push to registry
$DOCKER_CMD tag "$IMAGE_AND_TAG" "$QUALIFIED_IMAGE"
$DOCKER_CMD push "$QUALIFIED_IMAGE"

# remove local image
$DOCKER_CMD rmi "$IMAGE_AND_TAG"
