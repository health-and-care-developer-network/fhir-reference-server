FROM openjdk:8-jre

RUN apt-get update && apt-get install -y git && apt-get -y install sudo

COPY . /usr/publisher
WORKDIR /usr/publisher
COPY entrypoint.sh /usr/publisher/entrypoint.sh
RUN chmod +x entrypoint.sh

# Create a non-root user for our FHIR publisher to run as
RUN groupadd -r fhir -g 1000 && \
    useradd -u 1000 -r -g fhir fhir
# Add fhir user to sudo
RUN adduser fhir sudo
# Run sudo with no password prompt
RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
# Change owner and group to fhir on publisher location
RUN chown -R fhir:fhir /usr/publisher

USER fhir

ENV GITHUB_OAUTH ""

VOLUME ["/source", "/generated"]

ENTRYPOINT ["/bin/bash", "-xe", "/usr/publisher/entrypoint.sh"]

