# FHIR Artefact Renderer
> Embed an HTML representation into FHIR API specification documents

A simple utility to add an HTML representation of the described profile into the text div of one or more FHIR Resource Specification XML files (such as StructureDefinitions, ValueSets and CodeSystems).
- [Build](#Build)
- [Usage](#Usage)
- [Limitations](#Limitations)
- [Contributing](#Contributing)
- [Disclaimer](#Disclaimer)
---
### Build
```
# install dependency to local .m2
git clone https://github.com/health-and-care-developer-network/fhir-model-utils.git \
&& cd fhir-model-utils \
&& mvn install

# back to containing directory
cd ..

# checkout and package this project
git clone https://github.com/health-and-care-developer-network/fhir-profile-renderer.git \
&& cd fhir-profile-renderer \
&& mvn package

# jar with dependencies is now present in ./target/
```
### Usage
```
java -jar MakeHTML-1.0-SNAPSHOT.jar <source folder> <target folder>
```

Any supported files in `<source folder>` will have an HTML view of itself inserted into the *<text>* element, and output into the `<target folder>`. Supporting HTML artefacts are created in a subfolder of the same name as follows:
| File type             | Supporting artefact views                             |
| -                     | -                                                     |
| CodeSystem            | metadata, concepts, filters                           |
| ConceptMap            | metadata, mappings                                    |
| OperationDefinition   | render (composite of metadata, inputs and outputs)    |
| StructureDefinition   | metadata, snapshot, differential, details, bindings   |
| ValueSet              | render (composite of metadata and table)              |

N.B. StructureDefinitions specifying Extension structures do not get a differential view.

The tree view uses inline styles and Base64 encoded background images. It currently relies on NHS Digital serving FHIR icons.
If or when the XML file is then opened using a browser, the majority of the XML is disregarded, but the XHTML contained in the `<text><div>...</div></text>` section will be properly rendered.

This repo' also contains all the necessary in order to *dockerise* the project.

## Limitations
XML is assumed as the input format.
Any existing content in the text element is completely overwritten by this.
Many features permitted by FHIR are not supported. Including these features will cause the process to fail. This is to ensure that we do not accidentally omit/ignore information in the rendered artefacts.

## Contributing
Please feel free to fork and send pull requests if you feel able to contribute to this project.

## Disclaimer
No guarantee of correctness is provided. You may get benefit from the use of this, in which case, excellent, if not, sorry but we probably can't help further.

## Release Notes

> ### v1.0.1 (09/11/2017)
> - Hide cardinality and type of StructureDefinition root node
> - Perform empty links check earlier
> - Display dialog after rendering even if only warnings (no errors) were encountered
> - Add option to copy all successfully rendered resources even if some failed

> ### v1.0.0 (01/11/2017)
> - Initial Release
