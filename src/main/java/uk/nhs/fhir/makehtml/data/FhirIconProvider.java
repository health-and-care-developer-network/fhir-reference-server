package uk.nhs.fhir.makehtml.data;

public class FhirIconProvider {

	public FhirDstu2Icon getIcon(FhirTreeNode node) {
		
		if (node.getLinkedNode().isPresent()) {
			return FhirDstu2Icon.REUSE;
		}
		
		if (node.getSlicingInfo().isPresent()) {
			return FhirDstu2Icon.SLICE;
		}
		
		if (node.getParent() == null) {
			return FhirDstu2Icon.RESOURCE;
		}
		
		if (node.getExtensionType().isPresent()) {
			ExtensionType extensionType = node.getExtensionType().get();
			switch (extensionType) {
				case SIMPLE:
					return FhirDstu2Icon.EXTENSION_SIMPLE;
				case COMPLEX:
					return FhirDstu2Icon.EXTENSION_COMPLEX;
				default:
					throw new IllegalStateException("which icon should be used for extension type " + extensionType.toString());
			}
		}

		FhirDataType dataType = node.getDataType();
		switch (dataType) {
			case CHOICE:
				return FhirDstu2Icon.CHOICE;
			case REFERENCE:
				return FhirDstu2Icon.REFERENCE;
			case PRIMITIVE:
				return FhirDstu2Icon.PRIMITIVE;
			case RESOURCE:
			case COMPLEX_ELEMENT:
				return FhirDstu2Icon.DATATYPE;
			default:
				if (node.hasChildren()) {
					return FhirDstu2Icon.DATATYPE;
				} else {
					return FhirDstu2Icon.ELEMENT;
				}
		}
	}
}
