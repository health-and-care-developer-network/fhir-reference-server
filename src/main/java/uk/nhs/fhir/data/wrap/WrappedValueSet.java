package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.valueset.FhirValueSetCompose;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.util.FhirFileRegistry;

public abstract class WrappedValueSet extends WrappedResource<WrappedValueSet> {

	public abstract Optional<String> getCopyright();
	public abstract void setCopyright(String copyRight);
	public abstract List<WrappedConceptMap> getConceptMaps(FhirFileRegistry otherResources);
	public abstract String getStatus();
	public abstract List<FhirIdentifier> getIdentifiers();
	public abstract Optional<String> getOid();
	public abstract Optional<String> getReference();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPublisher();
	public abstract Optional<String> getRequirements();
	public abstract Optional<Date> getDate();
	public abstract boolean hasComposeIncludeFilter();
	public abstract Optional<FhirCodeSystemConcepts> getCodeSystem();
	public abstract FhirValueSetCompose getCompose();
	public abstract List<FhirContacts> getContacts();
	public abstract Optional<Boolean> getExperimental();
	public abstract Optional<Boolean> getImmutable();
	
	public abstract boolean isSNOMED();
	protected abstract void checkForUnexpectedFeatures();
	
	public void fixHtmlEntities() {
		Optional<String> copyRight = getCopyright();
	    if(copyRight.isPresent()) {
	        String updatedCopyRight = copyRight.get().replace("©", "&#169;");
	        updatedCopyRight = updatedCopyRight.replace("\\u00a9", "&#169;");
	        setCopyright(updatedCopyRight);
	    }
	}
	
	public List<FhirCodeSystemConcept> getConceptsToDisplay() {
		List<FhirCodeSystemConcept> conceptsForDisplay = Lists.newArrayList();
		
		Optional<FhirCodeSystemConcepts> inlineCodeSystem = getCodeSystem();
		if (inlineCodeSystem.isPresent()) {
			conceptsForDisplay.addAll(inlineCodeSystem.get().getConcepts());
		}
		
		FhirValueSetCompose compose2 = getCompose();
		for (FhirValueSetComposeInclude include : compose2.getIncludes()) {
			conceptsForDisplay.addAll(include.getConcepts());
		}
		for (FhirValueSetComposeInclude exclude : compose2.getExcludes()) {
			conceptsForDisplay.addAll(exclude.getConcepts());
		}
		return conceptsForDisplay;
	}

	@Override
	public ResourceMetadata getMetadataImpl(File source) {
    	String url = getUrl().get();
    	String resourceName = getName();
    	String resourceID = getIdFromUrl().orElse(resourceName);

		String displayGroup;
    	if (isSNOMED()) {
    		displayGroup = "SNOMED CT Code List";
    	} else {
    		displayGroup = "Code List";
    	}
    	
    	VersionNumber versionNo = parseVersionNumber();
    	
    	String status = getStatus();
    	
    	return new ResourceMetadata(resourceName, source, ResourceType.VALUESET,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.VALUESET;
	}

	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse("FHIR Server: ValueSet " + getName());
	}
}
