#!/bin/bash -xe

# Usage:
# build.sh registryhostname tagname

REGISTRY_HOST=$1
TAG_NAME=$2

if [ "$JENKINS_BUILD" = "true" ] && (( $# < 2 )); then
  echo "Expected at least two arguments (registry host and tag name) for a Jenkins build"
  exit 1
fi

# Resolve image name
IMAGE_NAME="nhsd/fhir-make-html"
if [ ! -z $TAG_NAME ]
then
  IMAGE_NAME="$IMAGE_NAME:$TAG_NAME"
fi
echo "IMAGE_NAME=$IMAGE_NAME"

# Include tlsverify if we have supplied a host
if [ -z $REGISTRY_HOST ]; then
  DOCKER_CMD="docker"
else
  DOCKER_CMD="docker --tlsverify -H $REGISTRY_HOST:2376"
fi
echo "DOCKER_CMD=$DOCKER_CMD"

# Build the publisher image
set -e # Stop on error
echo "$DOCKER_CMD build -t $IMAGE_NAME ."

if [ ! -z $REGISTRY_HOST ]
then
  REGISTRY_URL=$REGISTRY_HOST:5000
  echo "REGISTRY_URL=$REGISTRY_URL"
  
  echo "$DOCKER_CMD tag $IMAGE_NAME $REGISTRY_URL/$IMAGE_NAME"
  echo "$DOCKER_CMD push $REGISTRY_URL/$IMAGE_NAME"
  echo "$DOCKER_CMD $IMAGE_NAME"
fi