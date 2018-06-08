FROM openjdk:8-jre

RUN apt-get update && apt-get install -y git

COPY . /usr/publisher
WORKDIR /usr/publisher
COPY entrypoint.sh /usr/publisher/entrypoint.sh
RUN chmod +x entrypoint.sh

# Create a non-root user for our FHIR publisher to run as
RUN groupadd -r fhir -g 1000 && \
    useradd -u 1000 -r -g fhir fhir
RUN chown -R fhir:fhir /usr/publisher

USER fhir

ENV GITHUB_OAUTH ""

VOLUME ["/source", "/generated"]

ENTRYPOINT ["/bin/bash", "-xe", "/usr/publisher/entrypoint.sh"]

