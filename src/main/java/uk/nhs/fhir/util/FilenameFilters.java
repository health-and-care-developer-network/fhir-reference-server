package uk.nhs.fhir.util;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilters {
	private static final String EXTENSION_FROM_PROPERTIES = FhirServerProperties.getProperty("fileExtension");
	
	public static final FilenameFilter RESOURCE_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(EXTENSION_FROM_PROPERTIES);
        }
	};
}
