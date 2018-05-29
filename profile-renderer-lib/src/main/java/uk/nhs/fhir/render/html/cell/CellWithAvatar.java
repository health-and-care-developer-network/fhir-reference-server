package uk.nhs.fhir.render.html.cell;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;

public class CellWithAvatar extends TableCell {
	
	private final String text;
	private String avatarUrl;

	public CellWithAvatar(String text) {
		this(text, false);
	}
	
	public CellWithAvatar(String text, boolean bordered) {
		this(text, bordered, false, false);
	}
	
	public CellWithAvatar(String text, boolean bordered, boolean faded, boolean strikethrough) {
		super(bordered, faded, strikethrough);
		this.text = text;
	}
	
	public void setAvatarUrl(String url) {
		this.avatarUrl = url;
	}
	
	@Override
	public Element makeCell() {
		List<Attribute> cellAttributes = Lists.newArrayList();
		if (colspan.isPresent()) {
			cellAttributes.add(new Attribute("colspan", Integer.toString(colspan.get())));
		}
		
		ArrayList<Element> tableCellContent = new ArrayList<Element>();
		if (this.avatarUrl != null) {
			List<Attribute> imgAttributes = Lists.newArrayList();
			imgAttributes.add(new Attribute("src", this.avatarUrl));
			imgAttributes.add(new Attribute("height", "20"));
			imgAttributes.add(new Attribute("width", "20"));
			Element img = Elements.withAttributes("img", imgAttributes);
			tableCellContent.add(img);
		}
		Element name = Elements.withText("name",this.text);
		tableCellContent.add(name);
		
		return Elements.addClasses(
			Elements.withAttributesAndChildren(
				"td", cellAttributes, tableCellContent
				),
			cellClasses);
	}

}
