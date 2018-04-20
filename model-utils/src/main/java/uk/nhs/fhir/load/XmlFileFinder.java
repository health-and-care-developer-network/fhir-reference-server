package uk.nhs.fhir.load;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

public class XmlFileFinder {
	private static final String XML_EXTENSION = ".xml";
	
	public static final FilenameFilter XML_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
            return name.toLowerCase(Locale.UK).endsWith(XML_EXTENSION);
        }
	};
    
	private static final FileFilter directoryFileFilter = new FileFilter() {
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
        	return Lists.newArrayList();
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
		if (!searchDirectory.isDirectory()) {
			throw new IllegalStateException("Trying to search " + searchDirectory.getPath() + " which is not a directory");
		}
		
		File[] localXmlFiles = searchDirectory.listFiles(XML_FILE_FILTER);
		if (localXmlFiles != null) {
			for (File xmlFile : localXmlFiles) {
	        	xmlFiles.add(xmlFile);
	        }
		} else {
			throw new IllegalStateException("IOException finding XML files within " + searchDirectory.getPath());
		}
		
		File[] childDirectories = searchDirectory.listFiles(directoryFileFilter);
		if (childDirectories != null) {
			for (File childDirectory : childDirectories) {
				findXmlFilesRecursive(xmlFiles, childDirectory);
			}
		} else {
			throw new IllegalStateException("IOException finding subdirectories within " + searchDirectory.getPath());
		}
	}

}
