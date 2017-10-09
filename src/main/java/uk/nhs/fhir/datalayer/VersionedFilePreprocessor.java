package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FileLoader;

public class VersionedFilePreprocessor {
	
	private static final Logger LOG = Logger.getLogger(VersionedFilePreprocessor.class.getName());
	private static String fileExtension = FhirServerProperties.getProperty("fileExtension");
	

	protected static void copyFHIRResourcesIntoVersionedDirectory(FHIRVersion fhirVersion, ResourceType resourceType) throws IOException {
		
		//profileLoadMessages.clear();
		LOG.fine("Starting pre-processor to convert files into versioned files prior to loading into the server for " + fhirVersion);
		addMessage("--------------------------------------------------------------------------------------");
		addMessage("Loading " + resourceType + " files from disk: " + DateUtils.printCurrentDateTime());
		
		// Check the versioned directory exists, and create it if not
		String versioned_path = resourceType.getVersionedFilesystemPath(fhirVersion);
		FileUtils.forceMkdir(new File(versioned_path));
		
		// Now, look in the root path for this resource type to see if we have any files to process
        String path = resourceType.getFilesystemPath(fhirVersion);
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
	                
	                String resourceID = null;
	                VersionNumber versionNo = null; 
	                		
	                try {
	                	
	                	IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
	                	ResourceEntity newEntity = helper.getMetadataFromResource(thisFile);
	                	resourceID = newEntity.getResourceID();
	                	versionNo = newEntity.getVersionNo();
	                	
	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " error: " + ex.getMessage() + " - IGNORING");
	                	ex.printStackTrace();
	                }
	                
	                if (versionNo == null) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
	                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
	                } else if (!versionNo.isValid()) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
	                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
	                } else if (resourceID == null) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - No resource ID provided in the URL");
	                	LOG.severe("Unable to process file as it is has an invalid resource ID: " + thisFile.getName());
	                } else {
		                // Now, try to build a new versioned filename and copy the file to it
                    	String newFilename = resourceID + "-versioned-" + versionNo + ".xml";
                    	
                    	LOG.fine("Copying new profile into versioned directory with new filename: " + newFilename);
                    	addMessage("  - Copying new " + resourceType + " into versioned directory with new filename: " + newFilename);
                    	File newFile = new File(versioned_path + "/" + newFilename);
                    	FileUtils.copyFile(thisFile, newFile);
                    	
                    	// And also copy other resources (diffs, details, bindings, etc).
                    	copyOtherResources(thisFile, newFile);
	                }
	            }
	        }
        }
        
        addMessage("Finished pre-processing " + resourceType + " files from disk.");
        addMessage("--------------------------------------------------------------------------------------");
    }
	
	/**
	 * If the FHIR resoutce also has other generated resources (e.g. details view, diff, bindings, etc.) then also
	 * copy those into the relevant versioned directory along with our resource
	 * 
	 * @param oldFile Original filename
	 * @param newFile New filename of resource
	 */
	protected static void copyOtherResources(File oldFile, File newFile) {
		
		String oldDir = oldFile.getParent();
		String oldName = FileLoader.removeFileExtension(oldFile.getName());
		File resourceDir = new File(oldDir + "/" + oldName);
		//System.out.println(resourceDir.getAbsolutePath());
		
		String newDir = newFile.getParent();
		String newName = FileLoader.removeFileExtension(newFile.getName());
		File targetDir = new File(newDir + "/" + newName);
		//System.out.println(targetDir.getAbsolutePath());
		
		if(resourceDir.exists() && resourceDir.isDirectory()) { 
			try {
				// Create target dir
				FileUtils.forceMkdir(targetDir);
				
				// Now, loop through and copy any files into the target directory
	            File[] fileList = resourceDir.listFiles();
	            if (fileList != null) {
	    	        for (File thisFile : fileList) {
	    	        	FileUtils.copyFileToDirectory(thisFile, targetDir);
	    	        }
	            }
			} catch (IOException e) {
				LOG.severe("Unable to copy supporting resources!");
				e.printStackTrace();
			}
		}
	}

}
