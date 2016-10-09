/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
