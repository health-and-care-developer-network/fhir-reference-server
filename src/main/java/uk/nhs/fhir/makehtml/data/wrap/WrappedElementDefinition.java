package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ElementDefinition;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.ExtensionType;
import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.FhirElementMapping;
import uk.nhs.fhir.makehtml.data.LinkDatas;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.data.SlicingInfo;

public abstract class WrappedElementDefinition {

	public abstract String getName();
	public abstract String getPath();
	public abstract LinkDatas getTypeLinks();
	public abstract Set<FhirDataType> getDataTypes();
	public abstract ResourceFlags getResourceFlags();
	public abstract Integer getCardinalityMin();
	public abstract String getCardinalityMax();
	public abstract String getShortDescription();
	public abstract Set<String> getConditionIds();
	public abstract List<ConstraintInfo> getConstraintInfos();
	public abstract Optional<String> getDefinition();
	public abstract Optional<SlicingInfo> getSlicing();
	public abstract Optional<String> getFixedValue();
	public abstract List<String> getExamples();
	public abstract Optional<String> getDefaultValue();
	public abstract Optional<BindingInfo> getBinding();
	public abstract Optional<String> getRequirements();
	public abstract Optional<String> getComments();
	public abstract List<String> getAliases();
	public abstract Optional<ExtensionType> getExtensionType();
	public abstract Optional<String> getLinkedNodeName();
	public abstract Optional<String> getLinkedNodePath();
	public abstract List<FhirElementMapping> getMappings();
	public abstract Optional<String> getId();
	public abstract FhirVersion getVersion();
	
	// Introduced with STU3
	
	public abstract Optional<String> getSliceName();
	
	public static WrappedElementDefinition fromDefinition(Object definition) {
		if (definition instanceof ElementDefinitionDt) {
			return new WrappedDstu2ElementDefinition((ElementDefinitionDt)definition);
		} else if (definition instanceof ElementDefinition) {
			return new WrappedStu3ElementDefinition((ElementDefinition)definition);
		} else {
			throw new IllegalStateException("Can't wrap element definition class " + definition.getClass().getCanonicalName());
		}
	}
	
	public String[] getPathParts() {
		return getPath().split("\\.");
	}
	
	public boolean isRootElement() {
		return getPathParts().length == 1;
	}
}
