package uk.nhs.fhir.makehtml.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.StringUtil;

public class LinkDatas {
	private final LinkedHashMap<SimpleLinkData, List<SimpleLinkData>> links = new LinkedHashMap<>();
	
	public LinkDatas(SimpleLinkData... links) {
		for (SimpleLinkData link : links) {
			addSimpleLink(link);
		}
	}

	public boolean isEmpty() {
		return links.isEmpty();
	}
	
	public Set<Entry<SimpleLinkData, List<SimpleLinkData>>> links() {
		return links.entrySet();
	}
	
	public void addSimpleLink(SimpleLinkData link) {
		if (links.containsKey(link)) {
			throw new IllegalStateException("Trying to add link \"" + link.getText() + "\" twice");
		} else {
			links.put(link, Lists.newArrayList());
		}
	}
	
	public void addNestedLink(SimpleLinkData outer, SimpleLinkData inner) {
		List<LinkData> matchingKeys = 
			links
				.keySet()
				.stream()
				.filter(link -> link.getText().equals(outer.getText()))
				.collect(Collectors.toList());
		
		if (matchingKeys.size() > 1) {
			throw new IllegalStateException("More than one outer link with text " + outer.getText());
		} else if (matchingKeys.size() == 1
		  && !matchingKeys.get(0).getURL().equals(outer.getURL())) {
			throw new IllegalStateException("Outer links with matching text but different links " + outer.getText() + " " 
				+ outer.getURL().toFullString() + ", " 
				+ matchingKeys.get(0).getURL().toFullString());
		}
		
		if (matchingKeys.isEmpty()) {
			links.put(outer, Lists.newArrayList(inner));
		} else {
			List<SimpleLinkData> nestedLinks = links.get(matchingKeys.get(0));
			
			long nestedMatchingText = nestedLinks
				.stream()
				.filter(innerLink -> innerLink.getText().equals(inner.getText()))
				.count();
			
			if (nestedMatchingText > 0) {
				throw new IllegalStateException("Adding multiple nested links under " + outer.getText() + " with inner text " + inner.getText());
			}
			
			long nestedMatchingTarget = nestedLinks
				.stream()
				.filter(innerLink -> innerLink.getURL().equals(inner.getURL()))
				.count();
			
			if (nestedMatchingTarget > 0) {
				throw new IllegalStateException("Adding multiple nested links under " + outer.getText()
					+ " with URL " + inner.getURL().toFullString());
			}
			
			nestedLinks.add(inner);
		}
	}
	
	/**
	 * LinkDatas are equal if they contain the same outer links each containing the same inner links
	 */
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (!(other instanceof LinkDatas)) {
			return false;
		}
		
		LinkDatas otherLinkDatas = (LinkDatas)other;
		
		if (links.keySet().size() != otherLinkDatas.links.keySet().size()) {
			return false;
		}
		
		for (Entry<SimpleLinkData, List<SimpleLinkData>> e : links.entrySet()) {
			SimpleLinkData key = e.getKey();
			List<SimpleLinkData> nested = e.getValue();
			
			List<SimpleLinkData> otherNested = otherLinkDatas.links.get(key);
			if (otherNested == null) {
				return false;
			} else if (nested.size() != otherNested.size()) {
				return false;
			} else if (!otherNested.containsAll(nested)) {
				return false;
			}
		}
		
		return true;
	}
	
	public long hashcode() {
		long hc = 0;

		for (Entry<SimpleLinkData, List<SimpleLinkData>> e : links.entrySet()) {
			SimpleLinkData key = e.getKey();
			List<SimpleLinkData> nested = e.getValue();
			
			hc += key.hashCode() * nested.hashCode();
		}
		
		return hc;
	}

	public void addNestedUri(SimpleLinkData outerLink, String nestedLinkUri) {
		String[] uriTokens = nestedLinkUri.split("/");
		String linkTargetName = uriTokens[uriTokens.length - 1];
		SimpleLinkData inner = new SimpleLinkData(FhirURL.buildOrThrow(nestedLinkUri), StringUtil.capitaliseLowerCase(linkTargetName));
		addNestedLink(outerLink, inner);
	}
}
