package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;

import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirOperationParameter;
import uk.nhs.fhir.makehtml.data.LinkData;

public class WrappedStu3OperationDefinition extends WrappedOperationDefinition {

private final OperationDefinition definition;
	
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

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getNameTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getKindTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getDescriptionTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getCodeTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getSystemTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkData getInstanceTypeLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FhirOperationParameter> getInputParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FhirOperationParameter> getOutputParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBaseResource getWrappedResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fixHtmlEntities() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHumanReadableText(String textSection) {
		// TODO Auto-generated method stub
		
	}
}
