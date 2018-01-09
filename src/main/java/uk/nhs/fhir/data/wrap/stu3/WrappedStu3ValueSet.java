package uk.nhs.fhir.data.wrap.stu3;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.valueset.FhirValueSetCompose;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeFilter;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.util.FhirFileRegistry;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3ValueSet extends WrappedValueSet {

	private final ValueSet definition;

	public WrappedStu3ValueSet(ValueSet definition) {
		checkForUnexpectedFeatures(definition);
		
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
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}

	@Override
	public void setCopyright(String copyRight) {
		definition.setCopyright(copyRight);
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
	public String getName() {
		return definition.getName();
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
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getStatus() {
		return definition.getStatus().getDisplay();
	}

	@Override
	public List<WrappedConceptMap> getConceptMaps(FhirFileRegistry otherResources) {
		List<WrappedConceptMap> conceptMaps = Lists.newArrayList();
		conceptMaps.addAll(
			definition
				.getContained()
				.stream()
				.filter(resource -> resource instanceof ConceptMap)
				.map(resource -> WrappedConceptMap.fromDefinition(resource))
				.collect(Collectors.toList()));

		Optional<String> valueSetUrl = getUrl();
		if (valueSetUrl.isPresent()) {
			 conceptMaps.addAll(otherResources.getConceptMapsForSource(valueSetUrl.get()));
		}
		
		return conceptMaps;
	}

	@Override
	public Optional<Boolean> getExperimental() {
		return Optional.ofNullable(definition.getExtensibleElement().getValue());
	}

	@Override
	public Optional<String> getOid() {
		List<String> references = 
			definition
				.getExtension()
				.stream()
				.filter(extension -> extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-oid"))
				.map(extension -> extension.getValueAsPrimitive().getValueAsString())
				.collect(Collectors.toList());
			
		if (references.size() == 0) {
			return Optional.empty();
		} else if (references.size() == 1) {
			return Optional.of(references.get(0));
		} else {
			throw new IllegalStateException("Found multiple references for value set: " + String.join(", ", references) + " (" + definition.getUrl() + ")");
		}
	}

	@Override
	public Optional<String> getReference() {
		List<String> references = 
			definition
				.getExtension()
				.stream()
				.filter(extension -> extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-sourceReference"))
				.map(extension -> extension.getValueAsPrimitive().getValueAsString())
				.collect(Collectors.toList());
			
		if (references.size() == 0) {
			return Optional.empty();
		} else if (references.size() == 1) {
			return Optional.of(references.get(0));
		} else {
			throw new IllegalStateException("Found multiple references for value set: " + String.join(", ", references) + " (" + definition.getUrl() + ")");
		}
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(definition.getDescription());
	}

	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(definition.getPublisher());
	}

	@Override
	public Optional<String> getRequirements() {
		return Optional.ofNullable(definition.getPurpose());
	}

	@Override
	public Optional<Date> getDate() {
		return Optional.ofNullable(definition.getDate());
	}

	@Override
	public boolean hasComposeIncludeFilter() {
		return 
			definition
				.getCompose()
				.getInclude()
				.stream()
				.anyMatch(include -> !include.getFilter().isEmpty());
	}

	@Override
	public Optional<FhirCodeSystemConcepts> getCodeSystem() {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> getImmutable() {
		return Optional.ofNullable(definition.getImmutableElement().getValue());
	}

	@Override
	public FhirValueSetCompose getCompose() {
		ValueSetComposeComponent sourceCompose = definition.getCompose();
		
		FhirValueSetCompose compose = new FhirValueSetCompose();
		List<String> importUris = 
			sourceCompose
				.getInclude()
				.stream()
				.flatMap(
					include -> 
						include
						.getValueSet()
						.stream()
						.map(uri -> uri.getValue()))
				.collect(Collectors.toList());
		importUris.forEach(importUri -> compose.addImportUri(importUri));
		sourceCompose.getInclude().forEach(include -> compose.addInclude(convertInclude(include)));
		sourceCompose.getExclude().forEach(exclude -> compose.addExclude(convertInclude(exclude)));
		
		return compose;
	}

	@Override
	public List<FhirContacts> getContacts() {
		return new Stu3FhirContactConverter().convertList(definition.getContact());
	}

	private FhirValueSetComposeInclude convertInclude(ConceptSetComponent sourceInclude) {
		String system = sourceInclude.getSystem();
		String version = sourceInclude.getVersion();
		
		FhirValueSetComposeInclude include = new FhirValueSetComposeInclude(system, version);
		
		for (ConceptSetFilterComponent sourceFilter : sourceInclude.getFilter()) {
			String property = sourceFilter.getProperty();
			String op = sourceFilter.getOp().getDisplay();
			String value = sourceFilter.getValue();

			FhirValueSetComposeIncludeFilter filter = new FhirValueSetComposeIncludeFilter(property, op, value);
			include.addFilter(filter);
		}

		for (ConceptReferenceComponent sourceConcept : sourceInclude.getConcept()) {
			String code = sourceConcept.getCode();
			String description = sourceConcept.getDisplay();

			FhirCodeSystemConcept concept = new FhirCodeSystemConcept(code, description, null);
			include.addConcept(concept);
		}
		
		return include;
	}
	
	public static void checkForUnexpectedFeatures(ValueSet definition) {
		// accessible features
		// definition.getUrl();
		// definition.getVersion();
		// definition.getName();
		// definition.getStatus();
		// definition.getDate();
		// definition.getDescription();
		// definition.getPurpose();
		// definition.getCopyright();
		// definition.getIdentifier()
		// definition.getPublisher()
		// definition.getContact();
		// definition.getExtensibleElement().getValue();
		// definition.getImmutableElement().getValue()
		
		checkNoInfoPresent(definition.getTitle());
		checkNoInfoPresent(definition.getExperimentalElement().getValue());
		checkNoInfoPresent(definition.getUseContext());
		checkNoInfoPresent(definition.getJurisdiction());
		
		for (Identifier identifier : definition.getIdentifier()) {
			checkNoInfoPresent(identifier.getUse());
			checkNoInfoPresent(identifier.getType());
			checkNoInfoPresent(identifier.getPeriod());
			checkNoInfoPresent(identifier.getAssigner());
		}
		
		ValueSetComposeComponent compose = definition.getCompose();
		if (compose != null) {
			checkNoInfoPresent(compose.getLockedDate());
			checkNoInfoPresent(compose.getInactiveElement().getValue());
			
			for (ConceptSetComponent include : compose.getInclude()) {
				// include.getSystem();
				checkNoInfoPresent(include.getVersion());
				
				for (ConceptReferenceComponent concept : include.getConcept()) {
					// concept.getCode();
					// concept.getDisplay();
					checkNoInfoPresent(concept.getDesignation());
				}
				
				// for (ConceptSetFilterComponent filter : include.getFilter()) {
					// filter.getProperty();
					// filter.getOp();
					// filter.getValue();
				// }
				
				checkNoInfoPresent(include.getValueSet());
			}
			
			checkNoInfoPresent(compose.getExclude());
		}
		
		checkNoInfoPresent(definition.getExpansion());
		
		for (Identifier identifier : definition.getIdentifier()) {
			checkNoInfoPresent(identifier.getUse());
			checkNoInfoPresent(identifier.getType());
			checkNoInfoPresent(identifier.getPeriod());
			checkNoInfoPresent(identifier.getAssigner());
		}
	}

	private static void checkNoInfoPresent(Object o) {
		if (o instanceof Collection<?>) {
			if (!((Collection<?>) o).isEmpty()) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		} else if (o instanceof Base) {
			if (!((Base) o).isEmpty()) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		} else {
			if (o != null) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		}
	}

	@Override
	public List<FhirIdentifier> getIdentifiers() {
		List<FhirIdentifier> identifiers = Lists.newArrayList();
		
		for (Identifier identifier : definition.getIdentifier()) {
			identifiers.add(new FhirIdentifier(identifier.getValue(), identifier.getSystem()));
		}

		return identifiers;
	}

	@Override
	public boolean isSNOMED() {
		ValueSetComposeComponent compose = definition.getCompose();
		
		if (compose != null) {
			
			return compose
				.getInclude()
				.stream()
				.anyMatch(include -> FhirURLConstants.SNOMED_ID.equals(include.getSystem()));
			
    	} else {
    		return false;
    	}
	}
}
