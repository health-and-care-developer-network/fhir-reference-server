package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.html.table.TableRow;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.RendererContext;

public class ConceptsValueSetTableFormatter extends TableFormatter<WrappedValueSet> {

	public ConceptsValueSetTableFormatter(WrappedValueSet wrappedResource, RendererContext context) {
		super(wrappedResource, context);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		
		Element conceptsPanel = buildConceptsPanel();
		section.addBodyElement(conceptsPanel);
		
		return section;
	}

	private Element buildConceptsPanel() {
		ValueSetConceptsTableDataProvider tableData = new ValueSetConceptsTableDataProvider(wrappedResource, context);
		List<ValueSetConceptsTableDataCodeSystem> codeSystems = tableData.getCodeSystems();

		boolean needsDisplayColumn = false;
		for (ValueSetConceptsTableDataCodeSystem codeSystem : codeSystems) {
			for (ValueSetConceptsTableData concept : codeSystem.getConcepts()) {
				if (concept.getDisplay().isPresent()) {
					needsDisplayColumn = true;
					break;
				}
			}
		}
		
		boolean needsDefinitionColumn = false;
		for (ValueSetConceptsTableDataCodeSystem codeSystem : codeSystems) {
			for (ValueSetConceptsTableData concept : codeSystem.getConcepts()) {
				if (concept.getDefinition().isPresent()) {
					needsDefinitionColumn = true;
					break;
				}
			}
		}
	
		boolean needsMappingColumn = false;
		for (ValueSetConceptsTableDataCodeSystem codeSystem : codeSystems) {
			for (ValueSetConceptsTableData concept : codeSystem.getConcepts()) {
				if (concept.getMapping().isPresent()) {
					needsMappingColumn = true;
					break;
				}
			}
		}

		List<TableRow> tableRows = Lists.newArrayList();
		
		ValueSetConceptsCodeSystemFormatter codeSystemFormatter = new ValueSetConceptsCodeSystemFormatter(needsDisplayColumn, needsDefinitionColumn, needsMappingColumn, tableRows);
		
		for (ValueSetConceptsTableDataCodeSystem codeSystem : codeSystems) {
			codeSystemFormatter.addRows(codeSystem);
		}
		
		Element filteredCodeSystemTable = new Table(tableData.getColumns(needsDisplayColumn, needsDefinitionColumn, needsMappingColumn), tableRows).makeTable();
		return new FhirPanel("Value set concepts", filteredCodeSystemTable).makePanel();
	}

	private void addStyles(HTMLDocSection section) {
		section.addStyles(TableFormatter.getStyles());
		section.addStyles(LinkCell.getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}

}
