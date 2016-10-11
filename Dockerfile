# Dockerfile to run my WAR file
# This uses a base image from jetty, which can run war files...
FROM jetty
# Adds my war into the image
ADD ./target/fhir-1.0-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war
# Says that when it runs, it's port 80 needs to be available
EXPOSE 8080

# To build the image using this, simply run:
# $docker build -t myfhir .
# This creates an image called myfhir based on this dockerfile
#
# To run a container based on this image use:
# $docker run -d -p 80:8080 --link mymongo:mongo --name myfhirrunning myfhir
# This creates a running container, which exposes port 8080 on port 80, is
# called myfhirrunning, and knows how to connect (via hostname) to a running
# container called mymongo.
#
# This assumes you already did:
# $docker pull mongo
# docker run -p 28015:27017 -d --name mymongo mongo
