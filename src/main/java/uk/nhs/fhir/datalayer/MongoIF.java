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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.rest.param.StringParam;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.util.List;

/**
 *
 * @author Tim Coates
 */
public class MongoIF {
    MongoClient mongoClient;
    DB db;
    DBCollection docRefCollection;
    DBCollection orgCollection;
    FhirContext ctx;
    String host = "155.230.220.40";
    int port = 28015;
    
    /**
     * Constructor to set up our connection to a mongoDB database
     * 
     */
    public MongoIF() {
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB("mydb");
        docRefCollection = db.getCollection("StructureDefinitions");
        orgCollection = db.getCollection("Examples");
        
        ctx = FhirContext.forDstu2();
    }

    public StructureDefinition getSingleStructureDefinitionByName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<StructureDefinition> getMatchByName(StringParam theNamePart) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<StructureDefinition> getAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
