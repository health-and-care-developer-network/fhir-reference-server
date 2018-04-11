package uk.nhs.fhir.data.wrap.stu3;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3MessageDefinition extends WrappedMessageDefinition {
	
	private MessageDefinition definition;

	public WrappedStu3MessageDefinition(MessageDefinition definition) {
		this.definition = definition;
		checkForUnexpectedFeatures();
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
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
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public void setUrl(String url) {
		definition.setUrl(Preconditions.checkNotNull(url));
	}

	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse(getName());
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public String getName() {
		String name = definition.getName();
		
		if (name == null) {
			throw new NullPointerException("Expected name to be present");
		} else {
			return name;
		}
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
	protected ResourceMetadata getMetadataImpl(File source) {
		String resourceName = getName();
		String displayGroup = "Message Definitions";
		String resourceID = getIdFromUrl().orElse(resourceName);
		VersionNumber versionNo = parseVersionNumber();
		String status = getStatus();
		FhirVersion fhirVersion = getImplicitFhirVersion();
		String url = getUrl().get();
		
		return new ResourceMetadata(resourceName, source, ResourceType.MESSAGEDEFINITION,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, fhirVersion, url);
	}

	@Override
	public ResourceType getResourceType() {
		return ResourceType.MESSAGEDEFINITION;
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(definition.getDescription());
	}

	@Override
	public String getStatus() {
		String status = definition.getStatus().getDisplay();
		if (Strings.isNullOrEmpty(status)) {
			throw new IllegalStateException("Must have a status");
		} else {
			return status;
		}
	}

	private void checkForUnexpectedFeatures() {
	}
	
}
