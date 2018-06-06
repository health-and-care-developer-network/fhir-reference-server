#!/bin/bash

STAGING_HOST="dn-s-serv01"
LIVE_HOST="dn-p-serv01"
REGISTRY_HOST="dn-p-mgmt01"

# empty CONFIG_FILE means use default, which is appropriate for NHSD
NHSD_CONFIG_FILE=""
NHSD_CONTAINER_NAME="fhir-server"
NHSD_PORT="8080"
NHSD_URL="https://fhir.nhs.uk/"
NHSD_TEST_URL="https://fhir-test.nhs.uk/"
NHSD_OUT_PATH="NHSDigital"

HL7_CONFIG_FILE="hl7server.properties"
HL7_CONTAINER_NAME="hl7-fhir-server"
HL7_PORT="8084"
HL7_URL="https://fhir.hl7.org.uk"
HL7_TEST_URL="https://fhir-test.hl7.org.uk"
HL7_OUT_PATH="HL7UK"
