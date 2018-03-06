package uk.nhs.fhir.data.structdef.tree.validate;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class IgnorableMappingValues {
	
	public static final Set<String> VALUES = ImmutableSet.<String>builder().add("n/a", "N/A").build();
}
