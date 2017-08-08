package uk.nhs.fhir.makehtml.data.wrap.stu3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.AggregationMode;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ElementDefinitionSlicingComponent;
import org.hl7.fhir.dstu3.model.ElementDefinition.ReferenceVersionRules;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.hl7.fhir.dstu3.model.PrimitiveType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.utilities.Utilities;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.context.FhirStu3DataTypes;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.structdef.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.structdef.ExtensionType;
import uk.nhs.fhir.makehtml.data.structdef.FhirElementMapping;
import uk.nhs.fhir.makehtml.data.structdef.ResourceFlags;
import uk.nhs.fhir.makehtml.data.structdef.SlicingInfo;
import uk.nhs.fhir.makehtml.data.url.FhirURL;
import uk.nhs.fhir.makehtml.data.url.LinkData;
import uk.nhs.fhir.makehtml.data.url.LinkDatas;
import uk.nhs.fhir.makehtml.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.makehtml.html.RendererError;
import uk.nhs.fhir.makehtml.html.ValuesetLinkFix;
import uk.nhs.fhir.util.FhirContexts;

public class WrappedStu3ElementDefinition extends WrappedElementDefinition {
	
	private static final Stu3FhirDocLinkFactory typeLinkFactory = new Stu3FhirDocLinkFactory();
	private static final FhirDataTypes<TypeRefComponent> fhirDataTypes = new FhirStu3DataTypes(); 
	
	private final ElementDefinition definition;

	public WrappedStu3ElementDefinition(ElementDefinition definition) {
		this.definition = definition;
	}

	@Override
	public String getName() {
		return definition.getLabel();
	}

	@Override
	public String getPath() {
		return definition.getPath();
	}

	@Override
	public LinkDatas getTypeLinks() {
		LinkDatas typeLinks = new LinkDatas();
		
		List<TypeRefComponent> knownTypes = fhirDataTypes.knownTypes(definition.getType());
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
					} else {
						throw new IllegalStateException("should we be incorporating profile (" + profile + ") into type links? " + getPath());
					}
				} else if (type.hasTargetProfile()) {
					if (type.getCode().equals("Reference")) {
						LinkData referenceLink = typeLinkFactory.forDataTypeName(type.getCode());
						typeLinks.addNestedUri(referenceLink, type.getTargetProfile(), FhirVersion.STU3);
					} else if (type.getCode().equals("string")){
						RendererError.handle(RendererError.Key.TYPELINK_STRING_WITH_PROFILE, "Type link with type " + type.getCode() + " and a target profile " + type.getTargetProfile() + " - dropping targetProfile (" + getPath() + ")");
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
	public Set<FhirDataType> getDataTypes() {
		return FhirStu3DataTypes.getTypes(definition.getType());
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
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + fixed.getClass().getCanonicalName());
			}
		}
	}

	@Override
	public List<String> getExamples() {
		return definition.getExample().stream().map(example -> example.getValue().toString()).collect(Collectors.toList());
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
				String urlString = FhirStu3DataTypes.resolveValue(valueSet);
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
	public Optional<ExtensionType> getExtensionType() {
		if (!definition.getSlicing().isEmpty()) {
			return Optional.empty();
		}
		
		for (TypeRefComponent type : definition.getType()) {
			if (type.getCode() != null 
			  && type.getCode().equals("Extension")) {
				return Optional.of(lookupExtensionType(type));
			}
		}
		
		return Optional.empty();
	}

	private ExtensionType lookupExtensionType(TypeRefComponent type) {
		
		String profile = type.getProfile();
		if (profile == null) {
			return ExtensionType.SIMPLE;
		}
		
		String targetProfile = type.getTargetProfile();
		List<Enumeration<AggregationMode>> aggregation = type.getAggregation();
		ReferenceVersionRules versioning = type.getVersioning();

		if (!Utilities.noString(targetProfile)) {
			throw new IllegalStateException("Don't know how to handle target profile for Extension");
		}
		if (!aggregation.isEmpty()) {
			throw new IllegalStateException("Don't know how to handle aggregation for Extension");
		}
		if (versioning != null) {
			throw new IllegalStateException("Don't know how to handle versioning for Extension");
		}

		String filePath;
		try {
			URI uri = new URI(profile);
			filePath = uri.toURL().getFile() + ".xml";
		} catch (URISyntaxException | MalformedURLException e) {
			throw new IllegalStateException("URI/URL error for uri " + profile, e);
		}
		
		String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
		// case insensitive search
		File extensionFile = null;
		for (File f : new File(NewMain.getSuppliedResourcesFolderPath()).listFiles()) {
			if (f.getName().toLowerCase().equals(fileName.toLowerCase())) {
				extensionFile = f;
				break;
			}
		}

		if (extensionFile == null) {
			RendererError.handle(RendererError.Key.EXTENSION_FILE_NOT_FOUND, "Extension source expected at: " + fileName);
		} 
		
		try (FileInputStream fis = new FileInputStream(extensionFile);
			Reader reader = new InputStreamReader(fis)) {
			
			IParser parser = FhirContexts.xmlParser(FhirVersion.STU3);
			StructureDefinition extension = parser.parseResource(StructureDefinition.class, reader);
			
			org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind kind = extension.getKind();
			if (kind.equals(StructureDefinitionKind.COMPLEXTYPE)) {
				return ExtensionType.COMPLEX;
			} else if (kind.equals(StructureDefinitionKind.PRIMITIVETYPE)) {
				return ExtensionType.SIMPLE;
			} else {
				throw new IllegalStateException("Not sure whether extension " + extensionFile.getAbsolutePath() 
					+ " is simple or complex - kind is " + kind.getDisplay());
			}
		} catch (IOException ie) {
			throw new IllegalStateException(ie);
		}
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
