package uk.nhs.fhir.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.datalayer.FhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.FileLoader;

public class VersionedFolderImportWriter implements ImportListener {

	private static final Logger LOG = Logger.getLogger(VersionedFolderImportWriter.class.getName());
	
	private static final String versionedFolder = "versioned";
	
	private final FhirFileLocator fhirImportDestination;
	
	public VersionedFolderImportWriter(FhirFileLocator fhirImportDestination) {
		this.fhirImportDestination = fhirImportDestination;
	}
	
	@Override
	public void doImport(File sourceFile, WrappedResource<?> resource) {
		Path outputDirectory = ensureVersionedFolderExists(resource);
		ResourceMetadata metadata = resource.getMetadata(sourceFile);
		VersionNumber versionNo = metadata.getVersionNo();
		if (versionNo != null 
		  && versionNo.isValid()) {
			File newFile = outputDirectory.resolve(metadata.getVersionedFileName()).toFile();
        	
			try {
				FileUtils.copyFile(sourceFile, newFile);
	        	// Copy other resources (diffs, details, bindings, etc).
	        	copyOtherResources(sourceFile, newFile);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to copy files to versioned folder", e);
			}
		} else {
			LOG.severe("Failed to import file " + sourceFile.getPath() + " due to invalid version " + versionNo);
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
		String resourceFolderName = resource.getOutputFolderName();
		return fhirImportDestination.getRoot(fhirVersion).resolve(resourceFolderName).resolve(versionedFolder);
	}
	
	private void copyOtherResources(File oldFile, File newFile) {
		
		String oldDir = oldFile.getParent();
		String oldName = FileLoader.removeFileExtension(oldFile.getName());
		File resourceDir = new File(oldDir + "/" + oldName);
		
		String newDir = newFile.getParent();
		String newName = FileLoader.removeFileExtension(newFile.getName());
		File targetDir = new File(newDir + "/" + newName);
		
		if(resourceDir.exists()
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
				LOG.severe("Unable to copy supporting resources!");
				e.printStackTrace();
			}
		}
	}

}
