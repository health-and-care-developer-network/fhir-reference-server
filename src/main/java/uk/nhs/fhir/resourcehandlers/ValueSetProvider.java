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
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.ValueSetCodesCache;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;
import uk.nhs.fhir.validator.ValidateAny;

/**
 *
 * @author Tim Coates
 */
public class ValueSetProvider implements IResourceProvider {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;
    
//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which data source we're working with.
     *
     * @param dataSource
     */
    public ValueSetProvider(Datasource dataSource) {
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
        LOG.fine("Created ValueSetProvider handler to respond to requests for ValueSet resource types.");
    }

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can
     * say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
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
    public MethodOutcome validateStructureDefinition(
            @ResourceParam Patient resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) { 
        
        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="RESTFul request handlers">
    /**
     * The "@Read" annotation indicates that this method supports the
     * read operation. Read operations should return a single resource
     * instance.
     *
     * @param theId
     *    The read operation takes one parameter, which must be of type
     *    IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @return
     *    Returns a resource matching this identifier, or null if none exists.
     */
    @Read(version=true)
    public ValueSet getValueSetById(@IdParam IdDt theId) {
        ValueSet foundItem = (ValueSet)myDataSource.getResourceByID(FHIRVersion.DSTU2, theId);
        return foundItem;
    }
    
    /**
     * The "@Search" annotation indicates that this method supports the
     * search operation.
     *
     * @param theName
     *    This operation takes one parameter which is the search criteria. It is
     *    annotated with the "@Required" annotation. This annotation takes one argument,
     *    a string containing the name of the search criteria. The datatype here
     *    is StringParam, but there are other possible parameter types depending on the
     *    specific search criteria.
     * @return
     *    This method returns a list of ValueSets where the name matches the supplied parameter.
     */
    @Search()
    public List<IBaseResource> getValueSetsByName(@RequiredParam(name = ValueSet.SP_NAME) StringParam theName) {
    	LOG.info("Request for ValueSet objects matching name: " + theName);
    	List<IBaseResource> foundList = myDataSource.getResourceMatchByName(FHIRVersion.DSTU2, ResourceType.VALUESET, theName.getValue());
        return foundList;
    }
    
    /**
     * The "@Search" annotation indicates that this method supports the
     * search operation.
     *
     * @param theCode
     *    This operation takes one parameter which is the search criteria. It is
     *    annotated with the "@Required" annotation. This annotation takes one argument,
     *    a string containing the code of the search criteria.
     * @return
     *    This method returns a list of ValueSets which contain the supplied code.
     */
    @Search()
    public List<ValueSet> getValueSetsByCode(@RequiredParam(name = ValueSet.SP_CODE) StringParam theCode) {
        List<ValueSet> results = new ArrayList<ValueSet>();
        ValueSetCodesCache codeCache = ValueSetCodesCache.getInstance();
        
        List<String> ids = codeCache.findCode(theCode.getValue());
        for(String theID : ids) {
            results.add((ValueSet)myDataSource.getResourceByID(FHIRVersion.DSTU2, theID));
        }
        return results;
    }
    
    @Search
    public List<IBaseResource> getAllValueSets() {
        List<IBaseResource> results = myDataSource.getAllResourcesOfType(FHIRVersion.DSTU2, ResourceType.VALUESET);
        return results;
    }
//</editor-fold>
}
