package uk.nhs.fhir.data.structdef;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.context.FhirDstu2DataTypes;
import ca.uhn.fhir.model.api.BaseElement;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;

public class FhirElementDataTypeDstu2 {

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
		
		Optional<Class<?>> implementingType = FhirDstu2DataTypes.getImplementingType(typeName);
		
		if (implementingType.isPresent()) {
			Class<?> implementingClass = implementingType.get();
			
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

	public static List<Type> knownTypes(List<Type> types) {
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
