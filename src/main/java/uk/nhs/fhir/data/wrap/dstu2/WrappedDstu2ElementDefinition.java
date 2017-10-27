package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.context.FhirDstu2DataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Binding;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Constraint;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.url.ValuesetLinkFix;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.makehtml.RendererError;
import uk.nhs.fhir.makehtml.RendererErrorConfig;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class WrappedDstu2ElementDefinition extends WrappedElementDefinition {
	
	private static final Dstu2FhirDocLinkFactory typeLinkFactory = new Dstu2FhirDocLinkFactory();
	private static final FhirDstu2DataTypes fhirDataTypes = new FhirDstu2DataTypes();

	private final ElementDefinitionDt definition;

	public WrappedDstu2ElementDefinition(ElementDefinitionDt definition, RendererContext context) {
		super(context);
		this.definition = definition;
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public String getPath() {
		return definition.getPath();
	}

	@Override
	public LinkDatas getTypeLinks() {
		LinkDatas typeLinks = new LinkDatas();
		
		List<Type> knownTypes = fhirDataTypes.knownTypes(definition.getType());
		if (!knownTypes.isEmpty()) {
			for (Type type : knownTypes) {
				String code = type.getCode();
				LinkData codeLink = typeLinkFactory.forDataTypeName(code);
				
				List<UriDt> profileUris = type.getProfile();
				
				
				if (profileUris.isEmpty()) {
					typeLinks.addSimpleLink(codeLink);
				} else {
					profileUris.forEach(
						(UriDt uri) -> typeLinks.addNestedLink(
								codeLink, 
							typeLinkFactory.fromUri(uri.getValue())));
				}
			}
		}
		
		return typeLinks;
	}

	@Override
	public Set<FhirElementDataType> getDataTypes() {
		return FhirDstu2DataTypes.getTypes(definition.getType());
	}

	@Override
	public ResourceFlags getResourceFlags() {
		
		boolean isSummary = Boolean.TRUE.equals(definition.getIsSummary());
		boolean isModifier = Boolean.TRUE.equals(definition.getIsModifier());
		boolean isConstrained = !definition.getConstraint().isEmpty();
		boolean isMustSupport = Boolean.TRUE.equals(definition.getMustSupport());
		
		ResourceFlags flags = new ResourceFlags();
		if (isSummary) {
			flags.addSummaryFlag();
		}
		if (isModifier) {
			flags.addModifierFlag();
		}
		if (isConstrained) {
			flags.addConstrainedFlag();
		}
		if (isMustSupport) {
			flags.addMustSupportFlag();
		}
		
		return flags;
	}

	@Override
	public Integer getCardinalityMin() {
		return definition.getMin();
	}

	@Override
	public String getCardinalityMax() {
		return definition.getMax();
	}

	public ElementDefinitionDt getWrappedDefinition() {
		return definition;
	}

	@Override
	public String getShortDescription() {
		return definition.getShort();
	}

	@Override
	public Set<String> getConditionIds() {
		Set<String> conditionIds = 
			definition
				.getCondition()
				.stream()
				.map(conditionId -> conditionId.toString())
				.collect(Collectors.toSet());
		return conditionIds;
	}

	@Override
	public List<ConstraintInfo> getConstraintInfos() {
		List<ConstraintInfo> constraints = Lists.newArrayList();
		
		for (Constraint constraint : definition.getConstraint()) {
			String key = constraint.getKey();
			String description = constraint.getHuman();
			String severity = constraint.getSeverity();
			String requirementsString = constraint.getRequirements();
			Optional<String> requirements = Strings.isNullOrEmpty(requirementsString) ? Optional.empty() : Optional.of(requirementsString);
			String xpath = constraint.getXpath();
			
			constraints.add(new ConstraintInfo(key, description, severity, requirements, xpath));
		}
		
		return constraints;
	}

	@Override
	public Optional<String> getDefinition() {
		return Optional.ofNullable(definition.getDefinition());
	}

	@Override
	public Optional<SlicingInfo> getSlicing() {
		if (definition.getSlicing().isEmpty()) {
			return Optional.empty();
		} else {
			Slicing slicing = definition.getSlicing();

			String description = slicing.getDescription();
			String descriptionDesc = description == null ? "[no description]" : description;
			if (slicing.getDiscriminator().isEmpty()
			  && !getSliceName().isPresent()) {
				RendererErrorConfig.handle(RendererError.SLICING_WITHOUT_DISCRIMINATOR, 
					"Slicing " + descriptionDesc + " doesn't have a discriminator (" + getPath() + ")");
			}
			
			SlicingInfo slicingInfo = new SlicingInfo(
				slicing.getDescription(),
				slicing.getDiscriminator().stream()
					.map(stringDt -> stringDt.getValue())
					.collect(Collectors.toSet()),
				slicing.getOrdered(),
				slicing.getRules());
			return Optional.of(slicingInfo);
		}
	}

	@Override
	public Optional<String> getFixedValue() {
		IDatatype fixed = definition.getFixed();
		if (fixed != null) {
			if (fixed instanceof BasePrimitive) {
				BasePrimitive<?> fixedPrimitive = (BasePrimitive<?>)fixed;
				String fixedValueAsString = fixedPrimitive.getValueAsString();
				
				if (fixedValueAsString.equals("https://hl7.org.uk/fhir/CareConnect-ConditionCategory-1")) {
					String correctedUrl = "https://fhir.hl7.org.uk/CareConnect-ConditionCategory-1";
					RendererErrorConfig.handle(RendererError.HL7_ORG_UK_HOST, "Fixing https://hl7.org.uk/fhir/CareConnect-ConditionCategory-1 to " + correctedUrl);
					fixedValueAsString = correctedUrl;
				}
				
				return Optional.of(fixedValueAsString);
			} else {
				throw new IllegalStateException("Unhandled type for fixed value: " + fixed.getClass().getName());
			}
		}
		
		return Optional.empty();
	}

	@Override
	public List<String> getExamples() {
		List<String> examples = Lists.newArrayList();
		
		IDatatype example = definition.getExample();
		
		if (example != null) {
			if (example instanceof BasePrimitive) {
				BasePrimitive<?> examplePrimitive = (BasePrimitive<?>)example;
				examples.add(examplePrimitive.getValueAsString());
			} else if (example instanceof PeriodDt) {
				PeriodDt examplePeriod = (PeriodDt)example;
				
				String dateText = StringUtil.dateRange(examplePeriod.getStart(), examplePeriod.getEnd());
				
				examples.add(dateText);
			} else {
				throw new IllegalStateException("Unhandled type for example value: " + example.getClass().getName());
			}
		}
		
		return examples;
	}

	@Override
	public Optional<String> getDefaultValue() {		
		IDatatype defaultValue = definition.getDefaultValue();
		
		if (defaultValue != null) {
			if (defaultValue instanceof BasePrimitive) {
				BasePrimitive<?> defaultValuePrimitive = (BasePrimitive<?>)defaultValue;
				return Optional.of(defaultValuePrimitive.getValueAsString());
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + defaultValue.getClass().getName());
			}
		}
		
		return Optional.empty();
	}

	@Override
	public Optional<BindingInfo> getBinding() {
		Binding binding = definition.getBinding();
		
		if (!binding.isEmpty()) {
			
			IDatatype valueSet = binding.getValueSet();
			Optional<FhirURL> url = Optional.empty();
			if (valueSet != null) {
				String urlString = FhirDstu2DataTypes.resolveDstu2DatatypeValue(valueSet);
				url = Optional.of(FhirURL.buildOrThrow(ValuesetLinkFix.fixDstu2(urlString), FhirVersion.DSTU2));
			}
			
			Optional<String> description = Optional.ofNullable(binding.getDescription());
			String strength = binding.getStrength();
			
			return Optional.of(new BindingInfo(description, url, strength));
		}
		
		return Optional.empty();
	}

	@Override
	public Optional<String> getRequirements() {
		return Optional.ofNullable(definition.getRequirements());
	}

	@Override
	public Optional<String> getComments() {
		return Optional.ofNullable(definition.getComments());
	}

	@Override
	public List<String> getAliases() {
		return definition
				.getAlias()
				.stream()
				.map(stringDt -> stringDt.getValue())
				.collect(Collectors.toList());
	}

	@Override
	public Optional<ExtensionType> getExtensionType() {
		if (!definition.getSlicing().isEmpty()) {
			return Optional.empty();
		}
		
		Set<ExtensionType> extensionTypes = Sets.newHashSet();
		
		for (Type type : definition.getType()) {
			if (type.getCode() != null 
			  && type.getCode().equals("Extension")) {
				for (UriDt profile : type.getProfile()) {
					ExtensionType extensionType = lookupExtensionType(profile.getValueAsString());
					if (extensionType != null) {
						extensionTypes.add(extensionType);
					}
				}
			}
		}
		
		if (extensionTypes.isEmpty()) {
			return Optional.empty();
		} else if (extensionTypes.size() == 1) {
			return Optional.of(extensionTypes.iterator().next());
		} else {
			throw new IllegalStateException("Unsure of extension type - multiple found: " + Arrays.toString(extensionTypes.toArray()));
		}
	}

	@Override
	public Optional<String> getLinkedNodeName() {
		return Optional.ofNullable(definition.getNameReference());
	}

	@Override
	public Optional<String> getLinkedNodePath() {
		return Optional.empty();
	}
	
	@Override
	public List<FhirElementMapping> getMappings() {
		return 
			definition
				.getMapping()
				.stream()
				.map(mapping -> 
					new FhirElementMapping(
						mapping.getIdentity(),
						mapping.getMap(),
						Optional.ofNullable(mapping.getLanguage())))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> getSliceName() {
		return Optional.ofNullable(definition.getName());
	}

	@Override
	public Optional<String> getId() {
		return Optional.ofNullable(definition.getElementSpecificId());
	}

	@Override
	public FhirVersion getVersion() {
		return FhirVersion.DSTU2;
	}
	
}
