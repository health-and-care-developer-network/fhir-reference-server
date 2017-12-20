package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.makehtml.RendererError;
import uk.nhs.fhir.makehtml.RendererEventConfig;

public class NodeMappingValidator {
	
	private static final Set<String> IGNORABLE_VALUES = Sets.newHashSet("n/a", "N/A");
	
	public static void validate(FhirTreeNode node) {
		Map<String, List<String>> mappingsByIdentity = Maps.newHashMap();
		Map<String, List<String>> mappingsByIdentityStripNonApplicable = Maps.newHashMap();
		
		for (FhirElementMapping mapping : node.getMappings()) {
			addToMap(mappingsByIdentity, mapping);
			
			if (canIgnore(mapping)) {
				RendererEventConfig.handle(RendererError.IGNORABLE_MAPPING_ID, 
					"Found ignorable mapping (" + mapping.getMap() + ") for identity " + mapping.getIdentity());
			} else {
				addToMap(mappingsByIdentityStripNonApplicable, mapping);
			}
		}
		
		for (Entry<String, List<String>> e : mappingsByIdentity.entrySet()) {
			String identity = e.getKey();
			List<String> allMappingsForKey = e.getValue();
			List<String> nonIgnoredMappingsForKey = mappingsByIdentityStripNonApplicable.get(identity);
			
			if (nonIgnoredMappingsForKey != null
			  && nonIgnoredMappingsForKey.size() > 1) {
				RendererEventConfig.handle(RendererError.MULTIPLE_MAPPINGS_SAME_KEY, 
					"Multiple mapping entries (" + nonIgnoredMappingsForKey.size() + ") on " + node.getPath() + " for identity " + identity + " [" + String.join(", ", nonIgnoredMappingsForKey + "]"));
			} else if (allMappingsForKey.size() > 1) {
				RendererEventConfig.handle(RendererError.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE, 
					"Multiple mapping entries (" + allMappingsForKey.size() + ") on " + node.getPath() + " for identity " + identity + " [" + String.join(", ", allMappingsForKey + "]"));
			}
		}
	}
	
	private static boolean canIgnore(FhirElementMapping mapping) {
		return IGNORABLE_VALUES.contains(mapping.getMap());
	}

	private static void addToMap(Map<String, List<String>> map, FhirElementMapping mapping) {
		if (!map.containsKey(mapping.getIdentity())) {
			map.put(mapping.getIdentity(), Lists.newArrayList());
		}
		
		map.get(mapping.getIdentity()).add(mapping.getMap());
	}
}
