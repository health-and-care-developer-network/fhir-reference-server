package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
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
import uk.nhs.fhir.FhirURLConstants;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.error.EventHandler;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.StringUtil;

public class FhirFileRegistry implements Iterable<Map.Entry<File, WrappedResource<?>>>, StructureDefinitionRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(FhirFileRegistry.class);

	private final FhirFileParser parser = new FhirFileParser();
	
	// useful to work out if an error was caused by another file failing parsing or validation
	private final Set<String> possibleFileNames = Sets.newHashSet();
	
	private final Map<File, WrappedResource<?>> resourcesByFile = Maps.newHashMap();
	private final Map<String, WrappedResource<?>> resourcesByUrl = Maps.newHashMap();
	
	private final Map<File, IBaseResource> exampleFhirResources = Maps.newHashMap();
	
	public Map<File, IBaseResource> getUnsupportedFhirResources() {
		return exampleFhirResources;
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
	
	public void registerMany(List<File> potentialFhirFiles, EventHandler errorHandler) {
		for (File potentialFhirFile : potentialFhirFiles) {
			try {
				register(potentialFhirFile);
			} catch (Exception e) {
				errorHandler.error(Optional.of("Error adding file " + potentialFhirFile.getAbsolutePath() + " to registry"), Optional.of(e));
			}
		}
	}
	
	public IBaseResource getExternal(FhirVersion version, String resourceUrl) {
		if (externalFhirResources.get(version).containsKey(resourceUrl)) {
			return externalFhirResources.get(version).get(resourceUrl);
		} else {
			// We don't have a pre-parsed version we can use for this FHIR version
			IParser parser = FhirContexts.xmlParser(version);
			
			String rawResource;
			if (localCopiesOfExternalFhirResources.containsKey(resourceUrl)) {
				// This was found in the local set of resources - now that we know which FHIR version we are dealing with, we can parse it
				try {
					byte[] fileBytes = Files.readAllBytes(localCopiesOfExternalFhirResources.get(resourceUrl).toPath());
					rawResource = new String(fileBytes, FileLoader.DEFAULT_ENCODING);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
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
		
		try {
			byte[] fileBytes = Files.readAllBytes(resourceFile.toPath());
			return new String(fileBytes, FileLoader.DEFAULT_ENCODING);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String requestExternalResource(String resourceUrl) {
		throw new IllegalStateException("Need to request resource from external provider: " + resourceUrl);
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
		possibleFileNames.add(StringUtil.getTrimmedFileName(xmlFile).toLowerCase());
		
		if (FhirFileParser.isSupported(parsedFile)) {
			WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
			
			if (wrappedResource.getUrl().isPresent()
			  && FhirURLConstants.isNhsResourceUrl(wrappedResource.getUrl().get())) {
				
				Optional<String> version = Optional.empty();
				try {
					version = wrappedResource.getVersion();
				} catch (Exception e) {
					RendererEventConfig.handle(RendererError.VERSION_NOT_AVAILABLE, "Error loading version for " + xmlFile.getAbsolutePath(), Optional.of(e));
				}
				if (!version.isPresent()) {
					RendererEventConfig.handle(RendererError.VERSION_NOT_AVAILABLE, "Version not present for " + xmlFile.getAbsolutePath());
				}
					
				try {
					// trigger error if we can't get the metadata. This will otherwise prevent it being imported into the server later.
					wrappedResource.getMetadata(xmlFile);
				} catch (Exception e) {
					RendererEventConfig.handle(RendererError.METADATA_NOT_AVAILABLE, "Couldn't load metadata for " + xmlFile.getAbsolutePath(), Optional.of(e));
				}
				
				resourcesByFile.put(xmlFile, wrappedResource);
				
				String extractedUrl = wrappedResource.getUrl().get();
				
				if (!resourcesByUrl.containsKey(extractedUrl)) {
					resourcesByUrl.put(extractedUrl, wrappedResource); 
				} else {
					throw new IllegalStateException("Found multiple resources with URL " + extractedUrl
						+ " (need to add support for multiple versions of same resource)");
				}
				return;
			}
		}
		
		if (parsedFile.getMeta() != null
		  && parsedFile.getMeta().getProfile().stream().anyMatch(profile -> FhirURLConstants.isNhsResourceUrl(profile.getValueAsString()))) {
			exampleFhirResources.put(xmlFile, parsedFile);
		} else if (FhirFileParser.isSupported(parsedFile)) {
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
	
	public WrappedCodeSystem getCodeSystem(String url) {
		if (resourcesByUrl.containsKey(url)) {
			return (WrappedCodeSystem)resourcesByUrl.get(url);
		} else {
			throw new IllegalStateException("Couldn't find code system by url [" + url + "]");
		}
	}

	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(FhirVersion version, String url) {
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
			String lastUrlPart = StringUtil.getLastPartOfUrlWithoutExtension(url).toLowerCase();
			if (FhirURLConstants.isNhsResourceUrl(url)
			  && possibleFileNames.contains(lastUrlPart)) {
				throw new IllegalStateException("Cannot find NHS extension " + url + " (did rendering fail for this extension?)");
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
}
