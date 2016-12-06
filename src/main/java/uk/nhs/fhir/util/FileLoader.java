/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.nhs.fhir.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;


/**
 * Convenience class to load a file from a given filename and return the
 * content as a string.
 * @author Adam Hatherly
 */
public class FileLoader {
	private static final Logger logger = Logger.getLogger(FileLoader.class.getName());
	
	/**
     * @param filename Filename to load content from
     * @return String containing content of specified file
     */
    public static String loadFile(final String filename) {
        return loadFile(new File(filename));
    }
    
	/**
     * @param file File to load content from
     * @return String containing content of specified file
     */
    public static String loadFile(final File file) {
    	logger.info("Loading file: " + file.getAbsolutePath());
        String defaultEncoding = PropertyReader.getProperty("fileEncoding");
    	InputStream inputStream;
    	Reader inputStreamReader;
    	ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
		try {
			inputStream = new FileInputStream(file);
			String charsetName = defaultEncoding;
			
			// Use commons.io to deal with byte-order-marker if present
			BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
			if (bOMInputStream.hasBOM()) {
				ByteOrderMark bom = bOMInputStream.getBOM();
			    charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
			}
			
		    logger.info("Loading file using encoding: " + charsetName);
		    
	    	inputStreamReader = new InputStreamReader(bOMInputStream, charsetName);
	    	int data = inputStreamReader.read();
	    	while(data != -1){
	    	    bOutStream.write(data);
	    	    data = inputStreamReader.read();
	    	}
	    	inputStreamReader.close();
		} catch (FileNotFoundException e) {
			logger.severe("Error reading file: " + file.getName() + " - message - " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.severe("Error reading file: " + file.getName() + " - message - " + e.getMessage());
		} catch (IOException e) {
			logger.severe("Error reading file: " + file.getName() + " - message - " + e.getMessage());
		}
		
		return cleanString(bOutStream.toString());
    }
    
    /**
     * @param is InputStream to load content from
     * @return String containing content of specified file
     */
    public static String loadFile(final InputStream is) {
        String content = null;
        try {
            ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
            int c = -1;
            while ((c = is.read()) > -1) {
                bOutStream.write(c);
            }
            content = bOutStream.toString();
        } catch (IOException ex) {
        	logger.severe("Error loading file: " + ex.getMessage());
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) { }
        }
        return content;
    }
    
    /**
     * @param fileName Name of file on the classpath to load
     * @return String containing content of specified file
     */
    public static String loadFileOnClasspath(String fileName) {
    		URL resource = FileLoader.class.getResource(fileName);
    		if (resource != null) {
    			return loadFile(FileLoader.class.getResourceAsStream(fileName));
    		} else {
    			logger.severe("Unable to load file from classpath: " + fileName);
    			return null;
    		}
    }
    
    private static String cleanString(String input) {
    	// Hack: The funny quote symbols are showing up as char codes 28 and 29.. replace them with '
    	return input.replace((char)28, (char)39)
    			    .replace((char)29, (char)39);
    }
    
    /**
     * This method will remove any illegal characters from a filename to avoid any injection of
     * script characters etc. when requesting a file using a parameter from the querystring.
     * @param input string to clean
     * @return cleaned string
     */
    public static String cleanFilename(String input) {
    	return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Removes the extension from a filename
     * @param filename
     * @return
     */
    public static String removeFileExtension(String filename) {
    	int idx = filename.lastIndexOf('.');
    	if (idx>0) {
    		return filename.substring(0, idx);
    	}
    	return filename;
    }
}
