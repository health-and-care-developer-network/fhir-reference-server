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

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;

public interface Datasource {

    /**
     * Gets a specific one
     *
     * @param name
     * @return
     */
    StructureDefinition getSingleStructureDefinitionByName(String name);

    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     *
     * @param theNamePart
     * @return
     */
    List<StructureDefinition> getStructureDefinitionMatchByName(String theNamePart);

    /**
     * Gets a full list of StructureDefinition objects
     *
     * @return
     */
    List<StructureDefinition> getAllStructureDefinitions();

    /**
     * Gets a full list of names for the web view of /StructureDefinition
     * requests.
     *
     * @return
     */
    List<String> getAllStructureDefinitionNames();

    /**
     * Gets a full list of names, grouped by base resource for the web view of
     * /StructureDefinition requests.
     *
     * @return
     */
    HashMap<String, List<String>> getAllStructureDefinitionNamesByBaseResource();

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     *
     * @param theNamePart
     * @return
     */
    List<String> getAllStructureDefinitionNames(String theNamePart);

    /**
     * This is the method to get a specific ValueSet by name.
     *
     * @param name
     * @return
     */
    ValueSet getSingleValueSetByName(String name);

    /**
     * This is the method to get all ValueSets.
     * 
     * @return 
     */
    List<ValueSet> getAllValueSets();
    
    List<String> getAllValueSetNames();
    
    HashMap<String, List<String>> getAllValueSetNamesByCategory();
}
