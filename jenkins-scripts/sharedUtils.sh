#!/bin/bash

# Some shared bash utility functions

# Set $DOCKER_CMD according to whether $REGISTRY_HOST is set
setDockerCmd()
{
  # Include tlsverify if we have supplied a host
  if [ -z $REGISTRY_HOST ]; then
    DOCKER_CMD="docker"
  else
    DOCKER_CMD="docker --tlsverify -H $REGISTRY_HOST:2376"
  fi
}