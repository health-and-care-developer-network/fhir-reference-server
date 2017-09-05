package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

import com.google.common.collect.Lists;

public class XmlFileFinder {

    private static final String EXTENSION = ".xml";
	private final FilenameFilter xmlFileFilter = new FilenameFilter() {
        @Override public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(EXTENSION);
        }
    };
    
	private final FileFilter directoryFileFilter = new FileFilter() {
		@Override public boolean accept(File f) {
			return f.isDirectory();
		}
    };
    
	private final File searchRoot;

	public XmlFileFinder(File searchRoot) {
		this.searchRoot = searchRoot;
	}

	public List<File> findFiles() {
		List<File> xmlFiles = Lists.newArrayList();
		
		findXmlFilesRecursive(xmlFiles, searchRoot);
		
		return xmlFiles;
	}

	private void findXmlFilesRecursive(List<File> xmlFiles, File searchDirectory) {
		for (File xmlFile : searchDirectory.listFiles(xmlFileFilter)) {
        	xmlFiles.add(xmlFile);
        }
		
		for (File childDirectory : searchDirectory.listFiles(directoryFileFilter)) {
			findXmlFilesRecursive(xmlFiles, childDirectory);
		}
	}

}
