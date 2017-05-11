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

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;

import static uk.nhs.fhir.enums.ResourceType.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Coates
 * @author Adam Hatherly
 */
public class ResourceWebHandler {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");
    
    private static String startOfBaseResourceBox = null;
    private static String endOfBaseResourceBox = null;
    Datasource myDataSource = null;

    public ResourceWebHandler(Datasource dataSource) {
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
        LOG.fine("Created ProfileWebHandler handler to respond to requests for Profile resource types from a browser.");
    }
    
    /*public String getAllStructureDefinitionNames(String resourceType) {
        LOG.fine("Called: ProfileWebHandler.getAllNames()");
        List<String> myNames = myDataSource.getAllResourceNames(ResourceType.STRUCTUREDEFINITION);
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append("<a href=").append(resourceType).append('/').append(name).append('>').append(name).append("</a>");
            sb.append("<br />");
        }
        return sb.toString();
    }*/
    
    public String getAGroupedListOfResources(ResourceType resourceType) {
        LOG.fine("Called: ProfileWebHandler.getAlGroupedNames()");
        StringBuilder sb = new StringBuilder();
        
        if(resourceType == STRUCTUREDEFINITION || resourceType == VALUESET
        		|| resourceType == OPERATIONDEFINITION || resourceType == IMPLEMENTATIONGUIDE) {
        	sb.append("<div class='fw_nav_boxes isotope' style='position: relative; overflow: hidden;'>");
        	
            HashMap<String, List<ResourceEntity>> myNames = null;
            
            if(resourceType == STRUCTUREDEFINITION) {
            	myNames = myDataSource.getAllResourceNamesByBaseResource(resourceType);
            } else if (resourceType == VALUESET) {
            	myNames = myDataSource.getAllValueSetNamesByCategory();
            } else if (resourceType == OPERATIONDEFINITION) {
            	myNames = myDataSource.getAllOperationNamesByCategory();
            } else if (resourceType == IMPLEMENTATIONGUIDE) {
            	myNames = myDataSource.getAllImplementationGuideNamesByCategory();
            }
            
            for(String base : myNames.keySet()) {
                    sb.append(startOfBaseResourceBox);
                    sb.append(base);
                    sb.append(endOfBaseResourceBox);
                    for(ResourceEntity resource : myNames.get(base)) {
                    	String name_for_url = resource.getResourceID();
						try {
							name_for_url = URLEncoder.encode(resource.getResourceID(), Charset.defaultCharset().name());
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	sb.append("<li><a href=").append(resourceType.getHAPIName()).append('/').append(name_for_url).append('>').append(resource.getResourceName()).append("</a></li>");
	                }
                sb.append("</ul></section></div></div>");
            }
        }
        
        /*
        if(resourceType == VALUESET) {
            List<String> myNames = myDataSource.getAllValueSetNames();
            
            for(String name : myNames) {
                sb.append("<li><a href=").append(resourceType).append('/').append(name).append('>').append(name).append("</a></li>");
            }
            sb.append("</ul></section></div></div>");        	
        }*/
        
        
        sb.append("</div>");        
        return sb.toString();
    }

    public String getAllNames(ResourceType resourceType, String namePart) {
        LOG.fine("Called: ProfileWebHandler.getAllNames(String namePart)");
        List<String> myResourceIDs = myDataSource.getAllResourceIDforResourcesMatchingNamePattern(resourceType, namePart);
        StringBuilder sb = new StringBuilder();
        
        for(String id : myResourceIDs) {
            sb.append("<a href=").append(resourceType.getHAPIName()).append('/').append(id).append('>').append(id).append("</a>");
            sb.append("<br />");
        }
        return sb.toString();
    }
        
    public StructureDefinition getSDByID(String id) {
        LOG.fine("Called: ProfileWebHandler.getSDByID(String id)");
        StructureDefinition sd = (StructureDefinition)myDataSource.getResourceByID(id);
        return sd;
    }

    public OperationDefinition getOperationByID(String id) {
        LOG.fine("Called: ProfileWebHandler.getOperationByID(String id)");
        OperationDefinition od = (OperationDefinition)myDataSource.getResourceByID(id);
        return od;
    }

    public ImplementationGuide getImplementationGuideByID(String id) {
        LOG.fine("Called: ProfileWebHandler.getImplementationGuideByID(String id)");
        ImplementationGuide ig = (ImplementationGuide)myDataSource.getResourceByID(id);
        return ig;
    }
    
    public ValueSet getVSByID(String id) {
        LOG.fine("Called: ProfileWebHandler.getVSByID(String id)");
        ValueSet valSet = (ValueSet)myDataSource.getResourceByID(id);
        return valSet;
    }
}
