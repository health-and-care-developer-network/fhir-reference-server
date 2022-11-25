#!/bin/bash -xe

# usage: entrypoint.sh github_url branch path url_to_replace new_url_to_insert out_path

GITHUB_URL=$1
BRANCH=$2
REPO_PATH=$3
OLD_URL=$4
NEW_URL=$5
OUT_PATH=$6
RENDERER_FLAGS=${RENDERER_FLAGS}

# First, clone the repository with our source files
rm -Rf /source/files
git clone $GITHUB_URL /source/files

chown 100:1000 /source/files
chmod g=rwx,o= /source/files

cd /source/files
git checkout $BRANCH

# Carry out url replacement
cd /source/files/$REPO_PATH/
if [ ! -z "$OLD_URL" ] && [ ! -z "$NEW_URL" ]; then
  echo "Doing URL replacement: OLD_URL=$OLD_URL NEW_URL=$NEW_URL"
  find . -name "*.xml" -type f -exec sed -i -e "s|$OLD_URL|$NEW_URL|g" {} \;
fi

mkdir -p /generated/$OUT_PATH

# directory containing renderer
cd /usr/publisher

# confirm that there is only one jar file
JAR_FILE_COUNT=$(find . -maxdepth 1 -name "*.jar" | wc -l)
if [ "$JAR_FILE_COUNT" -eq "1" ]; then
  JAR_FILE=$(find . -maxdepth 1 -name "*.jar")
  echo "Using Jar name: $JAR_FILE"
else
  echo "Expected 1 *.jar file but found $JAR_FILE_COUNT"
  exit 1
fi

ls /source/files
echo "Running command: java -jar $JAR_FILE /source/files/$REPO_PATH /generated/$OUT_PATH $RENDERER_FLAGS"
# no quotes around flags so that they are treated as separate arguments
java -jar "$JAR_FILE" "/source/files/$REPO_PATH" "/generated/$OUT_PATH" $RENDERER_FLAGS
