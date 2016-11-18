#!/bin/bash

# Usage:
# build.sh registryhostname

TAGNAME="fhir-make-html"
BUILDFOLDER="."

source $(dirname $0)/lib/build.sh

build_image

