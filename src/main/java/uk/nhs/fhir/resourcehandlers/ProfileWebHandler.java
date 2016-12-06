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
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.util.PropertyReader;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Coates
 */
public class ProfileWebHandler {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");
    
    private static String startOfBaseResourceBox = null;
    private static String endOfBaseResourceBox = null;
    Datasource myDataSource = null;

    public ProfileWebHandler(Datasource dataSource) {
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
        startOfBaseResourceBox = PropertyReader.getProperty("startOfBaseResourceBox");
        endOfBaseResourceBox = PropertyReader.getProperty("endOfBaseResourceBox");
    }
    
    public String getAllNames(String resourceType) {
        LOG.info("Called: ProfileWebHandler.getAllNames()");
        List<String> myNames = myDataSource.getAllNames();
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append("<a href=").append(resourceType).append('/').append(name).append('>').append(name).append("</a>");
            sb.append("<br />");
        }
        return sb.toString();
    }
    
    public String getAllGroupedNames(String resourceType) {
        LOG.info("Called: ProfileWebHandler.getAlGroupedNames()");
        HashMap<String, List<String>> myNames = myDataSource.getAllNamesByBaseResource();
        StringBuilder sb = new StringBuilder();
        
        sb.append("<div class='fw_nav_boxes isotope' style='position: relative; overflow: hidden;'>");
        
        for(String base : myNames.keySet()) {
        	sb.append(startOfBaseResourceBox);
        	sb.append(base);
        	sb.append(endOfBaseResourceBox);
        	for(String name : myNames.get(base)) {
                sb.append("<li><a href=").append(resourceType).append('/').append(name).append('>').append(name).append("</a></li>");
            }
        	sb.append("</ul></section></div></div>");
        }
        
        sb.append("</div>");
        
        return sb.toString();
    }

    public String getAllNames(String resourceType, String namePart) {
        LOG.info("Called: ProfileWebHandler.getAllNames(String namePart)");
        List<String> myNames = myDataSource.getAllNames(namePart);
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append("<a href=").append(resourceType).append('/').append(name).append('>').append(name).append("</a>");
            sb.append("<br />");
        }
        return sb.toString();
    }
        
    public StructureDefinition getSDByName(String name) {
        LOG.info("Called: ProfileWebHandler.getSDByName(String name)");
        StructureDefinition sd = myDataSource.getSingleStructureDefinitionByName(name);
        return sd;
    }
}
