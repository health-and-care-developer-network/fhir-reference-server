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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.util.PropertyReader;

/**
 *
 * @author Tim Coates
 */
public class MongoIF implements Datasource {
    private static final Logger LOG = Logger.getLogger(MongoIF.class.getName());

    private MongoClient mongoClient;
    private DB db;
    private DBCollection profiles;
    private DBCollection examples;
    private FhirContext ctx;

    private int port = 27017;
    private String host = PropertyReader.getProperty("mongoHost");
    private String portStr = PropertyReader.getProperty("mongoPort");

    /**
     * Constructor to set up our connection to a mongoDB database
     *
     */
    public MongoIF() {
    	
        if (portStr != null) {
        	port = Integer.parseInt(portStr);
        }
        
        LOG.info("Connecting to MongoDB at: " + host + " : " + port);
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB("mydb");
        profiles = db.getCollection("profiles");
        examples = db.getCollection("examples");
        ctx = FhirContext.forDstu2();
    }

    /* (non-Javadoc)
	 * @see uk.nhs.fhir.datalayer.Datasource#getSingleStructureDefinitionByName(java.lang.String)
	 */
    @Override
	public StructureDefinition getSingleStructureDefinitionByID(String id) {
        LOG.info("Getting StructureDefinitions with id=" + id);
        BasicDBObject query = new BasicDBObject("id", id);
        DBObject found = profiles.findOne(query);
        StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource(found.toString());
        return foundDocRef;
    }

    /* (non-Javadoc)
	 * @see uk.nhs.fhir.datalayer.Datasource#getMatchByName(java.lang.String)
	 */
    @Override
	public List<StructureDefinition> getStructureDefinitionMatchByName(String theNamePart) {
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

    /* (non-Javadoc)
	 * @see uk.nhs.fhir.datalayer.Datasource#getAll()
	 */
    @Override
	public List<StructureDefinition> getAllStructureDefinitions() {
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
    
    /* (non-Javadoc)
	 * @see uk.nhs.fhir.datalayer.Datasource#getAllNames()
	 */
    @Override
	public List<String> getAllStructureDefinitionNames() {
        LOG.info("Getting all StructureDefinition Names");
        
        List<String> list = new ArrayList<String>();
        
        Cursor cursor;
        cursor = profiles.find();
        try {
            while(cursor.hasNext()) {
                LOG.info("Got one...");
                StructureDefinition foundDocRef = (StructureDefinition) ctx.newJsonParser().parseResource((String) cursor.next().toString());
                list.add("<li><a href='/FHIR/StructureDefinition/" + foundDocRef.getName() + "'>" + foundDocRef.getName() + "</a> - " + foundDocRef.getUrl() + "</li>");
            }
        } finally {
            cursor.close();
        }        
        LOG.info("Returning a list of : " + list.size() + "StructureDefinition names");
        return list;
    }


    /* (non-Javadoc)
	 * @see uk.nhs.fhir.datalayer.Datasource#getAllNames(java.lang.String)
	 */
    @Override
	public List<String> getAllStructureDefinitionNames(String theNamePart) {
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
                list.add("<li><a href='/FHIR/StructureDefinition/" + foundDocRef.getName() + "'>" + foundDocRef.getName() + "</a> - " + foundDocRef.getUrl() + "</li>");
            }
        } finally {
            if(cursor != null)
                cursor.close();
        }        
        LOG.info("Returning a list of : " + list.size() + "StructureDefinition names");
        return list;
    }
    
    public HashMap<String, List<ResourceEntity>> getAllStructureDefinitionNamesByBaseResource() {
    	//TODO: Implement this for Mongo
    	return null;
    }

    @Override
    public ValueSet getSingleValueSetByID(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ValueSet> getAllValueSets() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getAllValueSetNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	@Override
	public HashMap<String, List<ResourceEntity>> getAllValueSetNamesByCategory() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public OperationDefinition getSingleOperationDefinitionByID(String id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<OperationDefinition> getAllOperations() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getAllOperationNames() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public HashMap<String, List<ResourceEntity>> getAllOperationNamesByCategory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ImplementationGuide getSingleImplementationGuideByID(String id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<ImplementationGuide> getAllImplementationGuides() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getAllImplementationGuideNames() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public HashMap<String, List<ResourceEntity>> getAllImplementationGuideNamesByCategory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
