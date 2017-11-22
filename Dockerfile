# Dockerfile to run my WAR file
# This uses a base image from jetty, which can run war files...
FROM jetty:9.3.12-jre8-alpine

ENV CONFIG_FILE ""

# Adds war into the image
ADD ./target/fhir-1.0.1-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war

# Create a non-root user for our FHIR server to run as
RUN adduser -D -u 1000 fhir

# Says that when it runs, it's port 8080 needs to be available
EXPOSE 8080

# Volume to store profiles that the server will serve up
VOLUME /opt/fhir

USER fhir

# To build the image using this, simply run:
# $docker build -t myfhir .
# This creates an image called myfhir based on this dockerfile
#
# To run a container based on this image use:
# $docker run -d -p 80:8080 --link mymongo:mongo --name myfhir myfhir
# This creates a running container, which exposes port 8080 on port 80, is
# called myfhirrunning, and knows how to connect (via hostname) to a running
# container called mymongo.
#
# This assumes you already did:
# $docker pull mongo
# docker run -p 28015:27017 -d --name mymongo mongo

# If you are using the filesystem persistence instead of MongoDB, use this
# command to run the container:
# docker run -d -p 80:8080 -v /opt/fhir:/opt/fhir --name myfhir myfhir
