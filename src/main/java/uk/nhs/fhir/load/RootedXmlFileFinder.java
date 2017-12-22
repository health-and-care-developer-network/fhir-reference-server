package uk.nhs.fhir.load;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class RootedXmlFileFinder {
    
	private final Path searchRoot;
	private final XmlFileFinder finder = new XmlFileFinder();

	public RootedXmlFileFinder(Path searchRoot) {
		this.searchRoot = searchRoot;
	}

	public List<File> findFilesRecursively() {
		return finder.findFilesRecursively(searchRoot);
	}
	
}
