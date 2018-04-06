package uk.nhs.fhir.render.format.valueset;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.util.ListUtils;

/**
 * Formatter for a ValueSet defined by a reference to an unavailable CodeSystem (i.e. one we can't unpack)
 */
public class ImportedValueSetTableFormatter extends TableFormatter<WrappedValueSet> {

	public ImportedValueSetTableFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		
		Element filteredCodeSystemPanel = buildReferencedCodeSystemPanel();
		section.addBodyElement(filteredCodeSystemPanel);
		
		return section;
	}
	
	private Element buildReferencedCodeSystemPanel() {
		FhirValueSetComposeInclude include = ListUtils.expectUnique(wrappedResource.getCompose().getIncludes(), "include");
		String system = include.getSystem();

		// Display a single row with a link to the CodeSystem url
		Element row = new TableRow(new LinkCell(new LinkDatas(new LinkData(FhirURL.buildOrThrow(system, getResourceVersion()), system)))).makeRow();
		return new FhirPanel("Referenced code system (not locally available for full display)", row).makePanel();
	}
	
	public void addStyles(HTMLDocSection section) {
		section.addStyles(TableFormatter.getStyles());
		section.addStyles(LinkCell.getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}
}
