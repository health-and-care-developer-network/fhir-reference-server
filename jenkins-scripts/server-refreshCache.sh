#!/bin/bash

# Usage:
# refreshCache.sh [targethostname] containername

. ./utils.sh

# These can be set by calling script
TARGET_HOST=${TARGET_HOST:-${1}}
CONTAINER_NAME=${CONTAINER_NAME:-${2}}

DOCKER_CMD=$(dockerCmd $TARGET_HOST)

$DOCKER_CMD exec $CONTAINER_NAME wget -O - http://localhost:8080/InvalidateCache

echo "Complete."
