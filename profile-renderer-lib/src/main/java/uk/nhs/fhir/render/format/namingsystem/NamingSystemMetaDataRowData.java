package uk.nhs.fhir.render.format.namingsystem;

import uk.nhs.fhir.data.url.LinkData;

public class NamingSystemMetaDataRowData {

	private final String rowTitle;
	private final String content;

	public NamingSystemMetaDataRowData(String rowTitle,  String content) {
		this.rowTitle = rowTitle;
		this.content = content;
	}
	
	public String getRowTitle() {
		return rowTitle;
	}
	
	public String getContent() {
		return content;
	}
}