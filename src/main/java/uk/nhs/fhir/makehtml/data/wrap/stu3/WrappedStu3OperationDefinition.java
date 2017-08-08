package uk.nhs.fhir.makehtml.data.wrap.stu3;

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

import ca.uhn.fhir.context.FhirStu3DataTypes;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.makehtml.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.makehtml.data.url.FhirURL;
import uk.nhs.fhir.makehtml.data.url.LinkData;
import uk.nhs.fhir.makehtml.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.util.StringUtil;

public class WrappedStu3OperationDefinition extends WrappedOperationDefinition {

private final OperationDefinition definition;

	private final Stu3FhirDocLinkFactory linkFactory = new Stu3FhirDocLinkFactory();
	
	public WrappedStu3OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}
	
	private ResourceInfo buildBindingResourceInfo(OperationDefinitionParameterBindingComponent binding) {
		String choice = FhirStu3DataTypes.resolveValue(binding.getValueSet());
		String strength = binding.getStrength().getDisplay();
		
		return new BindingResourceInfo(Optional.empty(), Optional.of(FhirURL.buildOrThrow(choice, getImplicitFhirVersion())), strength);
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
	public String getDescription() {
		return definition.getDescription();
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
				new FhirOperationParameter(param.getName(), param.getMin(), param.getMax(), 
						linkFactory.forDataType(param.getTypeElement()), param.getDocumentation(), getResourceInfos(param)))
			.collect(Collectors.toList());
	}

	private List<ResourceInfo> getResourceInfos(OperationDefinitionParameterComponent parameter) {
		
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
					FhirURL.buildOrThrow(profile.getReferenceElement().getValue(), getImplicitFhirVersion()), 
					ResourceInfoType.PROFILE));
		}
		
		return resourceFlags;
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
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
}
