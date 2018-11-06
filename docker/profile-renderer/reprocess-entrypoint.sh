#!/bin/bash -xe

# usage: entrypoint.sh github_url branch path url_to_replace new_url_to_insert out_path

OUT_PATH=$1
RENDERER_FLAGS=${RENDERER_FLAGS}

# First, copy all the previously rendered profiles to be re-rendered
rm -Rf /source/files

resourceTypes=("CodeSystem" "ConceptMap" "ImplementationGuide" "MessageDefinition" "NamingSystem" "OperationDefinition" "SearchParameter" "StructureDefinition" "ValueSet")
for resourceType in "${resourceTypes[@]}"
do
  DIR=/generated/$OUT_PATH/STU3/$resourceType/versioned
  if [ "$(ls -A $DIR/*.xml)" ]; then
    mkdir -p /source/files/STU3/$resourceType
    cp $DIR/*.xml /source/files/STU3/$resourceType
  fi
done

resourceTypes=("ImplementationGuide" "OperationDefinition" "StructureDefinition" "ValueSet")
for resourceType in "${resourceTypes[@]}"
do  
  DIR=/generated/$OUT_PATH/DSTU2/$resourceType/versioned
  if [ "$(ls -A $DIR/*.xml)" ]; then
    mkdir -p /source/files/DSTU2/$resourceType
    cp $DIR/*.xml /source/files/DSTU2/$resourceType
  fi
done

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
echo "Running command: java -jar $JAR_FILE /source/files/ /generated/$OUT_PATH $RENDERER_FLAGS"
# no quotes around flags so that they are treated as separate arguments
java -jar "$JAR_FILE" "/source/files/" "/generated/$OUT_PATH" $RENDERER_FLAGS
