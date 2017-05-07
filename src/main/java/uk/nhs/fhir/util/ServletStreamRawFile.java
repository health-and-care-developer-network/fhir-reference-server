package uk.nhs.fhir.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ServletStreamRawFile {
	public static void streamRawFileFromClasspath(HttpServletResponse response, String mimeType, String filename) throws IOException {
    	System.out.println("Streaming raw file from classpath: " + filename);
		response.setStatus(200);
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
