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
package uk.nhs.fhir.datalayer;

import java.util.HashMap;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.ResourceType;

public interface Datasource {

    /**
     * Gets a specific one
     *
     * @param name
     * @return
     */
	IBaseResource getResourceByID(String id);
    
    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     *
     * @param theNamePart
     * @return
     */
    List<IBaseResource> getResourceMatchByName(ResourceType resourceType, String theNamePart);

    /**
     * Gets a full list of StructureDefinition objects
     *
     * @return
     */
    List<IBaseResource> getAllResourcesOfType(ResourceType resourceType);

    /**
     * Gets a full list of names for the web view of /StructureDefinition
     * requests.
     *
     * @return
     */
    List<String> getAllResourceNames(ResourceType resourceType);

    /**
     * Gets a full list of names, grouped by base resource for the web view of
     * /StructureDefinition requests.
     *
     * @return
     */
    HashMap<String, List<ResourceEntity>> getAllResourceNamesByBaseResource(ResourceType resourceType);

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     *
     * @param theNamePart
     * @return
     */
    public List<String> getAllResourceIDforResourcesMatchingNamePattern(ResourceType resourceType, String theNamePart);

    /**
     * Gets a full list of names, grouped by category (specific to the resourcetype) for the web view
     * 
     * @param resourceType
     * @return
     */
    HashMap<String, List<ResourceEntity>> getAllResourceNamesByCategory(ResourceType resourceType);
    
}
