package uk.nhs.fhir;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

@WebServlet(urlPatterns = {"/index.html", ""}, displayName = "FHIR Server Home Page", loadOnStartup = 1)
public class IndexServlet extends javax.servlet.http.HttpServlet {
	
	private static final Logger LOG = Logger.getLogger(IndexServlet.class.getName());
	private String template = null;
    private String fhirServerNotice = null;
    private String fhirServerWarning = null;
    private static String startOfBaseResourceBox = null;
    private static String endOfBaseResourceBox = null;
	
	@Override
	public void init() throws ServletException {
		template = FileLoader.loadFileOnClasspath("/template/index.html");
        fhirServerNotice = PropertyReader.getProperty("fhirServerNotice");
        fhirServerWarning = PropertyReader.getProperty("fhirServerWarning");
        startOfBaseResourceBox = PropertyReader.getProperty("startOfBaseResourceBox");
        endOfBaseResourceBox = PropertyReader.getProperty("endOfBaseResourceBox");
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter outputStream = null;
		StringBuffer content = new StringBuffer();
		
		LOG.info("Requested URL: " + req.getRequestURL());
		
		// Page content
		content.append("<div class='fhirServer'>");
    	content.append(fhirServerWarning);
    	
    	content.append("<div class='fw_nav_boxes isotope' style='position: relative; overflow: hidden;'>");
    	outputFancyBox(content, "StructureDefinition", "StructureDefinition", "List all StructureDefinitions");
    	outputFancyBox(content, "ValueSet", "#", "[Coming Soon] List all ValueSets");
    	outputFancyBox(content, "OperationDefinition", "#", "[Coming Soon] List all OperationDefinition resources");
    	outputFancyBox(content, "Conformance", "#", "[Coming Soon] List all Conformance resources");
    	content.append("</div>");
    	
		content.append("<p><a href=\"metadata\">Download conformance resource for this FHIR server</a></p>");
		
		content.append("</div>");
		
		// Initialise the output
        resp.setStatus(200);
        resp.setContentType("text/html");
		outputStream = resp.getWriter();
		
		// Put the content into our template
	    String outputString = template;
        outputString = outputString.replaceFirst("\\{\\{PAGE-CONTENT\\}\\}", content.toString());
        
        // Send it to the output
        outputStream.append(outputString);
	}
	
	private void outputFancyBox(StringBuffer sb, String title, String url, String description) {
    	
		boolean showHyperlink = !(url.equals("#"));
		
		sb.append(startOfBaseResourceBox);
    	sb.append(title);
    	sb.append(endOfBaseResourceBox);
        sb.append("<li>");
        if (showHyperlink) sb.append("<a href='").append(url).append("'>");
        sb.append(description);
        if (showHyperlink) sb.append("</a>");
        sb.append("</li>");
    	sb.append("</ul></section></div></div>");
	}
}
