# FHIR Reference Server
> Provide FHIR Specifications

A FHIR Server with a UI, to provide CareConnect and NHS Digital FHIR specification artefacts.

---

### Features

- [HAPI](hapifhir.io)-based FHIR server
- Renderers to create user-friendly HTML versions of FHIR artefacts
- Supports multiple versions of FHIR alongside one another by wrapping HAPI FHIR profile classes.

### Requirements

Java 8 Runtime
Maven
Docker

### Quick Start (local server and renderer application)

```
git clone https://github.com/health-and-care-developer-network/fhir-reference-server.git
cd fhir-reference-server
git checkout develop
mvn clean package
cd server-renderer/target
java -jar server-renderer-*.jar
```

Navigate to localhost:8080 in a browser. No artefacts will currently be available.

You can then click "Select..." to choose a directory to discover FHIR artefacts in.
Then click Run renderer to generate HTML representations of the resources. Errors or warnings are displayed in a window after rendering completes.

Various profiles are also available if you want some examples:
```
# INTEROPen's CareConnect profiles
git clone https://github.com/nhsconnect/CareConnect-profiles-STU3.git
cd CareConnect-profiles-STU3
git checkout develop

# Various NHS Digital project profiles
git clone https://github.com/nhsconnect/STU3-FHIR-Assets.git
cd STU3-FHIR-Assets
git checkout develop
```

### CLI options (local server and renderer application)
```
java jar server-renderer-*.jar [-p|--missing-ext-prefix string1;string2;string3] [-l|--local-domains string1;string2;string3]

-p|--missing-ext-prefix                 If an extension with this prefix is depended on but unavailable, the renderer will try to continue rendering without it
-l|--local-domains                      Local domains (for resources hosted on this FHIR server)
```
For CareConnect:
```
java -jar server-renderer-*.jar -l "https://fhir.hl7.org.uk/"
```

For STU3-FHIR-Assets:
```
java -jar server-renderer-*.jar -p "https://fhir.hl7.org.uk/" -l "https://fhir.nhs.uk/"
```


### Subprojects:

> ### [model-utils (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/model-utils)-
> Shared utilities, including loading and wrapping of versioned FHIR profiles
> ### [profile-renderer-lib (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/profile-renderer-lib)
> Renderers to output specification artefacts as HTML pages
> ### [profile-renderer (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/profile-renderer)
> Deployment wrapper for Profile Renderer
> ### [reference-server-lib (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/reference-server-lib)
> Runs multiple HAPI FHIR servlets alongside one another and reads rendered artefacts from a filesystem DB.
> ### [reference-server (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/reference-server)
> Deployment wrapper for Reference Server
> ### [server-renderer (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/server-renderer)
> Local instance running both the reference server and profile renderer, primarily for testing and profile development purposes

### Built using [HAPI](http://hapifhir.io)
