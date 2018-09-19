package uk.nhs.fhir.data.wrap.dstu2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.ParameterBinding;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.OperationKindEnum;
import ca.uhn.fhir.model.dstu2.valueset.OperationParameterUseEnum;
import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.ResourceInfoType;
import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.structdef.FhirElementDataTypeDstu2;
import uk.nhs.fhir.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class WrappedDstu2OperationDefinition extends WrappedOperationDefinition {

	private final OperationDefinition definition;
	private static final Dstu2FhirDocLinkFactory linkFactory = new Dstu2FhirDocLinkFactory();
	
	public WrappedDstu2OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
		checkForUnexpectedFeatures();
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}
	
	private static ResourceInfo buildBindingResourceInfo(ParameterBinding binding) {
		String choice = FhirElementDataTypeDstu2.resolveDstu2DatatypeValue(binding.getValueSet());
		String strength = binding.getStrength();
		
		return new BindingResourceInfo(Optional.empty(), Optional.of(FhirURL.buildOrThrow(choice, FhirVersion.DSTU2)), strength);
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
		return StringUtil.capitaliseLowerCase(definition.getKindElement().getValueAsEnum().getCode());
	}

	@Override
	public LinkData getKindTypeLink() {
		return new LinkData(
			FhirURL.buildOrThrow(definition.getKindElement().getValueAsEnum().getSystem(), getImplicitFhirVersion()), 
			OperationKindEnum.VALUESET_NAME);
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
		return linkFactory.forDataTypeName("code");
	}

	@Override
	public String getIsSystem() {
		return definition.getSystem().toString();
	}

	@Override
	public LinkData getSystemTypeLink() {
		return linkFactory.forDataType(definition.getSystemElement());
	}
	
	@Override
	public String getIsInstance() {
		return definition.getInstance().toString();
	}
	
	@Override
	public LinkData getInstanceTypeLink() {
		return linkFactory.forDataType(definition.getInstanceElement());
	}
	
	@Override
	public String getIsType() { 
		return definition.getType().toString();
	}
	
	@Override
	public LinkData getTypeLink() {
		return linkFactory.forDataType(definition.getTypeFirstRep());
	}

	
	@Override
	public List<FhirOperationParameter> getInputParameters() {
		return parametersOfType(OperationParameterUseEnum.IN);
	}

	@Override
	public List<FhirOperationParameter> getOutputParameters() {
		return parametersOfType(OperationParameterUseEnum.OUT);
	}
	
	private List<FhirOperationParameter> parametersOfType(OperationParameterUseEnum type) {
		return definition.getParameter()
			.stream()
			.filter(param -> param.getUseElement().getValueAsEnum().equals(type))
			.map(param -> 
				buildParameter("", param))
			.collect(Collectors.toList());
	}

	private static List<FhirOperationParameter> getParts(String namePrefix, OperationDefinition.Parameter param) {
		return 
			param
				.getPart()
				.stream()
				.map(part -> 
					buildParameter(namePrefix, part))
				.collect(Collectors.toList());
	}
	
	private static FhirOperationParameter buildParameter(String namePrefix, OperationDefinition.Parameter param) {
		String paramLocalName = param.getName();
		String paramFullName = namePrefix.isEmpty() ? paramLocalName : namePrefix + "." + paramLocalName; 
		
		return new FhirOperationParameter(
				param.getName(), 
				param.getMin(), 
				param.getMax(), 
				param.getTypeElement().isEmpty() ? Optional.empty() : Optional.of(linkFactory.forDataType(param.getTypeElement())),
				param.getDocumentation(),
				getResourceInfos(param),
				getParts(paramFullName, param));
	}

	private static List<ResourceInfo> getResourceInfos(Parameter parameter) {
		
		List<ResourceInfo> resourceFlags = Lists.newArrayList();
		
		ParameterBinding binding = parameter.getBinding();
		if (!binding.isEmpty()) {
			ResourceInfo bindingFlag = buildBindingResourceInfo(binding);
			resourceFlags.add(bindingFlag);
		}
		
		ResourceReferenceDt profile = parameter.getProfile();
		if (!profile.isEmpty()) {
			resourceFlags.add(
				new ResourceInfo(
					"Profile", 
					FhirURL.buildOrThrow(profile.getReferenceElement().getValue(), FhirVersion.DSTU2), 
					ResourceInfoType.PROFILE));
		}
		
		List<Parameter> parts = parameter.getPart();
		if (!parts.isEmpty()) {
			//TODO tuple parameters
			throw new NotImplementedException("DSTU2 OperationDefinition Tuple parameter");
		}
		
		return resourceFlags;
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}

	@Override
	public void setUrl(String url) {
		definition.setUrl(Preconditions.checkNotNull(url));
	}

	@Override
	public void addHumanReadableText(String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getStatus() {
		return definition.getStatus();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public void checkForUnexpectedFeatures() {
		// TODO
		//definition.getUrlElement();
		
		// TODO
		//definition.getVersionElement();
		
		//definition.getNameElement();
		//definition.getStatusElement();
		//definition.getKindElement();
		
		// TODO
		//definition.getExperimentalElement();
		
		// TODO
		//definition.getPublisherElement();
		
		// TODO
		//definition.getContact();
		
		// TODO
		//definition.getDateElement();
		
		//definition.getDescriptionElement();
		
		// TODO
		//definition.getRequirementsElement();

		// TODO
		//definition.getIdempotentElement();
		
		//defintion.getCodeElement();
		
		// TODO
		//definition.getNotesElement();
		
		// TODO
		//definition.getBase();
		
		//definition.getSystemElement();
		
		// TODO
		//definition.getType();
		
		//definition.getInstanceElement();
		
		for (Parameter param : definition.getParameter()) {
			checkForUnexpectedFeaturesParam(param);
		}
	}
	
	public void checkForUnexpectedFeaturesParam(Parameter param) {
		//param.getNameElement();
		
		// TODO
		//param.getUseElement();
		
		//param.getMinElement();
		//param.getMaxElement();
		//param.getDocumentationElement();
		//param.getTypeElement();
		
		// TODO
		//param.getProfile();
		// TODO
		//param.getBinding();
		
		for (Parameter part : param.getPart()) {
			checkForUnexpectedFeaturesParam(part);
		}
	}
}
