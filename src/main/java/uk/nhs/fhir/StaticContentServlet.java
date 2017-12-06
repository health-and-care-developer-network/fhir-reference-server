package uk.nhs.fhir;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.servlethelpers.ServletStreamRawFile;
import uk.nhs.fhir.util.FhirServerProperties;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/favicon.ico", "/images/*", "/js/*", "/style/*"}, displayName = "Static content Servlet", loadOnStartup = 1)
public class StaticContentServlet extends HttpServlet {
	private static Logger LOG = LoggerFactory.getLogger(StaticContentServlet.class.getName());
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.info("Requested URI: " + request.getRequestURI() + " handled by Static content Servlet");
        
        String requestedPath = request.getRequestURI().substring(5);
        String stu3Prefix = "/STU3/";
		if (requestedPath.startsWith(stu3Prefix)) {
			requestedPath = requestedPath.substring(stu3Prefix.length());
		}
		
		if(requestedPath.endsWith(".css")) {
            // Stylesheets
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "text/css", request.getRequestURI());
        } else if (requestedPath.endsWith("favicon.ico")) {
        	// favicon.ico
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "image/x-icon", FhirServerProperties.getProperty("faviconFile"));
        } else if (requestedPath.startsWith("/images/") || request.getRequestURI().startsWith("/js/")) {
        	// Image and JS files
        	ServletStreamRawFile.streamRawFileFromClasspath(response, null, request.getRequestURI());
        } 
	}
}
