FROM openjdk:8-jre

RUN apt-get update && apt-get install -y git

COPY . /usr/makehtml
WORKDIR /usr/makehtml
COPY entrypoint.sh /usr/makehtml/entrypoint.sh
RUN chmod +x entrypoint.sh

VOLUME ["/source", "/generated"]

ENTRYPOINT ["/bin/bash", "/usr/makehtml/entrypoint.sh"]

