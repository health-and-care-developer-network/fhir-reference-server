package uk.nhs.fhir.render.format.valueset;

import java.util.List;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;

public class ConceptsValueSetTableFormatter extends TableFormatter<WrappedValueSet> {

	public ConceptsValueSetTableFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
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
		ValueSetConceptsTableDataProvider tableData = new ValueSetConceptsTableDataProvider(wrappedResource);
		List<ValueSetConceptsTableDataCodeSystem> codeSystems = tableData.getCodeSystems();

		boolean needsDisplayColumn = findConceptFeature(codeSystems, concept -> concept.getDisplay().isPresent());
		boolean needsDefinitionColumn = findConceptFeature(codeSystems, concept -> concept.getDefinition().isPresent());
		boolean needsMappingColumn = findConceptFeature(codeSystems, concept -> concept.getMapping().isPresent());

		List<TableRow> tableRows = Lists.newArrayList();
		
		ValueSetConceptsCodeSystemFormatter codeSystemFormatter = new ValueSetConceptsCodeSystemFormatter(needsDisplayColumn, needsDefinitionColumn, needsMappingColumn, tableRows);
		
		for (ValueSetConceptsTableDataCodeSystem codeSystem : codeSystems) {
			codeSystemFormatter.addRows(codeSystem);
		}
		
		Element filteredCodeSystemTable = new Table(tableData.getColumns(needsDisplayColumn, needsDefinitionColumn, needsMappingColumn), tableRows).makeTable();
		return new FhirPanel("Value set concepts", filteredCodeSystemTable).makePanel();
	}
	
	private boolean findConceptFeature(List<ValueSetConceptsTableDataCodeSystem> codeSystems, Predicate<? super ValueSetConceptsTableData> predicate) {
		return 
			codeSystems
				.stream()
				.flatMap(codeSystem -> 
					codeSystem
						.getConcepts()
						.stream())
				.anyMatch(predicate);
	}

	private void addStyles(HTMLDocSection section) {
		section.addStyles(TableFormatter.getStyles());
		section.addStyles(LinkCell.getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}

}
