package uk.nhs.fhir.util;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;

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
		String url = urlForDataTypeName(dataTypeName);
		return new LinkData(url, StringUtil.capitaliseLowerCase(dataTypeName));
	}

	public LinkData withNestedLinks(String dataTypeName, List<String> nestedLinkUris) {
		String url = urlForDataTypeName(dataTypeName);
		LinkData outer = new LinkData(url, StringUtil.capitaliseLowerCase(dataTypeName));
		
		List<LinkData> nestedLinks = Lists.newArrayList();
		for (String nestedLinkUri : nestedLinkUris) {
			String[] uriTokens = nestedLinkUri.split("/");
			String linkTargetName = uriTokens[uriTokens.length - 1];
			nestedLinks.add(new LinkData(nestedLinkUri, StringUtil.capitaliseLowerCase(linkTargetName)));
		}
		
		return new NestedLinkData(outer, nestedLinks);
	}

	private String urlForDataTypeName(String dataTypeName) {
		switch (FhirDataTypes.forType(dataTypeName)) {
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
		default:
			throw new IllegalStateException("Couldn't get type for [" + dataTypeName + "]");
		}
	}

	private String urlForComplexDataType(String complexTypeName) {
		return "https://www.hl7.org/fhir/" + complexTypeName.toLowerCase() + ".html";
	}
	
	private String urlForSimpleDataType(String dataTypeName) {
		return "https://www.hl7.org/fhir/datatypes.html#" + dataTypeName.toLowerCase();
	}
}
