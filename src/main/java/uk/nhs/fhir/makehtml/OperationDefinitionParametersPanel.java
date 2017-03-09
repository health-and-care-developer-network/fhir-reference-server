package uk.nhs.fhir.makehtml;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.ParameterBinding;
import ca.uhn.fhir.model.dstu2.valueset.BindingStrengthEnum;
import ca.uhn.fhir.model.primitive.BoundCodeDt;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.UriDt;
import uk.nhs.fhir.util.ColumnData;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.LinkData;
import uk.nhs.fhir.util.StringUtil;

public class OperationDefinitionParametersPanel extends HTMLDocumentTablePanel {

	private final String panelHeading;
	private final List<Parameter> parameters;
	private final FhirDocLinkFactory fhirDocLinkFactory;
	
	public OperationDefinitionParametersPanel(String panelHeading, List<Parameter> parameters) {
		this(panelHeading, parameters, new FhirDocLinkFactory(FhirContext.forDstu2()));
	}
	
	public OperationDefinitionParametersPanel(String panelHeading, List<Parameter> parameters, FhirDocLinkFactory linkDataFactory) {
		this.panelHeading = panelHeading;
		this.parameters = parameters;
		this.fhirDocLinkFactory = linkDataFactory;
	}

	@Override
	protected String getHeadingText() {
		return panelHeading;
	}
	
	@Override
	protected List<ColumnData> panelColumnTitles() {
		return Lists.newArrayList(
			new ColumnData("Name", "The logical name of the element", "200px"),
			new ColumnData("Card.", "Minimum and maximum # of times the element can appear in the instance", "100px"),
			new ColumnData("Type", "Reference to the type of the element", "150px"),
			new ColumnData("Value", "Additional information about the element", "500px")
		);
	}

	@Override
	protected Element createTableBody() {
		List<Element> bodyRows = Lists.newArrayList();
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			String cardinality = parameter.getMin() + ".." + parameter.getMax(); 
			CodeDt typeElement = parameter.getTypeElement();
			LinkData typeLink = fhirDocLinkFactory.forDataType(typeElement);
			String documentation = parameter.getDocumentation();
			
			List<ResourceFlag> flags = getParameterFlags(parameter);
			
			bodyRows.add(
				createDataRow(
					name,
					cardinality, 
					typeLink,
					flags,
					documentation));
		}
		
		return Elements.withChildren("tbody", bodyRows);
	}

	private List<ResourceFlag> getParameterFlags(Parameter parameter) {
		List<ResourceFlag> resourceFlags = Lists.newArrayList();
		
		ParameterBinding binding = parameter.getBinding();
		if (!binding.isEmpty()) {
			IDatatype choice = binding.getValueSet();
			if (choice instanceof UriDt) {
				UriDt uri = (UriDt)choice;
				resourceFlags.add(new ResourceFlag("Binding", uri.getValueAsString(), true));
			} else if (choice instanceof ResourceReferenceDt) {
				//TODO need to test this
				ResourceReferenceDt ref = (ResourceReferenceDt)choice;
				resourceFlags.add(new ResourceFlag("Binding", ref.getReferenceElement().getValue(), true));
			}
			
			BoundCodeDt<BindingStrengthEnum> strengthElement = binding.getStrengthElement();
			resourceFlags.add(new ResourceFlag("Binding Strength", strengthElement.getValueAsEnum().getCode(), false));
		}
		
		ResourceReferenceDt profile = parameter.getProfile();
		if (!profile.isEmpty()) {
			resourceFlags.add(new ResourceFlag("Profile", profile.getReferenceElement().getValue(), true));
		}
		
		//for tuple parameters
		List<Parameter> parts = parameter.getPart();
		if (!parts.isEmpty()) {
			throw new NotImplementedException("Tuple parameter");
		}
		
		return resourceFlags;
	}

	private Element createDataRow(String name, String cardinality, LinkData typeLink, List<ResourceFlag> flags, String documentation) {
		List<Content> valueDataNodes = Lists.newArrayList();
		valueDataNodes.add(new Text(StringUtil.capitaliseLowerCase(documentation)));
		for (ResourceFlag flag : flags) {
			valueDataNodes.add(new Element("br"));
			valueDataNodes.addAll(nodesForResourceFlag(flag));
		}
		
		return Elements.withChildren("tr",
			Lists.newArrayList(
				Elements.withText("td", name),
				Elements.withText("td", cardinality),
				Elements.withChild("td",
					Elements.withAttributesAndText("a", 
						Lists.newArrayList(
							new Attribute("class", "fhir-link"),
							new Attribute("href", typeLink.getURL())), 
						typeLink.getText())),
				Elements.withChildren("td", valueDataNodes))
		);
	}

	private Collection<Content> nodesForResourceFlag(ResourceFlag flag) {
		return Lists.newArrayList(
			Elements.withAttributesAndText("span", 
				Lists.newArrayList(new Attribute("class", "fhir-resource-flag")),
				flag.getName()),
			flag.descriptionIsLink() ?
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("href", flag.getDescription()),
						new Attribute("class", "fhir-link")),
					flag.getDescription()) :
				new Text(StringUtil.capitaliseLowerCase(flag.getDescription())));
	}
}
 