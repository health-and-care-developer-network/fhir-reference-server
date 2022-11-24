#!/bin/bash -xe

# Usage:
# publishResources.sh registryhostname targethostname out_path docker_tagname renderer_flags github_oauth_key

. ./utils.sh

REGISTRY_HOST="${REGISTRY_HOST:-${1}}"
TARGET_HOST="${TARGET_HOST:-${2}}"
OUT_PATH="${OUT_PATH:-${3}}"
TAG_NAME="${TAG_NAME:-${4}}"
RENDERER_FLAGS="${RENDERER_FLAGS:-${5}}"
GH_OAUTH="${GH_OAUTH:-${6}}"

IMAGE_NAME="nhsd/fhir-profile-renderer"
CONTAINER_NAME="fhir-publisher"

SOURCE=$(qualifiedImage $IMAGE_NAME $TAG_NAME $REGISTRY_HOST)
TARGET_DOCKER_CMD=$(dockerCmd $TARGET_HOST)

# Run the publisher to generate the FHIR content
if [ ! -z $REGISTRY_HOST ]; then
	$TARGET_DOCKER_CMD pull $SOURCE
fi

# Note: the ':' prevents the overall script failing if there is nothing to delete
$TARGET_DOCKER_CMD rm $CONTAINER_NAME || :

# Create a container from the image, mounting in host directories for the location to download to
# and location to output to
$TARGET_DOCKER_CMD run \
  --name $CONTAINER_NAME \
	-v /docker-data/fhir-server-temp:/source \
	-v /docker-data/fhir-server-httpcache:/tmp/git-http-cache \
	-v /docker-data/fhir-profiles:/generated \
	-e "RENDERER_FLAGS=$RENDERER_FLAGS" \
	-e "GITHUB_OAUTH=$GH_OAUTH" \
	--entrypoint="/usr/publisher/reprocess-entrypoint.sh" \
        "$SOURCE" "$OUT_PATH"

