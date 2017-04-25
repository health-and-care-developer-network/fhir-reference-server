package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Binding;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.HAPIUtils;
import uk.nhs.fhir.util.StringUtil;

public class FhirTreeNodeBuilder {
	private final FhirDocLinkFactory typeLinkFactory = new FhirDocLinkFactory();
	
	public FhirTreeNode fromElementDefinition(ElementDefinitionDt elementDefinition) {
		
		String displayName = getDisplayName(elementDefinition);
		
		List<Type> snapshotElementTypes = elementDefinition.getType();
		List<LinkData> typeLinks = Lists.newArrayList();
		if (!snapshotElementTypes.isEmpty()) {
			typeLinks.addAll(getTypeLinks(snapshotElementTypes));
		}

		ResourceFlags flags = ResourceFlags.forDefinition(elementDefinition);
		
		Integer min = elementDefinition.getMin();
		String max = elementDefinition.getMax();
		//FhirCardinality cardinality = new FhirCardinality(elementDefinition);
		
		FhirIcon icon = FhirIcon.forElementDefinition(elementDefinition);
		
		String shortDescription = elementDefinition.getShort();
		if (shortDescription == null) {
			shortDescription = "";
		}
		
		List<ResourceInfo> resourceInfos = Lists.newArrayList();
			
		String path = elementDefinition.getPath();
			
		FhirTreeNode node = new FhirTreeNode(
			new FhirTreeNodeId(displayName, icon),
			flags,
			min,
			max,
			//cardinality,
			typeLinks, 
			shortDescription,
			resourceInfos,
			path);

		Slicing slicing = elementDefinition.getSlicing();
		if (!slicing.isEmpty()) {
			node.setSlicingInfo(new SlicingInfo(slicing));
		}
		
		IDatatype fixed = elementDefinition.getFixed();
		if (fixed != null) {
			if (fixed instanceof BasePrimitive) {
				BasePrimitive<?> fixedPrimitive = (BasePrimitive<?>)fixed;
				node.setFixedValue(fixedPrimitive.getValueAsString());
			} else {
				throw new IllegalStateException("Unhandled type for fixed value: " + fixed.getClass().getName());
			}
		}
		
		IDatatype example = elementDefinition.getExample();
		if (example != null) {
			if (example instanceof BasePrimitive) {
				BasePrimitive<?> examplePrimitive = (BasePrimitive<?>)example;
				node.setExample(examplePrimitive.getValueAsString());
			} else if (example instanceof PeriodDt) {
				PeriodDt examplePeriod = (PeriodDt)example;
				node.setExample(StringUtil.periodToString(examplePeriod));
			} else {
				throw new IllegalStateException("Unhandled type for example value: " + example.getClass().getName());
			}
		}
		
		IDatatype defaultValue = elementDefinition.getDefaultValue();
		if (defaultValue != null) {
			if (defaultValue instanceof BasePrimitive) {
				BasePrimitive<?> defaultValuePrimitive = (BasePrimitive<?>)defaultValue;
				node.setDefaultValue(defaultValuePrimitive.getValueAsString());
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + defaultValue.getClass().getName());
			}
		}
		
		Binding binding = elementDefinition.getBinding();
		if (!binding.isEmpty()) {
			Optional<URL> url;
			try {
				IDatatype valueSet = binding.getValueSet();
				url = valueSet == null ? Optional.empty() : Optional.of(new URL(HAPIUtils.resolveDatatypeValue(valueSet)));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			Optional<String> description = Optional.ofNullable(binding.getDescription());
			String strength = binding.getStrength();
			
			node.setBinding(new BindingInfo(description, url, strength));
		}
		
		return node;
	}

	private List<LinkData> getTypeLinks(List<Type> snapshotElementTypes) {
		List<LinkData> typeLinks = Lists.newArrayList();
		
		List<Type> knownTypes = FhirDataTypes.knownTypes(snapshotElementTypes);
		if (!knownTypes.isEmpty()) {
			for (Type type : knownTypes) {
				String code = type.getCode();
				
				List<UriDt> profileUris = type.getProfile();
				if (profileUris.isEmpty()) {
					typeLinks.add(typeLinkFactory.forDataTypeName(code));
				} else {
					List<String> uris = Lists.newArrayList();
					profileUris.forEach((UriDt uri) -> uris.add(uri.getValue()));
					typeLinks.add(typeLinkFactory.withNestedLinks(code, uris));
				}
			}
		}
		
		return typeLinks;
	}

	public static String getDisplayName(ElementDefinitionDt elementDefinition) {
		String name = elementDefinition.getName();
		boolean hasName = !Strings.isNullOrEmpty(name);
		
		String path = elementDefinition.getPath();
		boolean hasPath = !Strings.isNullOrEmpty(path);
		
		String pathName = null;
		if (hasPath) {
			String[] pathTokens = path.split("\\.");
			pathName = pathTokens[pathTokens.length - 1]; 
		}
		
		String displayName;
		
		if (hasName && hasPath && !pathName.equals(name)) {
			displayName = pathName + " (" + name + ")";
		} else if (hasPath) {
			displayName = pathName;
		} else if (hasName) {
			displayName = name;
		} else {
			throw new IllegalStateException("No name or path information");
		}
		
		return displayName;
	}

}
