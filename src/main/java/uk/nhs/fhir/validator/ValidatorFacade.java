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
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Tim Coates
 */
public class ValidatorFacade {
    private static final Logger LOG = Logger.getLogger(ValidatorFacade.class.getName());
    FhirContext ctx = null;

    public ValidatorFacade() {
        ctx = FhirContext.forDstu2();
    }

    public MethodOutcome Validate(BaseResource resource, String profileURL, ValidatorManager myVMgr) {
        MethodOutcome results = new MethodOutcome();

        try {
            String resourceString = ctx.newXmlParser().encodeResourceToString(resource);
            Validator myValidator = myVMgr.getValidator();
            List<String> problemsFound = myValidator.validateXml(profileURL, resourceString);

            if (problemsFound.isEmpty() == false) {
                OperationOutcome outcome = new OperationOutcome();
                for(String problem : problemsFound) {
                    LOG.warning(problem);
                    outcome.addIssue().setSeverity(IssueSeverityEnum.WARNING).setDiagnostics(problem);
                }
                results.setOperationOutcome(outcome);
            }
        } catch (Exception ex) {
            LOG.severe("Exception calling validator: " + ex.getMessage());
        }
        return results;
    }

}