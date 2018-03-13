package uk.nhs.fhir.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;

public class VersionedFolderImportWriter implements ImportListener {

	private static final Logger LOG = LoggerFactory.getLogger(VersionedFolderImportWriter.class.getName());
	
	private final AbstractFhirFileLocator fhirFileLocator;
	
	public VersionedFolderImportWriter(AbstractFhirFileLocator fhirImportDestination) {
		this.fhirFileLocator = fhirImportDestination;
	}
	
	@Override
	public void doImport(File sourceFile, WrappedResource<?> resource) {
		Path outputDirectory = ensureVersionedFolderExists(resource);
		ResourceMetadata metadata = resource.getMetadata(sourceFile);
		VersionNumber versionNo = metadata.getVersionNo();
		if (versionNo != null) {
			File newFile = outputDirectory.resolve(metadata.getVersionedFileName()).toFile();
        	
			try {
				FileUtils.copyFile(sourceFile, newFile);
	        	// Copy other resources (diffs, details, bindings, etc).
	        	copyOtherResources(sourceFile, newFile);
			} catch (IOException e) {
				LOG.error("Failed to copy files to versioned folder", e);
			}
		} else {
			LOG.error("Failed to import file " + sourceFile.getPath() + " due to null version");
		}
	}

	private Path ensureVersionedFolderExists(WrappedResource<?> resource) {
		try {
			Path versionedPath = getVersionedFilesystemPath(resource);
			FileUtils.forceMkdir(versionedPath.toFile());
			return versionedPath;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private Path getVersionedFilesystemPath(WrappedResource<?> resource) {
		FhirVersion fhirVersion = resource.getImplicitFhirVersion();
		ResourceType resourceType = resource.getResourceType();
		return fhirFileLocator.getDestinationPathForResourceType(resourceType, fhirVersion);
	}
	
	private void copyOtherResources(File oldFile, File newFile) {
		
		String oldDir = oldFile.getParent();
		String oldName = FileLoader.removeFileExtension(oldFile.getName());
		File resourceDir = new File(oldDir + File.separator + oldName);
		
		String newDir = newFile.getParent();
		String newName = FileLoader.removeFileExtension(newFile.getName());
		File targetDir = new File(newDir + File.separator + newName);
		
		if (resourceDir.exists()
		  && resourceDir.isDirectory()) { 
			try {
				FileUtils.forceMkdir(targetDir);
				
				// Copy contents into the target directory
	            File[] fileList = resourceDir.listFiles();
	            if (fileList != null) {
	    	        for (File thisFile : fileList) {
	    	        	FileUtils.copyFileToDirectory(thisFile, targetDir);
	    	        }
	            }
			} catch (IOException e) {
				LOG.error("Unable to copy supporting resources!");
				e.printStackTrace();
			}
		}
	}

}
