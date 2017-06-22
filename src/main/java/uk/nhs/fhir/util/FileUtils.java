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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static void deleteFilesInDir(String directory) {
		ArrayList<String> exclusions = new ArrayList<String>();
		exclusions.add("package-info.java");
		deleteFilesInDir(directory, exclusions);
	}
	
	public static void deleteFilesInDir(String directory, ArrayList<String> exclusions) {
		File dir = new File(directory);
		File files[] = dir.listFiles();
		if (files != null) {
			for(int index = 0; index < files.length; index++) {
				if (exclusions.contains(files[index].getName())) {
					logger.info("Retaining file: {}", files[index].getName());
				} else {
					files[index].delete();
				}
			}
		}
	}
	
	public static void createDirectory(String directory) {
		new File(directory).mkdirs();
	}
	
	public static boolean fileExists(String filename) {
		File f = new File(filename);
		return f.exists();
	}
	
	/**
     * @param filename Filename to write data into
     * @param data array of bytes to write to specified file
     * @return true if successful, false otherwise
     */
    public static boolean writeFile(String filename, byte[] data) {
        return writeFile(new File(filename), data);
    }
    
    /**
     * @param file File to write data into
     * @param data array of bytes to write to specified file
     * @return true if successful, false otherwise
     */
    private static boolean writeFile(File file, byte[] data) {
        boolean success = false;
        try (
        	FileOutputStream fos = new FileOutputStream(file);
        	BufferedOutputStream bos = new BufferedOutputStream(fos);
        	) {
        	
            for (int n=0; n<data.length; n++) {
                bos.write(data[n]);
            }
            
            bos.flush();
            success = true;
        } catch (FileNotFoundException ex) {
        	logger.error("File not found {}", file.getAbsolutePath(), ex);
        } catch (IOException ex) {
        	logger.error("IOException writing to {}", file.getAbsolutePath(), ex);
        }
        
        return success;
    }
    
    /**
     * @param filename Filename to write data into - content will be added to the end of the file
     * @param data array of bytes to write to specified file
     * @return true if successful, false otherwise
     */
    public static boolean appendToFile(String filename, byte[] data) {
        boolean success = false;
        
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(new File(filename), true);
            bos = new BufferedOutputStream(fos);
            for (int n=0; n<data.length; n++) {
                bos.write(data[n]);
            }
            bos.flush();
            success = true;
        } catch (FileNotFoundException ex) {
        	logger.error("File not found {}", filename, ex);
        } catch (IOException ex) {
            logger.error("IOException writing to {}", filename, ex);
        } finally {
            try { if (bos != null) bos.close(); } catch (IOException ex) {}
            try { if (fos != null) fos.close(); } catch (IOException ex) {}
        }
        
        return success;
    }
}
