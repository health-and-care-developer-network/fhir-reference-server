# FHIR Model Utilities
> Utilities for working with HAPI FHIR objects

A set of utilities to support rendering and serving FHIR specification resources. 

---

### Features

- Provides version-agnostic 'WrappedResource' facades for: CodeSystem, ConceptMap, OperationDefinition, StructureDefinition and ValueSet types.
- Best-guess functionality to identify a suitable FHIR version for a provided file.
- Supports HAPI objects for FHIR versions DSTU2 and STU3
- Utilities for linking back to HL7 FHIR documentation at appropriate FHIR version
- Enums representing
    - FHIR Resource types
    - FHIR versions and releases

### Currently used by:

[FHIR Profile Renderer](https://github.com/health-and-care-developer-network/fhir-profile-renderer)

[FHIR Reference Server](https://github.com/health-and-care-developer-network/fhir-reference-server)

[FHIR Local Server](https://github.com/health-and-care-developer-network/fhir-server-renderer)

## Release Notes

> ### v1.0.1 (09/11/2017)
> - Updated maven artifactId to 'fhir-model-utils'

> ### v1.0.0 (01/11/2017)
> - Initial Release

### Built using [HAPI](http://hapifhir.io)
