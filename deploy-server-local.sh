#!/bin/bash -xe

export CONTAINER_NAME=
export CONFIG_FILE=
export FHIR_SERVER_PORT=

REGISTRY_HOST=
TAG_NAME="1.4.3"

cd jenkins-scripts
./server-deploy.sh "$REGISTRY_HOST" "" "$TAG_NAME"

# return to root directory
cd ..
