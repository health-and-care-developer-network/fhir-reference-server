package uk.nhs.fhir.data.wrap.stu3;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.Factory;
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

import uk.nhs.fhir.data.codesystem.FhirCodeSystem;
import uk.nhs.fhir.data.valueset.FhirValueSetCompose;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeConcept;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeFilter;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3ValueSet extends WrappedValueSet {

	private final ValueSet definition;

	public WrappedStu3ValueSet(ValueSet definition) {
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
	public List<WrappedConceptMap> getConceptMaps() {
		return definition
			.getContained()
			.stream()
			.filter(resource -> resource instanceof ConceptMap)
			.map(resource -> WrappedConceptMap.fromDefinition(resource))
			.collect(Collectors.toList());
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
	public Optional<FhirCodeSystem> getCodeSystem() {
		return Optional.empty();
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

			FhirValueSetComposeIncludeConcept concept = new FhirValueSetComposeIncludeConcept(code, description);
			include.addConcept(concept);
		}
		
		return include;
	}

}
