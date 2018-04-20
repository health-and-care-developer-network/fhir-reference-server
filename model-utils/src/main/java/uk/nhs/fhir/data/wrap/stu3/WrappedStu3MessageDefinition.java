package uk.nhs.fhir.data.wrap.stu3;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageDefinitionAllowedResponseComponent;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageDefinitionFocusComponent;
import org.hl7.fhir.dstu3.model.MessageDefinition.MessageSignificanceCategory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.message.MessageDefinitionAsset;
import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.message.MessageResponse;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ListUtils;

public class WrappedStu3MessageDefinition extends WrappedMessageDefinition {
	
	private MessageDefinition definition;

	private static final String EXTENSION_URL_UTILISED_ASSET = "utilisedAsset";
	private static final String EXTENSION_URL_TYPE = "type";
	private static final String EXTENSION_URL_REFERENCE = "reference";
	private static final String EXTENSION_URL_VERSION = "version";
	
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
			FhirURL messageDefinitionId = FhirURL.buildOrThrow(response.getMessage().getReference(), getImplicitFhirVersion());
			allowedResponses.add(new MessageResponse(messageDefinitionId));
		}
		
		return allowedResponses;
	}

	@Override
	public MessageDefinitionFocus getFocus() {
		MessageDefinitionFocusComponent sourceFocus = ListUtils.expectUnique(definition.getFocus(), "MessageDefinition focus components");
		
		if (!sourceFocus.hasProfile()) {
			throw new IllegalStateException("Expected Message Definition focus to have a profile");
		}

		String code = sourceFocus.getCode();
		Reference sourceProfile = sourceFocus.getProfile();
		String profile = sourceProfile.getReference();
		
		Extension bundleExtension = ListUtils.expectUnique(sourceProfile.getExtension(), "top level profile extensions");
		String bundleExtensionUrl = bundleExtension.getUrl();

		MessageDefinitionFocus focus = new MessageDefinitionFocus(code, profile, bundleExtensionUrl);
		
		for (Extension assetExtension : bundleExtension.getExtension()) {
			if (!assetExtension.getUrl().equals(EXTENSION_URL_UTILISED_ASSET)) {
				throw new IllegalStateException("Expected contained extension to have url \"" + EXTENSION_URL_UTILISED_ASSET + "\" but found " + assetExtension.getUrl());
			}

			String assetCode = null;
			String structureDefinitionReference = null;
			String structureDefinitionVersion = null;
			
			for (Extension assetDetailExtension : assetExtension.getExtension()) {
				switch (assetDetailExtension.getUrl()) {
					case EXTENSION_URL_TYPE:
						Type typeValue = assetDetailExtension.getValue();
						if (typeValue instanceof CodeType) {
							CodeType typeCode = (CodeType)typeValue;
							assetCode = typeCode.asStringValue();
						} else if (typeValue instanceof Coding) {
							Coding typeCoding = (Coding)typeValue;
							EventHandlerContext.forThread().event(RendererEventType.MESSAGE_ASSET_TYPE_CODING, "Found Coding for MessageDefinition Asset (should be Code)");
							assetCode = typeCoding.getCode();
						} else {
							throw new IllegalStateException("Expected \"" + EXTENSION_URL_TYPE + "\" extension to have value of type Code, but found " + typeValue.getClass().getSimpleName());
						}
						break;
					case EXTENSION_URL_REFERENCE:
						Reference ref = (Reference)assetDetailExtension.getValue();
						structureDefinitionReference = ref.getReference();
						break;
					case EXTENSION_URL_VERSION:
						String versionString = assetDetailExtension.getValueAsPrimitive().getValueAsString();
						try {
							// validate
							new VersionNumber(versionString);
							structureDefinitionVersion = versionString;
						} catch (Exception e) {
							if (!WrappedMessageDefinition.PERMITTED_VERSION_STRINGS.contains(versionString)) {
								EventHandlerContext.forThread().event(RendererEventType.UNRECOGNISED_MESSAGE_ASSET_VERSION, "Didn't recognise version string \"" + versionString + "\"");
							}
							structureDefinitionVersion = versionString;
						}
						break;
					default: 
						throw new IllegalStateException("Unexpected asset detail extension URL: " + assetDetailExtension.getUrl());
				}
			}
			
			if (assetCode == null) {throw new IllegalStateException("Asset didn't have a \"" + EXTENSION_URL_TYPE + "\" extension");}
			if (structureDefinitionReference == null) {throw new IllegalStateException("Asset didn't have a \"" + EXTENSION_URL_REFERENCE + "\" extension");}
			if (structureDefinitionVersion == null) {throw new IllegalStateException("Asset didn't have a \"" + EXTENSION_URL_VERSION + "\" extension");}
			
			focus.addAsset(new MessageDefinitionAsset(assetCode, structureDefinitionReference, structureDefinitionVersion));
		}
		
		if (focus.getAssets().isEmpty()) {
			throw new IllegalStateException("Focus didn't contain any assets");
		}
		
		return focus;
	}

	private void checkForUnexpectedFeatures() {
		// checkNoInfoPresent(definition.getUrl());
		// checkNoInfoPresent(definition.getIdentifier());
		// checkNoInfoPresent(definition.getVersion());
		// checkNoInfoPresent(definition.getName());
		// checkNoInfoPresent(definition.getTitle());
		// checkNoInfoPresent(definition.getStatus());
		checkNoInfoPresent(definition.getExperimentalElement(), "MessageDefinition.experimental");
		// checkNoInfoPresent(definition.getDate());
		checkNoInfoPresent(definition.getPublisher(), "MessageDefinition.publisher");
		checkNoInfoPresent(definition.getContact(), "MessageDefinition.contact");
		// checkNoInfoPresent(definition.getDescription());
		checkNoInfoPresent(definition.getUseContext(), "MessageDefinition.useContext");
		checkNoInfoPresent(definition.getJurisdiction(), "MessageDefinition.jurisdiction");
		checkNoInfoPresent(definition.getPurpose(), "MessageDefinition.purpose");
		checkNoInfoPresent(definition.getBase(), "MessageDefinition.base");
		checkNoInfoPresent(definition.getParent(), "MessageDefinition.parent");
		// checkNoInfoPresent(definition.getCopyright());
		checkNoInfoPresent(definition.getReplaces(), "MessageDefinition.replaces");
		// checkNoInfoPresent(definition.getEvent());
		// checkNoInfoPresent(definition.getCategory());
		// checkNoInfoPresent(definition.getFocus());
		for (MessageDefinitionFocusComponent focus : definition.getFocus()) {
			checkNoInfoPresent(focus.getMinElement());
			checkNoInfoPresent(focus.getMaxElement());
		}
		checkNoInfoPresent(definition.getResponseRequiredElement(), "MessageDefinition.responseRequired");
		for (MessageDefinitionAllowedResponseComponent response : definition.getAllowedResponse()) {
			checkNoInfoPresent(response.getSituation(), "MessageDefinition.response.situation");
		}
	}
	
}
