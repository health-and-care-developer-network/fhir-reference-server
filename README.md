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

## Release Notes

> ### v1.2.0 (23/03/2018)
> - make pluralisation of resource list title consistent
> - add link to STU3 conformance document
> - resource name in page titles
> - sort resource lists by name

> ### v1.1.0 (13/03/2018)
> - improve error handling when servicing a request
> - add web crawler descriptions
> - refactor properties provider into an object held on shared servlet context
> - fix unclosed InputStream resource leak
> - fix bug identifying latest active resource
> - depend on abstraction of event handler so that server/renderer can supply alternatives
> - various minor bug fixes prompted by FindBugs
> - refactor file finding code (remove duplication)
> - remove some unused code

> ### v1.0.2 (14/12/2017)
> - fix incorrect content type
> - prevent name clashes between resources with different types
> - refactor back into multiple servlets
> - move css resources into a dedicated directory

> ### v1.0.1 (09/11/2017)
> - Include ConceptMaps and CodeSystems on the index page
> - Handle most queries via a single servlet, delegating to HAPI FHIR for non-browser queries
> - HAPI FHIR Servers no longer known to Jetty (main servlet acts as servlet container for them)
> - Many metadata accessors now FHIR Version agnostic
> - Simplify VelocityTemplate
> - Abstract duplicated ResourceProvider code

> ### v1.0.0 (01/11/2017)
> - Initial Release
