/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.util.PropertyReader;
import uk.nhs.fhir.validator.ValidateAny;

/**
 *
 * @author tim
 */
public class ImplementationGuideProvider implements IResourceProvider  {
    private static final Logger LOG = Logger.getLogger(ImplementationGuideProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public ImplementationGuideProvider(Datasource dataSource) {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("INFO")) {
           LOG.setLevel(Level.INFO);
        }
        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        myDataSource = dataSource;
        ctx = FhirContext.forDstu2();
        LOG.fine("Created ImplementationGuideProvider handler to respond to requests for ImplementationGuide resource types.");

    }
    
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ImplementationGuide.class;
    }


//<editor-fold defaultstate="collapsed" desc="Validation">
    /**
     * Code to call the validation process, whatever that happens to be...
     *
     * See: http://hapifhir.io/doc_rest_operations.html#Type_Level_-_Validate
     *
     * @param resourceToTest
     * @param theMode
     * @param theProfile
     * @return
     */
    @Validate
    public MethodOutcome validateImplementationGuide(
            @ResourceParam Patient resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) { 
        
        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
    }
//</editor-fold>

    /**
     * Instance level GET of a resource... 
     *
     * @return An ImplementationGuide resource
     */
    @Read
    public ImplementationGuide getResourceById(@IdParam IdDt theId) {
        String id = theId.getIdPart().toString();
        ImplementationGuide foundItem = myDataSource.getSingleImplementationGuideByID(id);
        return foundItem;
    }
    
    /**
     * Overall search, will return ALL ImplementationGuides so responds to: /ImplementationGuide
     *
     * @return
     */
    @Search
    public List<ImplementationGuide> getAllImplementationGuides() {
        LOG.info("Request for ALL ImplementationGuide objects");
        List<ImplementationGuide> foundList = myDataSource.getAllImplementationGuides();
        return foundList;
    }


}
