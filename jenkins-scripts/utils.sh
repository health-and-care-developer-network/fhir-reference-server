#!/bin/bash

# print docker command according to whether a reistry_host is supplied
# dockerCmd [registry_host]
dockerCmd()
{
  local registry_host=${1}

  # Include tlsverify if we have supplied a host
  if [ -z $registry_host ]; then
    echo "docker"
  else
    echo "docker --tlsverify -H $registry_host:2376"
  fi
}

# print qualified docker image name
# qualifiedImage image tag registry
qualifiedImage()
{
  local image_name=${1:?"Specify an image name"}
  local tag=${2}
  local registry_host=${3}

  if [ ! -z "$tag" ]; then
    image_name=$image_name:$tag
  fi

  if [ ! -z "$registry_host" ]; then
    image_name=$registry_host:5000/$image_name
  fi

  echo $image_name
}

trimmedMavenVersion()
{
  local version=$(mavenVersion)
  # remove suffix of -SNAPSHOT if present
  echo ${version%-SNAPSHOT}
}
mavenVersion()
{
  # to root directory
  cd ..
  mavenEvaluate "project.version"
  cd ./jenkins-scripts
}
mavenFileExtension()
{
  local project=${1:?"Specify a project name"}
  cd ../$project
  mavenEvaluate "project.packaging"
  cd ../jenkins-scripts
}
mavenEvaluate()
{
  local pom_path=${1:?"Specify a '.'-delimited path within the pom.xml"}
  # if we don't have all dependencies in ~/.m2, we get messages about any packages being downloaded. Get that out the way.
  mvn help:evaluate -Dexpression=$pom_path > /dev/null
  # this time allow the evaluated data to be emitted
  mvn help:evaluate -Dexpression=$pom_path | grep -v "^\["
}
