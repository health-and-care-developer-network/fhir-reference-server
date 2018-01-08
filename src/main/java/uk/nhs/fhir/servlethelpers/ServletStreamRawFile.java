package uk.nhs.fhir.servlethelpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletStreamRawFile {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServletStreamRawFile.class.getName());
	
	public static void streamRawFileFromClasspath(HttpServletResponse response, String mimeType, String filename) throws IOException {
    	//LOG.info("Streaming raw file from classpath: " + filename);

    	try {
	    	response.setStatus(200);
			
			if (mimeType == null) {
				if (filename.endsWith(".js")) {
					mimeType = "application/javascript";
					LOG.debug("Using Javascript mimeType: " + mimeType);
				} else {
					// Now, try to guess from the file extension
					mimeType = URLConnection.guessContentTypeFromName(filename);
					LOG.debug("Detected mimeType using guessContentTypeFromName: " + mimeType);
				}
			}
			
			response.setContentType(mimeType);
			
			PrintWriter outputStream = response.getWriter();
	        try (InputStream is = ServletStreamRawFile.class.getResourceAsStream(filename)) {
		        int b = is.read();
		        while (b != -1) {
		        	outputStream.write(b);
		        	b = is.read();
		        }
	        }
    	} catch (Exception e) {
    		LOG.error("Error streaming raw file to requestor: " + filename + " - error: " + e.getMessage());
    		response.setStatus(404);
    	}
	}
}
