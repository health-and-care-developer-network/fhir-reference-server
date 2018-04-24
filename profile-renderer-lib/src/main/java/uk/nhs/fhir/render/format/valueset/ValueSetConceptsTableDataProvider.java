package uk.nhs.fhir.render.format.valueset;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.html.table.TableTitle;
import uk.nhs.fhir.util.FhirURLConstants;

public class ValueSetConceptsTableDataProvider {
	
	private final WrappedValueSet valueSet;
	
	public ValueSetConceptsTableDataProvider(WrappedValueSet valueSet) {
		this.valueSet = valueSet;
	}

	public List<ValueSetConceptsTableDataCodeSystem> getCodeSystems() {
		List<ValueSetConceptsTableDataCodeSystem> rows = Lists.newArrayList();
		
		Optional<FhirCodeSystemConcepts> inlineCodeSystem = valueSet.getCodeSystem();
		if (inlineCodeSystem.isPresent()) {
			FhirCodeSystemConcepts cs = inlineCodeSystem.get();
			addConcepts(rows, cs.getSystem(), cs.getConcepts());
		}
		
		for (FhirValueSetComposeInclude include : valueSet.getCompose().getIncludes()) {
			String system = include.getSystem();
			Optional<WrappedCodeSystem> standaloneCodeSystem = RendererContext.forThread().getFhirFileRegistry().getCodeSystem(system);
			List<FhirCodeSystemConcept> concepts = include.getConcepts();
			
			if (!concepts.isEmpty()) {
				//TODO could validate the codes selected by this ValueSet?
				addConcepts(rows, system, concepts);
			} else if (standaloneCodeSystem.isPresent()) {
				addConcepts(rows, system, standaloneCodeSystem.get().getCodeSystemConcepts().getConcepts());
			} else {
				EventHandlerContext.forThread().event(RendererEventType.EMPTY_VALUE_SET, 
					"Empty include and CodeSystem url [" + system + "] wasn't found locally (does it start with " + FhirURLConstants.FHIR_HL7_ORG_UK_QDOMAIN + "?)");
					// ensure that we still display the code system
					addConcepts(rows, system, Lists.newArrayList());
			}
		}
		
		return rows;
	}

	private void addConcepts(List<ValueSetConceptsTableDataCodeSystem> codeSystems, String system, List<FhirCodeSystemConcept> concepts) {
		ValueSetConceptsTableDataCodeSystem codeSystemWithConcepts = findOrAddSystem(codeSystems, system);
		
		for (FhirCodeSystemConcept concept : concepts) {
			String code = concept.getCode();
			
			List<String> mappedCodes = 
				valueSet
					.getConceptMaps(RendererContext.forThread().getFhirFileRegistry()).stream()
					.flatMap(conceptMap -> 
						StreamSupport.stream(conceptMap
							.getMappingGroups().spliterator(), false)
							.flatMap(group -> group.getMappings().stream()))
					.filter(mapping -> 
						code.equals(mapping.getCode()))
					.filter(mapping ->
						assertHasTargets(mapping, code))
					.flatMap(mapping -> 
						mapping.getTargets().stream())
					.map(target -> "~" + target.getCode())
					.collect(Collectors.toList());
			
			if (mappedCodes.isEmpty()) {
				// no map elements found which match this code
				codeSystemWithConcepts.addConcept(code, concept.getDescription(), concept.getDefinition(), Optional.empty());
			} else {
				for (String mappedCode : mappedCodes) {
					codeSystemWithConcepts.addConcept(code, concept.getDescription(), concept.getDefinition(), Optional.of(mappedCode));
				}
			}
		}
	}

	private boolean assertHasTargets(FhirConceptMapElement mapElement, String code) {
		if (mapElement.getTargets().isEmpty()) {
    		throw new IllegalStateException("Concept map includes matching code " + code + " but didn't include any targets");
    	}
		
		return true;
	}

	private ValueSetConceptsTableDataCodeSystem findOrAddSystem(List<ValueSetConceptsTableDataCodeSystem> conceptsHeaders, String system) {
		ValueSetConceptsTableDataCodeSystem systemRow = null;
		
		for (ValueSetConceptsTableDataCodeSystem potentialMatchingConceptsHeader : conceptsHeaders) {
			String headerCodeSystem = potentialMatchingConceptsHeader.getCodeSystem().toFullString();
			
			if (headerCodeSystem.equals(system)) {
				return potentialMatchingConceptsHeader;
			}
		}
		
		FhirURL systemUrl = FhirURL.buildOrThrow(system, valueSet.getImplicitFhirVersion());
		systemRow = new ValueSetConceptsTableDataCodeSystem(systemUrl);
		conceptsHeaders.add(systemRow);
		return systemRow;
	}

	List<TableTitle> getColumns(boolean needsDisplayColumn, boolean needsDefinitionColumn, boolean needsMappingColumn) {
		
		int collapsedCodeSize = 10;
		int collapsedDisplayPercent = 15;
		int collapsedDefinitionPercent = 25;
		
		int codesystemPercent = 25;
		int codePercent = 75;
		int displayPercent = 0;
		int definitionPercent = 0;
		int mappingPercent = 0;
		
		if (needsDisplayColumn) {
			if (codePercent > collapsedCodeSize) {
				displayPercent = codePercent - collapsedCodeSize;
				codePercent = collapsedCodeSize;
			}
		}
		
		if (needsDefinitionColumn) {
			if (codePercent > collapsedCodeSize) {
				definitionPercent = codePercent - collapsedCodeSize;
				codePercent = collapsedCodeSize;
			} else if (displayPercent > collapsedDisplayPercent) {
				definitionPercent = displayPercent - collapsedDisplayPercent;
				displayPercent = collapsedDisplayPercent;
			} else {
				throw new IllegalStateException("Can't find enough space for definition column (" + definitionPercent + ")");
			}
		}
		
		if (needsMappingColumn) {
			if (codePercent > collapsedCodeSize) {
				mappingPercent = codePercent - collapsedCodeSize;
				codePercent = collapsedCodeSize;
			} else if (displayPercent > collapsedDisplayPercent) {
				mappingPercent = displayPercent - collapsedDisplayPercent;
				displayPercent = collapsedDisplayPercent;
			} else if (definitionPercent > collapsedDefinitionPercent) {
				mappingPercent = definitionPercent - collapsedDefinitionPercent;
				definitionPercent = collapsedDefinitionPercent;
			} else {
				throw new IllegalStateException("Can't find enough space for mapping column (" + definitionPercent + ")");
			}
		}

		List<TableTitle> columns = Lists.newArrayList();

		columns.add(new TableTitle("CodeSystem", "System that the following concepts belong to", Integer.toString(codesystemPercent) + "%"));
		columns.add(new TableTitle("Code", "Identifier for this concept", Integer.toString(codePercent) + "%"));
		
		if (needsDisplayColumn) {
			columns.add(new TableTitle("Display", "Human-friendly name", Integer.toString(displayPercent) + "%"));
		}
		
		if (needsDefinitionColumn) { 
			columns.add(new TableTitle("Definition", "Formal definition", Integer.toString(definitionPercent) + "%"));
		}
		
		if (needsMappingColumn) {
			columns.add(new TableTitle("Mapping", "Code that this concept maps to (see ConceptMap)", Integer.toString(mappingPercent) + "%"));
		}
		
		return columns;
	}
}
