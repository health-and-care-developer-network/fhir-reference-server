package uk.nhs.fhir.data.wrap.stu3;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.dstu3.model.OperationDefinition.OperationDefinitionParameterBindingComponent;
import org.hl7.fhir.dstu3.model.OperationDefinition.OperationDefinitionParameterComponent;
import org.hl7.fhir.dstu3.model.OperationDefinition.OperationParameterUse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.ResourceInfoType;
import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.structdef.FhirElementDataTypeStu3;
import uk.nhs.fhir.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class WrappedStu3OperationDefinition extends WrappedOperationDefinition {

private final OperationDefinition definition;

	private static final Stu3FhirDocLinkFactory linkFactory = new Stu3FhirDocLinkFactory();
	
	public WrappedStu3OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
		checkForUnexpectedFeatures();
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}
	
	private static ResourceInfo buildBindingResourceInfo(OperationDefinitionParameterBindingComponent binding) {
		String choice = FhirElementDataTypeStu3.resolveValue(binding.getValueSet());
		String strength = binding.getStrength().getDisplay();
		
		return new BindingResourceInfo(Optional.empty(), Optional.of(FhirURL.buildOrThrow(choice, FhirVersion.STU3)), strength);
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public LinkData getNameTypeLink() {
		return linkFactory.forDataType(definition.getNameElement());
	}

	@Override
	public String getKind() {
		return StringUtil.capitaliseLowerCase(definition.getKindElement().getValueAsString());
	}

	@Override
	public LinkData getKindTypeLink() {
		return new LinkData(
			FhirURL.buildOrThrow(definition.getKindElement().getValueAsString(), getImplicitFhirVersion()), 
			"OperationKind");
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(definition.getDescription());
	}

	@Override
	public LinkData getDescriptionTypeLink() {
		return linkFactory.forDataType(definition.getDescriptionElement());
	}

	@Override
	public String getCode() {
		return definition.getCode();
	}

	@Override
	public LinkData getCodeTypeLink() {
		return linkFactory.forDataType(definition.getCodeElement());
	}

	@Override
	public String getIsSystem() {
		return Boolean.toString(definition.getSystem());
	}

	@Override
	public LinkData getSystemTypeLink() {
		return linkFactory.forDataType(definition.getSystemElement());
	}
	
	@Override
	public String getIsInstance() {
		return Boolean.toString(definition.getInstance());
	}

	@Override
	public LinkData getInstanceTypeLink() {
		return linkFactory.forDataType(definition.getInstanceElement());
	}

	@Override
	public List<FhirOperationParameter> getInputParameters() {
		return parametersOfType(OperationParameterUse.IN);
	}

	@Override
	public List<FhirOperationParameter> getOutputParameters() {
		return parametersOfType(OperationParameterUse.OUT);
	}
	
	private List<FhirOperationParameter> parametersOfType(OperationParameterUse type) {
		return definition.getParameter()
			.stream()
			.filter(param -> param.getUseElement().getValue().equals(type))
			.map(param -> 
				buildParameter("", param))
			.collect(Collectors.toList());
	}

	private static List<ResourceInfo> getResourceInfos(OperationDefinitionParameterComponent parameter) {
		
		List<ResourceInfo> resourceFlags = Lists.newArrayList();
		
		OperationDefinitionParameterBindingComponent binding = parameter.getBinding();
		if (!binding.isEmpty()) {
			ResourceInfo bindingFlag = buildBindingResourceInfo(binding);
			resourceFlags.add(bindingFlag);
		}
		
		Reference profile = parameter.getProfile();
		if (!profile.isEmpty()) {
			resourceFlags.add(
				new ResourceInfo(
					"Profile", 
					FhirURL.buildOrThrow(profile.getReferenceElement().getValue(), FhirVersion.STU3), 
					ResourceInfoType.PROFILE));
		}
		
		return resourceFlags;
	}

	private static List<FhirOperationParameter> getParts(String namePrefix, OperationDefinitionParameterComponent param) {
		return 
			param
				.getPart()
				.stream()
				.map(part -> 
					buildParameter(namePrefix, part))
				.collect(Collectors.toList());
	}
	
	private static FhirOperationParameter buildParameter(String namePrefix, OperationDefinitionParameterComponent param) {
		String paramLocalName = param.getName();
		String paramFullName = namePrefix.isEmpty() ? paramLocalName : namePrefix + "." + paramLocalName; 
		
		return new FhirOperationParameter(
				paramFullName, 
				param.getMin(), 
				param.getMax(), 
				param.getTypeElement().isEmpty() ? Optional.empty() : Optional.of(linkFactory.forDataType(param.getTypeElement())),
				param.getDocumentation(),
				getResourceInfos(param),
				getParts(paramFullName, param));
	}


	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public void setUrl(String url) {
		definition.setUrl(url);
	}

	@Override
	public void fixHtmlEntities() {
		// nothing to do
	}

	@Override
	public void addHumanReadableText(String textSection) {
		try {
			Narrative textElement = Factory.newNarrative(NarrativeStatus.GENERATED, textSection);
	        definition.setText(textElement);
		} catch (IOException | FHIRException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getStatus() {
		return definition.getStatus().getDisplay();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public void checkForUnexpectedFeatures() {
		// TODO Not included, but should be. Include when refactoring
		//definition.getUrlElement();
		
		// TODO Not included, but should be. Include when refactoring
		//definition.getVersionElement();
		
		//definition.getNameElement();
		//definition.getStatusElement();
		//definition.getKindElement();
		
		// TODO Not included, but should be. Include when refactoring
		//definition.getExperimentalElement();
		// TODO Not included, but should be. Include when refactoring
		//definition.getDateElement();
		// TODO Not included, but should be. Include when refactoring
		//definition.getPublisherElement();
		
		// TODO Not included, but should be. Include when refactoring
		/*for (ContactDetail contact : definition.getContact()) {
			
		}*/
		
		//definition.getDescriptionElement();
		/*for (UsageContext useContext : definition.getUseContext()) {
			
		}*/
		
		checkNoInfoPresent(definition.getJurisdiction());
		/*for (CodeableConcept jurisdiction : definition.getJurisdiction()) {
			
		}*/
		checkNoInfoPresent(definition.getPurposeElement());
		checkNoInfoPresent(definition.getIdempotentElement());
		//definition.getCodeElement();
		checkNoInfoPresent(definition.getCommentElement());
		checkNoInfoPresent(definition.getBase());
		
		// TODO Not included, but should be. Include when refactoring
		//definition.getResource();
		
		/*for (CodeType resource : definition.getResource()) {
			
		}*/
		//definition.getSystemElement();
		
		// TODO Not included, but should be. Include when refactoring
		//definition.getTypeElement();
		
		//definition.getInstanceElement();
		for (OperationDefinitionParameterComponent param : definition.getParameter()) {
			checkForUnexpectedFeaturesParam(param);
		}
		checkNoInfoPresent(definition.getOverload());
		/*for (OperationDefinitionOverloadComponent overload : definition.getOverload()) {
			for (StringType paramName : overload.getParameterName()) {
				
			}
			overload.getCommentElement();
		}*/
	}
	
	public void checkForUnexpectedFeaturesParam(OperationDefinitionParameterComponent param) {
		//param.getNameElement();
		
		// TODO Not included, but should be. Include when refactoring
		//checkNoInfoPresent(param.getUseElement());
		
		//param.getMinElement();
		//param.getMaxElement();
		//param.getDocumentationElement();
		//param.getTypeElement();
		checkNoInfoPresent(param.getSearchTypeElement());
		
		// TODO Not included, but should be. Include when refactoring
		//param.getProfile();

		// TODO Not included, but should be. Include when refactoring, with both subelements
		/*OperationDefinitionParameterBindingComponent binding = param.getBinding();
		if (!binding.isEmpty()) {
			binding.getStrengthElement();
			binding.getValueSet();
		}*/
		for (OperationDefinitionParameterComponent part : param.getPart()) {
			checkForUnexpectedFeaturesParam(part);
		}
	}
}
