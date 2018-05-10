package uk.nhs.fhir.render.format.searchparam;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedSearchParameter;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;

public class SearchParameterTableFormatter extends TableFormatter<WrappedSearchParameter> {

	public SearchParameterTableFormatter(WrappedSearchParameter wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		String urlCode = wrappedResource.getUrlCode();
		List<String> associatedResourceTypes = wrappedResource.getAssociatedResourceTypes();
		String searchParamtype = wrappedResource.getType();
		Optional<String> expression = wrappedResource.getExpression();
		Optional<String> xPath = wrappedResource.getXPath();
		Optional<String> xPathUsage = wrappedResource.getXPathUsage();
		List<String> supportedComparators = wrappedResource.getSupportedComparators();
		List<String> modifiers = wrappedResource.getModifiers();
		
		String invocations = String.join("\n", wrappedResource.getInvocations());
		
		
		Element colgroup = getColGroup(4);
		List<Element> tableContent = Lists.newArrayList(colgroup);
		
		addFullRow(tableContent, "Type", searchParamtype);
		addFullRow(tableContent, "URL Code", urlCode);
		addFullRow(tableContent, "Applicable resources", String.join(", ", associatedResourceTypes));
		if (expression.isPresent()) {
			addFullRow(tableContent, "Expression", expression.get());
		}
		if (xPath.isPresent()) {
			addFullRow(tableContent, "XPath", xPath.get());
		}
		if (xPathUsage.isPresent()) {
			addFullRow(tableContent, "XPath usage", xPathUsage.get());
		}
		addFullRow(tableContent, "Supported comparators", String.join(", ", supportedComparators));
		addFullRow(tableContent, "Supported modifiers", String.join(", ", modifiers));
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Invocations", invocations, 4)));
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitle = "Details";
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		HTMLDocSection section = new HTMLDocSection();
		section.addBodyElement(panel.makePanel());
		
		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		
		return section;
	}
	
	protected void addFullRow(List<Element> tableContent, String label, String content) {
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell(label, content, 4)));
	}

}
