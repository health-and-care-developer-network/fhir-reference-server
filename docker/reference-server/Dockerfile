# Dockerfile to run my WAR file
# This uses a base image from jetty, which can run war files...
FROM jetty:9.4.12-jre8-alpine

USER root

ENV CONFIG_FILE ""

# Adds war into the image
ADD ./ROOT.war /var/lib/jetty/webapps/ROOT.war

# Says that when it runs, it's port 8080 needs to be available
EXPOSE 8080

# Volume to store profiles that the server will serve up
VOLUME /opt/fhir

USER jetty

