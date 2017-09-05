package uk.nhs.fhir.makehtml.render.structdef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.tree.FhirTreeTableContent;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.ValuesetLinkFix;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.tree.FhirIcon;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class StructureDefinitionBindingFormatter extends ResourceFormatter<WrappedStructureDefinition> {
	
	public StructureDefinitionBindingFormatter(WrappedStructureDefinition structureDefinition, FhirFileRegistry otherResources) {
		super(structureDefinition, otherResources);
	}

    private static final String BLANK = "";

    private List<Element> tableContent = null;
    List<String> done = new ArrayList<String>();

    Boolean foundBinding = false;

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
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
        StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource, otherResources);

        for (FhirTreeTableContent content : dataProvider.getSnapshotTreeData()) {
            processNode(content);
        }

        addStyles(section);
        Element table =
                Elements.withAttributeAndChildren("table",
                        new Attribute("class", FhirCSS.TABLE),
                        tableContent);

        FhirPanel panel = new FhirPanel("Bindings", table);

        section.addBodyElement(panel.makePanel());

        if (!foundBinding) { return null; }
		return section;
	}

	private void processNode(FhirTreeTableContent node)
    {
        if (node.getBinding().isPresent()) {

            Optional<String> description = node.getDefinition();
            String displayDescription = description.isPresent() ? description.get() : BLANK;
           
            BindingInfo bindingInfo = node.getBinding().get();
			Optional<FhirURL> url = bindingInfo.getUrl();

            //String displayValueSet = url.isPresent() ? url.get().toString() : "";
            Element valueSetCell;
            if (url.isPresent()) {
            	valueSetCell = labelledValueCell(BLANK, url.get(), 1);
            } else {
            	valueSetCell = labelledValueCell(BLANK, "", 1, null);
            }
            
            String path = node.getPath() + displayDescription;
            if (!node.isRemovedByProfile() 
              && !done.stream().anyMatch(str -> str.trim().equals(path))) {
            	String bindingStrength = bindingInfo.getStrength();
            	String anchorStrength = bindingStrength.equals("required") ? "code" : bindingStrength;
            	
                foundBinding = true;
                tableContent.add(
                    Elements.withChildren("tr",
                        labelledValueCell(BLANK, node.getPath(), 1, "details.html#" + node.getNodeKey()),
                        labelledValueCell(BLANK, displayDescription, 1, null),
                        labelledValueCell(BLANK, bindingStrength, 1, FhirURLConstants.HTTP_HL7_FHIR + "/terminologies.html#" + anchorStrength),
                        valueSetCell
                    ));
                done.add(path);
            }

        }
    }
    
    private Element labelledValueCell(String label, FhirURL url, int colspan) {
    	
    	return labelledValueCell(label, url.toFullString(), colspan, url.isLogicalUrl() ? null : url.toLinkString());
    }

    private Element labelledValueCell(String label, String value, int colspan, String uriOverride) {
    	
    	Optional<String> uriToDisplay = Optional.empty();
    	if (!Strings.isNullOrEmpty(uriOverride)) {
    		uriToDisplay = Optional.of(uriOverride);
    	} else if (value.startsWith("http://") || value.startsWith("https://")) {
    		uriToDisplay = Optional.of(value);
    	}
    	
    	String cssClass = FhirCSS.DATA_VALUE;
    	String displayText = value;
    	if (!Strings.isNullOrEmpty(label)) {
    		displayText = label;
    		cssClass = FhirCSS.DATA_LABEL;
    	}
        
        List<Element> cellSpans = Lists.newArrayList();
        if (!uriToDisplay.isPresent()) {
            cellSpans.add(Elements.withAttributeAndText("span",
                new Attribute("class", cssClass),
                displayText));
        } else {
        	String uri = uriToDisplay.get();
        	
        	uri = ValuesetLinkFix.fixLink(uri, getResourceVersion());
        	
        	List<Content> linkContents = Lists.newArrayList();
    		linkContents.add(new Text(displayText));

        	boolean internal = (!uri.startsWith("http://") && !uri.startsWith("https://")) 
        	  || uri.contains(FhirURLConstants.FHIR_NHS_UK);
    		
        	if (!internal) {
        		linkContents.add(
        			Elements.withAttributes("img",
               			Lists.newArrayList(
           					new Attribute("src", FhirIcon.REFERENCE.getUrl()),
           					new Attribute("class", FhirCSS.TREE_RESOURCE_ICON))));
        	}
        	
        	boolean detailsLink = uri.startsWith("details.html#");
        	
        	if (!detailsLink) {
        		// allow checking whether these URLs are dead links
        		FhirURL.addLinkUrl(uri);
        	}
        	
    		cellSpans.add(linkSpan(linkContents, cssClass, uri, detailsLink));

        }
        
        return cell(cellSpans, colspan);
    }
    
    private Element linkSpan(List<Content> linkContents, String spanClass, String uri, boolean detailsLink) {
    	return Elements.withAttributeAndChild("span",
			new Attribute("class", spanClass),
			Elements.withAttributesAndChildren("a",
				Lists.newArrayList(
					new Attribute("class", FhirCSS.LINK + (detailsLink ? " " + "tabLink" : "")),
					new Attribute("href", uri)),
				linkContents));
    }

    private Element cell(List<? extends Content> content, int colspan) {
        return Elements.withAttributesAndChildren("td",
                Lists.newArrayList(
                        new Attribute("class", FhirCSS.DATA_CELL),
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
