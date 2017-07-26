package uk.nhs.fhir.makehtml.render.opdef;

import uk.nhs.fhir.makehtml.data.LinkDatas;

public class OperationDefinitionMetaDataRowData {

	private final String rowTitle;
	private final LinkDatas typeLink;
	private final String content;

	public OperationDefinitionMetaDataRowData(String rowTitle, LinkDatas typeLink, String content) {
		this.rowTitle = rowTitle;
		this.typeLink = typeLink;
		this.content = content;
	}
	
	public String getRowTitle() {
		return rowTitle;
	}
	
	public LinkDatas getTypeLink() {
		return typeLink;
	}

	public String getContent() {
		return content;
	}
}
