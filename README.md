# FHIR Reference Server
> Publish FHIR Specifications

A server built on [HAPI](http://hapifhir.io), customised to serve a set of FHIR reference resources for national profiles, valuesets and conformance resources, published through the NHS Developer Network.

---

### Build:
```bash
# install dependency to local .m2
git clone https://github.com/health-and-care-developer-network/fhir-model-utils.git \
  && cd fhir-model-utils \
  && mvn install

# back to containing directory
cd ..

# checkout and package this project
git clone https://github.com/health-and-care-developer-network/fhir-reference-server.git \
  && cd fhir-reference-server \
  && mvn package

# A packaged .war is produced in ./target/
```
Notes:
------

To deploy this into the root context in Tomcat, you will need to edit the context in Tomcat's conf/server.xml:

```
<Context docBase="fhir" path="/" reloadable="true" source="org.eclipse.jst.jee.server:fhir"/>
```

Deploying in Docker
-------------------

If you have Docker installed, you can easily build and deploy this in Docker.

First, build the WAR file:

```bash
mvn install
```

Then create and start the Docker image:

```bash
./build.sh
./deploy.sh
```

By default, the server will look for files to serve in a bind-mounted directory at `/docker-data/fhir-profiles`, as specified in deploy.sh.
DSTU2 artefacts will then be served from `./NHSDigital` and STU3 artefacts from `./NHSDigital-STU3`, as specified in PropertiesFhirFileLocator.java.
