package uk.nhs.fhir.util;

import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.parser.DataFormatException;

public class FhirDocLinkFactory {
	private final FhirContext fhirContext;
	
	public FhirDocLinkFactory(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}
	
	public FhirDocLinkFactory() {
		this.fhirContext = FhirContext.forDstu2();
	}
	
	public LinkData forDataType(BasePrimitive<?> fhirData) {
		String dataTypeName;
		String typeURL;
		if (fhirData instanceof CodeDt) {
			return forCodedType((CodeDt)fhirData);
		} else {
			dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			typeURL = "https://www.hl7.org/fhir/datatypes.html#" + dataTypeName;
			
			return new LinkData(typeURL, StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeDt codedType) {
		String dataTypeName = codedType.getValue();

		String typeURL;
		if (isResourceType(dataTypeName)) {
			typeURL = urlForComplexDataType(dataTypeName);
		} else if (isElementType(dataTypeName)) {
			typeURL = urlForElement(dataTypeName);
		} else {
			// The code doesn't represent an element or a resource. 
			// Don't try to unpack - just treat it as a 'Code' type.
			dataTypeName = "Code";
			typeURL = urlForSimpleDataType(dataTypeName);
		}
		
		return new LinkData(typeURL, StringUtil.capitaliseLowerCase(dataTypeName));
	}
	
	private String urlForElement(String dataTypeName) {
		BaseRuntimeElementDefinition<?> elementDefinition = fhirContext.getElementDefinition(dataTypeName);

		if (IDatatype.class.isAssignableFrom(elementDefinition.getImplementingClass())) {
			return urlForSimpleDataType(dataTypeName);
		} else {
			return urlForComplexDataType(dataTypeName);
		}
	}

	private String urlForComplexDataType(String complexTypeName) {
		return "https://www.hl7.org/fhir/" + complexTypeName + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return "https://www.hl7.org/fhir/datatypes.html#" + dataTypeName.toLowerCase();
	}
	
	private boolean isResourceType(String typeName) {
		try {
			fhirContext.getResourceDefinition(typeName);
			return true;
		} catch (DataFormatException e) {
			return false;
		}
	}
	
	private boolean isElementType(String typeName) {
		BaseRuntimeElementDefinition<?> elementDefinition = fhirContext.getElementDefinition(typeName);
		return elementDefinition != null;
	}
}
