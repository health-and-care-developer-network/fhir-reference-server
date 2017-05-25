package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Binding;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Constraint;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.HAPIUtils;
import uk.nhs.fhir.util.StringUtil;

public class FhirTreeNodeBuilder {
	private final FhirDocLinkFactory typeLinkFactory = new FhirDocLinkFactory();
	
	public FhirTreeNode fromElementDefinition(ElementDefinitionDt elementDefinition) {
		
		Optional<String> name = Optional.ofNullable(elementDefinition.getName());
		
		List<Type> snapshotElementTypes = elementDefinition.getType();
		List<LinkData> typeLinks = Lists.newArrayList();
		if (!snapshotElementTypes.isEmpty()) {
			typeLinks.addAll(getTypeLinks(snapshotElementTypes));
		}

		ResourceFlags flags = ResourceFlags.forDefinition(elementDefinition);
		
		Integer min = elementDefinition.getMin();
		String max = elementDefinition.getMax();
		
		FhirIcon icon = FhirIcon.forElementDefinition(elementDefinition);
		
		String shortDescription = elementDefinition.getShort();
		if (shortDescription == null) {
			shortDescription = "";
		}
		
		List<IdDt> condition = elementDefinition.getCondition();
		Set<String> conditionIds = Sets.newHashSet();
		condition.forEach(idDt -> conditionIds.add(idDt.toString()));
		
		List<ConstraintInfo> constraints = Lists.newArrayList();
		for (Constraint constraint : elementDefinition.getConstraint()) {
			String key = constraint.getKey();
			if (!conditionIds.contains(key)) {
				String errorMessage = "***Constraint " + key + " doesn't have an associated condition pointing at it***";
				/*if (NewMain.STRICT) {
					throw new IllegalStateException(errorMessage);
				} else {
					System.out.println(errorMessage);
				}*/
				System.out.println(errorMessage);
			}
			
			String description = constraint.getHuman();
			String severity = constraint.getSeverity();
			String requirementsString = constraint.getRequirements();
			Optional<String> requirements = Strings.isNullOrEmpty(requirementsString) ? Optional.empty() : Optional.of(requirementsString);
			String xpath = constraint.getXpath();
			
			constraints.add(new ConstraintInfo(key, description, severity, requirements, xpath));
		}

		//validate for duplicate keys
		for (int i=0; i<constraints.size(); i++) {
			ConstraintInfo constraint1 = constraints.get(i);
			for (int j=i+1; j<constraints.size(); j++) {
				ConstraintInfo constraint2 = constraints.get(j);
				if (constraint1.getKey().equals(constraint2.getKey())) {
					String warning = "Node with constraints with duplicate key: '" + constraint1.getKey() + "'";
					if (NewMain.STRICT) {
						throw new IllegalStateException(warning);
					} else {
						System.out.println("***" + warning + "***");
					}
				}
			}
		}
		
		String path = elementDefinition.getPath();

		// KGM Added Element 9/May/2017
		FhirTreeNode node = new FhirTreeNode(
			icon,
			name,
			flags,
			min,
			max,
			typeLinks, 
			shortDescription,
			constraints,
			path,

			elementDefinition);

		String definition = elementDefinition.getDefinition();
		if (!Strings.isNullOrEmpty(definition)) {
			node.setDefinition(Optional.of(definition));
		}
		
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
		
		String requirements = elementDefinition.getRequirements();
		if (!Strings.isNullOrEmpty(requirements)) {
			node.setRequirements(requirements);
		}
		
		String comments = elementDefinition.getComments();
		if (!Strings.isNullOrEmpty(comments)) {
			node.setComments(comments);
		}
		
		List<StringDt> alias = elementDefinition.getAlias();
		if (!alias.isEmpty()) {
			List<String> aliases = alias.stream().map(stringDt -> stringDt.getValue()).collect(Collectors.toList());
			node.setAliases(aliases);
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

}
