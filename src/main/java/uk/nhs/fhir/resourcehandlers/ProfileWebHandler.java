/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers;

import java.util.List;
import uk.nhs.fhir.datalayer.MongoIF;

/**
 *
 * @author tim
 */
public class ProfileWebHandler {
    
    MongoIF mymongo = null;

    public ProfileWebHandler(MongoIF mongoInterface) {
        mymongo = mongoInterface;
    }
    
    public String getAllNames() {
        List<String> myNames = mymongo.getAllNames();
        StringBuilder sb = new StringBuilder();
        
        for(String name : myNames) {
            sb.append(name);
            sb.append("<br />");
        }
        return sb.toString();
    }
}
