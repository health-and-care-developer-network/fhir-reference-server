package uk.nhs.fhir.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.servlethelpers.ServletStreamRawFile;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/favicon.ico", "/images/*", "/js/*", "/style/*","/wp-content/*", "/wp-includes/*"}, displayName = "Static content Servlet", loadOnStartup = 1)
public class StaticContentServlet extends HttpServlet {
	private static Logger LOG = LoggerFactory.getLogger(StaticContentServlet.class.getName());
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.info("Requested URI: " + request.getRequestURI() + " handled by Static content Servlet");
        
        String requestedPath = request.getRequestURI();
        
        if (requestedPath.contains("/../")) {
        	response.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Request URL may not contain the string \"/../\"");
        	return;
        }
        
        String stu3Prefix = "/STU3/";
		if (requestedPath.startsWith(stu3Prefix)) {
			requestedPath = requestedPath.substring(stu3Prefix.length());
		}
		
		if(requestedPath.endsWith(".css")) {
            // Stylesheets
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "text/css", request.getRequestURI());
        } else if(requestedPath.endsWith(".svg")) {
			// SVG
			ServletStreamRawFile.streamRawFileFromClasspath(response, "image/svg+xml", request.getRequestURI());
		} else if(requestedPath.endsWith(".map")) {
			// JavaScript map file
			ServletStreamRawFile.streamRawFileFromClasspath(response, "application/json", request.getRequestURI());
		} else if (requestedPath.endsWith("favicon.ico")) {
        	// favicon.ico
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "image/x-icon", SharedServletContext.getProperties().getFaviconPath());
        } else if (requestedPath.startsWith("/images/") || request.getRequestURI().startsWith("/js/") || request.getRequestURI().startsWith("/wp-content/") || request.getRequestURI().startsWith("/wp-includes/")) {
        	// Image and JS files
        	ServletStreamRawFile.streamRawFileFromClasspath(response, null, request.getRequestURI());
        }  else {
			LOG.warn("Cannot load static resource: " + request.getRequestURI());
		}
	}
}
