#!/bin/bash

# Usage:
# build.sh registryhostname targethostname

REGISTRY_HOST=$1
TARGET_HOST=$2

IMAGE_NAME=fhir-server
CONTAINER_NAME=fhir-server

REGISTRY_URL=$REGISTRY_HOST:5000

POSTGRES_IMAGE="postgres:9.5"

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
fi

echo "Pull and run FHIR server"
docker $TARGET_PREFIX pull $REGISTRY_URL/$IMAGE_NAME
docker $TARGET_PREFIX stop $CONTAINER_NAME
docker $TARGET_PREFIX rm $CONTAINER_NAME
docker $TARGET_PREFIX run -p 8100:8080 --name $CONTAINER_NAME \
	--restart=on-failure:5 \
	-v /docker-data/fhir-profiles:/opt/fhir
	-d $REGISTRY_URL/$IMAGE_NAME


