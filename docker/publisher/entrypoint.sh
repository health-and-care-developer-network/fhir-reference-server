#!/bin/bash -xe

# usage: entrypoint.sh github_url branch path url_to_replace new_url_to_insert out_path

GITHUB_URL=$1
BRANCH=$2
REPO_PATH=$3
OLD_URL=$4
NEW_URL=$5
OUT_PATH=$6
RENDERER_FLAGS=${RENDERER_FLAGS}

echo "ENTRYPOINT.SH"

# First, clone the repository with our source files
echo "deleting old source files"
rm -Rf /source/files
echo "cloning github repo into /source/files"
git clone $GITHUB_URL /source/files
echo "changing directory to /source/files"
cd /source/files
echo "swithcing to selected branch"
git checkout $BRANCH

# Carry out url replacement
cd /source/files/$REPO_PATH/
if [ ! -z $OLD_URL ] && [ ! -z $NEW_URL ]; then
  find . -name \*.xml -exec sed -i -e "s|$OLD_URL|$NEW_URL|g" {} \;
fi

mkdir -p /generated/$OUT_PATH

# directory containing renderer
cd /usr/publisher

# confirm that there is only one jar file
JAR_PATTERN="\*.jar"
JAR_FILE_COUNT=$(find . -maxdepth 1 -name "$JAR_PATTERN" | wc -l)
if (( $JAR_FILE_COUNT = 1 )); then
  JAR_FILE=$(*.jar)
  echo "Using Jar name: $JAR_FILE"
else
  echo "Expected 1 *.jar file but found $JAR_FILE_COUNT"
  exit 1
fi

echo "Running command: java -jar ./target/$JAR_FILE /source/files/$REPO_PATH /generated/$OUT_PATH $RENDERER_FLAGS"
ls ./target
java -jar ./target/$JAR_FILE /source/files/$REPO_PATH /generated/$OUT_PATH $RENDERER_FLAGS
