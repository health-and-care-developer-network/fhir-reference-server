package uk.nhs.fhir.util;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;

public class FhirDocLinkFactory {
	
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
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String typeURL;
		switch (FhirDataTypes.forType(dataTypeName)) {
		case RESOURCE:
			typeURL = urlForComplexDataType(dataTypeName);
			break;
		case SIMPLE_ELEMENT:
			typeURL = urlForSimpleDataType(dataTypeName);
			break;
		case PRIMITIVE:
			typeURL = urlForSimpleDataType(dataTypeName);
			break;
		case COMPLEX_ELEMENT:
			typeURL = urlForComplexDataType(dataTypeName);
			break;
		case UNKNOWN:
			// The code doesn't represent an element or a resource. 
			// Don't try to unpack - just treat it as a 'Code' type.
			dataTypeName = "Code";
			typeURL = urlForSimpleDataType(dataTypeName);
			break;
		default:
			throw new IllegalStateException("Couldn't get type for [" + dataTypeName + "]");
		}
		
		return new LinkData(typeURL, StringUtil.capitaliseLowerCase(dataTypeName));
	}

	private String urlForComplexDataType(String complexTypeName) {
		return "https://www.hl7.org/fhir/" + complexTypeName + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return "https://www.hl7.org/fhir/datatypes.html#" + dataTypeName.toLowerCase();
	}
}
