# FHIR Reference Server
> Provide FHIR Specifications

A FHIR Server with a UI, to provide CareConnect and NHS Digital FHIR specification artefacts.

---

### Features

- [HAPI](hapifhir.io)-based FHIR server
- Renderers to create user-friendly HTML versions of FHIR artefacts
- Supports multiple versions of FHIR alongside one another by wrapping HAPI FHIR profile classes.

### Subprojects:

> ### [model-utils (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/model-utils)
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
