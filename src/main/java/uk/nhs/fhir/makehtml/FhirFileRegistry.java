package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Maps;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;

public class FhirFileRegistry {

	private final FhirFileParser parser = new FhirFileParser();
	
	private final Map<File, WrappedResource<?>> resourcesByFile = Maps.newHashMap();
	private final Map<String, WrappedResource<?>> resourcesByUrl = Maps.newHashMap();
	
	public void register(File xmlFile) {
		try {
			IBaseResource parsedFile = parser.parseFile(xmlFile);
			WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
			resourcesByFile.put(xmlFile, wrappedResource);
			
			if (wrappedResource.getUrl().isPresent()) {
				resourcesByUrl.put(wrappedResource.getUrl().get(), wrappedResource);
			}
		} catch (IOException e) {
			System.out.println("Skipping file: " + e.getMessage());
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
			System.out.println(String.join("\n", resourcesByUrl.keySet()));
			throw new IllegalStateException("Expected a single structure definition to match url " + url + " but found " + matchingDefinitions.size());
		}
	}
}
