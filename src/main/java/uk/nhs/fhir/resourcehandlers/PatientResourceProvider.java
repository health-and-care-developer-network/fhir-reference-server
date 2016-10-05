/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.MongoIF;
import uk.nhs.fhir.validator.Validator;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class PatientResourceProvider implements IResourceProvider {

    MongoIF myMongo = null;

//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which mongo data source we're working with.
     *
     * @param mongoInterface
     */
    public PatientResourceProvider(MongoIF mongoInterface) {
        myMongo = mongoInterface;
    }

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }
//</editor-fold>

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
    public MethodOutcome validateStructureDefinition(@ResourceParam Patient resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) {

        Validator myValidator = new Validator();

        List<String> problemsFound = myValidator.validateResource(resourceToTest, theProfile);

        if(problemsFound.isEmpty()) {
            // Celebrate
        } else {
            // Weep
        }

        // This method returns a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();
        return retVal;
    }
//</editor-fold>
    
}
