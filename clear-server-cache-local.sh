#!/bin/bash

# Run this after the publishing job to prompt the server to make the newly rendered files available.
cd ./jenkins-scripts
./server-refreshCache.sh "" "fhir-server"

# return to root directory
cd ..
