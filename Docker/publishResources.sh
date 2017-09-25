#!/bin/bash

# Usage:
# publishResources.sh github_url branch path url_to_replace new_url_to_insert registryhostname targethostname out_path copy_only docker_tagname

# Note: If copy_only is passed as "true" then the renderer will not be called, the specified files will just be copied directly to
# the output directory. This would typically be used to copy examples across.

GITHUB_URL=$1
BRANCH=$2
IN_PATH=$3
OLD_URL=$4
NEW_URL=$5
REGISTRY_HOST=$6
TARGET_HOST=$7
OUT_PATH=$8
COPY_ONLY=$9
TAG_NAME=$10
 
IMAGE_NAME="nhsd/fhir-make-html"

if [ ! -z $TAG_NAME ]
then
  IMAGE_NAME="$IMAGE_NAME:$TAG_NAME"
fi

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
  SOURCE=$IMAGE_NAME
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
  SOURCE=$REGISTRY_HOST:5000/$IMAGE_NAME
fi

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

# Run the publisher to generate the FHIR content
if [ ! -z $REGISTRY_HOST ]
then
	docker $TARGET_PREFIX pull $SOURCE
fi
docker $TARGET_PREFIX rm makehtml
docker $TARGET_PREFIX run --name makehtml \
	-v /docker-data/fhir-server-temp:/source \
	-v /docker-data/fhir-profiles:/generated \
	$SOURCE $GITHUB_URL $BRANCH $IN_PATH $OLD_URL $NEW_URL $OUT_PATH $COPY_ONLY

