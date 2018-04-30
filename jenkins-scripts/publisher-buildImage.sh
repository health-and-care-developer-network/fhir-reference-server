#!/bin/bash -xe

# Usage:
# build.sh tagname registryhostname

# assign arguments
TAG_NAME=$1
REGISTRY_HOST=$2

# load utility scripts
SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
. $SCRIPT_DIR/sharedUtils.sh
# use utility script to set $DOCKER_CMD variable
setDockerCmd

echo "DOCKER_CMD=$DOCKER_CMD"
if [ -z $DOCKER_CMD ]; then
  echo "Exiting: docker command not set"
  exit 1
fi

# validate args
if [ "$JENKINS_BUILD" = "true" ] && (( $# < 2 )); then
  echo "Expected at least two arguments (tag name and registry host) for a Jenkins build"
  exit 1
fi

# Resolve image name
IMAGE_NAME="nhsd/fhir-make-html"
if [ ! -z $TAG_NAME ]; then
  IMAGE_NAME="$IMAGE_NAME:$TAG_NAME"
fi

# Build the publisher image
$DOCKER_CMD build -t $IMAGE_NAME .

# If a registry was supplied, push to registry and remove image locally
if [ ! -z $REGISTRY_HOST ]; then
  REGISTRY_URL=$REGISTRY_HOST:5000
  
  $DOCKER_CMD tag $IMAGE_NAME $REGISTRY_URL/$IMAGE_NAME
  $DOCKER_CMD push $REGISTRY_URL/$IMAGE_NAME
  $DOCKER_CMD rmi $IMAGE_NAME
fi