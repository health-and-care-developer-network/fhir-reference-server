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
import java.util.Optional;

import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.ValueSetCodesCache;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

/**
 *
 * @author Tim Coates
 */
public class ValueSetProvider extends AbstractResourceProviderDSTU2 {
    
	/**
     * Constructor, which tell us which data source we're working with.
     *
     * @param dataSource
     */
    public ValueSetProvider(FilesystemIF dataSource) {
    	super(dataSource, ResourceType.VALUESET, ca.uhn.fhir.model.dstu2.resource.ValueSet.class);
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
        
        List<String> ids = ValueSetCodesCache.findCode(theCode.getValue());
        for(String theID : ids) {
            results.add((ValueSet)dataSource.getResourceByID(FhirVersion.DSTU2, theID));
        }
        return results;
    }

    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	String displayGroup = "Code List";
    	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(FhirVersion.DSTU2, thisFile);
    	String url = profile.getUrl();
    	String resourceName = profile.getName();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	if (resourceName == null) {
    		resourceName = resourceID;
    	}
    	if (FHIRUtils.isValueSetSNOMED(profile)) {
    		displayGroup = "SNOMED CT Code List";
    	}
    	VersionNumber versionNo = new VersionNumber(profile.getVersion());
    	String status = profile.getStatus();
    	
    	return new ResourceMetadata(resourceName, thisFile, ResourceType.VALUESET,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FhirVersion.DSTU2, url);
    }

}
 