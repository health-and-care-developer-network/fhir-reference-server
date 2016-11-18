#!/bin/bash

REGISTRY_HOST=$1
TARGET_HOST=$2
GROUP_ID=$3

# Remove the arguments we have consumed
shift 3

CONTAINERNAME=${CONTAINERNAME:-$TAGNAME}

if [ -z $GROUP_ID ]
then
  GROUP_ID=$(id | perl -ne '/gid=(\d+)/ && { print "$1\n" }')
fi

if [ -z $TARGET_HOST ]
then
  TARGET_PREFIX=""
else
  TARGET_PREFIX="--tlsverify -H $TARGET_HOST:2376"
fi

if [ -z $REGISTRY_HOST ]
then
  REGISTRY_PREFIX=""
  REGISTRY_URL="$TAGNAME"
else
  REGISTRY_PREFIX="--tlsverify -H $REGISTRY_HOST:2376"
  REGISTRY_URL="$REGISTRY_HOST:5000/$TAGNAME"
fi

function deploy_image() {
  local EXTRAFLAGS="${1:-$DOCKER_EXTRAFLAGS}"
  local MEMORYFLAG=${2:-${DOCKER_MEMORY:-128m}}
  local CPUFLAG=${3:-${DOCKER_CPU:-768}} 
  local VOLUMES=${4:-$DOCKER_VOLUMES}
  local CAPABILITIES=${5:-$DOCKER_CAPABILITIES}
  local APPARMOR=${6:-$DOCKER_APPARMOR}
  local LINKS=${7:-$DOCKER_LINKS}
  local LOGHOST=${8:-$DOCKER_LOGHOST}

  if [ ! -z "$VOLUMES" ]
  then
    VOLUMES=$(echo "$VOLUMES" | sed 's/^[ 	]*\(-v\)\?\([ 	]*\)\(.\+\)/-v \3 /')
  fi

  if [ ! -z "$CAPABILITIES" ]
  then
    CAPABILITIES=$(echo "$CAPABILITIES" | sed 's/^[ 	]*\(--cap-add=\)\?\([^ 	]\+\)/--cap-add=\2/')
  fi

  if [ ! -z "$APPARMOR" ]
  then
    if [ -z "$DOCKER_DEVELOPMENT" ]
    then
      APPARMOR="--security-opt=apparmor=$APPARMOR"
    else
      echo "In development mode. Ignoring AppArmor profile: $APPARMOR"
      APPARMOR=
    fi
  fi

  if [ -z "$DOCKER_DEVELOPMENT" ]
  then
    # --read-only is disabled until docker 1.13 lands
    # READONLY="--read-only"
    READONLY=""
  else
    echo "In development mode. Skipping readonly flag"
    READONLY=""
  fi

  if [ ! -z "$LINKS" ]
  then
    LINKS=$(echo "$LINKS" | sed 's/^[ 	]*\(--link\)\?\([ 	]*\)\(.\+\)/--link \3 /')
  fi

  if [ -z "$LOGHOST" ]
  then
    IP=`docker $TARGET_PREFIX network inspect -f '{{ (index .IPAM.Config 0).Gateway }}' bridge`
    LOGHOST="--add-host=loghost:$IP"
  else
    LOGHOST="--add-host=loghost:$LOGHOST"
  fi

  if [ ! -z $REGISTRY_HOST ]
  then
    docker $TARGET_PREFIX pull $REGISTRY_URL
  fi

  if [ -z $NODAEMONIZE ]
  then
    DAEMONIZE="-d"
  else
    DAEMONIZE=""
  fi

  docker $TARGET_PREFIX stop $CONTAINERNAME 2>&1 | grep -v "No such container"
  docker $TARGET_PREFIX rm $CONTAINERNAME 2>&1 | grep -v "No such container"

  docker $TARGET_PREFIX run --name $CONTAINERNAME \
		--cap-drop=all \
		-m $MEMORYFLAG \
		-c $CPUFLAG \
		--restart=on-failure:5 \
		--group-add $GROUP_ID \
		--tmpfs /run --tmpfs /tmp --tmpfs /var/tmp --tmpfs /var/spool \
		$VOLUMES \
		$LINKS \
		$CAPABILITIES \
		$APPARMOR \
		$READONLY \
		$LOGHOST \
		$EXTRAFLAGS \
		$DAEMONIZE \
		$REGISTRY_URL
}
