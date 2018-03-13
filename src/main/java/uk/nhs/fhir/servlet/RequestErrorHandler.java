package uk.nhs.fhir.servlet;

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class RequestErrorHandler {
	public static final Logger LOG = LoggerFactory.getLogger(RequestErrorHandler.class);
	
	public static final String SERVLET_CONTEXT_KEY = "uk.nhs.fhir.servlet.RequestErrorHandler";
	
	public void handleError(Throwable t, HttpServletRequest request, HttpServletResponse response) {
		String queryString = request.getQueryString();
		StringBuffer requestURL = request.getRequestURL();
		LOG.error("Servlet encountered error handling request to " + requestURL + (Strings.isNullOrEmpty(queryString) ? "" : "?" + queryString), t);
		
		// Any 
		try {
			if (t instanceof FhirResourceNotFoundException) {
				response.sendError(HttpURLConnection.HTTP_NOT_FOUND, t.getMessage());
			} else if (t instanceof RequestIdMissingException) {
				response.sendError(HttpURLConnection.HTTP_BAD_REQUEST, t.getMessage());
			} else if (t instanceof UnhandledFhirOperationException) {
				response.sendError(HttpURLConnection.HTTP_NOT_IMPLEMENTED, t.getMessage());
			} else if (t instanceof UnrecognisedFhirOperationException) {
				response.sendError(HttpURLConnection.HTTP_NOT_FOUND, t.getMessage());
			} else {
				// default
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		} catch (Exception e) {
			LOG.error("Writing to response which has already been committed (" + response.getStatus() + ")");
		}
	}
}
