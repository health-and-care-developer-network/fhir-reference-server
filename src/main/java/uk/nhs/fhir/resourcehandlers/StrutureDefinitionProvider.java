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

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.List;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.MongoIF;
import uk.nhs.fhir.validator.Validator;

/**
 *
 * @author Tim Coates
 */
public class StrutureDefinitionProvider implements IResourceProvider {
    private static final Logger LOG = Logger.getLogger(StrutureDefinitionProvider.class.getName());

    MongoIF myMongo = null;

//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which mongo data source we're working with.
     *
     * @param mongoInterface
     */
    public StrutureDefinitionProvider(MongoIF mongoInterface) {
        myMongo = mongoInterface;
    }

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return StructureDefinition.class;
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
    public MethodOutcome validateStructureDefinition(@ResourceParam StructureDefinition resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) {

        Validator myValidator = new Validator();

        List<String> problemsFound = myValidator.validateResource(resourceToTest, theProfile);

        if(problemsFound.isEmpty()) {
            // Celebrate
        } else {
            // Weep
        }

        // Actually do our validation: The UnprocessableEntityException
        // results in an HTTP 422, which is appropriate for business rule failure
        if(resourceToTest.getIdentifierFirstRep().isEmpty()) {
            /* It is also possible to pass an OperationOutcome resource
             * to the UnprocessableEntityException if you want to return
             * a custom populated OperationOutcome. Otherwise, a simple one
             * is created using the string supplied below.
             */
            throw new UnprocessableEntityException("No identifier supplied");
        }

        // This method returns a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();

        // You may also add an OperationOutcome resource to return
        // This part is optional though:
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue().setSeverity(IssueSeverityEnum.WARNING).setDiagnostics("One minor issue detected");

        retVal.setOperationOutcome(outcome);

        return retVal;
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="RESTFul operations">
    /**
     * Instance level GET of a resource... This needs to get a Structure Definition resource by name, so will respond to for example:
     *
     * /StructureDefinition/nrls-documentreference-1-0
     *
     * @param theId ID value identifying the resource.
     *
     * @return A StructureDefinition resource
     */
    @Read
    public StructureDefinition getResourceById(@IdParam IdDt theId) {
        String name = theId.getIdPart().toString();
        StructureDefinition foundItem = myMongo.getSingleStructureDefinitionByName(name);
        return foundItem;
    }

    /**
     * Search by name, so will respond to queries of the form: /StructureDefinition?name:contains=blah
     *
     * @param theNamePart
     * @return
     */
    @Search
    public List<StructureDefinition> searchByStructureDefinitionName(@RequiredParam(name = StructureDefinition.SP_NAME) StringParam theNamePart) {
        List<StructureDefinition> foundList = myMongo.getMatchByName(theNamePart.toString());
        return foundList;
    }

    /**
     * Overall search, will return ALL Structure Definitions so responds to: /StructureDefinition
     *
     * @return
     */
    @Search
    public List<StructureDefinition> getAllStructureDefinitions() {
        LOG.info("Request for ALL StructureDefinition objects");
        List<StructureDefinition> foundList = myMongo.getAll();
        return foundList;
    }
//</editor-fold>

}
