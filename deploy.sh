#!/bin/bash

# Usage:
# build.sh registryhostname targethostname

REGISTRY_HOST=$1
TARGET_HOST=$2
PORT=${PORT:-8080}

IMAGE_NAME=fhir-server
CONTAINER_NAME=fhir-server

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
  SOURCE=$IMAGE_NAME
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
  SOURCE=$REGISTRY_HOST:5000/$IMAGE_NAME
fi

MEMORYFLAG=1g
CPUFLAG=768

echo "Pull and run FHIR server"
if [ ! -z $REGISTRY_HOST ]
then
  docker $TARGET_PREFIX pull $SOURCE
fi

docker $TARGET_PREFIX stop $CONTAINER_NAME
docker $TARGET_PREFIX rm $CONTAINER_NAME
docker $TARGET_PREFIX run -p $PORT:8080 --name $CONTAINER_NAME \
	--restart=on-failure:5 \
        -m $MEMORYFLAG \
	-c $CPUFLAG \
	-v /docker-data/fhir-profiles:/opt/fhir \
	-v /docker-data/fhir-server-temp:/tmp/jetty \
	-d $SOURCE


