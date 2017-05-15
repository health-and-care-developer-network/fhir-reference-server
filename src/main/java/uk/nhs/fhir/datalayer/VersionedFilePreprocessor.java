package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.PropertyReader;

public class VersionedFilePreprocessor {
	
	private static final Logger LOG = Logger.getLogger(VersionedFilePreprocessor.class.getName());
	private static String fileExtension = PropertyReader.getProperty("fileExtension");
	private static ArrayList<String> profileLoadMessages = new ArrayList<String>(); 

	protected static void copyFHIRResourcesIntoVersionedDirectory(ResourceType resourceType) throws IOException {
		
		//profileLoadMessages.clear();
		profileLoadMessages.add("--------------------------------------------------------------------------------------");
		profileLoadMessages.add("Loading " + resourceType + " files from disk: " + DateUtils.printCurrentDateTime());
		
		// Check the versioned directory exists, and create it if not
		String versioned_path = resourceType.getVersionedFilesystemPath();
		FileUtils.forceMkdir(new File(versioned_path));
		
		// Now, look in the root path for this resource type to see if we have any files to process
        ArrayList<ResourceEntity> newFileList = new ArrayList<ResourceEntity>();
        String path = resourceType.getFilesystemPath();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
        
        if (fileList != null) {
	        for (File thisFile : fileList) {
	            if (thisFile.isFile()) {
	                LOG.fine("Pre-processing " + resourceType + " resource from file: " + thisFile.getName());
	                
	                String name = null;
	                String resourceID = null;
	                VersionNumber versionNo = null;
	                
	                try {
		                if (resourceType == STRUCTUREDEFINITION) {
		                	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                    name = profile.getName();
		                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
		                    versionNo = new VersionNumber(profile.getVersion());
		                } else if (resourceType == VALUESET) {
		                	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = profile.getName();
		                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
		                	versionNo = new VersionNumber(profile.getVersion());
		                } else if (resourceType == OPERATIONDEFINITION) {
		                	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = operation.getName();
		                    resourceID = getResourceIDFromURL(operation.getUrl(), name);
		                    versionNo = new VersionNumber(operation.getVersion());
		                } else if (resourceType == IMPLEMENTATIONGUIDE) {
		                	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = guide.getName();
		                    resourceID = getResourceIDFromURL(guide.getUrl(), name);
		                    versionNo = new VersionNumber(guide.getVersion());
		                }
	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " error: " + ex.getMessage() + " - IGNORING");
	                	ex.printStackTrace();
	                }
	                
	                if (!versionNo.isValid()) {
	                	profileLoadMessages.add("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
	                }
	                if (resourceID == null) {
	                	profileLoadMessages.add("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - No resource ID provided in the URL");
	                }
	                
	                // Now, try to build a new versioned filename and copy the file to it
                    if (versionNo.isValid() && resourceID != null) {
                    	String newFilename = resourceID + "-versioned-" + versionNo;
                    	LOG.info("Copying new profile into versioned directory with new filename: " + newFilename);
                    	profileLoadMessages.add("  - Copying new " + resourceType + " into versioned directory with new filename: " + newFilename);
                    	FileUtils.copyFile(thisFile, new File(versioned_path + "/" + newFilename));
                    } else {
                    	LOG.severe("Unable to process file as it is has an invalid ID or version: " + thisFile.getName());
                    }
	            }
	        }
        }
        
        profileLoadMessages.add("Finished loading files from disk.");
        profileLoadMessages.add("--------------------------------------------------------------------------------------");
    }

	public static void clearProfileLoadMessages() {
		profileLoadMessages.clear();
	}
	
	public static String getProfileLoadMessages() {
		String messages = "Messages from profile loader:\n\n";
		for (String message : profileLoadMessages) {
			messages = messages + message + "\n";
		}
		return messages;
	}
}
