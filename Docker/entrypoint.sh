#!/bin/bash

# usage: entrypoint.sh github_url branch path url_to_replace new_url_to_insert out_path

GITHUB_URL=$1
BRANCH=$2
REPO_PATH=$3
OLD_URL=$4
NEW_URL=$5
OUT_PATH=$6

# First, clone the repository with our source files
rm -Rf /source/files
git clone $GITHUB_URL /source/files
cd /source/files
git checkout $BRANCH

# Workaround
echo "Copying extensions..."
cp -R ./Extensions/* ./StructureDefinitions/

sed -i -- "s|$OLD_URL|$NEW_URL|g" /source/files/$REPO_PATH/*

mkdir -p /generated/$OUT_PATH

cd /usr/makehtml
java -cp ./target/MakeHTML-1.0-SNAPSHOT.jar uk.nhs.fhir.makehtml.NewMain /source/files/$REPO_PATH /generated/$OUT_PATH

