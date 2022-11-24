package uk.nhs.fhir.data.wrap.stu3;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.wrap.WrappedSearchParameter;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3SearchParameter extends WrappedSearchParameter {
	
	private final SearchParameter definition;
	
	public WrappedStu3SearchParameter(SearchParameter source) {
		this.definition = source;
		
		checkForExpectedFeatures();
		checkForUnexpectedFeatures();
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.of(definition.getUrl());
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}
	
	@Override
 	public String getName() {
		return definition.getName();
	}
	
	@Override
	public String getStatus() {
		return definition.getStatus().getDisplay();
	}

	@Override
	public Optional<Date> getDate() {
		return Optional.ofNullable(definition.getDate());
	}
	
	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(definition.getPublisher());
	}

	@Override
	public List<FhirContacts> getContacts() {
		return new Stu3FhirContactConverter().convertList(definition.getContact());
	}
	
	@Override
	public Optional<String> getPurpose() {
		return Optional.of(definition.getPurpose());
	}
	
	@Override
	public String getUrlCode() {
		return definition.getCode();
	}
	
	@Override
	public List<String> getAssociatedResourceTypes() {
		return definition.getBase().stream().map(base -> base.getValueAsString()).collect(Collectors.toList());
	}
	
	@Override
	public String getType() {
		return definition.getTypeElement().getValueAsString();
	}
	
	@Override
	public String getDescription() {
		return definition.getDescription();
	}

	@Override
	public Optional<String> getExpression() {
		return Optional.ofNullable(definition.getExpression());
	}

	@Override
	public Optional<String> getXPath() {
		return Optional.ofNullable(definition.getXpath());
	}

	@Override
	public Optional<String> getXPathUsage() {
		return Optional.ofNullable(definition.getXpathUsage().getDisplay());
	}

	@Override
	public List<String> getSupportedComparators() {
		return 
			definition
				.getComparator()
				.stream()
				.map(comparator -> comparator.getValueAsString())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getModifiers() {
		return
			definition
				.getModifier()
				.stream()
				.map(modifier -> modifier.getValueAsString())
				.collect(Collectors.toList());
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
	public void setUrl(String url) {
		definition.setUrl(Preconditions.checkNotNull(url));
	}

	@Override
	public String getCrawlerDescription() {
		return getDescription();
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
	public void clearHumanReadableText() {
		definition.setText(null);
	}

	@Override
	public ResourceMetadata getMetadataImpl(File source) {
		String displayGroup = "Search Parameters";
    	String name = getName();
    	String url = getUrl().get();
        String resourceID = getIdFromUrl().orElse(name);
    	VersionNumber versionNo = parseVersionNumber();
    	String status = getStatus();
    	
    	return new ResourceMetadata(name, source, ResourceType.SEARCHPARAMETER,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, getImplicitFhirVersion(), url);
	}

	@Override
	public ResourceType getResourceType() {
		return ResourceType.SEARCHPARAMETER;
	}
	
	private static final String COMPARATOR_EQUALS = "eq";
	//private static final String COMPARATOR_NOT_EQUALS = "ne";
	
	private static final String MODIFIER_EXACT = "exact";
	//private static final String MODIFIER_TEXT = "text";
	//private static final String MODIFIER_NOT = "not";
	//private static final String MODIFIER_ABOVE = "above";
	//private static final String MODIFIER_BELOW = "below";
	//private static final String MODIFIER_IN = "in";
	//private static final String MODIFIER_NOT_IN = "not-in";
	
	//private static final String TYPE_NUMBER = "number";
	//private static final String TYPE_DATE = "date";
	//private static final String TYPE_STRING = "string";
	private static final String TYPE_TOKEN = "token";
	//private static final String TYPE_REFERENCE = "reference";
	//private static final String TYPE_COMPOSITE = "composite";
	//private static final String TYPE_QUANTITY = "quantity";
	//private static final String TYPE_URI = "uri";
	
	@Override
	public List<String> getInvocations() {
		// I'm not confident about how Simplifier is generating its 'Invocations' nor about which features
		// we are likely to need to support in time, so coding this conservatively for now.
		for (String comparator : getSupportedComparators()) {
			if (!comparator.equals(COMPARATOR_EQUALS)) {
				EventHandlerContext.forThread().event(RendererEventType.UNRECOGNISED_SEARCH_PARAM_FEATURE, "Unhandled comparator: " + comparator);
				return Lists.newArrayList("(unable to generate)");
			}
		}
		for (String modifier : getModifiers()) {
			if (!modifier.equals(MODIFIER_EXACT)) {
				EventHandlerContext.forThread().event(RendererEventType.UNRECOGNISED_SEARCH_PARAM_FEATURE, "Unhandled modifier: " + modifier);
				return Lists.newArrayList("(unable to generate)");
			}
		}
		
		List<String> invocations = Lists.newArrayList();
		
		if (getType().equals(TYPE_TOKEN)) {
			for (String type : getAssociatedResourceTypes()) {
				invocations.add("[base]/" + type + "?[system]|[code]");
			}
		} else {
			EventHandlerContext.forThread().event(RendererEventType.UNRECOGNISED_SEARCH_PARAM_FEATURE, "Unhandled type: " + getType());
			return Lists.newArrayList("(unable to generate)");
		}
		
		return invocations;
	}
	
	// These are not wrapped in Optional, since they have min cardinality > 0
	private void checkForExpectedFeatures() {
		checkInfoPresent(getName(), "SearchParameter.name");
		checkInfoPresent(getUrl(), "SearchParameter.url");
		checkInfoPresent(getStatus(), "SearchParameter.status");
		checkInfoPresent(getUrlCode(), "SearchParameter.code");
		checkInfoPresent(getAssociatedResourceTypes(), "SearchParameter.base");
		checkInfoPresent(getType(), "SearchParameter.type");
		checkInfoPresent(getDescription(), "SearchParameter.description");
	}

	// These are not displayed anywhere. If they start appearing, we should add support.
	private void checkForUnexpectedFeatures() {
		checkNoInfoPresent(definition.getExperimentalElement().getValue(), "SearchParameter.experimental");
		checkNoInfoPresent(definition.getUseContext(), "SearchParameter.useContext");
		checkNoInfoPresent(definition.getJurisdiction(), "SearchParameter.jurisdiction");
		checkNoInfoPresent(definition.getDerivedFromElement().getValue(), "SearchParameter.derivedFrom");
		checkNoInfoPresent(definition.getTarget(), "SearchParameter.target");
		checkNoInfoPresent(definition.getChain(), "SearchParameter.chain");
		checkNoInfoPresent(definition.getComponent(), "SearchParameter.component");
	}
}
