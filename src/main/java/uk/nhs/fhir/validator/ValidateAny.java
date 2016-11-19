/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author tim
 */
public class ValidateAny {
    private static final Logger LOG = Logger.getLogger(ValidateAny.class.getName());

        public static MethodOutcome validateStructureDefinition(
                FhirContext ctx,
                @ResourceParam Patient resourceToTest,
                @Validate.Mode ValidationModeEnum theMode,
                @Validate.Profile String theProfile) {
        FhirValidator validator = ctx.newValidator();
        
        // Create some modules and register them
        IValidatorModule module1 = new SchemaBaseValidator(ctx);
        validator.registerValidatorModule(module1);
        
        // NB we also do instance validation...
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
        
        validator.setValidateAgainstStandardSchematron(true);
        // TODO: These two lines don't yet work :-(
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
           OperationOutcome oo = (OperationOutcome) result.toOperationOutcome();        
           retval.setOperationOutcome(oo);
        }
        return retval;
    }
    
}
