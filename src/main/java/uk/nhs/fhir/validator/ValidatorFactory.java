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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IFhirVersion;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Factory class, which is never instantiated, but instantiates a new
 * validator, or gives us an existing.
 * 
 * @author Tim Coates
 */
public class ValidatorFactory {
    private static final Logger LOG = Logger.getLogger(ValidatorFactory.class.getName());

    // Get the log level we want to logging done at...
    private static final String logLevel = PropertyReader.getProperty("logLevel");
    
    // Get whether we're expecting to validate against local profile.
    private static final String localValidation = PropertyReader.getProperty("localValidation");
    private static boolean localValidationFlag;
    
    // We store a validator which we'll instantiate on first use,
    // and return for future uses.
    static FhirValidator validator = null;
    // It has a specific version, if someone asks for a different one, we bin the existing one, and recreate.
    static IFhirVersion version = null;
    
    
    /**
     * Exists only to thwart anyone trying to instantiate the factory.
     * 
     */
    private ValidatorFactory() {
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
        
        
        localValidationFlag = true;
        if(localValidation != null) {
            if(localValidation.equals("FALSE")) {
                localValidationFlag = false;
            }
        }
    }
    
    /**
     * Here's how we want people to get our customised validator.
     * 
     * @param ctx The FhirContext they are working with.
     * 
     * @return A validator, which also knows how to handle locally profiled StructureDefinitions
     */
    public static FhirValidator getValidator(FhirContext ctx) {
        
        LOG.info("getValidator called for " + ctx.getVersion());
        
        // Someone has asked for a version our stored one wasn't built for, so we'll bin it and make the one they want.
        // Unlikely we'll find ourselves here.
        if(ctx.getVersion() != version) {
            validator = null;
        }
        
        // Now we check whether we've already created a validator, in which case ignore all this.
        if(validator == null) {
            LOG.info("Creating new validator");
            // We didn't have one in stock, so we need to create one.
            validator = ctx.newValidator();
            
            // NB we also do instance validation
            FhirInstanceValidator instanceValidator = new FhirInstanceValidator();

            if(localValidationFlag) {
                LOG.info("New validator will validate against custom (local) profiles");

                // ... with our own profile loader implementation
                IValidationSupport ourProfileLoader = new ProfileLoader(); // This is our custom profile loader
                ValidationSupportChain supportChain = new ValidationSupportChain(new DefaultProfileValidationSupport(), ourProfileLoader);
                instanceValidator.setValidationSupport(supportChain);
                validator.registerValidatorModule(instanceValidator);
            } else {
                LOG.info("New validator will _NOT_ validate against custom (local) profiles");
                validator.registerValidatorModule(instanceValidator);
            }
            // Create some validation modules and register them
            IValidatorModule schemaBaseValidator = new SchemaBaseValidator(ctx);
            validator.registerValidatorModule(schemaBaseValidator);

            // We also validate against schematrons ?
            validator.setValidateAgainstStandardSchematron(true);
            IValidatorModule schematronBaseValidator = new SchematronBaseValidator(ctx);
            validator.registerValidatorModule(schematronBaseValidator);
            version = ctx.getVersion();
        } else {
            LOG.info("We will reuse existing validator :-)");
        }
        return validator;
    }
}
