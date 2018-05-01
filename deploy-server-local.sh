#!/bin/bash -xe

REGISTRY_HOST=
TARGET_HOST=
IMAGE_NAME="nhsd/fhir-reference-server"
TAG_NAME="1.3.0"

cd jenkins-scripts
./server-deploy.sh "$REGISTRY_HOST" "$TARGET_HOST" "$IMAGE_NAME" "$TAG_NAME"
