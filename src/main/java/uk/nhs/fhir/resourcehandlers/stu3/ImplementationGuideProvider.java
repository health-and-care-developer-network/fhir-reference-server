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
import org.hl7.fhir.dstu3.model.ImplementationGuide;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
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
public class ImplementationGuideProvider implements IResourceProvider, IResourceHelper  {
    private static final Logger LOG = Logger.getLogger(ImplementationGuideProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public ImplementationGuideProvider(Datasource dataSource) {
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
        LOG.fine("Created ImplementationGuideProvider handler to respond to requests for ImplementationGuide resource types.");

    }
    
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ImplementationGuide.class;
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
    public MethodOutcome validateImplementationGuide(
            @ResourceParam ImplementationGuide resourceToTest,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) { 
        
        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
    }*/
//</editor-fold>

    /**
     * Instance level GET of a resource... 
     *
     * @return An ImplementationGuide resource
     */
    @Read(version=true)
    public ImplementationGuide getResourceById(@IdParam IdType theId) {
        ImplementationGuide foundItem = (ImplementationGuide)myDataSource.getResourceByID(FHIRVersion.STU3, theId);
        return foundItem;
    }
    
    /**
     * Overall search, will return ALL ImplementationGuides so responds to: /ImplementationGuide
     *
     * @return
     */
    @Search
    public List<IBaseResource> getAllImplementationGuides() {
        LOG.fine("Request for ALL ImplementationGuide objects");
        List<IBaseResource> foundList = myDataSource.getAllResourcesOfType(FHIRVersion.STU3, ResourceType.IMPLEMENTATIONGUIDE);
        return foundList;
    }

    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
    	ImplementationGuide output = (ImplementationGuide)resource;
    	output.setText(textElement);
    	return output;
    }

    public String getTextSection(IBaseResource resource) {
    	return ((ImplementationGuide)resource).getText().getDivAsString();
    }
    
    public ResourceEntity getMetadataFromResource(File thisFile) {
    	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = guide.getName();
    	String url = guide.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	String displayGroup = "Implementation Guides";
        VersionNumber versionNo = new VersionNumber(guide.getVersion());
        String status = guide.getStatus().name();
    	
        return new ResourceEntity(resourceName, thisFile, ResourceType.IMPLEMENTATIONGUIDE,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3, url);
    }
}
