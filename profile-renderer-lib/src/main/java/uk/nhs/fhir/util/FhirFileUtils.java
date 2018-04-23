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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class FhirFileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FhirFileUtils.class);
    
    /**
     * @param file File to write data into
     * @param data array of bytes to write to specified file
     * @return true if successful, false otherwise
     */
    public static boolean writeFile(File file, byte[] data) {
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
        
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filename), true))) {
        	for (byte b : data) {
        		bos.write(b);
        	}
            bos.flush();
            success = true;
        } catch (FileNotFoundException ex) {
        	logger.error("File not found {}", filename, ex);
        } catch (IOException ex) {
            logger.error("IOException writing to {}", filename, ex);
        }
        
        return success;
    }
    
    public static Path makeTempDir(String tmpDirName, boolean deleteExisting) throws IOException {
    	if (deleteExisting) {
    		Path tmpDir = getTempDir(tmpDirName);
    		FileUtils.deleteDirectory(tmpDir.toFile());
    	}
    	
    	return makeTempDir(tmpDirName);
    }
    
    private static Path getTempDir(String tmpDirName) {
	    return getSystemTempDir().resolve(tmpDirName);
    }
    
    public static Path getSystemTempDir() {
    	String systemTempDir = System.getProperty("java.io.tmpdir");
	    if (Strings.isNullOrEmpty(systemTempDir)) {
	    	throw new IllegalStateException("No system temp dir available");
	    }
	    
	    return Paths.get(systemTempDir);
    }
    
    private static Path makeTempDir(String tmpDirName) {
		Path tmpDir = getTempDir(tmpDirName);
    	
		if (!tmpDir.toFile().mkdir()) {
			throw new IllegalStateException("Failed to create temp directory at " + tmpDir.toString());
		}
		
		return tmpDir;
    }
    
    public static void deleteRecursive(Path f) {
    	try {
			Files.walkFileTree(f, new SimpleFileVisitor<Path>() {
			   @Override
			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			       Files.delete(file);
			       return FileVisitResult.CONTINUE;
			   }

			   @Override
			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			       Files.delete(dir);
			       return FileVisitResult.CONTINUE;
			   }
			});
		} catch (IOException e) {
			logger.error("Caught exception trying to delete " + f.toString() + ".", e);
		}
    }
}
