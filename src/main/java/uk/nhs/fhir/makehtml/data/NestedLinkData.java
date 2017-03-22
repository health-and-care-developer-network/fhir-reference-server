package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Data for two URL links, one of which is 'nested' within the other
 * @author jon
 */
public class NestedLinkData extends LinkData {

	private List<LinkData> nestedLinks = Lists.newArrayList();
	
	public NestedLinkData(LinkData outer, List<LinkData> nested) {
		super(outer.getURL(), outer.getText());
		
		if (nested != null) {
			nestedLinks.addAll(nested);
		}
	}
	
	public List<LinkData> getNestedLinks() {
		return nestedLinks;
	}
}
