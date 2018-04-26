package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.load.XmlFileFinder;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FhirVersion;

public class VersionedFilePreprocessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersionedFilePreprocessor.class.getName());

	private static final XmlFileFinder resourceFileFinder = new XmlFileFinder();
	private static final FhirFileParser parser = new FhirFileParser();

	private AbstractFhirFileLocator fhirFileLocator;  
	
	public VersionedFilePreprocessor(AbstractFhirFileLocator versionedFileLocator) {
		this.fhirFileLocator = versionedFileLocator;
	}

	public void setFhirFileLocator(AbstractFhirFileLocator versionedFileLocator) {
		this.fhirFileLocator = versionedFileLocator;
	}

	protected void copyFHIRResourcesIntoVersionedDirectory(FhirVersion fhirVersion, ResourceType resourceType) throws IOException {
		logStart(fhirVersion, resourceType);
		
		String outputDirectory = ensureVersionedFolderExists(fhirVersion, resourceType);
		
        String resourcePath = fhirFileLocator.getSourcePathForResourceType(resourceType, fhirVersion).toString();
        
        String logMessage = "Copying resources from root " + resourcePath + " to versioned directory " + outputDirectory; 
        LOG.info(logMessage);
        addMessage(logMessage);
        
		List<File> fileList = resourceFileFinder.findFiles(resourcePath);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.debug("Pre-processing " + resourceType + " resource from file: " + thisFile.getName());
                
                String resourceID = null;
                VersionNumber versionNo = null; 
                		
                try {
                	ResourceMetadata newEntity = WrappedResource.fromBaseResource(parser.parseFile(thisFile)).getMetadata(thisFile);
                	resourceID = newEntity.getResourceID();
                	versionNo = newEntity.getVersionNo();
                	
                } catch (Exception ex) {
                	LOG.error("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " error: " + ex.getMessage() + " - IGNORING");
                	ex.printStackTrace();
                }
                
                if (versionNo == null) {
                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
                	LOG.error("Unable to process file as it is has an invalid version: " + thisFile.getName());
                } else if (resourceID == null) {
                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - No resource ID provided in the URL");
                	LOG.error("Unable to process file as it is has an invalid resource ID: " + thisFile.getName());
                } else {
	                // Now, try to build a new versioned filename and copy the file to it
                	String newFilename = resourceID + "-versioned-" + versionNo + ".xml";
                	
                	LOG.debug("Copying new profile into versioned directory with new filename: " + newFilename);
                	addMessage("  - Copying new " + resourceType + " into versioned directory with new filename: " + newFilename);
                	File newFile = new File(outputDirectory + File.separator + newFilename);
                	FileUtils.copyFile(thisFile, newFile);
                	
                	// And also copy other resources (diffs, details, bindings, etc).
                	copyOtherResources(thisFile.toPath(), newFile.toPath());
                }
            }
        }
        
        addMessage("Finished pre-processing " + resourceType + " files from disk.");
        addMessage("--------------------------------------------------------------------------------------");
    }

	private void logStart(FhirVersion fhirVersion, ResourceType resourceType) {
		//profileLoadMessages.clear();
		LOG.debug("Starting pre-processor to convert files into versioned files prior to loading into the server for " + fhirVersion);
		addMessage("--------------------------------------------------------------------------------------");
		addMessage("Loading " + resourceType + " files from disk: " + DateUtils.printCurrentDateTime());
	}

	private String ensureVersionedFolderExists(FhirVersion fhirVersion, ResourceType resourceType) throws IOException {
		String versionedPath = fhirFileLocator.getDestinationPathForResourceType(resourceType, fhirVersion).toString();
		FileUtils.forceMkdir(new File(versionedPath));
		return versionedPath;
	}
	
	/**
	 * If the FHIR resource also has other generated resources (e.g. details view, diff, bindings, etc.) then also
	 * copy those into the relevant versioned directory along with our resource
	 * 
	 * @param source Original filename
	 * @param dest New filename of resource
	 * @throws IOException 
	 */
	protected void copyOtherResources(Path source, Path dest) throws IOException {
		source = Preconditions.checkNotNull(source);
		
		Path oldDir = source.getParent();
		String oldName = FileLoader.removeFileExtension(source.getFileName().toString());
		File sourceDir = oldDir.resolve(oldName).toFile();
		
		Path newDir = dest.getParent();
		String newName = FileLoader.removeFileExtension(dest.getFileName().toString());
		File targetDir = newDir.resolve(newName).toFile();
		
		if (sourceDir.isDirectory()) { 
			// Create target dir
			FileUtils.forceMkdir(sourceDir);
			
			// Now, loop through and copy any files into the target directory
            File[] fileList = sourceDir.listFiles();
            if (fileList != null) {
    	        for (File thisFile : fileList) {
    	        	FileUtils.copyFileToDirectory(thisFile, targetDir);
    	        }
            }
		}
	}

}
