#!/bin/bash

REGISTRY_HOST=$1
shift

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
  REGISTRY_URL=""
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
  REGISTRY_URL="$REGISTRY_HOST:5000/$TAGNAME"
fi

BUILDARGS=""
if [ ! -z $http_proxy ]
then
  BUILDARGS="$BUILDARGS --build-arg http_proxy=${http_proxy}"
fi
if [ ! -z $https_proxy ]
then
  BUILDARGS="$BUILDARGS --build-arg https_proxy=${https_proxy}"
fi

function build_image() {
  # Build the image
  set -e # Stop on error
  docker $REGISTRY_PREFIX build -t $TAGNAME --no-cache $BUILDARGS "$BUILDFOLDER"

  # Push to the registry if there is one
  if [ ! -z $REGISTRY_HOST ]
  then
    docker $REGISTRY_PREFIX tag $TAGNAME $REGISTRY_URL
    docker $REGISTRY_PREFIX push $REGISTRY_URL
    docker $REGISTRY_PREFIX rmi $TAGNAME
  fi
}

function pull_image() {
  # Pull the image 
  set -e # Stop on error
  docker $REGISTRY_PREFIX pull $PULLNAME

  # Push to the registry if there is one
  if [ ! -z $REGISTRY_HOST ]
  then
    docker $REGISTRY_PREFIX tag $PULLNAME $REGISTRY_URL
    docker $REGISTRY_PREFIX push $REGISTRY_URL
    docker $REGISTRY_PREFIX rmi $PULLNAME
  else
    # And just tag if not
    docker $REGISTRY_PREFIX tag $PULLNAME $TAGNAME
  fi
}

function copy_common() {
  local BUILD_DIR=${1:-$BUILDFOLDER}

  if [ -d common ]
  then
    cp -a common/* $BUILD_DIR
    cp -a common/.??* $BUILD_DIR
  fi
}
