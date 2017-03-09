package uk.nhs.fhir.makehtml;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import uk.nhs.fhir.util.Elements;

public class OperationDefinitionFormatter extends ResourceFormatter<OperationDefinition> {
	
	@Override
	public HTMLDocSection makeSectionHTML(OperationDefinition source) throws ParserConfigurationException {
		
		List<Parameter> inputParameters = Lists.newArrayList();
		List<Parameter> outputParameters = Lists.newArrayList();
		for (Parameter parameter : source.getParameter()) {
			switch (parameter.getUseElement().getValueAsEnum()) {
			case IN:
				inputParameters.add(parameter);
				break;
			case OUT:
				outputParameters.add(parameter);
				break;
			}
		}
		
		Element renderedOperationDefinition =
			Elements.withAttributeAndChildren("div",
				new Attribute("id", "fhir-ref-operation-definition-structure"),
				Lists.newArrayList(
					new OperationDefinitionMetaDataPanel(source).buildPanel(),
					new OperationDefinitionParametersPanel("Input Parameters", inputParameters, fhirDocLinkFactory).buildPanel(),
					new OperationDefinitionParametersPanel("Output Parameters", outputParameters, fhirDocLinkFactory).buildPanel()));
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(renderedOperationDefinition);
		
		return section;
	}

	private void addStyles(HTMLDocSection section) {
		section.addStyle(
			new CSSStyleBlock(
				Lists.newArrayList("*"),
				Lists.newArrayList(
					new CSSRule("-webkit-box-sizing", "border-box"),
					new CSSRule("-moz-box-sizing", "border-box"),
					new CSSRule("box-sizing", "border-box"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList("tr", "th"), 
				Lists.newArrayList(
					new CSSRule("font-size", "11px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("border", "0")
				)));
		section.addStyle(new CSSStyleBlock(Lists.newArrayList("td", "th"), 
			Lists.newArrayList(
				new CSSRule("padding", "0px 4px 0px 4px"))));
		section.addStyle(new CSSStyleBlock(Lists.newArrayList("tr"),
			Lists.newArrayList(
					new CSSRule("padding", "3px"),
					new CSSRule("line-height", "1.66em"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList("thead tr"), 
				Lists.newArrayList(
					new CSSRule("border", "1px #F0F0F0 solid"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList("tr", "th", "td"), 
				Lists.newArrayList(
					new CSSRule("text-align", "left"),
					new CSSRule("vertical-align", "top"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList("table"), 
				Lists.newArrayList(
					new CSSRule("width", "100%"),
					new CSSRule("border-collapse", "collapse"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList("body"), 
				Lists.newArrayList(
					new CSSRule("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif"),
					new CSSRule("font-size", "14px"),
					new CSSRule("line-height", "1.4"),
					new CSSRule("width", "95%"),
					new CSSRule("max-width", "940px"),
					new CSSRule("word-wrap", "break-word"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel-heading-box"), 
				Lists.newArrayList(
					new CSSRule("margin", "-15px -15px 15px"),
					new CSSRule("padding", "10px 15px"),
					new CSSRule("background-color", "#f7f7f7"),
					new CSSRule("border-bottom", "1px solid #dddddd"),
					new CSSRule("border-top-left-radius", "3px"),
					new CSSRule("border-top-right-radius", "3px")
					)));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel-heading-text"), 
				Lists.newArrayList(
					new CSSRule("margin-top", "0"),
					new CSSRule("margin-bottom", "0"),
					new CSSRule("font-size", "17.5px"),
					new CSSRule("font-weight", "500")
					)));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel"), 
				Lists.newArrayList(
					new CSSRule("padding", "15px"),
					new CSSRule("margin-bottom", "20"),
					new CSSRule("background-color", "#ffffff"),
					new CSSRule("border", "1px solid #dddddd"),
					new CSSRule("border-radius", "4px"),
					new CSSRule("box-shadow", "0 1px 1px rgba(0, 0, 0, 0.05)")
					)));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList(".fhir-link"), 
				Lists.newArrayList(
					new CSSRule("text-decoration", "none"),
					new CSSRule("color", "#005EB8"))));
		section.addStyle(
			new CSSStyleBlock(Lists.newArrayList(".fhir-resource-flag"),
				Lists.newArrayList(
					new CSSRule("display", "inline"),
					new CSSRule("background-color", "#cccccc"),
					new CSSRule("color", "#ffffff"),
					new CSSRule("font-weight", "bold"),
					new CSSRule("font-size", "10px"),
					new CSSRule("padding", ".2em .6em .3em"),
					new CSSRule("text-align", "center"),
					new CSSRule("vertical-align", "baseline"),
					new CSSRule("white-space", "nowrap"),
					new CSSRule("line-height", "1"),
					new CSSRule("border-radius", ".25em"))));
	}
}
