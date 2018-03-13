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

> ### v1.0.3 (13/03/2018)
> - Tree building improvements: support shallow cloning of FhirTree by reusing data objects on new tree of nodes; lazily create these trees and cache a template tree on each wrapped resource, to reduce work for each renderer
> - support references to user-defined data types
> - construct a minimal "skeleton" backup tree to permit backup node resolution before differential node creation
> - improve backup node resolution and sanity checking
> - code simplification/refactor (version number parsing, minimise code under HAPI package, unify datatype resolution, Preconditions.checkNotNull usage)
> - improve UTF-8 character handling when loading files
> - bug fixes: missing slash for HL7 metadatatypes links, handle trailing slash on resource list pages, support operation definition parameters without types
> - improve some error logging
> - use threadlocal event handler and logger
> - reorganise package structure
> - various minor improvements prompted by FindBugs

> ### v1.0.2 (18/12/2017)
> - Fixed 2 null pointers

> ### v1.0.1 (09/11/2017)
> - Updated maven artifactId to 'fhir-model-utils'

> ### v1.0.0 (01/11/2017)
> - Initial Release

### Built using [HAPI](http://hapifhir.io)
