package uk.nhs.fhir.servlethelpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.enums.FHIRVersion;

public class ServletStreamArtefact {
	private static final Logger LOG = Logger.getLogger(ServletStreamArtefact.class.getName());
	
	public static void streamArtefact(HttpServletRequest request, HttpServletResponse response,
										FHIRVersion fhirVersion, Datasource dataSource) throws IOException {
    	
		// Load a supporting artefact
    	String resourceID = request.getParameter("resourceID");
    	String resourceVersion = request.getParameter("resourceVersion");
    	String artefactType = request.getParameter("artefactType");
    	LOG.info("Request to stream artefact: " + artefactType);
    	
    	if (resourceID != null && artefactType != null) {
    		IdDt theId = new IdDt(resourceID);
    		if (resourceVersion != null) {
    			theId = theId.withVersion(resourceVersion);
    		}
    		ResourceEntity entity = dataSource.getResourceEntityByID(fhirVersion, theId);
    		for (SupportingArtefact artefact : entity.getArtefacts()) {
    			if (artefact.getArtefactType().name().equals(artefactType)) {
    				// We've found a matching artefact - stream it back
    				response.setStatus(200);
    				response.setContentType("text/html");
    				File srcFile = artefact.getFilename();
    			    FileUtils.copyFile(srcFile, response.getOutputStream());
    			    return;
    			}
    		}
    	}
    	LOG.severe("Unable to find matching artefact - resourceID=" + resourceID + ", version=" + resourceVersion);
	}
}
