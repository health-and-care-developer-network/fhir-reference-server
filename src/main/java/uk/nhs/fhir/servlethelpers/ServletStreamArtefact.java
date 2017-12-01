package uk.nhs.fhir.servlethelpers;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

public class ServletStreamArtefact {
	private static final Logger LOG = LoggerFactory.getLogger(ServletStreamArtefact.class.getName());
	
	private final FilesystemIF dataSource;
	
	public ServletStreamArtefact(FilesystemIF dataSource) {
		this.dataSource = dataSource;
	}
	
	public void streamArtefact(HttpServletRequest request, HttpServletResponse response,
										FhirVersion fhirVersion) throws IOException {
    	
		// Load a supporting artefact
    	String resourceID = request.getParameter("resourceID");
    	String resourceVersion = request.getParameter("resourceVersion");
    	String artefactType = request.getParameter("artefactType");
    	String resourceTypeParam = request.getParameter("resourceType");
    	LOG.debug("Request to stream artefact: " + artefactType);
    	
    	if (resourceID != null 
    	  && artefactType != null
    	  && resourceTypeParam != null) {
    		// catch unrecognised types
    		ResourceType resourceType = ResourceType.getTypeFromHAPIName(resourceTypeParam);
    		
    		IdDt theId = new IdDt(resourceID).withResourceType(resourceType.toString());
    		if (resourceVersion != null) {
    			theId = theId.withVersion(resourceVersion);
    		}
    		
			ResourceMetadata entity = dataSource.getResourceEntityByID(fhirVersion, theId);
			
			if (entity == null) {
				LOG.error("Unable to find matching artefact - type=" + artefactType + ", resourceID=" + resourceID + ", version=" + resourceVersion + ", fhirVersion=" + fhirVersion + ", resourceType=" + resourceType);
				return;
			}
			
			for (SupportingArtefact artefact : entity.getArtefacts()) {
    			if (artefact.getArtefactType().name().equals(artefactType)) {
    				// We've found a matching artefact - stream it back
    				File srcFile = artefact.getFilename();
    			    ServletUtils.setResponseContentForSuccess(response, "text/html", srcFile);
    			    return;
    			}
    		}
    	}
    	
    	LOG.error("Unable to find matching artefact - type=" + artefactType + ", resourceID=" + resourceID + ", version=" + resourceVersion + ", fhirVersion=" + fhirVersion + ", resourceType=" + resourceTypeParam);
	}
}
