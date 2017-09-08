/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.OperationDefinition;
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
 * @author tim
 */
public class OperationDefinitionProvider implements IResourceProvider, IResourceHelper  {
    private static final Logger LOG = Logger.getLogger(OperationDefinitionProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public OperationDefinitionProvider(Datasource dataSource) {
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
        LOG.fine("Created OperationDefinitionProvider handler to respond to requests for OperationDefinition resource types.");

    }
    
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return OperationDefinition.class;
    }


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
    public MethodOutcome validateOperationDefinition(
            @ResourceParam OperationDefinition resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) { 
        
        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
    }*/
//</editor-fold>

    /**
     * Instance level GET of a resource... 
     *
     * @return An OperationDefinition resource
     */
    @Read(version=true)
    public OperationDefinition getResourceById(@IdParam IdType theId) {
        OperationDefinition foundItem = (OperationDefinition)myDataSource.getResourceByID(FHIRVersion.STU3, theId);
        return foundItem;
    }
    
    /**
     * Overall search, will return ALL Operation Definitions so responds to: /OperationDefinition
     *
     * @return
     */
    @Search
    public List<IBaseResource> getAllOperationDefinitions() {
        LOG.fine("Request for ALL OperationDefinition objects");
        List<IBaseResource> foundList = myDataSource.getAllResourcesOfType(FHIRVersion.STU3, ResourceType.OPERATIONDEFINITION);
        return foundList;
    }

    /**
     * Search by name, so will respond to queries of the form: /OperationDefinition?name:contains=blah
     *
     * @param theNamePart
     * @return
     */
    @Search
    public List<IBaseResource> searchByNamePart(@RequiredParam(name = OperationDefinition.SP_NAME) StringParam theNamePart) {
    	LOG.fine("Request for OperationDefinition objects matching name: " + theNamePart);
    	List<IBaseResource> foundList = myDataSource.getResourceMatchByName(FHIRVersion.STU3,
    										ResourceType.OPERATIONDEFINITION, theNamePart.getValue());
        return foundList;
    }

    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
    	OperationDefinition output = (OperationDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((OperationDefinition)resource).getText().getDivAsString();
    }

    public ResourceEntity getMetadataFromResource(File thisFile) {
    	
    	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = operation.getName();
    	String url = operation.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	String displayGroup = "Operations";
        VersionNumber versionNo = new VersionNumber(operation.getVersion());
        String status = operation.getStatus().name();
    	
        return new ResourceEntity(resourceName, thisFile, ResourceType.OPERATIONDEFINITION,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3);
    }

}