package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ElementDefinition;

import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.ExtensionType;
import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.FhirElementMapping;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.data.SlicingInfo;

public class WrappedDstu3ElementDefinition extends WrappedElementDefinition {

	private final ElementDefinition definition;

	public WrappedDstu3ElementDefinition(ElementDefinition definition) {
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
	public List<LinkData> getTypeLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<FhirDataType> getDataTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceFlags getResourceFlags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getCardinalityMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCardinalityMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getConditionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ConstraintInfo> getConstraintInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<SlicingInfo> getSlicing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getFixedValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getExample() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<BindingInfo> getBinding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getRequirements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ExtensionType> getExtensionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getLinkedNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FhirElementMapping> getMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getSliceName() {
		return Optional.ofNullable(definition.getSliceName());
	}

}
