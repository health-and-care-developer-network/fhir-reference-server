package uk.nhs.fhir.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirReflectionUtils;
import uk.nhs.fhir.util.FhirRelease;
import uk.nhs.fhir.util.FhirVersion;

public class FhirFileParser {
	private static Logger LOG = LoggerFactory.getLogger(FhirFileParser.class);
	
	private static final FhirVersion DEFAULT_FHIR_VERSION = FhirVersion.DSTU2;

	private final static Set<Class<? extends IBaseResource>> supportedClasses =
		ImmutableSet.of(
			ca.uhn.fhir.model.dstu2.resource.StructureDefinition.class,
			ca.uhn.fhir.model.dstu2.resource.ValueSet.class,
			ca.uhn.fhir.model.dstu2.resource.OperationDefinition.class,
			ca.uhn.fhir.model.dstu2.resource.ConceptMap.class,
			
			org.hl7.fhir.dstu3.model.StructureDefinition.class,
			org.hl7.fhir.dstu3.model.ValueSet.class,
			org.hl7.fhir.dstu3.model.OperationDefinition.class,
			org.hl7.fhir.dstu3.model.ConceptMap.class,
			org.hl7.fhir.dstu3.model.CodeSystem.class,
			org.hl7.fhir.dstu3.model.MessageDefinition.class,
			org.hl7.fhir.dstu3.model.SearchParameter.class,
			org.hl7.fhir.dstu3.model.NamingSystem.class);
	
	public static boolean isSupported(IBaseResource resource) {
		return supportedClasses.contains(resource.getClass());
	}
	
	public IBaseResource parseFile(File thisFile) throws FhirParsingFailedException {
		
		List<FhirVersion> successfullyParsedVersions = Lists.newArrayList();
		
		IBaseResource resource;
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			resource = tryParse(thisFile, version, successfullyParsedVersions);
			if (resource != null) {
				return resource;
			}
		}
		
		if (successfullyParsedVersions.isEmpty()) {
			// No versions succeeded, let's trigger an error if we can and get some idea what went wrong. 
			// We don't know what FHIR version it should have been.
			try {
				parseFile(FhirContexts.xmlParser(FhirVersion.STU3), thisFile);
			} catch (IOException e) {
				throw new FhirParsingFailedException("Parsing failed for file: " + thisFile.getAbsolutePath(), e);
			}

			throw new FhirParsingFailedException("Parsing failed for file: " + thisFile.getAbsolutePath());
		} else if (successfullyParsedVersions.size() == 1) {
			// Couldn't confirm that any was correct. If we only successfully parsed a single version, use that.
			FhirVersion onlyParsableVersion = successfullyParsedVersions.get(0);
			return parseFile(FhirContexts.xmlParser(onlyParsableVersion), thisFile, onlyParsableVersion);
		} else {
			// Multiple FHIR versions succeeded - default to DEFAULT_FHIR_VERSION
			try {
				LOG.info("Successfully parsed {} to multiple versions {}, defaulting to {}", 
					new Object[]{thisFile.getAbsolutePath(), successfullyParsedVersions, DEFAULT_FHIR_VERSION});
				return parseFile(FhirContexts.xmlParser(DEFAULT_FHIR_VERSION), thisFile);
			} catch (IOException e) {
				throw new FhirParsingFailedException("Failed default parsing to " + DEFAULT_FHIR_VERSION.toString() + ": " + thisFile.getAbsolutePath(), e);
			}
		}
	}
	
	private IBaseResource tryParse(File thisFile, FhirVersion versionToTry, List<FhirVersion> successfullyParsedVersions) {
		IParser xmlParser = FhirContexts.xmlParser(versionToTry);
		IBaseResource resource = parseFile(xmlParser, thisFile, versionToTry);
		
		if (resource != null) {
			successfullyParsedVersions.add(versionToTry);

			String className = resource.getClass().getName();
			
			if (!isSupported(resource)) {
				
				IBaseMetaType meta = null;
				switch (versionToTry) {
					case DSTU2:
						ca.uhn.fhir.model.dstu2.resource.BaseResource dstu2Resource = (ca.uhn.fhir.model.dstu2.resource.BaseResource)resource;
						meta = dstu2Resource.getMeta();
						break;
					case STU3:
						org.hl7.fhir.dstu3.model.BaseResource stu3Resource = (org.hl7.fhir.dstu3.model.BaseResource)resource;
						meta = stu3Resource.getMeta();
						break;
					default:
						throw new IllegalStateException("Trying unexpected version " + versionToTry.toString());
				}
				
				if (meta != null) {
					for (IPrimitiveType<String> profile : meta.getProfile()) {
						String url = profile.getValueAsString();
						if (fromResourceUrl(url).equals(versionToTry)) {
							return resource;
						}
					}
					
					StringJoiner profileUrlList = new StringJoiner(", ", "[", "]");
					meta.getProfile().forEach(profile -> profileUrlList.add(profile.getValueAsString()));
					
					LOG.debug("Parsed file {} to type {} (possibly an example resource) version from urls {} didn't match version tried {}", 
							new Object[]{thisFile.toPath(), className, profileUrlList, versionToTry});
					return null;
				} else {
					LOG.info("Successfully parsed file {} for {} but meta wasn't present or didn't have a profile URL. Class={}",
							new Object[]{thisFile.getAbsolutePath(), versionToTry.toString(), className});
					return null;
				}
				
			} else {
				Optional<FhirVersion> selfIdentifiedVersion = getResourceVersion(resource);
				
				if (selfIdentifiedVersion.isPresent()
				  && versionToTry.equals(selfIdentifiedVersion.get())) {
					// Successfully parsed, was a supported class type and matched the version we tried
					return resource;
				} else if (selfIdentifiedVersion.isPresent()) {
					LOG.debug("Parsed file {} to type {} but self identified version {} didn't match version tried {}", 
						new Object[]{thisFile.toPath(), className, selfIdentifiedVersion.get(), versionToTry});
					return null;
				} else {
					LOG.debug("Couldn't identify version for file {} when parsed into class {}", thisFile.toPath(), resource.getClass().getName());
					return null;
				}
			}
		} else {
			// failed to parse the source for this FHIR version
			return null;
		}
	}

	/**
	 * Note this may be used for external resources so we cannot rely on e.g. there being a URL
	 * Assumes that there will be a getUrl() method on all resources (even if it returns null) and will throw if not.
	 */
	private Optional<FhirVersion> getResourceVersion(IBaseResource resource) {
		
		Optional<String> urlByReflection = FhirReflectionUtils.getUrlByReflection(resource, false);
		if (urlByReflection.isPresent()) {
			FhirVersion version = fromResourceUrl(urlByReflection.get());
			return Optional.of(version);
		}
		
		// Naming System doesn't carry Version either in URL or in method, Hence hard coded the version
		if (resource.getClass().getSimpleName().equals("NamingSystem"))
			return Optional.of(FhirVersion.STU3);
		
		// Some external resources might store the version for e.g. StructureDefinitions this way
		Optional<String> fhirReleaseByReflection = FhirReflectionUtils.getFhirReleaseByReflection(resource);
		if (fhirReleaseByReflection.isPresent()) {
			FhirRelease release = FhirRelease.forString(fhirReleaseByReflection.get());
			return Optional.ofNullable(release.getVersion());
		}
		
		return Optional.empty();
	}
	
	/**
	 * Nasty, but the requirement is a hard default of DSTU2 if there is no prefix
	 */
	private FhirVersion fromResourceUrl(String urlString) {
		//e.g. https://fhir.nhs.uk/STU3/StructureDefinition/extension-optoutsource-1
		
		try {
			URL url = new URL(urlString);
			String path = url.getPath();
			
			if (path.startsWith("/STU3")) {
				return FhirVersion.STU3;
			} else {
				return FhirVersion.DSTU2;
			}
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	private IBaseResource parseFile(IParser xmlParser, File thisFile, FhirVersion expectedVersion) {
		try {
			return parseFile(xmlParser, thisFile);
		} catch (IOException | DataFormatException e) {}
		
		return null;
	}
	
	private IBaseResource parseFile(IParser xmlParser, File thisFile) throws FileNotFoundException, IOException {
		String fileContents = FileLoader.loadFile(thisFile);
		return xmlParser.parseResource(new StringReader(fileContents));
	}
}
