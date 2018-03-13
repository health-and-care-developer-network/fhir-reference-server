#!/bin/bash

# usage: entrypoint.sh github_url branch path url_to_replace new_url_to_insert out_path copy_only

# Note: If copy_only is passed as "true" then the renderer will not be called, the specified files will just be copied directly to
# the output directory. This would typically be used to copy examples across.

GITHUB_URL=$1
BRANCH=$2
REPO_PATH=$3
OLD_URL=$4
NEW_URL=$5
OUT_PATH=$6
COPY_ONLY=$7
ARGUMENTS=${ARGUMENTS}

# First, clone the repository with our source files
rm -Rf /source/files
git clone $GITHUB_URL /source/files
cd /source/files
git checkout $BRANCH

cd /source/files/$REPO_PATH/
find . -name \*.xml -exec sed -i -e "s|$OLD_URL|$NEW_URL|g" {} \;
mkdir -p /generated/$OUT_PATH

if [ $COPY_ONLY == "true" ]
then
  echo "Copying resources directly (no rendering)"
  cp /source/files/$REPO_PATH/* /generated/$OUT_PATH
else
  cd /usr/makehtml
  echo "Running command: java $ARGUMENTS -jar ./target/MakeHTML-1.1.0-SNAPSHOT.jar /source/files/$REPO_PATH /generated/$OUT_PATH"
  ls ./target
  java $ARGUMENTS -jar ./target/MakeHTML-1.1.0-SNAPSHOT.jar /source/files/$REPO_PATH /generated/$OUT_PATH
fi

