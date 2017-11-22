package uk.nhs.fhir.datalayer.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.nhs.fhir.util.FilenameFilters;

public class ResourceFileFinder {
	public List<File> findFiles(String path) {
		return findFiles(new File(path));
	}

	public List<File> findFiles(File directory) {
        File[] fileList = directory.listFiles(FilenameFilters.RESOURCE_FILE_FILTER);
        
        if (fileList == null) {
        	return new ArrayList<>();
        } else {
        	return Arrays.asList(fileList);
        }
	}
}
