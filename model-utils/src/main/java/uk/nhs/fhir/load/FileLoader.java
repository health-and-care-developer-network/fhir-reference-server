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
package uk.nhs.fhir.load;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringJoiner;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Convenience class to load a file from a given filename and return the
 * content as a string.
 * @author Adam Hatherly
 */
public class FileLoader {
	private static final Logger LOG = LoggerFactory.getLogger(FileLoader.class.getName());
	
	public static final String DEFAULT_ENCODING = "UTF-8";
    
	/**
     * @param file File to load content from
     * @return String containing content of specified file
     */
    public static String loadFile(final File file) {
    	LOG.debug("Loading file: " + file.getAbsolutePath());
        
        String charsetName;
		try {
			charsetName = getCharset(file);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to determine appropriate charset for file " + file.getName(), e);
		}
		
	    LOG.debug("Loading file using encoding: " + charsetName);
	    
		try (
			ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
			InputStream fis = new FileInputStream(file);
			InputStream bomInputStream = new BOMInputStream(fis);
			Reader streamReader = new InputStreamReader(bomInputStream, charsetName);
			BufferedReader in = new BufferedReader(streamReader);) {
			
			StringJoiner sj = new StringJoiner("\n");
			
			String str;
			while ((str = in.readLine()) != null) {
			    sj.add(str);
			}
			
			return sj.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Error reading file: " + file.getName(), e);
		}
    }

	/**
     * Return an appropriate charset which handles BOMs if necessary
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    private static String getCharset(File file) throws IOException {
    	try (BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(file))) {
    		
    		// Use commons.io to deal with byte-order-marker if present
			ByteOrderMark bom = bomInputStream.getBOM();
			
			if (bom != null) {
				return bom.getCharsetName();
			} else {
				return DEFAULT_ENCODING;
			}
		}
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
