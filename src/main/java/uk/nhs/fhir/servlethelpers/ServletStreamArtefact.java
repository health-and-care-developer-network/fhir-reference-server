package uk.nhs.fhir.servlethelpers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.ServletUtils;

public class ServletStreamArtefact {
	private static final Logger LOG = Logger.getLogger(ServletStreamArtefact.class.getName());
	
	public static void streamArtefact(HttpServletRequest request, HttpServletResponse response,
										FHIRVersion fhirVersion, FilesystemIF dataSource) throws IOException {
    	
		// Load a supporting artefact
    	String resourceID = request.getParameter("resourceID");
    	String resourceVersion = request.getParameter("resourceVersion");
    	String artefactType = request.getParameter("artefactType");
    	LOG.fine("Request to stream artefact: " + artefactType);
    	
    	if (resourceID != null && artefactType != null) {
    		IdDt theId = new IdDt(resourceID);
    		if (resourceVersion != null) {
    			theId = theId.withVersion(resourceVersion);
    		}
    		try {
    			ResourceMetadata entity = dataSource.getResourceEntityByID(fhirVersion, theId);
    			for (SupportingArtefact artefact : entity.getArtefacts()) {
        			if (artefact.getArtefactType().name().equals(artefactType)) {
        				// We've found a matching artefact - stream it back
        				File srcFile = artefact.getFilename();
        			    ServletUtils.setResponseSuccess(response, "text/html", srcFile);
        			    return;
        			}
        		}
    		} catch (NullPointerException ex) {
    			LOG.severe("Unable to find matching artefact - type=" + artefactType + ", resourceID=" + resourceID + ", version=" + resourceVersion + ", fhirVersion=" + fhirVersion);
    		}
    	}
    	LOG.severe("Unable to find matching artefact - type=" + artefactType + ", resourceID=" + resourceID + ", version=" + resourceVersion + ", fhirVersion=" + fhirVersion);
	}
}
