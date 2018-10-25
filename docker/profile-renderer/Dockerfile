FROM openjdk:8-jre

RUN apt-get update && apt-get install -y git

COPY . /usr/publisher
WORKDIR /usr/publisher
COPY entrypoint.sh /usr/publisher/entrypoint.sh
COPY reprocess-entrypoint.sh /usr/publisher/reprocess-entrypoint.sh
RUN chmod +x entrypoint.sh
RUN chmod +x reprocess-entrypoint.sh

# Use the Jetty user (uid=100) to publish profiles
#RUN groupadd -r fhir -g 100 && \
#    useradd -u 100 -r -g fhir fhir
RUN chown -R 100:100 /usr/publisher

USER 100

ENV GITHUB_OAUTH ""

VOLUME ["/source", "/generated"]

ENTRYPOINT ["/bin/bash", "-xe", "/usr/publisher/entrypoint.sh"]

