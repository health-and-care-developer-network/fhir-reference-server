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
package uk.nhs.fhir.validator;

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;
import java.util.logging.Logger;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;

/**
 * This class can in theory be used to validate any resource. It is called from the
 *  various resource handlers, to do the validation process.
 * 
 * @author Tim Coates
 */
public class ValidateAny {
    private static final Logger LOG = Logger.getLogger(ValidateAny.class.getName());

        public static MethodOutcome validateStructureDefinition(
                FhirContext ctx,
                @ResourceParam Patient resourceToTest,
                @Validate.Mode ValidationModeEnum theMode,
                @Validate.Profile String theProfile) {
        FhirValidator validator = ctx.newValidator();
        
        // Create some validation modules and register them
        IValidatorModule module1 = new SchemaBaseValidator(ctx);
        validator.registerValidatorModule(module1);
        
        // NB we also do instance validation...
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();

        // ... with our own profile loader implementation
        IValidationSupport valSupport = new ProfileLoader(); // This is our custom profile loader
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
        instanceValidator.setValidationSupport(support);        
        validator.registerValidatorModule(instanceValidator);
        
        // We also validate against schematrons ?
        validator.setValidateAgainstStandardSchematron(true);
        IValidatorModule module2 = new SchematronBaseValidator(ctx);
        validator.registerValidatorModule(module2);

        // Pass a resource in to be validated. The resource can
        // be an IBaseResource instance, or can be a raw String
        // containing a serialized resource as text.
        ValidationResult result = validator.validateWithResult(resourceToTest);
        
        MethodOutcome retval = new MethodOutcome();        
        if (result.isSuccessful()) {
           LOG.info("Validation passed");
        } else {
           LOG.warning("Validation failed");
           for(int i = 0; i < result.getMessages().size(); i++) {
               LOG.warning(result.getMessages().get(i).toString());
           }
           
           OperationOutcome oo = (OperationOutcome) result.toOperationOutcome();        
           retval.setOperationOutcome(oo);
        }
        return retval;
    }
}
