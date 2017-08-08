package uk.nhs.fhir.util;

import ca.uhn.fhir.context.FhirStu3DataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.LinkData;

public class Stu3FhirDocLinkFactory extends FhirDocLinkFactory {
	
	public LinkData forDataType(BasePrimitive<?> fhirData) {
		String dataTypeName;
		String typeURL;
		if (fhirData instanceof CodeDt) {
			return forCodedType((CodeDt)fhirData);
		} else {
			dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			typeURL = FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#" + dataTypeName;
			
			return new LinkData(FhirURL.buildOrThrow(typeURL, FhirVersion.STU3), StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeDt codedType) {
		String dataTypeName = codedType.getValue();
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String url = urlForDataTypeName(dataTypeName);
		return new LinkData(FhirURL.buildOrThrow(url, FhirVersion.STU3), StringUtil.capitaliseLowerCase(dataTypeName));
	}

	private String urlForDataTypeName(String dataTypeName) {
		switch (FhirStu3DataTypes.forType(dataTypeName)) {
			case EXTENSION:
				return urlForExtension();
			case RESOURCE:
				return urlForComplexDataType(dataTypeName);
			case SIMPLE_ELEMENT:
				return urlForSimpleDataType(dataTypeName);
			case PRIMITIVE:
				return urlForSimpleDataType(dataTypeName);
			case COMPLEX_ELEMENT:
				return urlForComplexDataType(dataTypeName);
			case UNKNOWN:
				// The code doesn't represent an element or a resource. 
				// Don't try to unpack - just treat it as a 'Code' type.
				dataTypeName = "Code";
				return urlForSimpleDataType(dataTypeName);
			case DOMAIN_RESOURCE:
				return urlForDomainResource();
			case ELEMENT:
				return urlForComplexDataType(dataTypeName);
			case META:
				return FhirURLConstants.HTTP_HL7_STU3 + "/resource.html#Meta";
			case NARRATIVE:
				return FhirURLConstants.HTTP_HL7_STU3 + "/narrative.html#Narrative";
			case XHTML_NODE:
				return FhirURLConstants.HTTP_HL7_STU3 + "/narrative.html#xhtml";
			case REFERENCE:
				return FhirURLConstants.HTTP_HL7_STU3 + "/references.html";
			case METADATA:
				return FhirURLConstants.HTTP_HL7_STU3 + "metadatatypes.html#" + dataTypeName;
			default:
				throw new IllegalStateException("Couldn't get type for [" + dataTypeName + "]");
		}
	}

	private String urlForDomainResource() {
		return FhirURLConstants.HTTP_HL7_STU3 + "/domainresource.html";
	}

	private String urlForExtension() {
		return FhirURLConstants.HTTP_HL7_STU3 + "/extensibility.html#Extension";
	}

	private String urlForComplexDataType(String complexTypeName) {
		return FhirURLConstants.HTTP_HL7_STU3 + "/" + complexTypeName.toLowerCase() + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#" + dataTypeName.toLowerCase();
	}
}
