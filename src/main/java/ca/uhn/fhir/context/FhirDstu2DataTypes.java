package ca.uhn.fhir.context;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ca.uhn.fhir.model.api.BaseElement;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Uses the HAPI FHIR package-protected ModelScanner to gather information about the data types defined by HL7, according
 * to the class mapping configured in hapi-fhir-structures-dstu2-2.0!ca/uhn/fhir/model/dstu2/fhirversion.properties.
 * @author jon
 */
public class FhirDstu2DataTypes {
	
	private static final Map<String, BaseRuntimeElementDefinition<?>> nameToDefinition = Maps.newHashMap();
	static {
		// The FhirContext accessor methods for nameTo[X] maps don't work properly because they call
		// toLowerCase even though some keys require uppercase characters. This map allows us to access
		// implementing classes appropriately.
		FhirContext fhirContext = FhirContexts.forVersion(FhirVersion.DSTU2);
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
		return definition == null ? Optional.empty() : Optional.of(definition.getImplementingClass());
	}

	public List<Type> knownTypes(List<Type> types) {
		List<Type> knownTypes = Lists.newArrayList();
		
		for (Type type : types) {
			String code = type.getCode();
			if (code != null 
			  && !forType(code).equals(FhirElementDataType.UNKNOWN)) {
				knownTypes.add(type);
			}
		}
		
		return knownTypes;
	}
	
	public static Set<FhirElementDataType> getTypes(List<Type> types) {
		Set<FhirElementDataType> dataTypes = Sets.newHashSet();
		
		for (Type type : types) {
			String code = type.getCode();
			if (code != null) {
				dataTypes.add(forType(code));
			}
		}
		
		return dataTypes;
	}
	
	public static FhirElementDataType forType(String typeName) {
		typeName = typeName.toLowerCase();
		
		// mysteriously missing any object representation
		if (typeName.equals("backboneelement")) {
			return FhirElementDataType.COMPLEX_ELEMENT;
		} else if (typeName.equals("resource")) {
			return FhirElementDataType.RESOURCE;
		} else if (typeName.equals("domainresource")) {
			return FhirElementDataType.DOMAIN_RESOURCE;
		} else if (typeName.equals("element")) {
			return FhirElementDataType.ELEMENT;
		}
		
		if (nameToDefinition.containsKey(typeName)) {
			Class<?> implementingClass = nameToDefinition.get(typeName).getImplementingClass();
			
			if (implementsOrExtends(implementingClass, ExtensionDt.class)) {
				return FhirElementDataType.EXTENSION;
			} else if (implementsOrExtends(implementingClass, BaseResource.class)) {
				return FhirElementDataType.RESOURCE;
			} else if (implementsOrExtends(implementingClass, BasePrimitive.class)) {
				return FhirElementDataType.PRIMITIVE;
			} else if (implementsOrExtends(implementingClass, IDatatype.class)) {
				return FhirElementDataType.SIMPLE_ELEMENT;
			} else if (implementsOrExtends(implementingClass, BaseElement.class)) {
				// should always match
				return FhirElementDataType.COMPLEX_ELEMENT;
			} else {
				throw new IllegalStateException("Type from properties file wasn't a resource or element");
			}
		} else {
			// not present in HL7 types - probably user defined type
			return FhirElementDataType.UNKNOWN;
		}
	}
	
	private static boolean implementsOrExtends(Class<?> implementor, Class<?> implementee) {
		return implementee.isAssignableFrom(implementor);
	}
	
	public static String resolveDstu2DatatypeValue(IDatatype datatype) {
		if (datatype instanceof BasePrimitive) {
			return ((BasePrimitive<?>) datatype).getValueAsString();
		} else if (datatype instanceof ResourceReferenceDt) {
			return ((ResourceReferenceDt) datatype).getReference().getValueAsString();
		} else {
			throw new IllegalStateException("Unhandled type for datatype: " + datatype.getClass().getName());
		}
	}
}
