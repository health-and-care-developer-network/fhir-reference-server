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

public interface Datasource {

	/**
	 * Gets a specific one
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
	List<StructureDefinition> getMatchByName(String theNamePart);

	/**
	 * Gets a full list of StructureDefinition objects
	 * 
	 * @return 
	 */
	List<StructureDefinition> getAll();

	/**
	 * Gets a full list of names for the web view of /StructureDefinition requests.
	 * 
	 * @return 
	 */
	List<String> getAllNames();
	
	/**
	 * Gets a full list of names, grouped by base resource for the web view
	 * of /StructureDefinition requests.
	 * 
	 * @return
	 */
	HashMap<String, List<String>> getAllNamesByBaseResource();

	/**
	 * This is the method to search by name, e.g. name:contains=Patient
	 * 
	 * @param theNamePart
	 * @return 
	 */
	List<String> getAllNames(String theNamePart);

}