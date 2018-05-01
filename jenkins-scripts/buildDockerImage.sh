#!/bin/bash -xe

# Usage:
# buildDockerImage.sh [reference-server | profile-renderer] image_name mvn_version

. ./utils.sh

if (( $# < 3 )); then
  echo "Exiting: no project name supplied"
  exit 1
fi

PROJECT="$1"
IMAGE_AND_TAG="$2"
REFERENCE_SERVER_MVN_VERSION="$3"
DOCKER_CMD="$4"
DEPLOYABLE_EXTENSION="$5"
DEPLOYABLE_TARGET_NAME="$6"

# prepare directory for server image build
DEPLOY_DIR="../$PROJECT-deploy"
rm -rf $DEPLOY_DIR
mkdir $DEPLOY_DIR
cp ../docker/$PROJECT/* $DEPLOY_DIR/
cp ../$PROJECT/target/$PROJECT-$REFERENCE_SERVER_MVN_VERSION.$DEPLOYABLE_EXTENSION $DEPLOY_DIR/$DEPLOYABLE_TARGET_NAME

# Build the publisher image
cd $DEPLOY_DIR
$DOCKER_CMD build -t $IMAGE_AND_TAG .
