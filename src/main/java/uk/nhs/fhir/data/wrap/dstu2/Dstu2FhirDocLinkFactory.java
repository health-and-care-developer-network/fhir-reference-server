package uk.nhs.fhir.data.wrap.dstu2;

import ca.uhn.fhir.context.FhirDstu2DataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.url.FhirDocLinkFactory;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class Dstu2FhirDocLinkFactory extends FhirDocLinkFactory {
	
	public LinkData forDataType(BasePrimitive<?> fhirData) {
		if (fhirData instanceof CodeDt) {
			return forCodedType((CodeDt)fhirData);
		} else {
			String dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			String typeURL = FhirURLConstants.HTTP_HL7_DSTU2 + "/datatypes.html#" + dataTypeName;
			
			return new LinkData(FhirURL.buildOrThrow(typeURL, FhirVersion.DSTU2), StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeDt codedType) {
		String dataTypeName = codedType.getValue();
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String url;
		switch (FhirDstu2DataTypes.forType(dataTypeName)) {
			case EXTENSION:
				url = urlForExtension();
				break;
			case RESOURCE:
				url = urlForComplexDataType(dataTypeName);
				break;
			case SIMPLE_ELEMENT:
				url = urlForSimpleDataType(dataTypeName);
				break;
			case PRIMITIVE:
				url = urlForSimpleDataType(dataTypeName);
				break;
			case COMPLEX_ELEMENT:
				url = urlForComplexDataType(dataTypeName);
				break;
			case DOMAIN_RESOURCE:
				url = urlForDomainResource();
				break;
			case ELEMENT:
				url = urlForComplexDataType(dataTypeName);
				break;
			case UNKNOWN:
				// The code doesn't represent an element or a resource. 
				// Don't try to unpack - just treat it as a 'Code' type.
				dataTypeName = "Code";
				url = urlForSimpleDataType(dataTypeName);
				break;
			default:
				throw new IllegalStateException("Couldn't get type for [" + dataTypeName + "]");
		}
		
		return new LinkData(FhirURL.buildOrThrow(url, FhirVersion.DSTU2), StringUtil.capitaliseLowerCase(dataTypeName));
	}
	
	public LinkData fromUri(String uri) {
		String[] uriTokens = uri.split("/");
		String linkTargetName = uriTokens[uriTokens.length - 1];
		return new LinkData(FhirURL.buildOrThrow(uri, FhirVersion.DSTU2), StringUtil.capitaliseLowerCase(linkTargetName));
	}

	private String urlForDomainResource() {
		return FhirURLConstants.HTTP_HL7_DSTU2 + "/domainresource.html";
	}

	private String urlForExtension() {
		return FhirURLConstants.HTTP_HL7_DSTU2 + "/extensibility.html#Extension";
	}

	private String urlForComplexDataType(String complexTypeName) {
		return FhirURLConstants.HTTP_HL7_DSTU2 + "/" + complexTypeName.toLowerCase() + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return FhirURLConstants.HTTP_HL7_DSTU2 + "/datatypes.html#" + dataTypeName.toLowerCase();
	}
}
