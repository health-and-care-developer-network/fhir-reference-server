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
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
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
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.PropertyReader;
import uk.nhs.fhir.validator.ValidateAny;

/**
 *
 * @author Tim Coates
 */
public class CodeSystemProvider implements IResourceProvider, IResourceHelper {
    private static final Logger LOG = Logger.getLogger(CodeSystemProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;
    
//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which data source we're working with.
     *
     * @param dataSource
     */
    public CodeSystemProvider(Datasource dataSource) {
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
        ctx = FHIRVersion.STU3.getContext();
        LOG.fine("Created CodeSystemProvider handler to respond to requests for CodeSystem resource types.");
    }

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can
     * say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
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
    /*@Validate
    public MethodOutcome validateStructureDefinition(
            @ResourceParam ValueSet resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) { 
        
        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
    }*/
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
    public CodeSystem getValueSetById(@IdParam IdType theId) {
    	CodeSystem foundItem = (CodeSystem)myDataSource.getResourceByID(FHIRVersion.STU3, theId);
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
    public List<IBaseResource> getCodeSystemsByName(@RequiredParam(name = CodeSystem.SP_NAME) StringParam theName) {
    	LOG.fine("Request for CodeSystem objects matching name: " + theName);
    	List<IBaseResource> foundList = myDataSource.getResourceMatchByName(FHIRVersion.STU3, ResourceType.CODESYSTEM, theName.getValue());
        return foundList;
    }
    
    @Search
    public List<IBaseResource> getAllCodeSystems() {
        List<IBaseResource> results = myDataSource.getAllResourcesOfType(FHIRVersion.STU3, ResourceType.CODESYSTEM);
        return results;
    }
    
    /**
     * Search by URL, so will respond to queries of the form:
     * /CodeSystem?url=http://acme.org/fhir/CodeSystem/123
     *
     * @param theURL
     * @return
     */
    @Search
    public List<IBaseResource> searchByURL(@RequiredParam(name = CodeSystem.SP_URL) StringParam theURL) {
    	LOG.fine("Request for CodeSystem objects matching URL: " + theURL);
    	List<IBaseResource> foundList = myDataSource.getResourceMatchByURL(FHIRVersion.STU3,
    											ResourceType.CODESYSTEM, theURL.getValue());
        return foundList;
    }
    
//</editor-fold>
    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
    	Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
        CodeSystem output = (CodeSystem)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((CodeSystem)resource).getText().getDivAsString();
    }

    public ResourceEntity getMetadataFromResource(File thisFile) {
    	String displayGroup = "Code List";
    	CodeSystem profile = (CodeSystem)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = profile.getName();
    	String url = profile.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	VersionNumber versionNo = new VersionNumber(profile.getVersion());
    	String status = profile.getStatus().name();
    	
    	return new ResourceEntity(resourceName, thisFile, ResourceType.CODESYSTEM,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3, url);
    }

}
