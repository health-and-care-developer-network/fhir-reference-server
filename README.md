# FHIR Server/Renderer
> Run a local FHIR artefact renderer and serve the rendered artefacts

A combination of the NHS Developer Network FHIR [Renderer](https://github.com/health-and-care-developer-network/fhir-profile-renderer) and [Server](https://github.com/health-and-care-developer-network/fhir-reference-server) projects, designed for data modellers to run locally and test rendering of resources as they are created/modified.

---

### Build
```
builddir=$(pwd)/build-server-renderer
mkdir $builddir \
  && cd $builddir

# install fhir-model-utils to .m2
git clone https://github.com/health-and-care-developer-network/fhir-model-utils.git \
  && cd fhir-model-utils \
  && mvn install \
  && cd ..

# install fhir-profile-renderer to .m2
git clone https://github.com/health-and-care-developer-network/fhir-profile-renderer.git \
  && cd fhir-profile-renderer \
  && mvn install \
  && cd ..

# install fhir-reference-server to .m2
git clone https://github.com/health-and-care-developer-network/fhir-reference-server.git \
  && cd fhir-reference-server \
  && mvn install \
  && cd ..

# build and package this project
git clone https://github.com/health-and-care-developer-network/fhir-server-renderer.git \
  && cd fhir-server-renderer \
  && mvn package
  
# executable jar is now present in $builddir/fhir-server-renderer/target
```

### Usage
```
java -jar fhir-server-renderer-1.0.1-SNAPSHOT-jar-with-dependencies.jar
```
(Or simply double-click the jar file.)

### Populating the server

Upon startup, the server starts in the background and a **dialog** is displayed. Select a directory containing FHIR artefacts then click the `Run renderer` button. Once the buttons are no longer disabled, rendering is complete. If there are any errors or warnings, a dialog will be shown.

Any artefacts that were successfully generated can then be accessed through the browser-friendly website served at `http://localhost:8080`.

Repeat to add further FHIR artefacts. Use the `Clear server cache` button to clear cached artefacts down and start over.

### Notes

**Server population**
The first request sent to the server after rendering artefacts triggers the server to cache metadata for available artefacts. This may take several seconds.

**Temp Directory**
The server uses a temporary folder to hold imported artefacts. This should be deleted automatically on normal shutdown, but if the process is killed, or there is a HotSpot error etc, this folder should be deleted manually. This folder is named `FhirServerRenderer-[timestamp]` and is found within whatever is returned by `System.getProperty("java.io.tmpdir")` (usually `/tmp` on a Linux machine, but varies according to Windows versions).

## Release Notes

> ### v1.1.0 (09/11/2017)
> - Improve event dialog layout
> - Remove superfluous file separator
> - Update package organisation

> ### v1.0.2 (14/12/2017)
> - Minor refactoring around cached file storage

> ### v1.0.1 (09/11/2017)
> - Minor code tidying only

> ### v1.0.0 (01/11/2017)
> - Initial Release
