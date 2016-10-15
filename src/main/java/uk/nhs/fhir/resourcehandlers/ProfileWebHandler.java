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

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import java.util.List;
import java.util.logging.Logger;
import uk.nhs.fhir.datalayer.MongoIF;

/**
 *
 * @author tim
 */
public class ProfileWebHandler {
    private static final Logger LOG = Logger.getLogger(ProfileWebHandler.class.getName());
    
    MongoIF mymongo = null;

    public ProfileWebHandler(MongoIF mongoInterface) {
        mymongo = mongoInterface;
    }
    
    public String getAllNames() {
        LOG.info("Called: ProfileWebHandler.getAllNames()");
        List<String> myNames = mymongo.getAllNames();
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append(name);
            sb.append("<br />");
        }
        return sb.toString();
    }

    public String getAllNames(String namePart) {
        LOG.info("Called: ProfileWebHandler.getAllNames(String namePart)");
        List<String> myNames = mymongo.getAllNames(namePart);
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append(name);
            sb.append("<br />");
        }
        return sb.toString();
    }
        
    public StructureDefinition getSDByName(String name) {
        LOG.info("Called: ProfileWebHandler.getSDByName(String name)");
        StructureDefinition sd = mymongo.getSingleStructureDefinitionByName(name);
        return sd;
    }
}
