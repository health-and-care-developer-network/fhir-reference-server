package uk.nhs.fhir.render.html.tree;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;

public class FhirIconProvider<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {

	public FhirIcon getIcon(U node) {
		
		T nodeData = node.getData();
		
		if (nodeData.getLinkedNode().isPresent()) {
			return FhirIcon.REUSE;
		}
		
		if (nodeData.getSlicingInfo().isPresent()) {
			return FhirIcon.SLICE;
		}
		
		if (node.getParent() == null) {
			return FhirIcon.RESOURCE;
		}
		
		if (nodeData.getExtensionType().isPresent()) {
			ExtensionType extensionType = nodeData.getExtensionType().get();
			switch (extensionType) {
				case SIMPLE:
					return FhirIcon.EXTENSION_SIMPLE;
				case COMPLEX:
					return FhirIcon.EXTENSION_COMPLEX;
				default:
					throw new IllegalStateException("which icon should be used for extension type " + extensionType.toString());
			}
		}

		FhirElementDataType dataType = nodeData.getDataType();
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
