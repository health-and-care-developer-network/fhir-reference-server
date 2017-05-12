package uk.nhs.fhir.makehtml;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.html.CSSRule;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.TablePNGGenerator;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.util.Elements;

public abstract class TreeTableFormatter extends ResourceFormatter {

	TablePNGGenerator backgrounds = new TablePNGGenerator();
	
	protected void addStyles(HTMLDocSection section) {
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		ResourceFlagsCell.getStyles().forEach(section::addStyle);
	}

	protected Set<String> getTableBackgroundStyleKeys(Element table) {
		Set<String> tableBackgroundStyles = Sets.newHashSet();
		
		for (Element dataCell : table.getDescendants(Filters.element("td", Namespace.getNamespace(Elements.HTML_NS_URL)))) {
			Attribute classAttribute = dataCell.getAttribute("class");
			
			if (classAttribute != null) {
				Set<String> classes = Sets.newHashSet(classAttribute.getValue().split(" "));
				for (String classProperty : classes) {
					if (classProperty.startsWith("fhirtreebg-")) {
						tableBackgroundStyles.add(classProperty);
					}
				}
			}
		}
		
		return tableBackgroundStyles;
	}
	
	protected List<CSSStyleBlock> getTableBackgroundStyles(Element table) {
		
		List<CSSStyleBlock> backgroundStyles = Lists.newArrayList();
		
		Set<String> backgroundKeys = getTableBackgroundStyleKeys(table);
		for (String key : backgroundKeys) {
			String backgroundBase64 = backgrounds.getBase64(key);
			backgroundStyles.add(
				new CSSStyleBlock(Lists.newArrayList("." + key),
					Lists.newArrayList(
						new CSSRule("background-image", "url(data:image/png;base64," + backgroundBase64 + ")"),
						new CSSRule("background-repeat", "repeat-y"))));
		}
		
		return backgroundStyles;
	}

}
