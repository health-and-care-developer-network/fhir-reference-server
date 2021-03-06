package uk.nhs.fhir.render.format;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.TablePNGGenerator;

public abstract class TreeTableFormatter<T extends WrappedResource<T>> extends ResourceFormatter<T> {

	public TreeTableFormatter(T wrappedResource) {
		super(wrappedResource);
	}

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
						new CSSRule(CSSTag.BACKGROUND_IMAGE, "url(data:image/png;base64," + backgroundBase64 + ")"),
						new CSSRule(CSSTag.BACKGROUND_REPEAT, "repeat-y"))));
		}
		
		return backgroundStyles;
	}

}
