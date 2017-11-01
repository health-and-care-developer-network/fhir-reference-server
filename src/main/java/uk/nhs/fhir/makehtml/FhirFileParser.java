package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirRelease;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.FileLoader;

public class FhirFileParser {
	private static Logger LOG = LoggerFactory.getLogger(FhirFileParser.class);

	@SuppressWarnings("unchecked")
	private final static Set<Class<? extends IBaseResource>> supportedClasses =
		Sets.newHashSet(
			ca.uhn.fhir.model.dstu2.resource.StructureDefinition.class,
			org.hl7.fhir.dstu3.model.StructureDefinition.class,
			ca.uhn.fhir.model.dstu2.resource.ValueSet.class,
			org.hl7.fhir.dstu3.model.ValueSet.class,
			ca.uhn.fhir.model.dstu2.resource.OperationDefinition.class,
			org.hl7.fhir.dstu3.model.CodeSystem.class,
			ca.uhn.fhir.model.dstu2.resource.ConceptMap.class,
			org.hl7.fhir.dstu3.model.ConceptMap.class);
	
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
			try {
				parseFile(FhirContexts.xmlParser(FhirVersion.STU3), thisFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			throw new FhirParsingFailedException("Parsing failed for file: " + thisFile.getAbsolutePath());
		}
		
		// Couldn't confirm that any was correct. If we only successfully parsed a single version, use that.
		if (successfullyParsedVersions.size() == 1) {
			FhirVersion onlyParsableVersion = successfullyParsedVersions.get(0);
			return parseFile(FhirContexts.xmlParser(onlyParsableVersion), thisFile, onlyParsableVersion);
		}
		
		// otherwise default to DSTU2
		try {
			return parseFile(FhirContexts.xmlParser(FhirVersion.DSTU2), thisFile);
		} catch (IOException e) {
			throw new FhirParsingFailedException("Failed default parsing to DSTU2: " + thisFile.getAbsolutePath(), e);
		}
	}
	
	private IBaseResource tryParse(File thisFile, FhirVersion versionToTry, List<FhirVersion> successfullyParsedVersions) {
		IParser xmlParser = FhirContexts.xmlParser(versionToTry);
		IBaseResource resource = parseFile(xmlParser, thisFile, versionToTry);
		
		if (resource != null) {
			successfullyParsedVersions.add(versionToTry);
			
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
				}

				String className = resource.getClass().getName();
				LOG.warn("Successfully parsed file " + thisFile.getAbsolutePath() + " for " + versionToTry.toString() 
					+ " but meta wasn't present or didn't have a profile URL. Class=" + className);
				
			} else if (versionToTry.equals(getResourceVersion(resource))) {
				// If the version we found matches the intended version (i.e. the parser version we used) we are done
				return resource;
			}
		}
		
		return null;
	}

	private FhirVersion getResourceVersion(IBaseResource resource) {
		
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			ca.uhn.fhir.model.dstu2.resource.StructureDefinition dstu2StructureDefinition = (ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource;
			
			String url = dstu2StructureDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			String fhirVersion = dstu2StructureDefinition.getFhirVersion();
			if (!Strings.isNullOrEmpty(fhirVersion)) {
				return FhirRelease.forString(fhirVersion).getVersion();
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			
			org.hl7.fhir.dstu3.model.StructureDefinition stu3StructureDefinition = (org.hl7.fhir.dstu3.model.StructureDefinition)resource;
			
			String url = stu3StructureDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			String fhirVersion = stu3StructureDefinition.getFhirVersion();
			if (!Strings.isNullOrEmpty(fhirVersion)) {
				return FhirRelease.forString(fhirVersion).getVersion(); 
			}
			
			return null;
			
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			
			ca.uhn.fhir.model.dstu2.resource.ValueSet dstu2ValueSet = (ca.uhn.fhir.model.dstu2.resource.ValueSet)resource;
			
			String url = dstu2ValueSet.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			
			org.hl7.fhir.dstu3.model.ValueSet stu3ValueSet = (org.hl7.fhir.dstu3.model.ValueSet)resource;
			
			String url = stu3ValueSet.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			
			ca.uhn.fhir.model.dstu2.resource.OperationDefinition dstu2OperationDefinition = (ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource;
			
			String url = dstu2OperationDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
			
			org.hl7.fhir.dstu3.model.CodeSystem stu3CodeSystem = (org.hl7.fhir.dstu3.model.CodeSystem)resource;
			
			String url = stu3CodeSystem.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ConceptMap) {
			ca.uhn.fhir.model.dstu2.resource.ConceptMap dstu2CodeSystem = (ca.uhn.fhir.model.dstu2.resource.ConceptMap)resource;
			
			String url = dstu2CodeSystem.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ConceptMap) {
			org.hl7.fhir.dstu3.model.ConceptMap stu3CodeSystem = (org.hl7.fhir.dstu3.model.ConceptMap)resource;
			
			String url = stu3CodeSystem.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}

			return null;
			
		} else {
			throw new IllegalStateException("Class " + resource.getClass().getCanonicalName() + " is marked as supported, but wasn't handled");
		}
	}
	
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
