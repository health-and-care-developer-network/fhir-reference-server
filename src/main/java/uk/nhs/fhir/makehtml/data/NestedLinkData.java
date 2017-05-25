package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Data for two URL links, one of which is 'nested' within the other
 * @author jon
 */
public class NestedLinkData implements LinkData {

	private SimpleLinkData outerLink;
	private List<SimpleLinkData> nestedLinks = Lists.newArrayList();
	
	public NestedLinkData(SimpleLinkData outerLink, List<SimpleLinkData> nested) {
		Preconditions.checkNotNull(outerLink);
		Preconditions.checkNotNull(nested);
		Preconditions.checkArgument(!nested.isEmpty());
		
		this.outerLink = outerLink;
		this.nestedLinks = nested;
	}
	
	public List<SimpleLinkData> getNestedLinks() {
		return nestedLinks;
	}
	
	@Override
	public SimpleLinkData getPrimaryLinkData() {
		return outerLink;
	}

	@Override
	public String getURL() {
		return outerLink.getURL();
	}

	@Override
	public String getText() {
		return outerLink.getText();
	}
	
	@Override
	public int hashCode() {
		return outerLink.hashCode() + nestedLinks.hashCode();
	}
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof NestedLinkData)) {
			return false;
		}
		
		NestedLinkData otherLinkData = (NestedLinkData)other;
		return outerLink.equals(otherLinkData.getPrimaryLinkData())
			&& nestedLinks.stream().allMatch(link -> otherLinkData.getNestedLinks().contains(link));
	}
}
