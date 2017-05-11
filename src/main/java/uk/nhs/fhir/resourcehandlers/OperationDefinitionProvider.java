/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;
import uk.nhs.fhir.validator.ValidateAny;

/**
 *
 * @author tim
 */
public class OperationDefinitionProvider implements IResourceProvider  {
    private static final Logger LOG = Logger.getLogger(BundleProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public OperationDefinitionProvider(Datasource dataSource) {
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
        LOG.fine("Created OperationDefinitionProvider handler to respond to requests for OperationDefinition resource types.");

    }
    
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return OperationDefinition.class;
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
    public MethodOutcome validateStructureDefinition(
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
     * @return An OperationDefinition resource
     */
    @Read
    public OperationDefinition getResourceById(@IdParam IdDt theId) {
        String id = theId.getIdPart().toString();
        OperationDefinition foundItem = (OperationDefinition)myDataSource.getResourceByID(id);
        return foundItem;
    }
    
    /**
     * Overall search, will return ALL Operation Definitions so responds to: /OperationDefinition
     *
     * @return
     */
    @Search
    public List<IBaseResource> getAllStructureDefinitions() {
        LOG.info("Request for ALL OperationDefinition objects");
        List<IBaseResource> foundList = myDataSource.getAllResourcesOfType(ResourceType.OPERATIONDEFINITION);
        return foundList;
    }


}
