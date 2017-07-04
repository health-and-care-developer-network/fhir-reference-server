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
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
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
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
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
public class StrutureDefinitionProvider implements IResourceProvider, IResourceHelper {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDatasource = null;
    FhirContext ctx = null;

//<editor-fold defaultstate="collapsed" desc="Housekeeping code">
    /**
     * Constructor, which tell us which mongo data source we're working with.
     *
     * @param dataSource
     */
    public StrutureDefinitionProvider(Datasource dataSource) {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        myDatasource = dataSource;
        ctx = FhirContext.forDstu2();
        LOG.fine("Created StrutureDefinitionProvider handler to respond to requests for StrutureDefinition resource types.");
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

        MethodOutcome retval = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        return retval;
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
    @Read(version=true)
    public StructureDefinition getResourceById(@IdParam IdDt theId) {
        StructureDefinition foundItem = (StructureDefinition)myDatasource.getResourceByID(FHIRVersion.DSTU2, theId);
        return foundItem;
    }

    /**
     * Search by name, so will respond to queries of the form: /StructureDefinition?name:contains=blah
     *
     * @param theNamePart
     * @return
     */
    @Search
    public List<IBaseResource> searchByStructureDefinitionName(@RequiredParam(name = StructureDefinition.SP_NAME) StringParam theNamePart) {
    	LOG.info("Request for StructureDefinition objects matching name: " + theNamePart);
    	List<IBaseResource> foundList = myDatasource.getResourceMatchByName(FHIRVersion.DSTU2, ResourceType.STRUCTUREDEFINITION, theNamePart.getValue());
        return foundList;
    }

    /**
     * Overall search, will return ALL Structure Definitions so responds to: /StructureDefinition
     *
     * @return
     */
    @Search
    public List<IBaseResource> getAllStructureDefinitions() {
        LOG.info("Request for ALL StructureDefinition objects");
        List<IBaseResource> foundList = myDatasource.getAllResourcesOfType(FHIRVersion.DSTU2, ResourceType.STRUCTUREDEFINITION);
        return foundList;
    }
//</editor-fold>

    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	StructureDefinition output = (StructureDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((StructureDefinition)resource).getText().getDivAsString();
    }
    
    public ResourceEntity getMetadataFromResource(File thisFile) {
    	String resourceName = null;
    	String baseType = null;
    	boolean extension = false;
    	String extensionCardinality = null;
    	ArrayList<String> extensionContexts = new ArrayList<String>();
    	String extensionDescription = null;
    	
    	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.DSTU2, thisFile);
    	resourceName = profile.getName();
    	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
        
    	if (!extension) {
    		baseType = profile.getConstrainedType();
    	} else {
    		// Extra metadata for extensions
    		int min = profile.getSnapshot().getElementFirstRep().getMin();
    		String max = profile.getSnapshot().getElementFirstRep().getMax();
    		extensionCardinality = min + ".." + max;
    		
    		extensionContexts = new ArrayList<String>();
    		List<StringDt> contextList = profile.getContext();
    		for (StringDt context : contextList) {
    			extensionContexts.add(context.getValueAsString());
    		}
    		
    		extensionDescription = profile.getDifferential().getElementFirstRep().getShort();
    		if (extensionDescription == null) {
    			extensionDescription = profile.getDifferential().getElementFirstRep().getDefinition();
    		}
    		
    		List<ElementDefinitionDt> diffElements = profile.getDifferential().getElement();
    		boolean isSimple = false;
    		if (diffElements.size() == 3) {
    			if (diffElements.get(1).getPath().equals("Extension.url")) {
    				isSimple = true;
    				// It is a simple extension, so we can also find a type
    				List<Type> typeList = diffElements.get(2).getType();
    				if (typeList.size() == 1) {
    					baseType = typeList.get(0).getCode();
    				} else {
    					baseType = "(choice)";
    				}
    			}
    		}
    		if (!isSimple) {
    			baseType = "(complex)";
    		}
    	
    	}
        String url = profile.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = baseType;
        VersionNumber versionNo = new VersionNumber(profile.getVersion());
        String status = profile.getStatus();
        
        return new ResourceEntity(resourceName, thisFile, ResourceType.STRUCTUREDEFINITION,
							extension, baseType, displayGroup, false,
							resourceID, versionNo, status, null, extensionCardinality,
							extensionContexts, extensionDescription, FHIRVersion.DSTU2);
    }
    
}
