/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.dstu2;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.util.PropertyReader;

/**
 *
 * @author Adam Hatherly
 */
public class ConformanceProvider implements IResourceProvider, IResourceHelper  {
    private static final Logger LOG = Logger.getLogger(ConformanceProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    Datasource myDataSource = null;
    FhirContext ctx = null;

    public ConformanceProvider(Datasource dataSource) {
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
        LOG.fine("Created ConformanceProvider handler to respond to requests for Conformance resource types.");

    }
    
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Conformance.class;
    }


    /**
     * Instance level GET of a resource... 
     *
     * @return A Conformance resource
     */
    @Read(version=true)
    public Conformance getResourceById(@IdParam IdDt theId) {
    	Conformance foundItem = (Conformance)myDataSource.getResourceByID(FHIRVersion.DSTU2, theId);
        return foundItem;
    }
    
    /**
     * Overall search, will return ALL Conformance resources so responds to: /Conformance
     *
     * @return
     */
    @Search
    public List<IBaseResource> getAllConformance() {
        LOG.info("Request for ALL Conformance objects");
        List<IBaseResource> foundList = myDataSource.getAllResourcesOfType(FHIRVersion.DSTU2, ResourceType.CONFORMANCE);
        return foundList;
    }

    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	Conformance output = (Conformance)resource;
    	output.setText(textElement);
    	return output;
    }

    public String getTextSection(IBaseResource resource) {
    	return ((Conformance)resource).getText().getDivAsString();
    }
}
