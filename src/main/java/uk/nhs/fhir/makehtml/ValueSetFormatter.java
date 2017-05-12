package uk.nhs.fhir.makehtml;

import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Element;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import uk.nhs.fhir.makehtml.html.ConceptMapMetadataFormatter;
import uk.nhs.fhir.makehtml.html.ConceptMapTableFormatter;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.TablePNGGenerator;
import uk.nhs.fhir.makehtml.html.ValueSetMetadataFormatter;
import uk.nhs.fhir.makehtml.html.ValueSetTableFormatter;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.util.SharedFhirContext;

public class ValueSetFormatter extends ResourceFormatter {

    private static final Logger LOG = Logger.getLogger(ValueSetFormatter.class.getName());

	TablePNGGenerator backgrounds = new TablePNGGenerator();

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		ValueSet valueSet = (ValueSet)source;
		
        FhirValidator validator = SharedFhirContext.get().newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        // Validate - Check resource is valid
        ValidationResult result = validator.validateWithResult(valueSet);

        // Do we have any errors or fatal errors?
        if (result.isSuccessful()) {
            LOG.info("FHIR Validator: Result: " + result.isSuccessful()); // true
        } else {
            LOG.warning("FHIR Validator: Result: " + result.isSuccessful()); // false
        }
        // Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            LOG.warning(" FHIR Validator: Next issue: " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
        }

		Element metadataPanel = new ValueSetMetadataFormatter().getMetadataTable(valueSet);
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(metadataPanel);

		// Inline CodeSystem - must have system if present
		// External CodeSystem - should have Import or Include if present

		ValueSetTableFormatter tableformatter = new ValueSetTableFormatter();
		Element tableDataPanel = tableformatter.getConceptDataTable(valueSet);

		section.addBodyElement(tableDataPanel);

		// Expansion

		// Included ConceptMaps - this is coded so ConceptMap can be a separate resource
		if (valueSet.getContained().getContainedResources().size() > 0 )
		{
			for (IResource resource :valueSet.getContained().getContainedResources())
			{
				if (resource instanceof ConceptMap)
				{
					ConceptMap conceptMap = (ConceptMap) resource;
					ConceptMapMetadataFormatter conceptMapMetadata = new ConceptMapMetadataFormatter(conceptMap);

					ConceptMapTableFormatter conceptMapTableData = new ConceptMapTableFormatter(conceptMap);

					Element conceptMapMetadataPanel = conceptMapMetadata.getMetadataTable();

					addStyles(section);
					section.addBodyElement(conceptMapMetadataPanel);

                    Element containedTableDataPanel = conceptMapTableData.getElementMapsDataTable();

                    section.addBodyElement(containedTableDataPanel);
				}
			}
		}

		return section;
	}

	public void addStyles(HTMLDocSection section) {
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		ResourceFlagsCell.getStyles().forEach(section::addStyle);
		ValueSetMetadataFormatter.getStyles().forEach(section::addStyle);
	}
}
