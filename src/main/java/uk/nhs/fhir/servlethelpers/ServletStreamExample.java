package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PageTemplateHelper;

public class ServletStreamExample {
	private static final Logger LOG = Logger.getLogger(ServletStreamExample.class.getName());
	private static PageTemplateHelper templateHelper = new PageTemplateHelper();
	
	public static void streamExample(HttpServletRequest request, HttpServletResponse response,
			FHIRVersion fhirVersion, Datasource dataSource, RawResourceRender myRawResourceRenderer) throws IOException {
    	
		// Parse the URL
		String exampleName = request.getRequestURI().substring(10);
		System.out.println("Example="+exampleName);
		
		ResourceEntity exampleEntity = dataSource.getExampleByName(fhirVersion, exampleName);
		
		if (exampleEntity != null) {
			// We've found a matching example - stream it back
			response.setStatus(200);
			//response.setContentType("text/html");
			File srcFile = exampleEntity.getResourceFile();
			
		    //FileUtils.copyFile(srcFile, response.getOutputStream());
			
			String fileContent = FileLoader.loadFile(srcFile);
			fileContent = syntaxHighlight(fileContent);
			StringBuffer sb = new StringBuffer();
			myRawResourceRenderer.renderSingleWrappedRAWResource(fileContent, sb, MimeType.XML);
			
			templateHelper.streamTemplatedHTMLresponse(response, ResourceType.EXAMPLES.toString(), exampleName, sb, request.getContextPath());
		}
	}
}
