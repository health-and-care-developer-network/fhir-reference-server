package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Strings;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.data.FhirRelease;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.util.HAPIUtils;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
	public void processFile(String outPath, String newBaseURL, File folder, File thisFile) throws Exception {
		if (thisFile.isFile()) {
			
		    String inFilePath = thisFile.getPath();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");

		    IBaseResource resource = parseFile(thisFile);
		    
			WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(resource);

		    wrappedResource.saveAugmentedResource(thisFile, wrappedResource, outPath, newBaseURL);
		    wrappedResource.saveFormattedOutputs(thisFile, outPath, newBaseURL);
		}
	}

	/*
	 * We can't inspect a resource's reported version until it has been parsed, but we can't guarantee that parsing will succeed.
	 * Also, some resources may not report their own version.
	 * 
	 * TODO support parsing the file name to calculate expected version.
	 */
	IBaseResource parseFile(File thisFile) throws IOException {
		IBaseResource resource;
		
		resource = parseFile(HAPIUtils.xmlParser(FhirVersion.STU3), thisFile, FhirVersion.STU3);
		
		//return resource;
		
		if (resource != null
		  && getStu3ResourceVersion(resource).equals(FhirVersion.STU3)) {
			return resource;
		}
		
		resource = parseFile(HAPIUtils.xmlParser(FhirVersion.DSTU2), thisFile, FhirVersion.DSTU2);
		if (resource != null
		  && getDstu2ResourceVersion(resource).equals(FhirVersion.DSTU2)) {
			return resource;
		}
		
		throw new IllegalStateException("Couldn't work out appropriate FHIR version for " + thisFile.getAbsolutePath());
	}

	private FhirVersion getStu3ResourceVersion(IBaseResource resource) {
		Optional<FhirVersion> version = getFhirVersionFromMetaInfo(resource);
		if (version.isPresent()) {
			return version.get();
		}
		
		if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return FhirRelease.forString(((org.hl7.fhir.dstu3.model.StructureDefinition)resource).getFhirVersion()).getVersion();
		} else {
			throw new IllegalStateException("Need to support STU3 class " + resource.getClass().getCanonicalName());
		}
	}

	private FhirVersion getDstu2ResourceVersion(IBaseResource resource) {
		Optional<FhirVersion> version = getFhirVersionFromMetaInfo(resource);
		if (version.isPresent()) {
			return version.get();
		}
		
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return FhirRelease.forString(((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource).getFhirVersion()).getVersion();
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			return FhirVersion.DSTU2;
		} else {
			throw new IllegalStateException("Need to support DSTU2 class " + resource.getClass().getCanonicalName());
		}
	}
	
	private Optional<FhirVersion> getFhirVersionFromMetaInfo(IBaseResource resource) {
		IBaseMetaType meta = resource.getMeta();
		if (meta != null) {
			String versionId = meta.getVersionId();
			if (!Strings.isNullOrEmpty(versionId)) {
				FhirRelease release = FhirRelease.forString(versionId);
				return Optional.of(release.getVersion());
			}
		}
		
		return Optional.empty();
	}

	private IBaseResource parseFile(IParser xmlParser, File thisFile, FhirVersion expectedVersion) {
		try (FileReader fr = new FileReader(thisFile)) {
			return xmlParser.parseResource(fr);
		} catch (IOException | DataFormatException e) {}
		
		return null;
	}
}
