package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.util.StringUtil;

public abstract class WrappedResource<T extends WrappedResource<T>> {

	public abstract IBaseMetaType getSourceMeta();
	public abstract FhirVersion getImplicitFhirVersion();
	
	/**
	 * Returns the formatter which will be used to generate the <Text/> section in the profile when supplied as a raw profile
	 * rather than as a web page for a browser.
	 */
	public abstract ResourceFormatter<T> getDefaultViewFormatter();
	public abstract List<FormattedOutputSpec<T>> getFormatSpecs(String outputDirectory);

	public boolean isDstu2() {
		return getImplicitFhirVersion().equals(FhirVersion.DSTU2);
	};
	public boolean isStu3() {
		return getImplicitFhirVersion().equals(FhirVersion.STU3);
	};
	
	private Optional<IBaseMetaType> getMeta() {
		IBaseMetaType metaInfo = getSourceMeta();
		if (!metaInfo.isEmpty()) {
			return Optional.of(metaInfo);
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getVersionId() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			return Optional.ofNullable(metaInfo.get().getVersionId());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<String> getLastUpdated() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			Date lastUpdated = metaInfo.get().getLastUpdated();
			if (lastUpdated != null) {
				return Optional.of(StringUtil.dateToString(lastUpdated));
			}
		}
		
		return Optional.empty();
	}
	
	public static WrappedResource<?> fromBaseResource(IBaseResource resource) {
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return new WrappedDstu2StructureDefinition((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return new WrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			return new WrappedDstu2ValueSet((ca.uhn.fhir.model.dstu2.resource.ValueSet)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			return new WrappedStu3ValueSet((org.hl7.fhir.dstu3.model.ValueSet)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			return new WrappedDstu2OperationDefinition((ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.OperationDefinition) {
			return new WrappedStu3OperationDefinition((org.hl7.fhir.dstu3.model.OperationDefinition)resource);
		}
		
		else {
			throw new IllegalStateException("Couldn't make a WrappedResource for " + resource.getClass().getCanonicalName());
		}
	}
}
