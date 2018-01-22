package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionDetails;

public class NodeMappingValidator {

	private final List<FhirElementMapping> mappings;
	private final String path;
	private final Map<String, List<String>> mappingsByIdentity = Maps.newHashMap();
	private final Map<String, List<String>> mappingsByIdentityStripIgnorable = Maps.newHashMap();
	
	public NodeMappingValidator(HasMappings hasMappings) {
		this(hasMappings.getMappings(), hasMappings.getPath());
	}
	
	public NodeMappingValidator(List<FhirElementMapping> mappings, String path) {
		this.mappings = mappings;
		this.path = path;
	}
	
	public void validate() {
		
		for (FhirElementMapping mapping : mappings) {
			
			String mappingMap = mapping.getMap();
			String mappingId = mapping.getIdentity();
			
			mappingsByIdentity
				.computeIfAbsent(mappingId, id -> Lists.newArrayList())
				.add(mappingMap);
			
			if (StructureDefinitionDetails.IGNORABLE_MAPPING_VALUES.contains(mappingMap)) {
				EventHandlerContext.forThread().event(RendererEventType.IGNORABLE_MAPPING_ID, 
					"Found ignorable mapping (" + mappingMap + ") for identity " + mappingId);
			} else {
				mappingsByIdentityStripIgnorable
					.computeIfAbsent(mappingId, id -> Lists.newArrayList())
					.add(mappingMap);
			}
		}
		
		for (Entry<String, List<String>> e : mappingsByIdentity.entrySet()) {
			String identity = e.getKey();
			
			List<String> allMappingsForKey = e.getValue();
			int allMappingsCount = allMappingsForKey.size();
			
			List<String> nonIgnoredMappingsForKey = mappingsByIdentityStripIgnorable.getOrDefault(identity, Lists.newArrayList());
			int nonIgnoredMappingsCount = nonIgnoredMappingsForKey.size();
			
			if (nonIgnoredMappingsCount > 1) {
				EventHandlerContext.forThread().event(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY, 
					"Multiple mapping entries (" + nonIgnoredMappingsCount + ") on " + path
						+ " for identity " + identity + " [" + String.join(", ", nonIgnoredMappingsForKey + "]"));
			} else if (allMappingsCount > 1) {
				EventHandlerContext.forThread().event(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE, 
					"Multiple mapping entries (" + allMappingsCount + ") on " + path + " for identity "
						+ identity + " [" + String.join(", ", allMappingsForKey + "]"));
			}
		}
	}
}
