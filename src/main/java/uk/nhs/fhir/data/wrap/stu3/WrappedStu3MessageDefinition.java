package uk.nhs.fhir.data.wrap.stu3;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageDefinitionAllowedResponseComponent;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageDefinitionFocusComponent;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageSignificanceCategory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.message.MessageResponse;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ListUtils;

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
			Optional<String> title = getTitle();
			if (title.isPresent()) {
				return title.get();
			}
			throw new NullPointerException("Expected name or title to be present");
		} else {
			return name;
		}
	}

	@Override
	public Optional<String> getTitle() {
		return Optional.ofNullable(definition.getTitle());
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

	@Override
	public Optional<FhirIdentifier> getIdentifier() {
		return Optional.ofNullable(definition.getIdentifier())
				.map(id -> new FhirIdentifier(id.getValue(), id.getSystem()));
		
	}

	@Override
	public Date getDate() {
		return definition.getDate();
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}
	
	@Override
	public String getEvent() {
		return definition.getEvent().getDisplay();
	}
	
	@Override
	public Optional<String> getCategory() {
		return Optional.ofNullable(definition.getCategory()).map(MessageSignificanceCategory::getDisplay);
	}
	
	@Override
	public List<MessageResponse> getAllowedResponses() {
		List<MessageResponse> allowedResponses = Lists.newArrayList();
		
		for (MessageDefinitionAllowedResponseComponent response : definition.getAllowedResponse()) {
			
		}
		
		return allowedResponses;
	}

	@Override
	public MessageDefinitionFocus getFocus() {
		
		MessageDefinitionFocusComponent focus = ListUtils.expectUnique(definition.getFocus(), "focus component");
		
		return new MessageDefinitionFocus();
	}

	private void checkForUnexpectedFeatures() {
		// checkNoInfoPresent(definition.getUrl());
		// checkNoInfoPresent(definition.getIdentifier());
		// checkNoInfoPresent(definition.getVersion());
		// checkNoInfoPresent(definition.getName());
		// checkNoInfoPresent(definition.getTitle());
		// checkNoInfoPresent(definition.getStatus());
		checkNoInfoPresent(definition.getExperimentalElement());
		// checkNoInfoPresent(definition.getDate());
		checkNoInfoPresent(definition.getPublisher());
		checkNoInfoPresent(definition.getContact());
		// checkNoInfoPresent(definition.getDescription());
		checkNoInfoPresent(definition.getUseContext());
		checkNoInfoPresent(definition.getJurisdiction());
		checkNoInfoPresent(definition.getPurpose());
		checkNoInfoPresent(definition.getBase());
		checkNoInfoPresent(definition.getParent());
		// checkNoInfoPresent(definition.getCopyright());
		checkNoInfoPresent(definition.getReplaces());
		// checkNoInfoPresent(definition.getEvent());
		// checkNoInfoPresent(definition.getCategory());
		// checkNoInfoPresent(definition.getFocus());
		checkNoInfoPresent(definition.getResponseRequiredElement());
		// checkNoInfoPresent(definition.getAllowedResponse());
	}
	
}
