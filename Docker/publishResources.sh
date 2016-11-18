#!/bin/bash

# Usage:
# publishResources.sh registryhostname targethostname github_url branch path

REGISTRY_HOST=$1
TARGET_HOST=$2
GITHUB_URL=$3
BRANCH=$4
IN_PATH=$5

IMAGE_NAME=fhir-make-html

REGISTRY_URL=$REGISTRY_HOST:5000

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
fi

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

# Run the publisher to generate the FHIR content
docker $TARGET_PREFIX pull $REGISTRY_URL/$IMAGE_NAME
docker $TARGET_PREFIX rm makehtml
docker $TARGET_PREFIX run --name makehtml \
	-v /docker-data/fhir-profiles/temp \
	-v /docker-data/fhir-profiles/profiles:/generated \
	$REGISTRY_URL/$IMAGE_NAME $GITHUB_URL $BRANCH $IN_PATH
