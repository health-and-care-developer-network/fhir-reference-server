package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.page.raw.RawResourceTemplate;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

public class ServletStreamExample {
	private static final Logger LOG = LoggerFactory.getLogger(ServletStreamExample.class.getName());
	
	private final FilesystemIF dataSource;
	
	public ServletStreamExample(FilesystemIF dataSource) {
		this.dataSource = dataSource;
	}
	
	public void streamExample(String uriAfterBase, HttpServletResponse response,
			FhirVersion fhirVersion, RawResourceRenderer myRawResourceRenderer) throws IOException {
    	
		// Parse the URL
		String exampleName = uriAfterBase.substring("/Examples/".length());
		
		Optional<ResourceMetadata> exampleEntity = dataSource.getExampleByName(fhirVersion, exampleName);
		
		if (exampleEntity.isPresent()) {
			// We've found a matching example - stream it back
			File srcFile = exampleEntity.get().getResourceFile();
			
			String fileContent = FileLoader.loadFile(srcFile);
			MimeType mimeType = null;
			
			// Indent XML
			if (srcFile.getName().toLowerCase(Locale.UK).endsWith("xml")) {
				mimeType = MimeType.XML;
				try {
					String prettyPrinted = ServletUtils.prettyPrintXML(fileContent);
					fileContent = prettyPrinted;
				} catch (Exception e) {
					LOG.warn("Unable to pretty-print XML example: " + srcFile.getName());
					e.printStackTrace();
				}
				// Pretty print XML
				fileContent = syntaxHighlight(fileContent);
			} else if (srcFile.getName().toLowerCase(Locale.UK).endsWith("json")) {
				mimeType = MimeType.JSON;
			} else {
				mimeType = MimeType.UNKNOWN_MIME;
			}
			
			String wrappedContent = new RawResourceTemplate(Optional.of(ResourceType.EXAMPLES), Optional.of(exampleName), fileContent, mimeType).getHtml("FHIR Server: Example resource (" + exampleEntity.get().getResourceName() + ")" );
			
			ServletUtils.setResponseContentForSuccess(response, "text/html", wrappedContent);
		} else {
			LOG.error("Unable to find example: " + exampleName + ", FhirVersion=" + fhirVersion);
			response.setStatus(404);
		}
	}
}
