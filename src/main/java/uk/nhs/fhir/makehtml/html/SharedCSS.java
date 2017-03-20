package uk.nhs.fhir.makehtml.html;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;

public class SharedCSS {
	
	public static List<CSSStyleBlock> getTableStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("tr", ".fhir-table-title"), 
				Lists.newArrayList(
					new CSSRule("font-size", "11px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("border", "0"))));
		
		styles.add(new CSSStyleBlock(Lists.newArrayList("td", ".fhir-table-title"), 
			Lists.newArrayList(
				new CSSRule("padding", "0px 4px 0px 4px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel-heading-box"), 
				Lists.newArrayList(
					new CSSRule("margin", "-15px -15px 15px"),
					new CSSRule("padding", "10px 15px"),
					new CSSRule("background-color", "#f7f7f7"),
					new CSSRule("border-bottom", "1px solid #dddddd"),
					new CSSRule("border-top-left-radius", "3px"),
					new CSSRule("border-top-right-radius", "3px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel-heading-text"), 
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
			new CSSStyleBlock(Lists.newArrayList(".fhir-table-header-row"), 
				Lists.newArrayList(
					new CSSRule("border", "1px #F0F0F0 solid"),
					new CSSRule("margin-bottom", "10px"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("tr", "fhir-table-title", "td"), 
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
			Lists.newArrayList(".fhir-panel *"),
			Lists.newArrayList(
				new CSSRule("-webkit-box-sizing", "border-box"),
				new CSSRule("-moz-box-sizing", "border-box"),
				new CSSRule("box-sizing", "border-box"))));
		
		return styles;
	}
	
	public static List<CSSStyleBlock> getPanelStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel"), //html
				Lists.newArrayList(
					new CSSRule("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif"),
					new CSSRule("font-size", "14px"),
					new CSSRule("line-height", "1.4"),
					new CSSRule("width", "95%"),
					new CSSRule("max-width", "940px"),
					new CSSRule("word-wrap", "break-word"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel"), 
				Lists.newArrayList(
					new CSSRule("padding", "15px"),
					new CSSRule("margin-bottom", "20"),
					new CSSRule("background-color", "#ffffff"),
					new CSSRule("border", "1px solid #dddddd"),
					new CSSRule("border-radius", "4px"),
					new CSSRule("box-shadow", "0 1px 1px rgba(0, 0, 0, 0.05)"))));

		return styles;
	}
}
