package uk.nhs.fhir.makehtml.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.util.Elements;

public class ValueSetMetadataFormatter extends MetadataTableFormatter {

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		ValueSet valueSet = (ValueSet)source;
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(valueSet);
		section.addBodyElement(metadataPanel);
		
		return section;
	}

	public Element getMetadataTable(ValueSet source) {

		Optional<String>  url = Optional.ofNullable(source.getUrl());
		Optional<String>  name = Optional.ofNullable(source.getName());

		String status = source.getStatus().toString();

		Optional<String> OID= Optional.empty();
		Optional<String> reference = Optional.empty();

		for (ExtensionDt extension : source.getUndeclaredExtensions())
		{
			if (extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-sourceReference")) {
				UriDt uri = (UriDt) extension.getValue();
				reference = Optional.ofNullable(uri.getValueAsString());
			}
			if (extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-oid")) {
				UriDt uri = (UriDt) extension.getValue();
				OID = Optional.ofNullable(uri.getValueAsString());
			}

		}

		Optional<String> version = Optional.ofNullable(source.getVersion());

		String displayExperimental;
		Boolean experimental = source.getExperimental();
		if (experimental == null) {
			displayExperimental = BLANK;
		} else {
			displayExperimental = experimental ? "Yes" : "No";
		}
		Optional<String> description = Optional.ofNullable(source.getDescription());
		Optional<String> publisher = Optional.ofNullable(source.getPublisher());
		Optional<String> copyright = Optional.ofNullable(source.getCopyright());

		Optional<String> requirement = Optional.ofNullable(source.getRequirements());

		String gridName = name.get();
		if (version.isPresent()) {
			gridName += " (v" + version.get() + ")";
		}
        DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
		Date date = source.getDate();
		Optional<String> displayDate =
				(date == null) ?
						Optional.empty() :
						Optional.of(df.format(date));
        if (!displayDate.isPresent())
        {
            Date lastUpdated = source.getMeta().getLastUpdated();
            if (lastUpdated != null)
            displayDate = Optional.of(df.format(lastUpdated));
        }


        Element colgroup = Elements.newElement("colgroup");
        int columns = 4;
        Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

        int percentPerColumn = 100/columns;
        for (int i=0; i<columns; i++) {
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(percentPerColumn) + "%"))));
        }

        List<Element> tableContent = Lists.newArrayList(colgroup);

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", gridName, 2, true),
				labelledValueCell("URL", url.get(), 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
					labelledValueCell("Status", status, 1),
					labelledValueCell("Version", version, 1),
					labelledValueCell("Last updated", displayDate, 1),

						labelledValueCell("Experimental", displayExperimental, 1)
						));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Description", description.get(), 4, true)
				));
        // KGM 5/May/2017
        if (requirement.isPresent()) {
            tableContent.add(
                    Elements.withChildren("tr",
                            labelledValueCell("Requirements", requirement.get(), 4, true)
                    ));
        }
        if (publisher.isPresent()) {
            tableContent.add(
                    Elements.withChildren("tr",
                            labelledValueCell("Publisher", publisher.get(), 4)
                    ));
        }
        if (copyright.isPresent()) {
            tableContent.add(
                    Elements.withChildren("tr",
                            labelledValueCell("Copyright", copyright.get(), 4, true)
                    ));
        }
		if (reference.isPresent()) {
			tableContent.add(
					Elements.withChildren("tr",
							labelledValueCell("Reference", reference.get(), 4, true)
					));
		}

		// Should this be in the identifier section? Makes sense when linked to Hl7v2 tables
		if (OID.isPresent()) {
			tableContent.add(
					Elements.withChildren("tr",
							labelledValueCell("OID", OID.get(), 4, true)
					));
		}

		

		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", "fhir-table"),
				tableContent);
		
		String panelTitleName =  name.get();
		String panelTitle = "ValueSet: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
}
