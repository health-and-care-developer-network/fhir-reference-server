#!/bin/bash -xe

# Usage:
# publishResources.sh github_url branch path url_to_replace new_url_to_insert registryhostname targethostname out_path docker_tagname

. ./utils.sh

GITHUB_URL="${GITHUB_URL:-${1}}"
BRANCH="${BRANCH:-${2}}"
IN_PATH="${IN_PATH:-${3}}"
OLD_URL="${OLD_URL:-${4}}"
NEW_URL="${NEW_URL:-${5}}"
REGISTRY_HOST="${REGISTRY_HOST:-${6}}"
TARGET_HOST="${TARGET_HOST:-${7}}"
OUT_PATH="${OUT_PATH:-${8}}"
TAG_NAME="${TAG_NAME:-${9}}"
RENDERER_FLAGS="${RENDERER_FLAGS:-${10}}"
GH_OAUTH="${GH_OAUTH:-${11}}"

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
	-e "GH_OAUTH=$GH_OAUTH" \
	"$SOURCE" "$GITHUB_URL" "$BRANCH" "$IN_PATH" "$OLD_URL" "$NEW_URL" "$OUT_PATH"

