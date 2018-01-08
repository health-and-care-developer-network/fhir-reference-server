package uk.nhs.fhir.data.wrap.stu3;

import java.util.Locale;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.PrimitiveType;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import uk.nhs.fhir.data.structdef.FhirElementDataTypeStu3;
import uk.nhs.fhir.data.url.FhirDocLinkFactory;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class Stu3FhirDocLinkFactory extends FhirDocLinkFactory {
	
	public LinkData forDataType(PrimitiveType<?> fhirData) {
		String dataTypeName;
		String typeURL;
		if (fhirData instanceof CodeType) {
			return forCodedType((CodeType)fhirData);
		} else {
			dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			typeURL = FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#" + dataTypeName;
			
			return new LinkData(FhirURL.buildOrThrow(typeURL, FhirVersion.STU3), StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeType codedType) {
		String dataTypeName = codedType.getValue();
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String url = urlForDataTypeName(dataTypeName);
		return new LinkData(FhirURL.buildOrThrow(url, FhirVersion.STU3), StringUtil.capitaliseLowerCase(dataTypeName));
	}

	private String urlForDataTypeName(String dataTypeName) {
		switch (FhirElementDataTypeStu3.forType(dataTypeName)) {
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
		return FhirURLConstants.HTTP_HL7_STU3 + "/" + complexTypeName.toLowerCase(Locale.UK) + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#" + dataTypeName.toLowerCase(Locale.UK);
	}
}
