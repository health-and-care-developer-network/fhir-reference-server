package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirStu3DataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.util.StringUtil;

public class Stu3FhirDocLinkFactory {
	public LinkData forDataType(BasePrimitive<?> fhirData) {
		String dataTypeName;
		String typeURL;
		if (fhirData instanceof CodeDt) {
			return forCodedType((CodeDt)fhirData);
		} else {
			dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			typeURL = FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#" + dataTypeName;
			
			return new SimpleLinkData(FhirURL.buildOrThrow(typeURL), StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeDt codedType) {
		String dataTypeName = codedType.getValue();
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String url = urlForDataTypeName(dataTypeName);
		return new SimpleLinkData(FhirURL.buildOrThrow(url), StringUtil.capitaliseLowerCase(dataTypeName));
	}

	public LinkData withNestedLinks(String dataTypeName, List<String> nestedLinkUris) {
		String url = urlForDataTypeName(dataTypeName);
		SimpleLinkData outer = new SimpleLinkData(FhirURL.buildOrThrow(url), StringUtil.capitaliseLowerCase(dataTypeName));
		
		List<SimpleLinkData> nestedLinks = Lists.newArrayList();
		for (String nestedLinkUri : nestedLinkUris) {
			String[] uriTokens = nestedLinkUri.split("/");
			String linkTargetName = uriTokens[uriTokens.length - 1];
			nestedLinks.add(new SimpleLinkData(FhirURL.buildOrThrow(nestedLinkUri), StringUtil.capitaliseLowerCase(linkTargetName)));
		}
		
		return new NestedLinkData(outer, nestedLinks);
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
