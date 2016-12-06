/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.validator.ValidateAny;

/**
 *
 * @author tim
 */
public class OperationDefinitionProvider implements IResourceProvider  {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public OperationDefinitionProvider(Datasource dataSource) {
        myDataSource = dataSource;
        ctx = FhirContext.forDstu2();
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
}
