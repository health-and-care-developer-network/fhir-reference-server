package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.jdom2.Element;
import uk.nhs.fhir.makehtml.html.*;
import uk.nhs.fhir.util.SharedFhirContext;

import javax.xml.parsers.ParserConfigurationException;
import java.util.logging.Logger;

public class ValueSetFormatter extends ResourceFormatter<ValueSet> {

    private static final Logger LOG = Logger.getLogger(ValueSetFormatter.class.getName());

	TablePNGGenerator backgrounds = new TablePNGGenerator();

	@Override
	public HTMLDocSection makeSectionHTML(ValueSet source) throws ParserConfigurationException {

        FhirValidator validator = SharedFhirContext.get().newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        // Validate - Check resource is valid
        ValidationResult result = validator.validateWithResult(source);

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

		ValueSetMetadataFormatter metadata = new ValueSetMetadataFormatter(source);

		ValueSetTableFormatter tableData = new ValueSetTableFormatter(source);


		Element metadataPanel = metadata.getMetadataTable();
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(metadataPanel);

		// Inline CodeSystem - must have system if present
		// External CodeSystem - should have Import or Include if present

		Element tableDataPanel = tableData.getConceptDataTable();

		section.addBodyElement(tableDataPanel);

		// Expansion

		// Included ConceptMaps - this is coded so ConceptMap can be a separate resource
		if (source.getContained().getContainedResources().size() > 0 )
		{
			for (IResource resource :source.getContained().getContainedResources())
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
		StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
	}
}
