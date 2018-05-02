#!/bin/bash -xe

# Usage:
# buildDockerImage.sh {reference-server | profile-renderer} image_name tag_name [reference_host] [deployable_target]

. ./utils.sh

if (( $# < 2 )); then
  exit 1
fi

PROJECT="$1"
IMAGE_NAME="$2"
TAG_NAME="$3"
REGISTRY_HOST="$4"
DEPLOYABLE_TARGET_NAME="$5"

# Version from the project root pom
MVN_VERSION=$(mavenVersion)
# File extension from the project pom
cd $PROJECT
DEPLOYABLE_EXTENSION=$(mavenFileExtension)
cd ..

# collect required artefacts for server image build
DEPLOY_DIR="../$PROJECT-deploy"
rm -rf $DEPLOY_DIR
mkdir $DEPLOY_DIR
cp ../docker/$PROJECT/* $DEPLOY_DIR/
cp ../$PROJECT/target/$PROJECT-$MVN_VERSION.$DEPLOYABLE_EXTENSION $DEPLOY_DIR/$DEPLOYABLE_TARGET_NAME

# Build the publisher image
DOCKER_CMD=$(dockerCmd $REGISTRY_HOST)
IMAGE_AND_TAG=$(qualifiedImage $IMAGE_NAME $TAG_NAME)

cd $DEPLOY_DIR
$DOCKER_CMD build -t $IMAGE_AND_TAG .

# Return working directory to script directory
cd ../jenkins-scripts
