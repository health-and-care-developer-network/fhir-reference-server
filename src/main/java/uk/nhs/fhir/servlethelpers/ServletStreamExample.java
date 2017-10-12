package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.html.RawResourceTemplate;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.ServletUtils;

public class ServletStreamExample {
	private static final Logger LOG = Logger.getLogger(ServletStreamExample.class.getName());
	
	public static void streamExample(HttpServletRequest request, HttpServletResponse response,
			FHIRVersion fhirVersion, FilesystemIF dataSource, RawResourceRender myRawResourceRenderer) throws IOException {
    	
		// Parse the URL
		String exampleName = request.getRequestURI().substring(10);
		
		ResourceMetadata exampleEntity = dataSource.getExampleByName(fhirVersion, exampleName);
		
		if (exampleEntity != null) {
			// We've found a matching example - stream it back
			File srcFile = exampleEntity.getResourceFile();
			
			String fileContent = FileLoader.loadFile(srcFile);
			MimeType mimeType = null;
			
			// Indent XML
			if (srcFile.getName().toLowerCase().endsWith("xml")) {
				mimeType = MimeType.XML;
				try {
					String prettyPrinted = ServletUtils.prettyPrintXML(fileContent);
					fileContent = prettyPrinted;
				} catch (Exception e) {
					LOG.warning("Unable to pretty-print XML example: " + srcFile.getName());
					e.printStackTrace();
				}
				// Pretty print XML
				fileContent = syntaxHighlight(fileContent);
			} else if (srcFile.getName().toLowerCase().endsWith("json")) {
				mimeType = MimeType.JSON;
			} else {
				mimeType = MimeType.UNKNOWN_MIME;
			}
			
			String resourceType = ResourceType.EXAMPLES.toString();
			String baseURL = request.getContextPath();
			String wrappedContent = new RawResourceTemplate(Optional.empty(), Optional.of(resourceType), Optional.of(exampleName), baseURL, fileContent, mimeType).getHtml();
			
			ServletUtils.setResponseSuccess(response, "text/html", wrappedContent);
		} else {
			LOG.severe("Unable to find example: " + exampleName + ", FHIRVersion=" + fhirVersion);
			response.setStatus(404);
		}
	}
}
