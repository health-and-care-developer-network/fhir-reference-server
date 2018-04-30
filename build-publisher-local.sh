#!/bin/bash -xe

mvn clean install

mvn install >/dev/null
export REFERENCE_SERVER_MVN_VERSION="$(mvn help:evaluate -Dexpression=project.version | grep -v "^\[")"
export REFERENCE_SERVER_TRIMMED_VERSION=$(bash -c "echo \${REFERENCE_SERVER_MVN_VERSION%-SNAPSHOT}")

# prepare directory for publisher image build
PUBLISHER_DEPLOY_DIR="./publisher-deploy"
rm -rf $PUBLISHER_DEPLOY_DIR
mkdir $PUBLISHER_DEPLOY_DIR
cp docker/publisher/* $PUBLISHER_DEPLOY_DIR/
cp profile-renderer/target/profile-renderer-$REFERENCE_SERVER_MVN_VERSION.jar $PUBLISHER_DEPLOY_DIR/

# no arguments, as we don't need to publish to a docker repo
cd $PUBLISHER_DEPLOY_DIR
../jenkins-scripts/publisher-buildImage.sh "$REFERENCE_SERVER_TRIMMED_VERSION"