# FHIR Server/Renderer
> Run a local FHIR artefact renderer and serve the rendered artefacts

A combination of the NHS Developer Network [Renderer](https://github.com/health-and-care-developer-network/fhir-profile-renderer) and [Server](https://github.com/health-and-care-developer-network/fhir-reference-server) projects, designed for data modellers to run locally and test rendering of modified resources.

---

### Features

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
  
# executable jar is now present in $


```
### Usage
```
java -jar fhir-server-renderer-1.0.1-SNAPSHOT-jar-with-dependencies.jar
```
(Or simply double-click the jar file.)
