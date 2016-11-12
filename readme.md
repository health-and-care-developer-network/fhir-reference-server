# MakeHTML

A simple utility to add a html representation of the described profile into the text div of one or more FHIR StructureDefinition XML files.

---

## Usage
Simply run:

```
java -jar MakeHTML-1.0-SNAPSHOT.jar [source folder] [target folder]
```

Any StructureDefinition files in `[source folder]` will have a tree view of itself inserted into the *text* element, and output into the `[target folder]`.
The tree view uses inline styles and Base64 encoded images, and is therefore completely self standing.
If or when the xml file is then opened using s browser, the majority of the XML is disregarded, but the `<text><div>...</div></text>` section will be properly rendered.

This repo' also contains all the necessary in order to *dockerise* the project.

## Limitations
This code hasn't been tested with every possible combination of complexities that can be applied in a StructureDefinition.
Slicing for example is pretty much ignored.
XML is assumed as the input format.
Any existing content in the text element is completely overwritten by this.

## Contributing
Please feel free to fork and send pull requests if you feel able to contribute to this project.

## Disclaimer
No guarantee of correctness is provided. You may get benefit from the use of this, in which case, excellent, if not, sorry but we probably can't help further.