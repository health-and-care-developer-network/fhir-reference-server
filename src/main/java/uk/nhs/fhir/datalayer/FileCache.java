package uk.nhs.fhir.datalayer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Holds an in-memory cache of the profiles from the filesystem, as well as the list of profile names.
 * @author Adam Hatherly
 */
public class FileCache {
	private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

	// Singleton object to act as a cache of the files in the profiles directory
	private static List<String> profileFileList = null;
	private static List<StructureDefinition> profileList = null;
	
	private static long lastUpdated = 0;
	private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));
	
	private static String profilePath = PropertyReader.getProperty("profilePath");
	private static String fileExtension = PropertyReader.getProperty("fileExtension");

	
	public static List<StructureDefinition> getProfiles() {
		if (updateRequired()) {
			updateCache();
		}
		return profileList;
	}
	
	public static List<String> getNameList() {
		if (updateRequired()) {
			updateCache();
		}
		return profileFileList;
	}
		
	private static boolean updateRequired() {
		long currentTime = System.currentTimeMillis();
		if (profileList == null || (currentTime > (lastUpdated + updateInterval))) {
			LOG.info("Cache needs updating");
			return true;
		}
		LOG.info("Using Cache");
		return false;
	}
	
	private synchronized static void updateCache() {
		if (updateRequired()) {
			lastUpdated = System.currentTimeMillis();
			LOG.info("Updating cache from fliesystem");
			ArrayList<String> newFileList = new ArrayList<String>();
			ArrayList<StructureDefinition> newProfileList = new ArrayList<StructureDefinition>();
			File folder = new File(profilePath);
			File[] files = folder.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(fileExtension);
			    }
			});
	
	        for (int i = 0; i < files.length; i++) {
		        if (files[i].isFile()) {
		            LOG.info("Reading profile file into cache: " + files[i].getName());
		            
		            // Add it to the name list
		            String name = files[i].getName();
		            newFileList.add(FileLoader.removeFileExtension(name));
		            
		            // Add the profile itself
		            StructureDefinition profile = FHIRUtils.loadProfileFromFile(files[i]);
		            newProfileList.add(profile);
		        }
		        /*else if (files[i].isDirectory()) {
		            System.out.println("Directory " + files[i].getName());
		        }*/
	        }
	        
	        profileList = newProfileList;
	        profileFileList = newFileList;
		}
	}
}
