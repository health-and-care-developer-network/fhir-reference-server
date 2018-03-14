package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.raw.RawResourceTemplate;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.text.FhirTextSectionHelpers;

public class RawResourceRenderer {

    public String renderSingleWrappedRAWResourceWithoutText(IBaseResource resource, FhirVersion fhirVersion, String resourceName, ResourceType resourceType, String baseURL, MimeType mimeType) {
        resource = FhirTextSectionHelpers.forVersion(fhirVersion).removeTextSection(resource);
        
        return renderSingleWrappedRAWResource(resource, fhirVersion, Optional.of(resourceName), resourceType, baseURL, mimeType);
    }
    
    public String renderSingleWrappedRAWResource(IBaseResource resource, FhirVersion fhirVersion, Optional<String> resourceName, ResourceType resourceType, String baseURL, MimeType mimeType) {
    	String rawResource = getRawResource(resource, mimeType, fhirVersion);
        return new RawResourceTemplate(Optional.of(resourceType.toString()), resourceName, rawResource, mimeType).getHtml("FHIR Server: Raw resource (" + resourceName.orElse(resource.getIdElement().getValueAsString()) + ")");
    }
    
    public String getRawResource(IBaseResource resource, MimeType mimeType, FhirVersion fhirVersion) {
    	FhirContext fhirContext = FhirContexts.forVersion(fhirVersion);
    	if (mimeType == JSON) {
        	return getResourceAsJSON(resource, fhirContext);
        } else {
        	return getResourceAsXML(resource, fhirContext);
        }
	}

	public String getResourceAsXML(IBaseResource resource, FhirContext fhirContext) {
        // Serialise it to XML
        String serialised = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        
        // Encode it for HTML output
        serialised = syntaxHighlight(serialised);
        
        return serialised;
    }
    
    public String getResourceAsJSON(IBaseResource resource, FhirContext fhirContext) {
        // Serialise it to JSON
    	return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
}
