package uk.nhs.fhir.render.format.message;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.message.MessageDefinitionAsset;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.util.FhirVersion;

public class MessageDefinitionAssetsTableRowFormatter {

	public List<TableRow> formatRows(List<MessageDefinitionAsset> assets, FhirVersion version, String assetType) {
		
		List<TableRow> rows = Lists.newArrayList();
		
		for (MessageDefinitionAsset asset : assets) {
		// returning rows only for the specified assetType or All
		 if (asset.getCode().equals(assetType) || assetType.equals("All"))
		  {	
			TableRow row = new TableRow();
			
			row.addCell(new SimpleTextCell(asset.getCode()));
			
			String structureDefinitionReference = asset.getStructureDefinitionReference();
			Optional<String> anyPermittedType = getTypeFromPermitAnyOfType(structureDefinitionReference);
			if (anyPermittedType.isPresent()) 
			{
				// row.addCell(new SimpleTextCell("Any " + anyPermittedType.get()));
				row.addCell(new SimpleTextCell(structureDefinitionReference));
			} else if (FhirURL.isLogicalUrl(structureDefinitionReference)) {
				row.addCell(new SimpleTextCell(structureDefinitionReference));
			} else 
			{
				row.addCell(new LinkCell(new LinkDatas(new LinkData(FhirURL.buildOrThrow(structureDefinitionReference, version), structureDefinitionReference))));
			}
			
			row.addCell(new SimpleTextCell(asset.getStructureDefinitionVersion()));
			
			rows.add(row);
		  }
		}
		return rows;
	}

	private Optional<String> getTypeFromPermitAnyOfType(String bundleExtensionUrl) {
		String prefix = "https://fhir.nhs.uk/STU3/";
		String suffix = "/any";
		if (bundleExtensionUrl.startsWith(prefix)
		  && bundleExtensionUrl.endsWith(suffix)) {
			String type = bundleExtensionUrl.substring(prefix.length(), bundleExtensionUrl.indexOf(suffix));
			if (!type.contains("/")) {
				return Optional.of(type);
			}
		}
		
		return Optional.empty();
	}

}
