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
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
    //String host = "mymongo";
    //int port = 27017;
    String host = "localhost";
    int port = 28015;

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

    /**
     * Gets a specific one
     * @param name
     * @return 
     */
    public StructureDefinition getSingleStructureDefinitionByName(String name) {
        LOG.info("Getting StructureDefinitions with name=" + name);
        BasicDBObject query = new BasicDBObject("name", name);
        DBObject found = profiles.findOne(query);
        StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource(found.toString());
        return foundDocRef;
    }

    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public List<StructureDefinition> getMatchByName(String theNamePart) {
        LOG.info("Getting StructureDefinitions with name=" + theNamePart);
        List<StructureDefinition> list = new ArrayList<StructureDefinition>();
        
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("name",
            new BasicDBObject("$regex", Pattern.quote(theNamePart))
            .append("$options", "i"));
    
        Cursor cursor = profiles.find(regexQuery);
        
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

    /**
     * Gets a full list of StructureDefinition objects
     * 
     * @return 
     */
    public List<StructureDefinition> getAll() {
        LOG.info("Getting all StructureDefinitions");
        
        List<StructureDefinition> list = new ArrayList<StructureDefinition>();
        
        Cursor cursor;
        cursor = profiles.find();
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
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllNames() {
        LOG.info("Getting all StructureDefinition Names");
        
        List<String> list = new ArrayList<String>();
        
        Cursor cursor;
        cursor = profiles.find();
        try {
            while(cursor.hasNext()) {
                LOG.info("Got one...");
                StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource((String) cursor.next().toString());
                list.add("<li><a href='/fhir/FHIR/StructureDefinition/" + foundDocRef.getName() + "'>" + foundDocRef.getName() + "</a> - " + foundDocRef.getUrl() + "</li>");
            }
        } finally {
            cursor.close();
        }        
        LOG.info("Returning a list of : " + list.size() + "StructureDefinition names");
        return list;
    }


    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return 
     */
    public List<String> getAllNames(String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        List<String> list = new ArrayList<String>();
        
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("name",
            new BasicDBObject("$regex", Pattern.quote(theNamePart))
            .append("$options", "i"));
    
        Cursor cursor = profiles.find(regexQuery);
        try {
            while(cursor.hasNext()) {
                LOG.info("Got one...");
                StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource((String) cursor.next().toString());
                list.add("<li><a href='/fhir/FHIR/StructureDefinition/" + foundDocRef.getName() + "'>" + foundDocRef.getName() + "</a> - " + foundDocRef.getUrl() + "</li>");
            }
        } finally {
            if(cursor != null)
                cursor.close();
        }        
        LOG.info("Returning a list of : " + list.size() + "StructureDefinition names");
        return list;
    }
}
