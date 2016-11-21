#!/bin/bash

# Usage:
# build.sh registryhostname

TAGNAME="nhsd/fhir-make-html"
BUILDFOLDER="."

source $(dirname $0)/lib/build.sh

# Copy jars
cp -R ../target .

build_image

