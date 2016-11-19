/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.resourcehandlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;
import java.util.logging.Logger;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.datalayer.Datasource;

/**
 *
 * @author Tim Coates
 */
public class PatientProvider implements IResourceProvider {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());

    Datasource myDataSource = null;
    FhirContext ctx = null;

//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which mongo data source we're working with.
     *
     * @param dataSource
     */
    public PatientProvider(Datasource dataSource) {
        myDataSource = dataSource;
        ctx = FhirContext.forDstu2();
    }

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can
     * say it handles that type.
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
        FhirValidator validator = ctx.newValidator();
 
        // Create some modules and register them
        IValidatorModule module1 = new SchemaBaseValidator(ctx);
        validator.registerValidatorModule(module1);
        
        // NB we also do instance validation...
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        
        
        // TODO: These don't yet work :-(
        //IValidatorModule module2 = new SchematronBaseValidator(ctx);
        //validator.registerValidatorModule(module2);

        // Pass a resource in to be validated. The resource can
        // be an IBaseResource instance, or can be a raw String
        // containing a serialized resource as text.
        ValidationResult result = validator.validateWithResult(resourceToTest);
        
        
        MethodOutcome retval = new MethodOutcome();        
        if (result.isSuccessful()) {
           LOG.info("Validation passed");
        } else {
           LOG.warning("Validation failed");
           OperationOutcome oo = (OperationOutcome) result.toOperationOutcome();        
           retval.setOperationOutcome(oo);
        }
        return retval;
    }
//</editor-fold>

}
