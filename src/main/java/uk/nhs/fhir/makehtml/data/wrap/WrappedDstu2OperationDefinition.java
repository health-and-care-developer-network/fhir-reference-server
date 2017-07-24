package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.ParameterBinding;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.OperationKindEnum;
import ca.uhn.fhir.model.dstu2.valueset.OperationParameterUseEnum;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.BindingResourceInfo;
import uk.nhs.fhir.makehtml.data.FhirOperationParameter;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.util.Dstu2FhirDocLinkFactory;
import uk.nhs.fhir.util.HAPIUtils;
import uk.nhs.fhir.util.StringUtil;

public class WrappedDstu2OperationDefinition extends WrappedOperationDefinition {

	private final OperationDefinition definition;
	private final Dstu2FhirDocLinkFactory linkFactory = new Dstu2FhirDocLinkFactory();
	
	public WrappedDstu2OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}
	
	private ResourceInfo buildBindingResourceInfo(ParameterBinding binding) {
		String choice = HAPIUtils.resolveDstu2DatatypeValue(binding.getValueSet());
		String strength = binding.getStrength();
		
		return new BindingResourceInfo(Optional.empty(), Optional.of(FhirURL.buildOrThrow(choice)), strength);
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
		return new SimpleLinkData(FhirURL.buildOrThrow(definition.getKindElement().getValueAsEnum().getSystem()), OperationKindEnum.VALUESET_NAME);
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
				new FhirOperationParameter(param.getName(), param.getMin(), param.getMax(), 
						linkFactory.forDataType(param.getTypeElement()), param.getDocumentation(), getResourceInfos(param)))
			.collect(Collectors.toList());
	}

	private List<ResourceInfo> getResourceInfos(Parameter parameter) {
		
		List<ResourceInfo> resourceFlags = Lists.newArrayList();
		
		ParameterBinding binding = parameter.getBinding();
		if (!binding.isEmpty()) {
			ResourceInfo bindingFlag = buildBindingResourceInfo(binding);
			resourceFlags.add(bindingFlag);
		}
		
		ResourceReferenceDt profile = parameter.getProfile();
		if (!profile.isEmpty()) {
			resourceFlags.add(new ResourceInfo("Profile", FhirURL.buildOrThrow(profile.getReferenceElement().getValue()),  ResourceInfoType.PROFILE));
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
		definition.setUrl(url);
	}

	@Override
	public void fixHtmlEntities() {
		// nothing to do
	}

	@Override
	public void addHumanReadableText(String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}
}
