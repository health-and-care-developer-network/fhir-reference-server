package uk.nhs.fhir.data.conceptmap;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class FhirConceptMapGroupCollection implements Iterable<FhirConceptMapGroup> {
	private final List<FhirConceptMapGroup> groups = Lists.newArrayList();
	
	public void add(String fromSystem, String fromCode, String toSystem, FhirConceptMapElementTarget target) {
		FhirConceptMapGroup group = findOrCreateGroup(fromSystem, toSystem);
		group.addMapping(fromCode, target);
	}

	private FhirConceptMapGroup findOrCreateGroup(String fromSystem, String toSystem) {
		for (FhirConceptMapGroup group : groups) {
			if (group.matches(fromSystem, toSystem)) {
				return group;
			}
		}
		
		FhirConceptMapGroup newGroup = new FhirConceptMapGroup(fromSystem, toSystem);
		groups.add(newGroup);
		return newGroup;
	}

	@Override
	public Iterator<FhirConceptMapGroup> iterator() {
		return groups.iterator();
	}
}
