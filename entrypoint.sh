#!/bin/bash

# usage: entrypoint.sh github_url branch path

GITHUB_URL=$1
BRANCH=$2
REPO_PATH=$3

# First, clone the repository with our source files
rm -Rf /source
rm -Rf /generated
git clone $GITHUB_URL /source
git checkout $BRANCH

cd /usr/makehtml
java -cp ./target/MakeHTML-1.0-SNAPSHOT.jar uk.nhs.fhir.makehtml.NewMain /source/$REPO_PATH /generated

