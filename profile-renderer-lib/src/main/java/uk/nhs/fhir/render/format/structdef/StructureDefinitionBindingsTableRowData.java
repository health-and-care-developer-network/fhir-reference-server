package uk.nhs.fhir.render.format.structdef;

import java.util.Optional;

import uk.nhs.fhir.data.url.FhirURL;

public class StructureDefinitionBindingsTableRowData {
	private final String nodeKey;
	private final Optional<String> description;
	private final String bindingStrength;
	private final String anchorStrength;
	private final Optional<FhirURL> valueSetUrl;
	
	public StructureDefinitionBindingsTableRowData(String nodeKey, Optional<String> description,
			String bindingStrength, String anchorStrength, Optional<FhirURL> valueSetUrl) {
		this.nodeKey = nodeKey;
		this.description = description;
		this.bindingStrength = bindingStrength;
		this.anchorStrength = anchorStrength;
		this.valueSetUrl = valueSetUrl;
	}

	public String getNodeKey() {
		return nodeKey;
	}
	public Optional<String> getDescription() {
		return description;
	}
	public String getBindingStrength() {
		return bindingStrength;
	}
	public String getAnchorStrength() {
		return anchorStrength;
	}
	public Optional<FhirURL> getValueSetUrl() {
		return valueSetUrl;
	}
}
