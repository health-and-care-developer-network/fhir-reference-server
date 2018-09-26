#!/bin/bash -xe

# Usage:
# build.sh registryhostname targethostname tagname

# load utility functions
. ./utils.sh

# location where docker image has been published
REGISTRY_HOST="$1"
# host which will run the container
TARGET_HOST="$2"
TAG_NAME="$3"

IMAGE_NAME="nhsd/fhir-reference-server"

# These variables may optionally be set by a calling script
FHIR_SERVER_PORT="${FHIR_SERVER_PORT:-8080}"
CONTAINER_NAME="${CONTAINER_NAME:-fhir-server}"
CONFIG_FILE="${CONFIG_FILE}"

TARGET_DOCKER_CMD="$(dockerCmd $TARGET_HOST)"
SOURCE="$(qualifiedImage $IMAGE_NAME $TAG_NAME $REGISTRY_HOST)"

MEMORYFLAG="3g"
CPUFLAG="768"

if [ ! -z $REGISTRY_HOST ]; then
  $TARGET_DOCKER_CMD pull $SOURCE
fi

$TARGET_DOCKER_CMD stop $CONTAINER_NAME || :
$TARGET_DOCKER_CMD rm $CONTAINER_NAME || :
$TARGET_DOCKER_CMD run \
  -p $FHIR_SERVER_PORT:8080 \
  --name $CONTAINER_NAME \
  --restart=on-failure:5 \
  -m $MEMORYFLAG \
  -c $CPUFLAG \
  -e "CONFIG_FILE=$CONFIG_FILE" \
  -v /docker-data/fhir-profiles:/opt/fhir \
  -v /docker-data/fhir-server-temp:/tmp/jetty \
  -d $SOURCE

