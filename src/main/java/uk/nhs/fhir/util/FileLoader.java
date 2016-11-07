/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to load a file from a given filename and return the
 * content as a string.
 * @author Adam Hatherly
 */
public class FileLoader {
	private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);
	
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
        String content = null;
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
            int c = -1;
            while ((c = fr.read()) > -1) {
                bOutStream.write(c);
            }
            content = bOutStream.toString();
        } catch (IOException ex) {
            logger.error("Error loading file", ex);
        } finally {
            try {
                if (fr != null) fr.close();
            } catch (IOException ex) { }
        }
        return content;
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
            logger.error("Error loading file", ex);
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
    			logger.error("Unable to load file from classpath: {}", fileName);
    			return null;
    		}
    }
    
    /**
     * @param fileName Name of file on the classpath to load
     * @return String containing content of specified file
     */
    public static InputStream loadFileOnClasspathAsStream(String fileName) {
    		URL resource = FileLoader.class.getResource(fileName);
    		if (resource != null) {
    			return FileLoader.class.getResourceAsStream(fileName);
    		} else {
    			logger.error("Unable to load file from classpath: {}", fileName);
    			return null;
    		}
    }
}
