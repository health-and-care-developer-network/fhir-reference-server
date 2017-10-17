package uk.nhs.fhir.makehtml.html.table;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.html.style.CSSTag;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.style.FhirColour;
import uk.nhs.fhir.makehtml.html.style.FhirFont;

public class Table {
	private final List<TableTitle> cols = Lists.newArrayList();
	private final List<TableRow> rows = Lists.newArrayList();
	
	public Table(List<TableTitle> cols) {
		this(cols, Lists.newArrayList());
	}
	
	public Table(List<TableTitle> cols, List<TableRow> rows) {
		this.cols.addAll(cols);
		this.rows.addAll(rows);
	}
	
	public void addRow(TableRow row) {
		rows.add(row);
	}
	
	public Element makeTable() {
		List<Element> titleElements = Lists.newArrayList();
		cols.forEach((TableTitle col) -> titleElements.add(col.makeTitleCell()));
		
		List<Element> rowElements = Lists.newArrayList();
		
		rows.forEach((TableRow row) -> rowElements.add(row.makeRow()));
		//rows.forEach((TableRow row) -> rowElements.add(applyMaxWidths(row.makeRow())));
		
		return Elements.withAttributeAndChildren("table",
			new Attribute("class", FhirCSS.TABLE),
			Lists.newArrayList(
				Elements.withChild("thead",
					Elements.withAttributeAndChildren("tr", 
						new Attribute("class", FhirCSS.TABLE_HEADER_ROW), 
						titleElements)),
				Elements.withChildren("tbody",
					rowElements)));
	}
	
	/*private Element applyMaxWidths(Element row) {
		List<Element> children = row.getChildren();
		
		for (int i=0; i<cols.size(); i++) {
			Optional<String> maxWidth = cols.get(i).getMaxWidth();
			if (maxWidth.isPresent()) {
				String styleKey = "style";
				String maxWidthStyle = "max-width: " + maxWidth.get();
				
				Element cell = children.get(i);
				
				Attribute styleAttribute = cell.getAttribute(styleKey);
				
				if (styleAttribute == null) {
					cell.setAttribute(styleKey, maxWidthStyle);
				} else {
					String currentValue = styleAttribute.getValue().trim();
					char lastChar = currentValue.charAt(currentValue.length() - 1);
					
					if (lastChar == ';') {
						cell.setAttribute(styleKey, currentValue + " " + maxWidthStyle);
					} else {
						cell.setAttribute(styleKey, currentValue + "; " + maxWidthStyle);
					}
				}
			}
		}
		
		return row;
	}*/

	public List<TableRow> getRows() {
		return rows;
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("tr", "." + FhirCSS.TABLE_TITLE), 
				Lists.newArrayList(
					new CSSRule(CSSTag.FONT_SIZE, "11px"),
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.TABLE_HEADER),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					new CSSRule(CSSTag.BORDER, "0"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TABLE_TITLE), 
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING, "0px 4px"))));

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("td"), 
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING, "5px 4px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL_HEADING_BOX), 
				Lists.newArrayList(
					new CSSRule(CSSTag.MARGIN, "-15px -15px 15px"),
					new CSSRule(CSSTag.PADDING, "10px 15px"),
					new CSSRule(CSSTag.BACKGROUND_COLOR, FhirColour.PANEL_HEADING_BACKGROUND),
					new CSSRule(CSSTag.BORDER_BOTTOM, "1px solid " + FhirColour.PANEL_HEADING_BOTTOM),
					new CSSRule(CSSTag.BORDER_TOP_LEFT_RADIUS, "3px"),
					new CSSRule(CSSTag.BORDER_TOP_RIGHT_RADIUS, "3px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL_HEADING_TEXT), 
				Lists.newArrayList(
					new CSSRule(CSSTag.MARGIN_TOP, "0"),
					new CSSRule(CSSTag.MARGIN_BOTTOM, "0"),
					new CSSRule(CSSTag.FONT_SIZE, "17.5px"),
					new CSSRule(CSSTag.FONT_WEIGHT, "500"))));
		
		
		styles.add(new CSSStyleBlock(Lists.newArrayList("tr"),
			Lists.newArrayList(
				new CSSRule(CSSTag.PADDING, "3px"),
				new CSSRule(CSSTag.LINE_HEIGHT, "1.66em"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TABLE_HEADER_ROW), 
				Lists.newArrayList(
					new CSSRule(CSSTag.BORDER, "1px solid " + FhirColour.TABLE_HEADER_BORDER))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("tr", FhirCSS.TABLE_TITLE, "td"), 
				Lists.newArrayList(
					new CSSRule(CSSTag.TEXT_ALIGN, "left"),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("table"), 
				Lists.newArrayList(
					new CSSRule(CSSTag.WIDTH, "100%"),
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.TABLE_BODY),
					new CSSRule(CSSTag.BORDER_COLLAPSE, "collapse"))));

		styles.add(new CSSStyleBlock(
			Lists.newArrayList("." + FhirCSS.PANEL + " *"),
			Lists.newArrayList(
				new CSSRule(CSSTag._WEBKIT_BOX_SIZING, "border-box"),
				new CSSRule(CSSTag._MOZ_BOX_SIZING, "border-box"),
				new CSSRule(CSSTag.BOX_SIZING, "border-box"))));
		
		// Styling hack to add space between thead and tbody
		styles.add(new CSSStyleBlock(
			Lists.newArrayList("." + FhirCSS.TABLE + " tbody:before"),
			Lists.newArrayList(
				new CSSRule(CSSTag.CONTENT, "'-'"),
				new CSSRule(CSSTag.DISPLAY, "block"),
				new CSSRule(CSSTag.LINE_HEIGHT, "1em"),
				new CSSRule(CSSTag.COLOR, "transparent"))));
		
		return styles;
	}
}
