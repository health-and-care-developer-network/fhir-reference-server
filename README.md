# FHIR Reference Server
> Provide FHIR Specifications

A FHIR Server with a UI, to provide CareConnect and NHS Digital FHIR specification artefacts.

---

### Features

- [HAPI](hapifhir.io)-based FHIR server
- Renderers to create user-friendly HTML versions of FHIR artefacts. These are output standalone as well as being inserted into the resource's 'text' section.
- Provides version-agnostic 'WrappedResource' facades for: CodeSystem, ConceptMap, OperationDefinition, StructureDefinition and ValueSet types.
- Best-guess functionality to identify a suitable FHIR version for a provided file.
- Supports HAPI objects for FHIR versions DSTU2 and STU3
- Utilities for linking back to HL7 FHIR documentation at appropriate FHIR version

### Requirements

Java 8 Runtime - [oracle.com](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
Maven - [maven.apache.org](https://maven.apache.org/install.html)
Docker - [docker.com](https://docs.docker.com/install/)
Bash - (for docker scripts - but you can build/run on Windows with `mvn clean package`)

### Subprojects:

> #### [model-utils (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/model-utils)
> Shared utilities, including loading and wrapping of versioned FHIR profiles
> #### [profile-renderer-lib (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/profile-renderer-lib)
> Renderers to output specification artefacts as HTML pages
> #### [profile-renderer (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/profile-renderer)
> Deployment wrapper for Profile Renderer
> #### [reference-server-lib (library)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/reference-server-lib)
> Runs multiple HAPI FHIR servlets alongside one another and reads rendered artefacts from a filesystem DB.
> #### [reference-server (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/reference-server)
> Deployment wrapper for Reference Server
> #### [server-renderer (deployable)](https://github.com/health-and-care-developer-network/fhir-reference-server/tree/develop/server-renderer)
> Local instance running both the reference server and profile renderer, primarily for testing and profile development purposes

### Quick Start (combined server/renderer application)

Combined server/renderer running as a plain java process:
```
git clone https://github.com/health-and-care-developer-network/fhir-reference-server.git
cd fhir-reference-server
git checkout develop
mvn clean package
cd server-renderer/target
java -jar server-renderer-*.jar
```

Dockerised standalone server and renderer:
```
git clone https://github.com/health-and-care-developer-network/fhir-reference-server.git
cd fhir-reference-server
# create tagged docker images for both the server (nhsd/fhir-reference-server) and renderer (nhsd/fhir-profile-renderer)
./build-local.sh
# create a dockerised instance of the fhir server (port and container name configurable, defaults to 8080, fhir-server)
./deploy-server-local.sh
# tweak parameters in run-publisher-local.sh to select an appropriate target repo/branch to publish
./run-publisher-local.sh
# Trigger the server to import the newly-rendered files and load them into the server's metadata cache
./clear-server-cache-local.sh
```

### Populating the server

Upon startup, the server starts in the background and a **dialog** is displayed. Select a directory containing FHIR artefacts then click the `Run renderer` button. Once the buttons are no longer disabled, rendering is complete. If there are any errors or warnings, a dialog will be shown.

See usage notes for the standalone renderer for instructions to render github commit history.

Any artefacts that were successfully generated can then be accessed through the browser-friendly website served at `http://localhost:8080`.

Repeat to add further FHIR artefacts. Use the `Clear server cache` button to clear cached artefacts down and start over.

Navigate to localhost:8080 in a browser. No artefacts will initially be present.

Various profiles are also available if you want some examples:
```
#### INTEROPen's CareConnect profiles
git clone https://github.com/nhsconnect/CareConnect-profiles-STU3.git
cd CareConnect-profiles-STU3
git checkout develop

#### Various NHS Digital project profiles
git clone https://github.com/nhsconnect/STU3-FHIR-Assets.git
cd STU3-FHIR-Assets
git checkout develop
```

### Combined server/renderer application CLI options
```
java jar server-renderer-*.jar [-p|--missing-ext-prefix string1;string2;string3] [-l|--local-domains string1;string2;string3]

-p|--missing-ext-prefix                 If an extension with this prefix is depended on but unavailable, the renderer will try to continue rendering without it
-l|--local-domains                      Local domains (for resources hosted on this FHIR server)
```
Options for CareConnect:
```
java -jar server-renderer-*.jar -l "https://fhir.hl7.org.uk/"
```

Options for STU3-FHIR-Assets:
```
java -jar server-renderer-*.jar -p "https://fhir.hl7.org.uk/" -l "https://fhir.nhs.uk/"
```

### Standalone Renderer usage
```
java -jar MakeHTML-1.0-SNAPSHOT.jar <source folder> <target folder>
```

Any supported files in `<source folder>` will have an HTML view of itself inserted into the *<text>* element, and output into the `<target folder>`. Supporting HTML artefacts are created in a subfolder of the same name as follows:
| File type             | Supporting artefact views                                      |
| -                     | -                                                              |
| CodeSystem            | metadata, concepts, filters, commits                           |
| ConceptMap            | metadata, mappings, commits                                    |
| OperationDefinition   | render (composite of metadata, inputs and outputs), commits    |
| StructureDefinition   | metadata, snapshot, differential, details, bindings, commits   |
| ValueSet              | render (composite of metadata and table), commits              |
| MessageDefinition     | metadata, focus, commits                                       |
| SearchParameter       | metadata, details, commits                                     |

N.B. StructureDefinitions specifying Extension structures do not get a differential view.

The tree view uses inline styles and Base64 encoded background images. It currently relies on NHS Digital serving FHIR icons.
If or when the XML file is then opened using a browser, the majority of the XML is disregarded, but the XHTML contained in the `<text><div>...</div></text>` section will be properly rendered.

This repo' also contains all the necessary in order to *dockerise* the project.
See scripts in profile-renderer-lib/Docker

### Standalone Server usage

To deploy this into the root context in Tomcat, you will need to edit the context in Tomcat's conf/server.xml:

```
<Context docBase="fhir" path="/" reloadable="true" source="org.eclipse.jst.jee.server:fhir"/>
```

Deploying in Docker
-------------------

If you have Docker installed, you can easily build and deploy this in Docker.
To create and start the Docker image, use the scripts in reference-server/:

```bash
./build.sh
./deploy.sh
```

By default, the server will look for files to serve in a bind-mounted directory at `/docker-data/fhir-profiles`, as specified in deploy.sh.
DSTU2 artefacts will then be served from `./NHSDigital` and STU3 artefacts from `./NHSDigital-STU3`, as specified in PropertiesFhirFileLocator.java.

## Release History:

#### (C) refers to combined server/renderer

> ### v1.3.0 (branch jenkins-config)
> - project restructure: everything combined into a single git repo, single parent pom with subprojects for libraries and executables
> - docker scripts migrated to their own folder, new Jenkins job and config screen
> - no server downtime after deployment. Existing resources available while new resources are loading (including on startup).
> - support/display SearchParameter resources
> - show Git commit history for all resources
> - events for each run of the profiler are logged to a file in the server/renderer tmp directory
> - logs which domains are treated as local on startup
> - hide titles on HL7 index screen if there are no related resources
> - characters used in text which cannot be display in XML are expanded to hex format
> - fixed value description text is never modified

> ### v1.2.1 (17/04/2018)
> - (C) handle exceptions arising when sorting events for event dialog
> - improve logging in the event of a failure
> - support MessageDefinition resources
> - support accessing bindings on associated extension(s) for StructureD 	Organisation	
￼
Organisation whose environment you are publishing resources to.	
 	Environment	
￼
Environment to deploy the resources to.	
 	GIT_REPO	
￼
Repo to use as resources source	
 	GIT_BRANCH	
￼￼
Filter
 	PublisherVersion	
￼
￼Buildefinition nodes
> - support identifying unavailable CodeSystem on ValueSets
> - only HTML-escape text sections
> - add command line arguments support
> - display StructureDefinition bindings inherited from utilised Extensions
> - upversion to HAPI v3.3.0

> ### v1.2.0 (23/03/2018)
> - make OperationDefinition parameter parts accessible through WrappedOperationDefinition
> - add unexpected features checks for OperationDefinitions
> - fixed bug where node removers wouldn't respect that a node appeared in the differential tree
> - use NodePath consistently where a '.'-separated path is used. Explicitly mutable or immutable.
> - Render OperationDefinition parameter parts
> - Allow viewing event messages while file copying is carried out
> - Support multiple target codes for a single source in a ConceptMap
> - Remove linked ValueSet from CodeSystem metadata
> - Render OperationDefinition parameter parts
> - Allow viewing event messages while file copying is carried out
> - Support multiple target codes for a single source in a ConceptMap
> - Remove linked ValueSet from CodeSystem metadata

> ### v1.1.0 (13/03/2018)
> - (C) Improve event dialog layout
> - (C) Remove superfluous file separator
> - Update package organisation
> - Fix empty row between table title and table body for non-tree tables
> - Support links to HL7 primitive types without changing logical URL displayed
> - Overhaul of tree building (code now migrated to fhir-model-utils).
> - Use threadlocal context for event handling
> - Restructure packages
> - Tree building improvements: support shallow cloning of FhirTree by reusing data objects on new tree of nodes; lazily create these trees and cache a template tree on each wrapped resource, to reduce work for each renderer
> - support references to user-defined data types
> - construct a minimal "skeleton" backup tree to permit backup node resolution before differential node creation
> - improve backup node resolution and sanity checking
> - code simplification/refactor (version number parsing, minimise code under HAPI package, unify datatype resolution, Preconditions.checkNotNull usage)
> - improve UTF-8 character handling when loading files
> - bug fixes: missing slash for HL7 metadatatypes links, handle trailing slash on resource list pages, support operation definition parameters without types
> - use threadlocal event handler and logger

> ### v1.0.2 (14/12/2017)
> - Minor refactoring around cached file storage
> - Fixed 2 null pointers
> - new Jenkins script to refresh the server cache
> - restore STU3 slice names in rendered tree

> ### v1.0.1 (09/11/2017)
> - Hide cardinality and type of StructureDefinition root node
> - Perform empty links check earlier
> - (C) Display dialog after rendering even if only warnings (no errors) were encountered
> - Add option to copy all successfully rendered resources even if some failed
> - Updated maven artifactId to 'fhir-model-utils'

> ### v1.0.0 (01/11/2017)
> - Initial Release


## Limitations
XML is assumed as the input format.
Any existing content in the text element is completely overwritten by the renderer.
Many features permitted by FHIR are not supported. Including these features will cause the process to fail. This is to ensure that we do not accidentally omit/ignore information in the rendered artefacts.

## Contributing
Please feel free to fork and send pull requests if you feel able to contribute to this project.

## Disclaimer
No guarantee of correctness is provided. You may get benefit from the use of this, in which case, excellent, if not, sorry but we probably can't help further.

### Built using [HAPI](http://hapifhir.io)
