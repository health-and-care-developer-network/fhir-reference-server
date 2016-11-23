#!/bin/bash

# Usage:
# publishResources.sh github_url branch path url_to_replace new_url_to_insert registryhostname targethostname

GITHUB_URL=$1
BRANCH=$2
IN_PATH=$3
OLD_URL=$4
NEW_URL=$5
REGISTRY_HOST=$6
TARGET_HOST=$7

IMAGE_NAME="nhsd/fhir-make-html"

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
  REGISTRY_URL=""
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
  REGISTRY_URL=$REGISTRY_HOST:5000/
fi

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

# Run the publisher to generate the FHIR content
if [ -z $REGISTRY_HOST ]
then
	docker $TARGET_PREFIX pull $REGISTRY_URL$IMAGE_NAME
fi
docker $TARGET_PREFIX rm makehtml
docker $TARGET_PREFIX run --name makehtml \
	-v /docker-data/fhir-server-temp:/source \
	-v /docker-data/fhir-profiles:/generated \
	$REGISTRY_URL$IMAGE_NAME $GITHUB_URL $BRANCH $IN_PATH $OLD_URL $NEW_URL

