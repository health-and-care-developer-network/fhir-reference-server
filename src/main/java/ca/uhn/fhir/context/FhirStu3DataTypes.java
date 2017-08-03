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
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.TriggerDefinition;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.dstu3.model.UsageContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.util.HAPIUtils;

public class FhirStu3DataTypes implements FhirDataTypes<TypeRefComponent> {
	private static final Map<String, BaseRuntimeElementDefinition<?>> nameToDefinition = Maps.newHashMap();
	static {
		// The FhirContext accessor methods for nameTo[X] maps don't work properly because they call
		// toLowerCase even though some keys require uppercase characters. This map allows us to access
		// implementing classes appropriately.
		FhirContext fhirContext = HAPIUtils.fhirContext(FhirVersion.STU3);
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
			  && !forType(code).equals(FhirDataType.UNKNOWN)) {
				knownTypes.add(type);
			}
		}
		
		return knownTypes;
	}
	
	public static Set<FhirDataType> getTypes(List<TypeRefComponent> list) {
		Set<FhirDataType> dataTypes = Sets.newHashSet();
		
		for (TypeRefComponent type : list) {
			String code = type.getCode();
			if (code != null) {
				dataTypes.add(forType(code));
			}
		}
		
		return dataTypes;
	}
	
	public static FhirDataType forType(String typeName) {
		typeName = typeName.toLowerCase();
		
		// mysteriously missing any object representation - from DSTU2
		if (typeName.equals("backboneelement")) {
			return FhirDataType.COMPLEX_ELEMENT;
		} else if (typeName.equals("resource")) {
			return FhirDataType.RESOURCE;
		} 
		/*else if (typeName.equals("domainresource")) {
			return FhirDataType.DOMAIN_RESOURCE;
		}*/
		else if (typeName.equals("element")) {
			return FhirDataType.ELEMENT;
		}
		
		if (nameToDefinition.containsKey(typeName)) {
			Class<?> implementingClass = nameToDefinition.get(typeName).getImplementingClass();
			
			if (isMetaDataClass(implementingClass)) {
				return FhirDataType.METADATA;
			} else if (implementsOrExtends(implementingClass, BaseReference.class)) {
				return FhirDataType.REFERENCE;
			} else if (implementsOrExtends(implementingClass, Meta.class)) {
				return FhirDataType.META;
			} else if (implementsOrExtends(implementingClass, Narrative.class)) {
				return FhirDataType.NARRATIVE;
			} else if (implementsOrExtends(implementingClass, Extension.class)) {
				return FhirDataType.EXTENSION;
			} else if (implementsOrExtends(implementingClass, BaseResource.class)) {
				return FhirDataType.RESOURCE;
			} else if (implementsOrExtends(implementingClass, PrimitiveType.class)) {
				return FhirDataType.PRIMITIVE;
			} else if (implementsOrExtends(implementingClass, Type.class)) {
				return FhirDataType.SIMPLE_ELEMENT;
			} else if (implementsOrExtends(implementingClass, Element.class)) {
				return FhirDataType.COMPLEX_ELEMENT;
			} else {
				throw new IllegalStateException("Type '" + typeName + "' (class " + implementingClass.getCanonicalName() + ") from properties file wasn't a resource or element");
			}
		} else {
			// not present in HL7 types - probably user defined type
			return FhirDataType.UNKNOWN;
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
}
