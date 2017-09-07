package ca.uhn.fhir.context;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hl7.fhir.dstu3.model.BaseReference;
import org.hl7.fhir.dstu3.model.BaseResource;
import org.hl7.fhir.dstu3.model.ContactDetail;
import org.hl7.fhir.dstu3.model.Contributor;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.Element;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.ParameterDefinition;
import org.hl7.fhir.dstu3.model.PrimitiveType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.TriggerDefinition;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.dstu3.model.UsageContext;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
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

	public List<TypeRefComponent> knownTypes(List<TypeRefComponent> types) {
		List<TypeRefComponent> knownTypes = Lists.newArrayList();
		
		for (TypeRefComponent type : types) {
			String code = type.getCode();
			if (code != null 
			  && !forType(code).equals(FhirElementDataType.UNKNOWN)) {
				knownTypes.add(type);
			}
		}
		
		return knownTypes;
	}
	
	public static Set<FhirElementDataType> getTypes(List<TypeRefComponent> list) {
		Set<FhirElementDataType> dataTypes = Sets.newHashSet();
		
		for (TypeRefComponent type : list) {
			String code = type.getCode();
			if (code != null) {
				dataTypes.add(forType(code));
			}
		}
		
		return dataTypes;
	}
	
	public static FhirElementDataType forType(String typeName) {
		typeName = typeName.toLowerCase();
		
		// mysteriously missing any object representation - from DSTU2
		if (typeName.equals("backboneelement")) {
			return FhirElementDataType.COMPLEX_ELEMENT;
		} else if (typeName.equals("resource")) {
			return FhirElementDataType.RESOURCE;
		} 
		/*else if (typeName.equals("domainresource")) {
			return FhirDataType.DOMAIN_RESOURCE;
		}*/
		else if (typeName.equals("element")) {
			return FhirElementDataType.ELEMENT;
		}
		
		if (nameToDefinition.containsKey(typeName)) {
			Class<?> implementingClass = nameToDefinition.get(typeName).getImplementingClass();
			
			if (isMetaDataClass(implementingClass)) {
				return FhirElementDataType.METADATA;
			} else if (implementsOrExtends(implementingClass, BaseReference.class)) {
				return FhirElementDataType.REFERENCE;
			} else if (implementsOrExtends(implementingClass, Meta.class)) {
				return FhirElementDataType.META;
			} else if (implementsOrExtends(implementingClass, Narrative.class)) {
				return FhirElementDataType.NARRATIVE;
			} else if (implementsOrExtends(implementingClass, XhtmlNode.class)) {
				return FhirElementDataType.XHTML_NODE;
			} else if (implementsOrExtends(implementingClass, Extension.class)) {
				return FhirElementDataType.EXTENSION;
			} else if (implementsOrExtends(implementingClass, BaseResource.class)) {
				return FhirElementDataType.RESOURCE;
			} else if (implementsOrExtends(implementingClass, PrimitiveType.class)) {
				return FhirElementDataType.PRIMITIVE;
			} else if (implementsOrExtends(implementingClass, Type.class)) {
				return FhirElementDataType.SIMPLE_ELEMENT;
			} else if (implementsOrExtends(implementingClass, Element.class)) {
				return FhirElementDataType.COMPLEX_ELEMENT;
			} else {
				throw new IllegalStateException("Type '" + typeName + "' (class " + implementingClass.getCanonicalName() + ") from properties file wasn't a resource or element");
			}
		} else {
			// not present in HL7 types - probably user defined type
			return FhirElementDataType.UNKNOWN;
		}
	}
	
	private static final Set<Class<?>> metadataClasses = Sets.newHashSet(
		ContactDetail.class, 
		Contributor.class, 
		DataRequirement.class, 
		ParameterDefinition.class,
		RelatedArtifact.class,
		TriggerDefinition.class,
		UsageContext.class);
	
	private static boolean isMetaDataClass(Class<?> dataClass) {
		return metadataClasses.contains(dataClass);
	}

	private static boolean implementsOrExtends(Class<?> implementor, Class<?> implementee) {
		return implementee.isAssignableFrom(implementor);
	}
	
	public static LinkData openTypeLink() {
		return new LinkData(FhirURL.buildOrThrow(FhirURLConstants.HTTP_HL7_STU3 + "/datatypes.html#open", FhirVersion.STU3), "*");
	}
	
	public static String resolveValue(Type dataType) {
		String value;
		if (dataType instanceof Reference) {
			value = ((Reference)dataType).getReference();
		} else if (dataType instanceof UriType) {
			value = ((UriType)dataType).getValue();
		} else {
			throw new IllegalStateException("Unhandled type for datatype: " + dataType.getClass().getName());
		}
		
		if(Strings.isNullOrEmpty(value)) {
			throw new IllegalStateException("Got empty or null value string for: " + dataType.getClass().getName());
		}
		
		return value;
	}
}
