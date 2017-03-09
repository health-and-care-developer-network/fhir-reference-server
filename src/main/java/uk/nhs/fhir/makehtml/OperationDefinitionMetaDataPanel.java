package uk.nhs.fhir.makehtml;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.valueset.OperationKindEnum;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.util.ColumnData;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.LinkData;
import uk.nhs.fhir.util.StringUtil;

public class OperationDefinitionMetaDataPanel extends HTMLDocumentTablePanel {
	
	private final OperationDefinition source;
	private final FhirDocLinkFactory fhirDocLinkFactory;

	public OperationDefinitionMetaDataPanel(OperationDefinition source) {
		this(source, new FhirDocLinkFactory(FhirContext.forDstu2()));
	}
	
	public OperationDefinitionMetaDataPanel(OperationDefinition source, FhirDocLinkFactory linkDataFactory) {
		this.source = source;
		this.fhirDocLinkFactory = linkDataFactory;
	}

	@Override
	protected String getHeadingText() {
		return "Meta";
	}
	
	protected final List<ColumnData> panelColumnTitles() {
		return Lists.newArrayList(
			new ColumnData("Name", "The logical name of the element", "100px"),
			new ColumnData("Type", "Reference to the type of the element", "100px"),
			new ColumnData("Value", "Additional information about the element", "290px")
		);
	}

	protected Element createTableBody() {
		List<Content> bodyRows = Lists.newArrayList(
			createElementRow("Name", source.getNameElement()),
			createOperationKindRow("Kind", source.getKindElement().getValueAsEnum()),
			createElementRow("Description", source.getDescriptionElement()),
			createElementRow("Code", source.getCodeElement()),
			createElementRow("System", source.getSystemElement()),
			createElementRow("Instance", source.getInstanceElement())
		);
		
		return Elements.withChildren("tbody", bodyRows);
	}
	
	private Element createOperationKindRow(String desc, OperationKindEnum operationKind) {
		return createDataRow(desc, 
			new LinkData(operationKind.getSystem(), OperationKindEnum.VALUESET_NAME),
			StringUtil.capitaliseLowerCase(operationKind.getCode()));
	}

	private Element createElementRow(String desc, BasePrimitive<?> fhirData) {
		LinkData typeLink = fhirDocLinkFactory.forDataType(fhirData);
		
		String value = fhirData.getValueAsString();
		
		if (!(fhirData instanceof CodeDt)) {
			value = StringUtil.capitaliseLowerCase(value);
		}
		
		return createDataRow(desc, typeLink, value);
	}

	private Element createDataRow(String desc, LinkData typeLink, String value) {
		return Elements.withChildren("tr",
			Lists.newArrayList(
				Elements.withText("td", desc),
				Elements.withChild("td",
					Elements.withAttributesAndText("a",
						Lists.newArrayList(
							new Attribute("class", "fhir-link"),
							new Attribute("href", typeLink.getURL())),
						typeLink.getText())),
				Elements.withText("td", value)));
	}
}
