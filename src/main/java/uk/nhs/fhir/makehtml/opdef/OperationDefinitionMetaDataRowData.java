package uk.nhs.fhir.makehtml.opdef;

import uk.nhs.fhir.util.LinkData;

public class OperationDefinitionMetaDataRowData {

	private final String rowTitle;
	private final LinkData typeLink;
	private final String content;

	public OperationDefinitionMetaDataRowData(String rowTitle, LinkData typeLink, String content) {
		this.rowTitle = rowTitle;
		this.typeLink = typeLink;
		this.content = content;
	}
	
	public String getRowTitle() {
		return rowTitle;
	}
	
	public LinkData getTypeLink() {
		return typeLink;
	}

	public String getContent() {
		return content;
	}
}
