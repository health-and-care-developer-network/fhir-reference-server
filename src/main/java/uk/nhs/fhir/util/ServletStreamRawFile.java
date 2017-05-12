package uk.nhs.fhir.util;

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
    	System.out.println("Streaming raw file from classpath: " + filename);
		response.setStatus(200);
        
		
		if (mimeType == null) {
			// Try to "guess" the right mime type
			try {
				// First try using Java 7's probeContentType method (doesn't work in a jar file)
				Path path;
				path = Paths.get(ServletStreamRawFile.class.getResource(filename).toURI());
				mimeType = Files.probeContentType(path);
				LOG.info("Detected mimeType using probeContentType: " + mimeType);
			} catch (Exception e) {
				LOG.info("Error when attempting to detect mimeType using probeContentType: " + e.getMessage());
			}
			if (mimeType == null) {
				// Now, try to guess from the file extension
				mimeType = URLConnection.guessContentTypeFromName(filename);
				LOG.info("Detected mimeType using guessContentTypeFromName: " + mimeType);
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
	}
}
