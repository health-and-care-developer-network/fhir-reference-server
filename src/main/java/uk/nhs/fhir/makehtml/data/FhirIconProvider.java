package uk.nhs.fhir.makehtml.data;

public class FhirIconProvider {

	public FhirIcon getIcon(FhirTreeTableContent node) {
		
		if (node.getLinkedNode().isPresent()) {
			return FhirIcon.REUSE;
		}
		
		if (node.getSlicingInfo().isPresent()) {
			return FhirIcon.SLICE;
		}
		
		if (node.getParent() == null) {
			return FhirIcon.RESOURCE;
		}
		
		if (node.getExtensionType().isPresent()) {
			ExtensionType extensionType = node.getExtensionType().get();
			switch (extensionType) {
				case SIMPLE:
					return FhirIcon.EXTENSION_SIMPLE;
				case COMPLEX:
					return FhirIcon.EXTENSION_COMPLEX;
				default:
					throw new IllegalStateException("which icon should be used for extension type " + extensionType.toString());
			}
		}

		FhirDataType dataType = node.getDataType();
		switch (dataType) {
			case CHOICE:
				return FhirIcon.CHOICE;
			case REFERENCE:
				return FhirIcon.REFERENCE;
			case PRIMITIVE:
				return FhirIcon.PRIMITIVE;
			case RESOURCE:
			case COMPLEX_ELEMENT:
			case XHTML_NODE:
				return FhirIcon.DATATYPE;
			default:
				if (node.hasChildren()) {
					return FhirIcon.DATATYPE;
				} else {
					return FhirIcon.ELEMENT;
				}
		}
	}
}
