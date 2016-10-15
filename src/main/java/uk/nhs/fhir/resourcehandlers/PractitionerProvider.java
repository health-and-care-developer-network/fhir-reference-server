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
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.MongoIF;
import uk.nhs.fhir.validator.Validator;
import uk.nhs.fhir.validator.ValidatorManager;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class PractitionerProvider implements IResourceProvider {

    MongoIF myMongo = null;
    ValidatorManager myVMgr = null;
    FhirContext ctx = null;

//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which mongo data source we're working with.
     *
     * @param mongoInterface
     */
    public PractitionerProvider(MongoIF mongoInterface, ValidatorManager vMgr) {
        myMongo = mongoInterface;
        myVMgr = vMgr;
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
        return Practitioner.class;
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
    public MethodOutcome validateStructureDefinition(@ResourceParam Practitioner resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) {
        MethodOutcome retVal = null;
        try {
            String resourceString = ctx.newXmlParser().encodeResourceToString(resourceToTest);
            Validator myValidator = myVMgr.getValidator();

            List<String> problemsFound = myValidator.validateXml(theProfile, resourceString);

            if (problemsFound.isEmpty()) {
                // Celebrate
            } else {
                // Weep
            }

            // This method returns a MethodOutcome object
            retVal = new MethodOutcome();

        } catch (Exception ex) {
            Logger.getLogger(PractitionerProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }
//</editor-fold>

}
