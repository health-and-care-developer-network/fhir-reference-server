FROM openjdk:8-jre

RUN apt-get update && apt-get install -y git

COPY . /usr/makehtml
WORKDIR /usr/makehtml
COPY entrypoint.sh /usr/makehtml/entrypoint.sh
RUN chmod +x entrypoint.sh

# Create a non-root user for our FHIR publisher to run as
RUN groupadd -r fhir -g 1000 && \
    useradd -u 1000 -r -g fhir fhir
RUN chown -R fhir:fhir /usr/makehtml

USER fhir

VOLUME ["/source", "/generated"]

ENTRYPOINT ["/bin/bash", "/usr/makehtml/entrypoint.sh"]

