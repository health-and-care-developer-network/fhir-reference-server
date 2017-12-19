package ca.uhn.fhir.context;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.Maps;

import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

public class FhirStu3DataTypes {
	private static final Map<String, BaseRuntimeElementDefinition<?>> nameToDefinition = Maps.newHashMap();
	static {
		// The FhirContext accessor methods for nameTo[X] maps don't work properly because they call
		// toLowerCase even though some keys require uppercase characters. This map allows us to access
		// implementing classes appropriately.
		FhirContext fhirContext = FhirContexts.forVersion(FhirVersion.STU3);
		ModelScanner scanner = new ModelScanner(fhirContext, fhirContext.getVersion().getVersion(), null, null);

		for (Entry<String, BaseRuntimeElementDefinition<?>>  entry : scanner.getNameToElementDefinitions().entrySet()) {
			nameToDefinition.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		for (Entry<String, RuntimeResourceDefinition>  entry : scanner.getNameToResourceDefinition().entrySet()) {
			nameToDefinition.put(entry.getKey().toLowerCase(), entry.getValue());
		}
	}
	
	public static Optional<Class<?>> getImplementingType(String typeName) {
		BaseRuntimeElementDefinition<?> definition = nameToDefinition.get(typeName.toLowerCase());
		return Optional.ofNullable(definition)
				.map(BaseRuntimeElementDefinition::getImplementingClass);
	}
}
