package uk.nhs.fhir.data.wrap.stu3;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionExampleComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionSlicingComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.PrimitiveType;
import org.hl7.fhir.dstu3.model.Type;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.Example;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementDataTypeStu3;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.url.ValuesetLinkFix;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ListUtils;
import uk.nhs.fhir.util.StringUtil;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public class WrappedStu3ElementDefinition extends WrappedElementDefinition {
	
	private static final Stu3FhirDocLinkFactory typeLinkFactory = new Stu3FhirDocLinkFactory();
	
	private final ElementDefinition definition;

	public WrappedStu3ElementDefinition(ElementDefinition definition) {
		super(definition.getPath());
		this.definition = definition;
	}

	@Override
	public String getName() {
		return definition.getLabel();
	}

	@Override
	public ImmutableNodePath getPath() {
		return new ImmutableNodePath(definition.getPath());
	}

	@Override
	public LinkDatas getTypeLinks(Optional<StructureDefinitionRepository> structureDefinitions) {
		LinkDatas typeLinks = new LinkDatas();
		
		List<TypeRefComponent> knownTypes = FhirElementDataTypeStu3.knownTypes(definition.getType());
		if (!knownTypes.isEmpty()) {
			for (TypeRefComponent type : knownTypes) {

				String code = type.getCode();
				
				if (type.hasProfile()) {
					String profile = type.getProfile();
					if (code.equals("Extension")
					  || code.equals("Quantity")
					  || code.equals("Reference")) {
						LinkData codeLink = typeLinkFactory.forDataTypeName(type.getCode());
						typeLinks.addNestedUri(codeLink, profile, FhirVersion.STU3);
					} else if (structureDefinitions.isPresent()) {
						Optional<WrappedStructureDefinition> structureDefinition = structureDefinitions.get().getUserDefinedType(code, profile, FhirVersion.STU3);
						if (structureDefinition.isPresent()) {
							typeLinks.addSimpleLink(new LinkData(FhirURL.buildOrThrow(profile, FhirVersion.STU3), structureDefinition.get().getName()));
						} else {
							throw new IllegalStateException("Found StructureDefinition for profile (" + profile + ") but wasn't a recognised data type (" + code + ") " + getPath());
						}
					} else {
						throw new IllegalStateException("should we be incorporating profile (" + profile + ") into type links? " + getPath());
					}
					
				} else if (type.hasTargetProfile()) {
					if (type.getCode().equals("Reference")) {
						LinkData referenceLink = typeLinkFactory.forDataTypeName(type.getCode());
						typeLinks.addNestedUri(referenceLink, type.getTargetProfile(), FhirVersion.STU3);
					} else if (type.getCode().equals("string")){
						EventHandlerContext.forThread().event(RendererEventType.TYPELINK_STRING_WITH_PROFILE, 
							"Type link with type " + type.getCode() + " and a target profile " + type.getTargetProfile()
							  + " - dropping targetProfile (" + getPath() + ")");
						typeLinks.addSimpleLink(typeLinkFactory.forDataTypeName(type.getCode()));
					} else {
						String targetProfile = type.getTargetProfile();
						throw new IllegalStateException("should we be incorporating target profile (" + targetProfile + ") into type links? " + getPath());
					}
				} else if (type.hasAggregation()) {
					String aggregation = String.join(", ", type.getAggregation().stream().map(aggregationMode -> aggregationMode.asStringValue()).collect(Collectors.toList()));
					throw new IllegalStateException("should we be incorporating profile (" + aggregation + ") into type links? " + getPath());
				} else {
					typeLinks.addSimpleLink(typeLinkFactory.forDataTypeName(code));
				}
			}
		}
		
		return typeLinks;
	}

	@Override
	public Set<FhirElementDataType> getDataTypes() {
		return FhirElementDataTypeStu3.getTypes(definition.getType());
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
		
		for (ElementDefinitionConstraintComponent constraint : definition.getConstraint()) {
			String key = constraint.getKey();
			String description = constraint.getHuman();
			String severity = constraint.getSeverity().getDisplay();
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
			ElementDefinitionSlicingComponent slicing = definition.getSlicing();

			String description = slicing.getDescription();
			String descriptionDesc = description == null ? "[no description]" : description;
			if (slicing.getDiscriminator().isEmpty()
			  && !getSliceName().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.SLICING_WITHOUT_DISCRIMINATOR, 
					"Slicing " + descriptionDesc + " doesn't have a discriminator (" + getPath() + ")");
			}
			
			SlicingInfo slicingInfo = new SlicingInfo(
				slicing.getDescription(),
				slicing.getDiscriminator().stream()
					.map(discriminator -> discriminator.getPath())
					.collect(Collectors.toSet()),
				slicing.getOrdered(),
				slicing.getRules().getDisplay());
			return Optional.of(slicingInfo);
		}
	}

	@Override
	public Optional<String> getFixedValue() {
		Type fixed = definition.getFixed();
		if (fixed == null) {
			return Optional.empty();
		} else {
			if (fixed instanceof PrimitiveType) {
				return Optional.ofNullable(((PrimitiveType<?>)fixed).asStringValue());
			} else if (fixed instanceof CodeableConcept) {
				CodeableConcept fixedCc = (CodeableConcept)fixed;
				List<Coding> codings = fixedCc.getCoding();
				if (codings.isEmpty()){
					throw new IllegalStateException("CodeableConcept fixed value without any codings: " + getPath().toString());
				} else if (codings.size() == 1) {
					Coding coding = codings.get(0);
					return Optional.of("code=" + coding.getCode() + ", system=" + coding.getSystem());
				} else {
					return Optional.of(
						codings
							.stream()
							.map(coding -> "[code=" + coding.getCode() + ", system=" + coding.getSystem() + "]")
							.collect(Collectors.joining(", ")));
				}
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + fixed.getClass().getCanonicalName());
			}
		}
	}

	@Override
	public List<Example> getExamples() {
		List<Example> examples = Lists.newArrayList();
		for (ElementDefinitionExampleComponent example : definition.getExample()) {
			Type value = example.getValue();
			Optional<String> label = Optional.ofNullable(example.getLabel());
			if (value instanceof PrimitiveType) {
				examples.add(new Example(((PrimitiveType<?>)value).asStringValue(), label));
			} else if (value instanceof Period) {
				Period examplePeriod = ((Period)value);
				
				String dateText = StringUtil.dateRange(examplePeriod.getStart(), examplePeriod.getEnd());
				
				examples.add(new Example(dateText, label));
			} else if (value instanceof Identifier){
				Identifier exampleIdentifier = (Identifier)value;
				examples.add(new Example(exampleIdentifier.getValue(), label));
			} else {
				throw new IllegalStateException("Handle example value of type " + value.getClass().getName());
			}
		}
		
		return examples;
	}

	@Override
	public Optional<String> getDefaultValue() {
		Type defaultValue = definition.getDefaultValue();
		if (defaultValue == null) {
			return Optional.empty();
		} else {
			if (defaultValue instanceof PrimitiveType) {
				return Optional.ofNullable(((PrimitiveType<?>)defaultValue).asStringValue());
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + defaultValue.getClass().getCanonicalName());
			}
		}
	}

	@Override
	public Optional<BindingInfo> getBinding() {
		ElementDefinitionBindingComponent binding = definition.getBinding();
		
		if (!binding.isEmpty()) {
			
			Type valueSet = binding.getValueSet();
			Optional<FhirURL> url = Optional.empty();
			if (valueSet != null) {
				String urlString = FhirElementDataTypeStu3.resolveValue(valueSet);
				url = Optional.of(FhirURL.buildOrThrow(ValuesetLinkFix.fixStu3(urlString), FhirVersion.STU3));
			}
			
			Optional<String> description = Optional.ofNullable(binding.getDescription());
			String strength = binding.getStrength().getDisplay();
			
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
		return Optional.ofNullable(definition.getComment());
	}

	@Override
	public List<String> getAliases() {
		return definition
			.getAlias()
			.stream()
			.map(stringType -> stringType.getValue())
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<ExtensionType> getExtensionType(Optional<StructureDefinitionRepository> structureDefinitions,
			Set<String> permittedMissingExtensionPrefixes) {
		if (!definition.getSlicing().isEmpty()) {
			return Optional.empty();
		} else {
			return getLinkedStructureDefinitionUrl().map(url -> lookupExtensionType(url, structureDefinitions, permittedMissingExtensionPrefixes));
		}
		
		/*for (TypeRefComponent type : definition.getType()) {
			if (type.getCode() != null 
			  && type.getCode().equals("Extension")) {
				return Optional.of(lookupExtensionType(type.getProfile(), structureDefinitions));
			}
		}
		
		return Optional.empty();*/
	}
	
	public Optional<String> getLinkedStructureDefinitionUrl() {
		return 
			ListUtils.uniqueIfPresent(
				definition
					.getType()
					.stream()
					.filter(type -> 
						type.getCode() != null 
						  && type.getCode().equals("Extension")
						  && type.getProfile() != null)
					.map(type -> type.getProfile())
					.collect(Collectors.toList()), "extension types");
	}

	@Override
	public Optional<String> getLinkedNodeName() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getLinkedNodePath() {
		String contentReference = definition.getContentReference();
		if (contentReference == null) {
			return Optional.empty();
		} else if (contentReference.startsWith("#")) {
			String trimmedContentReference = contentReference.substring(1);
			return Optional.ofNullable(trimmedContentReference);
		} else {
			throw new IllegalStateException("Linked node path didn't start with #: " + contentReference + " (" + getPath() + ")");
		}
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
		return Optional.ofNullable(definition.getSliceName());
	}

	@Override
	public Optional<String> getId() {
		return Optional.ofNullable(definition.getId());
	}

	@Override
	public FhirVersion getVersion() {
		return FhirVersion.STU3;
	}
}
