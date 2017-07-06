These files are used to generate Docker images for adding HTML sections to FHIR resources

In order to use them, you will need Docker Engine installed.

First, you need to have compiled the code in maven - from the project root type:

```bash
mvn clean install
```

Then, from the Docker directory, build the docker image:

```bash
./buildPublisher.sh
```

The image currently uses a couple of directories to store temp files and to store generated outputs. You can create these in advance:

```bash
mkdir -p /docker-data/fhir-profiles
mkdir -p /docker-data/fhir-server-temp
```

Now, you can use the image you just built to pull FHIR resources from Github, add the generated HTML content, and send the augmented resources into a folder that the FHIR server can serve up.
The command line parameters are:

publishResources.sh github_url branch path url_to_replace new_url_to_insert registryhostname targethostname out_path

 - github_url: Git URL to pull FHIR resources from
 - branch: Branch to checkout from Git
 - path: The subdirectory to read resources from (relative to the root of the Git repository)
 - url_to_replace: Used when doing a search and replace on URLs inside resources
 - new_url_to_insert: This is the URL that will be substituted for the URL matched above
 - registryhostname: Hostname of private docker registry to pull image from (pass in an empty string "" if not using a private registry)
 - targethostname: Hostname of Docker server to run the container on if using the Docker API to run remotely (use an empty string "" if not)
 - out_path: Output directory to write to (this will be a subdirectory of /docker-data/fhir-profiles currently) - the renderer will create subdirectories for the resource types.

For example:

```bash
# Generate StructureDefinitions
./publishResources.sh \
	https://github.com/nhsconnect/gpconnect-fhir.git \
	develop \
	StructureDefinitions \
	http://fhir.nhs.net/ \
	http://fhir-test.nhs.uk/ \
        "" \
	"" \
	""

# Generate ValueSets
./publishResources.sh \
	https://github.com/nhsconnect/gpconnect-fhir.git \
	develop \
	ValueSets \
	http://fhir.nhs.net/ \
	http://fhir-test.nhs.uk/ \
        "" \
	"" \
	""
```

