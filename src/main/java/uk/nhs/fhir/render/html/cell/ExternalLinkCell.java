package uk.nhs.fhir.render.html.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
import uk.nhs.fhir.render.html.tree.FhirIcon;

public class ExternalLinkCell extends TableCell {
	private final Set<String> linkClasses;
	private final String url;
	private final String display;

	public ExternalLinkCell(String url, String display) {
		this(url, display, Sets.newHashSet(), Sets.newHashSet());
	}
	
	public ExternalLinkCell(String url, String display, boolean bordered) {
		this(url, display, false, false, bordered);
	}
	
	public ExternalLinkCell(String url, String display, Set<String> cellClasses, Set<String> linkClasses) {
		this(url, display, cellClasses, linkClasses, false, false, false);
	}

	public ExternalLinkCell(String url, String display, boolean faded, boolean strikethrough, boolean bordered) {
		this(url, display, Sets.newHashSet(), Sets.newHashSet(), faded, strikethrough, bordered);
	}
	
	public ExternalLinkCell(String url, String display, Set<String> cellClasses, Set<String> linkClasses, boolean faded, boolean strikethrough, boolean bordered) {
		super(bordered, faded, strikethrough);
		this.url = url;
		this.display = display;
		this.cellClasses.addAll(cellClasses);
		this.linkClasses = linkClasses;
	}

	@Override
	public Element makeCell() {
		
		List<Attribute> cellAttributes = Lists.newArrayList();
		if (colspan.isPresent()) {
			cellAttributes.add(new Attribute("colspan", Integer.toString(colspan.get())));
		}
		
		ArrayList<Element> tableCellContent = new ArrayList<Element>();
		
		// Add link
		Attribute href = new Attribute("href", this.url);
		Element link = Elements.withAttributeAndText("a", href, this.display);
		tableCellContent.add(link);
		
		return Elements.addClasses(
			Elements.withAttributesAndChildren(
				"td", cellAttributes, tableCellContent
				),
			cellClasses);
	}
}
