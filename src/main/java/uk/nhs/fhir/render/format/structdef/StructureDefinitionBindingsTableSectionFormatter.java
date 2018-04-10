package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.format.TableRowFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.render.html.tree.FhirIcon;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public class StructureDefinitionBindingsTableSectionFormatter implements TableRowFormatter {

	private final FhirVersion fhirVersion;
	
	public StructureDefinitionBindingsTableSectionFormatter(FhirVersion fhirVersion) {
		this.fhirVersion = fhirVersion;
	}
	
	public List<TableRow> formatSections(List<StructureDefinitionBindingsTableSection> sections) {
		List<TableRow> tableRows = Lists.newArrayList();
		
		for (StructureDefinitionBindingsTableSection section : sections) {
			tableRows.addAll(formatSection(section));
		}
		
		return tableRows;
	}
	
	private List<TableRow> formatSection(StructureDefinitionBindingsTableSection section) {
		List<TableRow> sectionRows = Lists.newArrayList();
		
		if (section.getSourceNodeKey().isPresent()) {
			// section title row
			sectionRows.add(new TableRow(titleCell(section)));
			
			section.getRowData().forEach(data -> sectionRows.add(formatRow(data, fhirVersion, section.getSourceNodeKey().get())));
		} else {
			section.getRowData().forEach(data -> sectionRows.add(formatRow(data, fhirVersion)));
		}
		
		return sectionRows;
	}
	
	private TableCell titleCell(StructureDefinitionBindingsTableSection section) {
		TableCell titleCell = new SimpleTextCell("Inherited via referenced resource: " + section.getSourceExternalResourceUrl().get());
		titleCell.colspan(4);
		titleCell.addClass(FhirCSS.DATA_LABEL);
		return titleCell;
	}

	private TableRow formatRow(StructureDefinitionBindingsTableRowData data, FhirVersion version) {
		String nodeKey = data.getNodeKey();
		return formatRow(data, version, nodeKey);
	}

	private TableRow formatRow(StructureDefinitionBindingsTableRowData data, FhirVersion version, String nodeKey) {
		Optional<String> description = data.getDescription();
		String bindingStrength = data.getBindingStrength();
		String anchorStrength = data.getAnchorStrength();
		Optional<FhirURL> valueSetUrl = data.getValueSetUrl();

		TableCell typeLink = new LinkCell(new LinkDatas(new LinkData(
			FhirURL.buildOrThrow(FhirURLConstants.versionBase(version) + "/terminologies.html#" + anchorStrength, version), 
			bindingStrength)));
		
		TableCell referenceCell;
		if (valueSetUrl.isPresent()) {
			LinkCell referenceLink = new LinkCell(new LinkDatas(new LinkData(
				valueSetUrl.get(), 
				valueSetUrl.get().toFullString())));
			if (!FhirURLConstants.isNhsResourceUrl(valueSetUrl.get().toFullString())) {
				referenceLink.setLinkIcon(FhirIcon.REFERENCE);
			}
			referenceCell = referenceLink;
		} else {
			referenceCell = TableCell.empty();
		}
		
		return new TableRow(
			new LinkCell(new LinkDatas(new LinkData(FhirURL.buildOrThrow("details.html#" + nodeKey, version), nodeKey)), Sets.newHashSet(), Sets.newHashSet(FhirCSS.TAB_LINK)),
			new SimpleTextCell(description.orElse(BLANK)),
			typeLink,
			referenceCell);
	}
}
