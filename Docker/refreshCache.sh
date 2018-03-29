#!/bin/bash

# Usage:
# refreshCache.sh targethostname containername

TARGET_HOST=${TARGET_HOST:-${1}}
CONTAINER_NAME=${CONTAINER_NAME:-${2}}

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

docker $TARGET_PREFIX exec $CONTAINER_NAME wget -O - http://localhost:8080/InvalidateCache

echo "Complete."
