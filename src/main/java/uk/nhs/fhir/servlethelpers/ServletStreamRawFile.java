package uk.nhs.fhir.servlethelpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.RestfulServlet;

public class ServletStreamRawFile {
	
	private static final Logger LOG = Logger.getLogger(ServletStreamRawFile.class.getName());
	
	public static void streamRawFileFromClasspath(HttpServletResponse response, String mimeType, String filename) throws IOException {
    	//LOG.info("Streaming raw file from classpath: " + filename);

    	try {
	    	response.setStatus(200);
			
			if (mimeType == null) {
				if (filename.endsWith(".js")) {
					mimeType = "application/javascript";
					LOG.fine("Using Javascript mimeType: " + mimeType);
				} else {
					// Now, try to guess from the file extension
					mimeType = URLConnection.guessContentTypeFromName(filename);
					LOG.fine("Detected mimeType using guessContentTypeFromName: " + mimeType);
				}
			}
			
			response.setContentType(mimeType);
			
			PrintWriter outputStream = response.getWriter();
	        InputStream is = ServletStreamRawFile.class.getResourceAsStream(filename);
	        int b = is.read();
	        while (b != -1) {
	        	outputStream.write(b);
	        	b = is.read();
	        }
    	} catch (Exception e) {
    		LOG.severe("Error streaming raw file to requestor: " + filename + " - error: " + e.getMessage());
    		response.setStatus(404);
    	}
	}
}
