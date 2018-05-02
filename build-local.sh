#!/bin/bash -xe

mvn clean install

REGISTRY_HOST=
TAG_NAME_OVERRIDE=

cd jenkins-scripts
./buildAndPublishDockerImage.sh "profile-renderer" "$TAG_NAME_OVERRIDE" "$REGISTRY_HOST" ""
./buildAndPublishDockerImage.sh "reference-server" "$TAG_NAME_OVERRIDE" "$REGISTRY_HOST" "ROOT.war"

