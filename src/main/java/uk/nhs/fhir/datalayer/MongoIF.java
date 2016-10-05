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
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Tim Coates
 */
public class MongoIF {
    private static final Logger LOG = Logger.getLogger(MongoIF.class.getName());

    MongoClient mongoClient;
    DB db;
    DBCollection profiles;
    DBCollection examples;
    FhirContext ctx;
    String host = "155.231.220.40";
    int port = 80;

    /**
     * Constructor to set up our connection to a mongoDB database
     *
     */
    public MongoIF() {
        LOG.info("Connecting to MongoDB at: " + host + " : " + port);
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB("mydb");
        profiles = db.getCollection("profiles");
        examples = db.getCollection("examples");
        ctx = FhirContext.forDstu2();
    }

    public StructureDefinition getSingleStructureDefinitionByName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<StructureDefinition> getMatchByName(StringParam theNamePart) {
        // db.getCollection('profiles').find({name: theNamePart})
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<StructureDefinition> getAll() {
        LOG.info("Getting all StructureDefinitions");
        
        List<StructureDefinition> list = new ArrayList<StructureDefinition>();
        
        Cursor cursor = profiles.find();
        try {
            while(cursor.hasNext()) {
                LOG.info("Got one...");
                StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource((String) cursor.next().toString());
                list.add(foundDocRef);
            }
        } finally {
            cursor.close();
        }        
        LOG.info("Returning a list of : " + list.size() + "StructureDefinitions");
        return list;

    }

}
