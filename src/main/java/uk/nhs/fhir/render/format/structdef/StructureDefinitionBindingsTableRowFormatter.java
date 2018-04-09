package uk.nhs.fhir.render.format.structdef;

import java.util.Optional;

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

public class StructureDefinitionBindingsTableRowFormatter implements TableRowFormatter {
	public TableRow formatRow(StructureDefinitionBindingsTableRowData data, FhirVersion version) {
		
		String nodeKey = data.getNodeKey();
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
