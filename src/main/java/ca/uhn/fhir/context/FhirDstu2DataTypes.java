package ca.uhn.fhir.context;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Maps;

import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Uses the HAPI FHIR package-protected ModelScanner to gather information about the data types defined by HL7, according
 * to the class mapping configured in hapi-fhir-structures-dstu2-2.0!ca/uhn/fhir/model/dstu2/fhirversion.properties.
 * @author jon
 */
public class FhirDstu2DataTypes {
	
	private static final Map<String, Class<?>> nameToDefinitionClass = Maps.newHashMap();
	static {
		// The FhirContext accessor methods for nameTo[X] maps don't work properly because they call
		// toLowerCase even though some keys require uppercase characters. This map allows us to access
		// implementing classes appropriately.
		FhirContext fhirContext = FhirContexts.forVersion(FhirVersion.DSTU2);
		ModelScanner scanner = new ModelScanner(fhirContext, fhirContext.getVersion().getVersion(), null, null);

		for (Entry<String, BaseRuntimeElementDefinition<?>>  entry : scanner.getNameToElementDefinitions().entrySet()) {
			nameToDefinitionClass.put(entry.getKey().toLowerCase(), entry.getValue().getImplementingClass());
		}
		for (Entry<String, RuntimeResourceDefinition>  entry : scanner.getNameToResourceDefinition().entrySet()) {
			nameToDefinitionClass.put(entry.getKey().toLowerCase(), entry.getValue().getImplementingClass());
		}
		for (Entry<String, Class<? extends IBaseResource>>  entry : scanner.getNameToResourceType().entrySet()) {
			nameToDefinitionClass.put(entry.getKey().toLowerCase(), entry.getValue());
		}
	}
	
	public static Optional<Class<?>> getImplementingType(String typeName) {
		Class<?> definition = nameToDefinitionClass.get(typeName.toLowerCase());
		return Optional.ofNullable(definition);
	}
}
