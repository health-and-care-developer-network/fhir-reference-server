package uk.nhs.fhir.render.format.valueset;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
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
			if (include.getConcepts().isEmpty()) {
				// try to find the concept map from the registry
				WrappedCodeSystem standaloneCodeSystem = RendererContext.forThread().getFhirFileRegistry().getCodeSystem(include.getSystem());
				if (standaloneCodeSystem != null) {
						addConcepts(rows, include.getSystem(), standaloneCodeSystem.getCodeSystemConcepts().getConcepts());
				} else {
					EventHandlerContext.forThread().event(RendererEventType.EMPTY_VALUE_SET, 
						"Empty include and url [" + include.getSystem() + "] doesn't start with " + FhirURLConstants.HTTPS_FHIR_HL7_ORG_UK);
					// ensure that we still display the code system
					addConcepts(rows, include.getSystem(), Lists.newArrayList());
				}
			} else {
				addConcepts(rows, include.getSystem(), include.getConcepts());
			}
		}
		
		return rows;
	}

	private void addConcepts(List<ValueSetConceptsTableDataCodeSystem> rows, String system, List<FhirCodeSystemConcept> concepts) {
		ValueSetConceptsTableDataCodeSystem systemRow = findOrAddSystem(rows, system);
		
		for (FhirCodeSystemConcept concept : concepts) {
			String code = concept.getCode();
			
			String mappedCode = null;
			
			for (WrappedConceptMap conceptMap : valueSet.getConceptMaps(RendererContext.forThread().getFhirFileRegistry())) {
				for (FhirConceptMapElement mapElement : conceptMap.getElements()) {
	                if (code.equals(mapElement.getCode())) {
	                	if (mapElement.getTargets().isEmpty()) {
	                		throw new IllegalStateException("Concept map includes matching code " + code + " but didn't include any targets");
	                	} else if (mapElement.getTargets().size() > 1) {
                			throw new IllegalStateException("Concept map contains multiple targets for code " + code + ". How should this be displayed?");
                		} 
	                		
                		String newMappedCode = "~" + mapElement.getTargets().get(0).getCode();
                		
                		if (mappedCode != null
                		  && !mappedCode.equals(newMappedCode)) {
                			throw new IllegalStateException("Code " + code + " is mapped to 2 or more other codes: " + mappedCode + " & " + newMappedCode);
                		}
                		
                		mappedCode = newMappedCode;
	                }
	            }
			}
			
			Optional<String> mappingString = Optional.ofNullable(mappedCode);
			
			systemRow.addConcept(code, concept.getDescription(), concept.getDefinition(), mappingString);
		}
	}

	private ValueSetConceptsTableDataCodeSystem findOrAddSystem(List<ValueSetConceptsTableDataCodeSystem> rows, String system) {
		ValueSetConceptsTableDataCodeSystem systemRow = null;
		
		for (ValueSetConceptsTableDataCodeSystem potentialSystemRow : rows) {
			if (potentialSystemRow.getCodeSystem().equals(system)) {
				systemRow = potentialSystemRow;
				break;
			}
		}
		
		if (systemRow == null) {
			FhirURL systemUrl = FhirURL.buildOrThrow(system, valueSet.getImplicitFhirVersion());
			systemRow = new ValueSetConceptsTableDataCodeSystem(systemUrl);
			rows.add(systemRow);
		}
		
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
