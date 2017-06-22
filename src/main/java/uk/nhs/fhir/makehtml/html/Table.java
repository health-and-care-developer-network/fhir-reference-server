package uk.nhs.fhir.makehtml.html;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;

public class Table {
	private final List<TableTitle> cols = Lists.newArrayList();
	private final List<TableRow> rows = Lists.newArrayList();
	private final Set<String> tableClasses = Sets.newHashSet();
	
	public Table(List<TableTitle> cols, Set<String> tableClasses) {
		this(cols, Lists.newArrayList(), tableClasses);
	}
	
	public Table(List<TableTitle> cols, List<TableRow> rows, Set<String> tableClasses) {
		this.cols.addAll(cols);
		this.rows.addAll(rows);
		this.tableClasses.addAll(tableClasses);
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
					new CSSRule("font-size", "11px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("border", "0"))));
		
		styles.add(new CSSStyleBlock(Lists.newArrayList("td", "." + FhirCSS.TABLE_TITLE), 
			Lists.newArrayList(
				new CSSRule("padding", "0px 4px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL_HEADING_BOX), 
				Lists.newArrayList(
					new CSSRule("margin", "-15px -15px 15px"),
					new CSSRule("padding", "10px 15px"),
					new CSSRule("background-color", "#f7f7f7"),
					new CSSRule("border-bottom", "1px solid #dddddd"),
					new CSSRule("border-top-left-radius", "3px"),
					new CSSRule("border-top-right-radius", "3px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL_HEADING_TEXT), 
				Lists.newArrayList(
					new CSSRule("margin-top", "0"),
					new CSSRule("margin-bottom", "0"),
					new CSSRule("font-size", "17.5px"),
					new CSSRule("font-weight", "500"))));
		
		
		styles.add(new CSSStyleBlock(Lists.newArrayList("tr"),
			Lists.newArrayList(
				new CSSRule("padding", "3px"),
				new CSSRule("line-height", "1.66em"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TABLE_HEADER_ROW), 
				Lists.newArrayList(
					new CSSRule("border", "1px #F0F0F0 solid"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("tr", FhirCSS.TABLE_TITLE, "td"), 
				Lists.newArrayList(
					new CSSRule("text-align", "left"),
					new CSSRule("vertical-align", "top"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("table"), 
				Lists.newArrayList(
					new CSSRule("width", "100%"),
					new CSSRule("font-family", "sans-serif"),
					new CSSRule("border-collapse", "collapse"))));

		styles.add(new CSSStyleBlock(
			Lists.newArrayList("." + FhirCSS.PANEL + " *"),
			Lists.newArrayList(
				new CSSRule("-webkit-box-sizing", "border-box"),
				new CSSRule("-moz-box-sizing", "border-box"),
				new CSSRule("box-sizing", "border-box"))));
		
		// Styling hack to add space between thead and tbody
		styles.add(new CSSStyleBlock(
			Lists.newArrayList("." + FhirCSS.TABLE + " tbody:before"),
			Lists.newArrayList(
				new CSSRule("content", "'-'"),
				new CSSRule("display", "block"),
				new CSSRule("line-height", "1em"),
				new CSSRule("color", "transparent"))));
		
		return styles;
	}
}
