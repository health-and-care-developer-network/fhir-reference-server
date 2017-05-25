package uk.nhs.fhir.makehtml.structdef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.ResourceFormatter;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.html.Dstu2Fix;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.util.Elements;

public class StructureDefinitionBindingFormatter extends ResourceFormatter {

	public StructureDefinitionBindingFormatter() { this.resourceSectionType = ResourceSectionType.BINDING; }

    private static final String BLANK = "";

    private List<Element> tableContent = null;
    List<String> done = new ArrayList<String>();

    Boolean foundBinding = false;

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;
		
		HTMLDocSection section = new HTMLDocSection();

        Element colgroup = Elements.newElement("colgroup");
        int columns = 4;
        Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

        int percentPerColumn = 10;
        colgroup.addContent(
                Elements.withAttributes("col",
                        Lists.newArrayList(
                                new Attribute("width", Integer.toString(2 * percentPerColumn) + "%"))));
        colgroup.addContent(
                Elements.withAttributes("col",
                        Lists.newArrayList(
                                new Attribute("width", Integer.toString(4 * percentPerColumn) + "%"))));
        colgroup.addContent(
                Elements.withAttributes("col",
                        Lists.newArrayList(
                                new Attribute("width", Integer.toString(1 * percentPerColumn) + "%"))));
        colgroup.addContent(
                Elements.withAttributes("col",
                        Lists.newArrayList(
                                new Attribute("width", Integer.toString(3 * percentPerColumn) + "%"))));


        tableContent = Lists.newArrayList(colgroup);
        tableContent.add(
                Elements.withChildren("tr",
                        labelledValueCell("Path",BLANK,1, null),
                        labelledValueCell("Definition",BLANK, 1, null),
                        labelledValueCell("Type",BLANK,  1, null),
                        labelledValueCell("Reference",BLANK, 1, null)
                ));
        StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);

        for (FhirTreeTableContent content : dataProvider.getSnapshotTreeData()) {
            processNode(content);
        }

        addStyles(section);
        Element table =
                Elements.withAttributeAndChildren("table",
                        new Attribute("class", "fhir-table"),
                        tableContent);

        FhirPanel panel = new FhirPanel("Bindings", table);

        section.addBodyElement(panel.makePanel());

        if (!foundBinding) { return null; }
		return section;
	}

	private void processNode(FhirTreeTableContent node)
    {

        if (node.hasElement()) {
            ElementDefinitionDt element = node.getElement().get();
            if (node.getBinding().isPresent()) {

                Optional<String> description = Optional.ofNullable(element.getDefinition());
                String displayDescription = (description != null && description.isPresent()) ? description.get() : BLANK;
                //System.out.println(displayDescription);
                IDatatype valueset = element.getBinding().getValueSet();
                String displayValueSet = "";
                if (valueset instanceof ResourceReferenceDt) {
                    displayValueSet = ((ResourceReferenceDt) valueset).getReference().getValue();
                }
                if (valueset instanceof UriDt) {
                    displayValueSet = ((UriDt) valueset).getValue();
                }

                String path = element.getPath() + displayDescription;
                if (isElementIsActive(node) && !done.stream().anyMatch(str -> str.trim().equals(path))) {
                    foundBinding = true;
                    tableContent.add(
                            Elements.withChildren("tr",
                                    labelledValueCell(BLANK, element.getPath(), 1, null),
                                    labelledValueCell(BLANK, displayDescription, 1, null),
                                    labelledValueCell(BLANK, element.getBinding().getStrength(), 1, "https://www.hl7.org/fhir/terminologies.html#example"),
                                    labelledValueCell(BLANK, displayValueSet, 1, null)
                            ));
                    done.add(path);
                }

            }
        }
    }

    Boolean isElementIsActive(FhirTreeTableContent node)
    {
        if (node.isRemovedByProfile())
        {
            return false;
        }
        if (node.getParent() != null) { return isElementIsActive(node.getParent()); }
        return true;
    }

    private Element labelledValueCell(String label, String value, int colspan, String uri) {
        boolean url = (value.startsWith("http://") || value.startsWith("https://"));
        boolean internal = (value.contains("https://fhir.nhs.uk/"));

        if (uri == null) {
            if (url) { uri = value; }
            else { uri = ""; }
        }
        else { url = true; }


        String fhirMetadataClass = "fhir-metadata-value";
        if (label !=null && !label.isEmpty()) {
            value = label;
            fhirMetadataClass = "fhir-metadata-label";
        }
        List<Element> cellSpans = Lists.newArrayList();


        if (url) {
           if (internal) {
               cellSpans.add(
                       Elements.withAttributeAndChild("span",
                               new Attribute("class", fhirMetadataClass),
                               Elements.withAttributesAndText("a",
                                       Lists.newArrayList(
                                               new Attribute("class", "fhir-link"),
                                               new Attribute("href", Dstu2Fix.dstu2links(uri))
                                               ),
                                       value))
               );
           } else {
               cellSpans.add(
                       Elements.withAttributeAndChild("span",
                               new Attribute("class", fhirMetadataClass),
                               Elements.withAttributesAndChildren("a",
                                       Lists.newArrayList(
                                               new Attribute("class", "fhir-link"),
                                               new Attribute("href", Dstu2Fix.dstu2links(uri))
                                               ),
                                       Lists.newArrayList(
                                                new Text(value),
                                               Elements.withAttributes("img",
                                                       Lists.newArrayList(
                                                               new Attribute("src", FhirIcon.REFERENCE.getUrl()),
                                                               new Attribute("class", "fhir-tree-resource-icon")))
                                       )
                               )
                       ));
           }

        }
        else {
            cellSpans.add(Elements.withAttributeAndText("span",
                    new Attribute("class", fhirMetadataClass),
                    value));
        }
        return cell(cellSpans, colspan);
    }



    private Element cell(List<? extends Content> content, int colspan) {
        return Elements.withAttributesAndChildren("td",
                Lists.newArrayList(
                        new Attribute("class", "fhir-metadata-cell"),
                        new Attribute("colspan", Integer.toString(colspan))),
                content);
    }



    public void addStyles(HTMLDocSection section) {
        Table.getStyles().forEach(section::addStyle);
        FhirPanel.getStyles().forEach(section::addStyle);
        ValueWithInfoCell.getStyles().forEach(section::addStyle);
        LinkCell.getStyles().forEach(section::addStyle);
        ResourceFlagsCell.getStyles().forEach(section::addStyle);
        StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
    }
}
