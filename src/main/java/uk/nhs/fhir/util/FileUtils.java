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

import java.io.File;
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
}
