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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.FhirBrowserRequestServlet;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.page.extensions.ExtensionsListProvider;
import uk.nhs.fhir.page.home.ResourceCountsProvider;
import uk.nhs.fhir.page.list.GroupedResourcesProvider;
import uk.nhs.fhir.util.FhirVersion;

/**
 *
 * @author Tim Coates
 * @author Adam Hatherly
 */
public class ResourceWebHandler implements ResourceCountsProvider, ExtensionsListProvider, GroupedResourcesProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceWebHandler.class.getName());
    
    FilesystemIF myDataSource = null;

    public ResourceWebHandler(FilesystemIF dataSource) {
        myDataSource = dataSource;
        
        LOG.debug("Created ResourceWebHandler handler to respond to requests for Profile resource types from a browser.");
    }
    
    public HashMap<String, List<ResourceMetadata>> getAGroupedListOfResources(ResourceType resourceType) {
        LOG.debug("Called: ResourceWebHandler.getAlGroupedNames()");
        
        if (ArrayUtils.contains(FhirBrowserRequestServlet.getIndexedTypes(), resourceType)) {
            if (resourceType == ResourceType.STRUCTUREDEFINITION) {
            	return myDataSource.getAllResourceNamesByBaseResource(resourceType);
            } else {
            	return myDataSource.getAllResourceNamesByCategory(resourceType);
            }
        }
        
        return null;
    }

    public List<ResourceMetadata> getAllNames(FhirVersion fhirVersion, ResourceType resourceType, String namePart) {
        LOG.debug("Called: ResourceWebHandler.getAllNames(String namePart)");
        
        List<ResourceMetadata> myResourceList = myDataSource.getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, namePart);
        
        return myResourceList;
    }
    
    @Override
    public List<ResourceMetadata> getExtensions() {
        LOG.debug("Called: ResourceWebHandler.getExtensions()");
        
        return myDataSource.getExtensions();
    }
    
    public ResourceEntityWithMultipleVersions getVersionsForID(FhirVersion fhirVersion, IIdType id) {
        LOG.debug("Called: ResourceWebHandler.getVersionsForID(IIdType id)");
        
        return myDataSource.getVersionsByID(fhirVersion, id);
    }
    
    public ResourceMetadata getResourceEntityByID(FhirVersion fhirVersion, IIdType theId) {
        LOG.debug("Called: ResourceWebHandler.getResourceEntityByID(IIdType id)");
        
        return myDataSource.getResourceEntityByID(fhirVersion, theId);
    }

    public IBaseResource getResourceByID(FhirVersion fhirVersion, IIdType id) {
        LOG.debug("Called: ResourceWebHandler.getResourceByID(IIdType id)");
        
        IBaseResource resource = myDataSource.getResourceByID(fhirVersion, id);
        return resource;
    }

    public ExampleResources getExamples(FhirVersion fhirVersion, String resourceTypeAndID) {
        LOG.debug("Called: ResourceWebHandler.getExamples(String resourceTypeAndID)");
        
        ExampleResources examples = myDataSource.getExamples(fhirVersion, resourceTypeAndID);
        return examples;
    }
    
    public HashMap<String,Integer> getResourceTypeCounts() {
    	return myDataSource.getResourceTypeCounts();
    }

	public FilesystemIF getMyDataSource() {
		return myDataSource;
	}
}
