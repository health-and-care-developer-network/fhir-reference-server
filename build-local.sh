#!/bin/bash -xe

mvn clean install

. ./jenkins-scripts/utils.sh

# e.g. 1.2.3-SNAPSHOT
REFERENCE_SERVER_MVN_VERSION=$(mavenVersion)

cd jenkins-scripts
./buildAndPublishDockerImage.sh "profile-renderer" "$REFERENCE_SERVER_MVN_VERSION" "jar" 
./buildAndPublishDockerImage.sh "reference-server" "$REFERENCE_SERVER_MVN_VERSION" "war" "ROOT.war"

