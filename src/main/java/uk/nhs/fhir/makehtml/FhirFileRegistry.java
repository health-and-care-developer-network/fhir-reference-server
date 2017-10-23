package uk.nhs.fhir.makehtml;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;

public class FhirFileRegistry implements Iterable<Map.Entry<File, WrappedResource<?>>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FhirFileRegistry.class);

	private final FhirFileParser parser = new FhirFileParser();
	
	private final Map<File, WrappedResource<?>> resourcesByFile = Maps.newHashMap();
	private final Map<String, WrappedResource<?>> resourcesByUrl = Maps.newHashMap();
	
	public FhirFileRegistry(List<File> potentialFhirFiles) {
		potentialFhirFiles.forEach(file -> register(file));
	}

	private void register(File xmlFile) {
		try {
			IBaseResource parsedFile = parser.parseFile(xmlFile);
			
			if (FhirFileParser.isSupported(parsedFile)) {
				WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
				
				resourcesByFile.put(xmlFile, wrappedResource);
				
				if (wrappedResource.getUrl().isPresent()) {
					
					String extractedUrl = wrappedResource.getUrl().get();
					
					if (!resourcesByUrl.containsKey(extractedUrl)) {
						resourcesByUrl.put(extractedUrl, wrappedResource); 
					} else {
						throw new IllegalStateException("Found multiple resources with URL " + extractedUrl);
					}
				}
			}
		} catch (FhirParsingFailedException e) {
			LOG.error("Skipping file: " + e.getMessage());
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

	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(String url) {
		//String problemUrl = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-DateRecorded-1";
		//WrappedResource<?> wrappedResource = resourcesByUrl.get(problemUrl);
		//boolean urlsMatch = url.equals(problemUrl);
		
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
			throw new IllegalStateException("Expected a single structure definition to match url " 
					+ url + " but found " + matchingDefinitions.size() + ":\n" + String.join("\n", resourcesByUrl.keySet()));
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
