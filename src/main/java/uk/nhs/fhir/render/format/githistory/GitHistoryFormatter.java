package uk.nhs.fhir.render.format.githistory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.wrap.WrappedNull;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.util.FhirFileRegistry;

public class GitHistoryFormatter extends TableFormatter<WrappedNull> {
	
	protected String repoName = null;
	protected String filename = null;
	
	public GitHistoryFormatter(String repoName, String filename) {
		super(null);
		this.repoName = repoName;
		this.filename = filename;
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		
		HTMLDocSection section = new HTMLDocSection();
		
		Element historyPanel = buildHistoryPanel();
		section.addBodyElement(historyPanel);

		addStyles(section);
		return section;
	}
	
	private Element buildHistoryPanel() {
		GitHistoryTableDataProvider tableData = new GitHistoryTableDataProvider(this.repoName, this.filename);
		
		List<TableRow> tableRows = Lists.newArrayList();
		
		TableRow testRow = new TableRow();
		testRow.addCell(new SimpleTextCell("Test"));
		testRow.addCell(new SimpleTextCell(this.repoName));
		testRow.addCell(new SimpleTextCell(this.filename));
		tableRows.add(testRow);
		
		Element historyTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Git History for: "+this.filename, historyTable).makePanel();
	}

    public void addStyles(HTMLDocSection section) {
        Table.getStyles().forEach(section::addStyle);
        FhirPanel.getStyles().forEach(section::addStyle);
        ValueWithInfoCell.getStyles().forEach(section::addStyle);
        LinkCell.getStyles().forEach(section::addStyle);
        ResourceFlagsCell.getStyles().forEach(section::addStyle);
        StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
    }
}
