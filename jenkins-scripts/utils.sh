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

# Extract version from maven. Fails first time if dependencies need downloading
mavenVersion()
{
  mvn help:evaluate -Dexpression=project.version | grep -v "^\["
}
