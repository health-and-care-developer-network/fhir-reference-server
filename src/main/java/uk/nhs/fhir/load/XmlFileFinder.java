package uk.nhs.fhir.load;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class XmlFileFinder {
	private static final String XML_EXTENSION = ".xml";
	
	public static final FilenameFilter XML_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(XML_EXTENSION);
        }
	};
    
	private final FileFilter directoryFileFilter = new FileFilter() {
		@Override public boolean accept(File f) {
			return f.isDirectory();
		}
    };
	
	public List<File> findFiles(String path) {
		return findFiles(new File(path));
	}

	public List<File> findFiles(File directory) {
        File[] fileList = directory.listFiles(XML_FILE_FILTER);
        
        if (fileList == null) {
        	return new ArrayList<>();
        } else {
        	return Arrays.asList(fileList);
        }
	}

	public List<File> findFilesRecursively(Path searchRoot) {
		List<File> xmlFiles = Lists.newArrayList();
		
		findXmlFilesRecursive(xmlFiles, searchRoot.toFile());
		
		return xmlFiles;
	}

	private void findXmlFilesRecursive(List<File> xmlFiles, File searchDirectory) {
		for (File xmlFile : searchDirectory.listFiles(XML_FILE_FILTER)) {
        	xmlFiles.add(xmlFile);
        }
		
		for (File childDirectory : searchDirectory.listFiles(directoryFileFilter)) {
			findXmlFilesRecursive(xmlFiles, childDirectory);
		}
	}

}
