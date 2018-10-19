package uk.nhs.fhir.util;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.skeleton.SkeletonWrappedStu3StructureDefinition;
import uk.nhs.fhir.event.AbstractEventHandler;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FhirParsingFailedException;
import uk.nhs.fhir.load.FileLoader;

public class FhirFileRegistry implements Iterable<Map.Entry<File, WrappedResource<?>>>, StructureDefinitionRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(FhirFileRegistry.class);

	private final FhirFileParser parser = new FhirFileParser();
	
	// useful to work out if an error was caused by another file failing parsing or validation
	private final Set<String> possibleFileNames = Sets.newHashSet();
	
	private final Map<File, WrappedResource<?>> resourcesByFile = Maps.newHashMap();
	private final Map<String, WrappedResource<?>> resourcesByUrl = Maps.newHashMap();
	private final Map<FhirVersion, Map<String, Map<String, WrappedStructureDefinition>>> userDefinedDatatypes = Maps.newHashMap();
	
	private final Map<File, IBaseResource> exampleFhirResources = Maps.newHashMap();
	
	public Map<File, IBaseResource> getUnsupportedFhirResources() {
		return exampleFhirResources;
	}
	
	private final Set<String> cachedPermittedMissingExtensions = Sets.newConcurrentHashSet();

	public boolean isCachedPermittedMissingExtension(String url) {
		return cachedPermittedMissingExtensions.contains(url);
	}
	
	public void addCachedPermittedMissingExtension(String url) {
		cachedPermittedMissingExtensions.add(url);
	}
	
	private final Map<FhirVersion, Map<String, File>> hl7Extensions = Maps.newHashMap();
	private final Map<String, File> localCopiesOfExternalFhirResources = Maps.newHashMap();
	private final Map<FhirVersion, Map<String, IBaseResource>> externalFhirResources = Maps.newHashMap();
	
	public FhirFileRegistry() {
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			externalFhirResources.put(version, Maps.newHashMap());
			hl7Extensions.put(version, Maps.newHashMap());
		}
	}
	
	public void registerMany(List<File> potentialFhirFiles, AbstractEventHandler errorHandler) {
		for (File potentialFhirFile : potentialFhirFiles) {
			try {
				register(potentialFhirFile);
			} catch (Exception e) {
				errorHandler.error(Optional.of("Error adding file " + potentialFhirFile.getAbsolutePath() + " to registry"), Optional.of(e));
			}
		}
	}
	
	public IBaseResource getExternal(FhirVersion version, String resourceUrl) throws ResourceNotAvailableException {
		if (externalFhirResources.get(version).containsKey(resourceUrl)) {
			return externalFhirResources.get(version).get(resourceUrl);
		} else {
			// We don't have a pre-parsed version we can use for this FHIR version
			IParser parser = FhirContexts.xmlParser(version);
			
			String rawResource;
			if (localCopiesOfExternalFhirResources.containsKey(resourceUrl)) {
				// This was found in the local set of resources - now that we know which FHIR version we are dealing with, we can parse it
				rawResource = FileLoader.loadFile(localCopiesOfExternalFhirResources.get(resourceUrl));
			} else if (resourceUrl.startsWith("http://hl7.org/")) {
				// these are all known ahead of time and should be baked into the jar
				rawResource = getHl7Resource(version, resourceUrl);
			} else {
				rawResource = requestExternalResource(resourceUrl);
			}
			
			IBaseResource parsedResource = parser.parseResource(new StringReader(rawResource));
			externalFhirResources.get(version).put(resourceUrl, parsedResource);
			return parsedResource;
		}
	}
	
	private String getHl7Resource(FhirVersion version, String resourceUrl) {
		if (!hl7Extensions.get(version).containsKey(resourceUrl)) {
			throw new IllegalStateException("Couldn't find HL7 resource " + resourceUrl + " in local jar");
		}
		
		File resourceFile = hl7Extensions.get(version).get(resourceUrl);
		return FileLoader.loadFile(resourceFile);
	}

	private String requestExternalResource(String resourceUrl) {
		throw new ResourceNotAvailableException("Need to request resource from external provider: " + resourceUrl);
		/*URL url;
		try {
			url = new URL(resourceUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// probably need to specify format: 
			//"?_format=application/xml+fhir"
			
			int status = connection.getResponseCode();

			StringBuffer responseContent = new StringBuffer();
			
			InputStream connectionInputStream = connection.getInputStream();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connectionInputStream))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					responseContent.append(inputLine);
				}
				in.close();
			}
			connection.disconnect();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}*/
	}

	public Map<File, IBaseResource> getExternalFhirResources() {
		return exampleFhirResources;
	}

	public void register(File xmlFile) {
		IBaseResource parsedFile;
		try {
			parsedFile = parser.parseFile(xmlFile);
		} catch (FhirParsingFailedException e) {
			LOG.error("Skipping file: " + e.getMessage());
			return;
		}
		register(xmlFile, parsedFile);
	}
		
	public void register(File xmlFile, IBaseResource parsedFile) {
		possibleFileNames.add(StringUtil.getTrimmedFileName(xmlFile).toLowerCase(Locale.UK));
		
		if (FhirFileParser.isSupported(parsedFile)) {
			
			WrappedResource<?> wrappedResource = null;
			WrappedResource<?> fullwrappedResource = WrappedResource.fromBaseResource(parsedFile);
			
			if (fullwrappedResource.getResourceType().equals(ResourceType.STRUCTUREDEFINITION)
					&& fullwrappedResource.isStu3()) {
				// Replace this with a skeleton version to reduce memory usage
				LOG.info("Adding a skeleton STU3 StructureDefinition to the registry");
				wrappedResource = new SkeletonWrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)parsedFile, xmlFile);
			} else {
				LOG.info("Adding a FULL " + fullwrappedResource.getResourceType() + " to the registry");
				wrappedResource = fullwrappedResource;
			}
			
			if (wrappedResource.getUrl().isPresent()
			  && FhirURL.startsWithLocalQDomain(wrappedResource.getUrl().get())) {
				
				Optional<String> version = Optional.empty();
				try {
					version = wrappedResource.getVersion();
				} catch (Exception e) {
					EventHandlerContext.forThread().event(RendererEventType.VERSION_NOT_AVAILABLE,
						"Error loading version for " + xmlFile.getAbsolutePath(), Optional.of(e));
				}
				if (!version.isPresent()) {
					EventHandlerContext.forThread().event(RendererEventType.VERSION_NOT_AVAILABLE,
						"Version not present for " + xmlFile.getAbsolutePath());
				}
					
				try {
					// trigger error if we can't get the metadata. This will otherwise prevent it being imported into the server later.
					wrappedResource.getMetadata(xmlFile);
				} catch (Exception e) {
					EventHandlerContext.forThread().event(RendererEventType.METADATA_NOT_AVAILABLE, 
						"Couldn't load metadata for " + xmlFile.getAbsolutePath(), Optional.of(e));
				}
				
				resourcesByFile.put(xmlFile, wrappedResource);
				
				String extractedUrl = wrappedResource.getUrl().get();
				
				if (!resourcesByUrl.containsKey(extractedUrl)) {
					resourcesByUrl.put(extractedUrl, wrappedResource); 
				} else {
					/* We have multiple versions of the same resource which there isn't
					 * an obvious way of dealing with. For now, we'll only do something
					 * with StructureDefinitions. If they an extension we'll check the
					 * type and datatype and if they match we can just ignore other
					 * versions. If they don't however we will have to throw an
					 * exception
					 */
					if (wrappedResource.getResourceType().equals(ResourceType.STRUCTUREDEFINITION)) {
						
						WrappedStructureDefinition newDefinition = (WrappedStructureDefinition)wrappedResource;
						WrappedStructureDefinition cachedDefinition = (WrappedStructureDefinition)resourcesByUrl.get(extractedUrl);
						if (newDefinition.getExtensionType() == null && cachedDefinition.getExtensionType() == null) {
							// Both null types, so we can safely ignore the new version
						} else {
							if (newDefinition.getExtensionType().equals(cachedDefinition.getExtensionType())) {
								// The new version is the same extension type so we can safely ignore it
							} else {
								throw new IllegalStateException("Found multiple StructureDefinitions with URL " + extractedUrl
									 	+ " - they are different extension types so we can't safely determine"
									 	+ " which to use in references from other resources");
							}
						}
						
					} else {
						throw new IllegalStateException("Found multiple resources with URL " + extractedUrl
							 	+ " (can't safely determine which to use in references from other resources)");
					}
				}
				
				if (wrappedResource.getResourceType().equals(ResourceType.STRUCTUREDEFINITION)) {
					WrappedStructureDefinition structureDefinition = (WrappedStructureDefinition)wrappedResource;
					if (structureDefinition.getKind().equals("complex-type") 
					  || structureDefinition.getKind().equals("primitive-type")) {
						if (structureDefinition.getConstrainedType().isPresent()) {
							Map<String, Map<String, WrappedStructureDefinition>> mapForVersion = 
								userDefinedDatatypes.computeIfAbsent(wrappedResource.getImplicitFhirVersion(), v -> Maps.newHashMap());
							Map<String, WrappedStructureDefinition> mapForType = 
								mapForVersion.computeIfAbsent(structureDefinition.getConstrainedType().get(), s -> Maps.newHashMap());
							// should never get duplicates because URL collisions already blow up above
							mapForType.put(structureDefinition.getUrl().get(), structureDefinition);
						} else {
							EventHandlerContext.forThread().event(RendererEventType.USER_TYPE_WITHOUT_CONSTRAINED_TYPE, 
								"StructureDefinition " + wrappedResource.getUrl().get() + " has kind " + structureDefinition.getKind() + " but no constrained type");
						}
					}
				}
				
				return;
			}
			// else continue
		}
		
		if (FhirFileParser.isSupported(parsedFile)) {
			String errorNotes = "";
			Optional<Exception> error = Optional.empty(); 
			
			try {
				Optional<String> url = WrappedResource.fromBaseResource(parsedFile).getUrl();
				if (url.isPresent()) {
					LOG.info("Resource appears to be external: " + url.get() + " - " + xmlFile.getAbsolutePath());
					localCopiesOfExternalFhirResources.put(url.get(), xmlFile);
					return;
				} else {
					errorNotes = " - No url found";
				}
			} catch (Exception e) {
				error = Optional.of(e);
				errorNotes = " - caught Exception while trying to get URL";
			}
			
			String errorMessage = "Don't know what to do with file " + xmlFile.getAbsolutePath() + errorNotes;
			if (error.isPresent()) {
				throw new IllegalStateException(errorMessage, error.get());
			} else {
				throw new IllegalStateException(errorMessage);
			}
		} else if (parsedFile.getMeta() != null
		  && parsedFile.getMeta().getProfile().stream().anyMatch(profile -> FhirURL.startsWithLocalQDomain(profile.getValueAsString()))) {
			exampleFhirResources.put(xmlFile, parsedFile);
		} else {
			LOG.warn("SKIPPING " + xmlFile.getAbsolutePath() + " - not an example and not a supported type");
		}
	}

	public WrappedResource<?> getResource(File f) {
		return resourcesByFile.get(f);
	}

	public List<WrappedCodeSystem> getCodeSystems() {
		return resourcesByFile
			.values()
			.stream()
			.map(file -> file.getWrappedResource())
			.filter(resource -> resource instanceof WrappedCodeSystem)
			.map(resource -> (WrappedCodeSystem)resource)
			.collect(Collectors.toList());
	}
	
	public Optional<WrappedCodeSystem> getCodeSystem(String url) {
		if (resourcesByUrl.containsKey(url)) {
			return Optional.of((WrappedCodeSystem)resourcesByUrl.get(url));
		} else {
			return Optional.empty();
		}
	}

	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(FhirVersion version, String url) throws ResourceNotAvailableException {
		List<WrappedStructureDefinition> matchingDefinitions = 
			resourcesByUrl
				.entrySet()
				.stream()
				.filter(entry -> 
					entry.getValue() instanceof WrappedStructureDefinition
					  && entry.getKey().equalsIgnoreCase(url))
				.map(entry -> (WrappedStructureDefinition)entry.getValue())
				.collect(Collectors.toList());
		
		if (matchingDefinitions.size() == 1) {
			return matchingDefinitions.get(0);
		} else {
			String lastUrlPart = StringUtil.getLastPartOfUrlWithoutExtension(url).toLowerCase(Locale.UK);
			if (FhirURL.startsWithLocalQDomain(url)
			  && possibleFileNames.contains(lastUrlPart)) {
				throw new ResourceNotAvailableException("Cannot find NHS extension " + url + " (did rendering fail for this extension?)");
			}
			
			IBaseResource external = getExternal(version, url);
			return (WrappedStructureDefinition)WrappedStructureDefinition.fromBaseResource(external);
		}
	}

	@Override
	public Iterator<Entry<File, WrappedResource<?>>> iterator() {
		return resourcesByFile.entrySet().iterator();
	}

	public List<WrappedConceptMap> getConceptMapsForSource(String sourceUrl) {
		return resourcesByFile
			.values()
			.stream()
			.filter(resource -> resource instanceof WrappedConceptMap)
			.map(resource -> (WrappedConceptMap)resource)
			.filter(conceptMap -> conceptMapUrlMatches(conceptMap, sourceUrl))
			.collect(Collectors.toList());
	}
	
	private boolean conceptMapUrlMatches(WrappedConceptMap map, String match) {
		return map.getSource().equals(match);
	}
	
	public Optional<WrappedStructureDefinition> getUserDefinedType(String constrainedType, String url, FhirVersion version) {
		if (userDefinedDatatypes.containsKey(version)
		  && userDefinedDatatypes.get(version).containsKey(constrainedType)
		  && userDefinedDatatypes.get(version).get(constrainedType).containsKey(url)) {
			return Optional.of(userDefinedDatatypes.get(version).get(constrainedType).get(url));
		} else {
			return Optional.empty();
		}
	}
}
