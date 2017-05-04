package uk.nhs.fhir.util;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.HTMLConstants;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;

public class FhirDocLinkFactory {
	
	public LinkData forDataType(BasePrimitive<?> fhirData) {
		String dataTypeName;
		String typeURL;
		if (fhirData instanceof CodeDt) {
			return forCodedType((CodeDt)fhirData);
		} else {
			dataTypeName = fhirData.getClass().getAnnotation(DatatypeDef.class).name();
			typeURL = HTMLConstants.HL7_DSTU2 + "/datatypes.html#" + dataTypeName;
			
			return new SimpleLinkData(typeURL, StringUtil.capitaliseLowerCase(dataTypeName));
		}
	}

	private LinkData forCodedType(CodeDt codedType) {
		String dataTypeName = codedType.getValue();
		
		return forDataTypeName(dataTypeName);
	}
	
	public LinkData forDataTypeName(String dataTypeName) {
		String url = urlForDataTypeName(dataTypeName);
		return new SimpleLinkData(url, StringUtil.capitaliseLowerCase(dataTypeName));
	}

	public LinkData withNestedLinks(String dataTypeName, List<String> nestedLinkUris) {
		String url = urlForDataTypeName(dataTypeName);
		SimpleLinkData outer = new SimpleLinkData(url, StringUtil.capitaliseLowerCase(dataTypeName));
		
		List<SimpleLinkData> nestedLinks = Lists.newArrayList();
		for (String nestedLinkUri : nestedLinkUris) {
			String[] uriTokens = nestedLinkUri.split("/");
			String linkTargetName = uriTokens[uriTokens.length - 1];
			nestedLinks.add(new SimpleLinkData(nestedLinkUri, StringUtil.capitaliseLowerCase(linkTargetName)));
		}
		
		return new NestedLinkData(outer, nestedLinks);
	}

	private String urlForDataTypeName(String dataTypeName) {
		switch (FhirDataTypes.forType(dataTypeName)) {
			case EXTENSION:
				return HTMLConstants.HL7_DSTU2 + "/extensibility.html#Extension";
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
				return HTMLConstants.HL7_DSTU2 + "/domainresource.html";
			default:
				throw new IllegalStateException("Couldn't get type for [" + dataTypeName + "]");
		}
	}

	private String urlForComplexDataType(String complexTypeName) {
		return HTMLConstants.HL7_DSTU2 + "/" + complexTypeName.toLowerCase() + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return HTMLConstants.HL7_DSTU2 + "/datatypes.html#" + dataTypeName.toLowerCase();
	}
}
