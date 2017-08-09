package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.CodeSystem;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.CodeSystemConcept;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.Compose;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeInclude;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeIncludeConcept;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeIncludeFilter;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.codesystem.FhirCodeSystem;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.valueset.FhirValueSetCompose;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeConcept;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeFilter;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedDstu2ValueSet extends WrappedValueSet {
	private final ValueSet definition;
	
	public WrappedDstu2ValueSet(ValueSet definition) {
		this.definition = definition;
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
		return FhirVersion.DSTU2;
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
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}

	@Override
	public void setCopyright(String copyRight) {
		definition.setCopyright(copyRight);
	}

	@Override
	public void addHumanReadableText(String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}

	@Override
	public List<WrappedConceptMap> getConceptMaps() {
		return definition
			.getContained()
			.getContainedResources()
			.stream()
			.filter(resource -> resource instanceof ConceptMap)
			.map(resource -> WrappedConceptMap.fromDefinition(resource))
			.collect(Collectors.toList());
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getStatus() {
		return definition.getStatus();
	}

	@Override
	public Optional<String> getOid() {
		List<String> oids = 
			definition
				.getUndeclaredExtensions()
				.stream()
				.filter(extension -> extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-oid"))
				.map(extension -> extension.getValueAsPrimitive().getValueAsString())
				.collect(Collectors.toList());
		
		if (oids.size() == 0) {
			return Optional.empty();
		} else if (oids.size() == 1) {
			return Optional.of(oids.get(0));
		} else {
			throw new IllegalStateException("Found multiple oids for value set: " + String.join(", ", oids) + " (" + definition.getUrl() + ")");
		}
	}

	@Override
	public Optional<String> getReference() {
		List<String> references = 
			definition
				.getUndeclaredExtensions()
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
		return Optional.ofNullable(definition.getRequirements());
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
		CodeSystem sourceCodeSystem = definition.getCodeSystem();
		
		String sourceCodeSystemSystem = sourceCodeSystem.getSystem();
		
		FhirCodeSystem codeSystem = new FhirCodeSystem(sourceCodeSystemSystem);
		
		for (CodeSystemConcept concept : sourceCodeSystem.getConcept()) {
			String description = concept.getDisplay();
			String definition = concept.getDefinition();
			String code = concept.getCode();
			
			codeSystem.addConcept(new FhirCodeSystemConcept(code, description, definition));
		}
		
		return Optional.of(codeSystem);
	}

	@Override
	public FhirValueSetCompose getCompose() {
		Compose sourceCompose = definition.getCompose();
		
		FhirValueSetCompose compose = new FhirValueSetCompose();
		sourceCompose.getImport().forEach(importUri -> compose.addImportUri(importUri.getValue()));
		sourceCompose.getInclude().forEach(include -> compose.addInclude(convertInclude(include)));
		sourceCompose.getExclude().forEach(exclude -> compose.addExclude(convertInclude(exclude)));
		
		return compose;
	}

	private FhirValueSetComposeInclude convertInclude(ComposeInclude sourceInclude) {
		String system = sourceInclude.getSystem();
		String version = sourceInclude.getVersion();
		
		FhirValueSetComposeInclude include = new FhirValueSetComposeInclude(system, version);
		
		for (ComposeIncludeFilter sourceFilter : sourceInclude.getFilter()) {
			String property = sourceFilter.getProperty();
			String op = sourceFilter.getOp();
			String value = sourceFilter.getValue();

			FhirValueSetComposeIncludeFilter filter = new FhirValueSetComposeIncludeFilter(property, op, value);
			include.addFilter(filter);
		}

		for (ComposeIncludeConcept sourceConcept : sourceInclude.getConcept()) {
			String code = sourceConcept.getCode();
			String description = sourceConcept.getDisplay();

			FhirValueSetComposeIncludeConcept concept = new FhirValueSetComposeIncludeConcept(code, description);
			include.addConcept(concept);
		}
		
		return include;
	}

}
