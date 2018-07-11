package uk.nhs.fhir.render.html.table;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
import uk.nhs.fhir.render.html.style.FhirFont;

public class Table {
	private final List<TableTitle> cols = Lists.newArrayList();
	private final List<TableRow> rows = Lists.newArrayList();
	private final List<String> additionalClasses = Lists.newArrayList();
	
	public Table(List<TableTitle> cols) {
		this(cols, Lists.newArrayList());
	}
	
	public Table(List<TableTitle> cols, List<TableRow> rows) {
		this(cols, rows, Lists.newArrayList());
	}
	
	public Table(List<TableTitle> cols, List<TableRow> rows, List<String> additionalClasses) {
		this.cols.addAll(cols);
		this.rows.addAll(rows);
		this.additionalClasses.addAll(additionalClasses);
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
		
		List<String> classes = Lists.newArrayList(FhirCSS.TABLE);
		classes.addAll(additionalClasses);
		
		return Elements.withAttributeAndChildren("table",
			new Attribute("class", String.join(" ", classes)),
			Lists.newArrayList(
				Elements.withChild("thead",
					Elements.withAttributeAndChildren("tr", 
						new Attribute("class", FhirCSS.TABLE_HEADER_ROW), 
						titleElements)),
				Elements.withChildren("tbody",
					rowElements)));
	}
	
	// ALP4-298 Changes -- Anand 11-July
	
    public Element makeTable_collapse() {
        List<Element> titleElements = Lists.newArrayList();
        cols.forEach((TableTitle col) -> titleElements.add(col.makeTitleCell()));
        
        List<Element> rowElements = Lists.newArrayList();
        rows.forEach((TableRow row) -> rowElements.add(row.makeRow()));
        String treeName = "treetable" + (int )(Math. random() * 5000 + 1);  // Anand comments - Generating random tablename
        List<String> classes = Lists.newArrayList(FhirCSS.TABLE);
        classes.addAll(additionalClasses);
        Element tableContent =  Elements.withAttributesAndChildren("table",
            Lists.newArrayList(new Attribute("class", "treetable ig-treetable"),new Attribute("id", treeName)),
            Lists.newArrayList(
                Elements.withChild("thead",
                    Elements.withAttributeAndChildren("tr", 
                        new Attribute("class", FhirCSS.TABLE_HEADER_ROW), 
                        titleElements)),
                Elements.withChildren("tbody",
                    rowElements)
                ));
        List<Element> contentElements = Lists.newArrayList();
        contentElements.add(Elements.withAttributeAndText("script", new  Attribute("src","/js/jquery-ui/treetable.min.js"),"null"));
        contentElements.add( Elements.withAttributesAndText("link", Lists.newArrayList(new Attribute("href","/js/jquery-ui/igViewer.min.css"),new Attribute("rel","stylesheet")) ,""));
        contentElements.add(Elements.withAttributeAndText("button", new  Attribute("onclick","resourceTreeTable.expandAll(\"" + treeName + "\");resourceTreeTable.init(\"" + treeName + "\")"),"ExpandAll"));
        contentElements.add(tableContent);
        contentElements.add(Elements.withAttributeAndText("script", new Attribute("language","javascript"),"var resourceTreeTable = new BaseTreeTable();\n" + 
                " resourceTreeTable.init(\"" + treeName + "\");"));
        
        
        Element finalContent = Elements.withChildren("br", contentElements);
        
        return finalContent;
        
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
			Lists.newArrayList("." + FhirCSS.TABLE + "." + FhirCSS.TREE + " tbody:before"),
			Lists.newArrayList(
				new CSSRule(CSSTag.CONTENT, "'-'"),
				new CSSRule(CSSTag.DISPLAY, "block"),
				new CSSRule(CSSTag.LINE_HEIGHT, "1em"),
				new CSSRule(CSSTag.COLOR, "transparent"))));

		styles.add(new CSSStyleBlock(
			Lists.newArrayList("." + FhirCSS.TREE),
			Lists.newArrayList(
				new CSSRule(CSSTag.TABLE_LAYOUT, "fixed"),
				new CSSRule(CSSTag.WIDTH, "100%"))));
		
		return styles;
	}
}
