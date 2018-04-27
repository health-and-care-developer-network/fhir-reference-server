#!/bin/bash

mvn clean install

export REFERENCE_SERVER_MVN_VERSION="$(mvn help:evaluate -Dexpression=project.version | grep -v "^\[")"
export REFERENCE_SERVER_TRIMMED_VERSION=$(bash -c "echo \${REFERENCE_SERVER_MVN_VERSION%-SNAPSHOT}")

# prepare directory for publisher image build
PUBLISHER_DEPLOY_DIR="publisher-deploy"
mkdir $PUBLISHER_DEPLOY_DIR
cp docker/publisher/* $PUBLISHER_DEPLOY_DIR/
cp profile-renderer/target/profile-renderer-$REFERENCE_SERVER_MVN_VERSION.jar $PUBLISHER_DEPLOY_DIR/
cd $PUBLISHER_DEPLOY_DIR

../jenkins/publisher-buildImage.sh