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
package uk.nhs.fhir.validator;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.DomainResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim
 */
public class ResourceCacheTest {

    String VALUESET = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ValueSet xmlns=\"http://hl7.org/fhir\">\n" +
        "  <id value=\"example-inline\"/>\n" +
        "  <meta>\n" +
        "    <profile value=\"http://hl7.org/fhir/StructureDefinition/valueset-shareable-definition\"/>\n" +
        "  </meta>\n" +
        "  <url value=\"http://hl7.org/fhir/ValueSet/example-inline\"/>\n" +
        "  <identifier>\n" +
        "    <system value=\"http://acme.com/identifiers/valuesets\"/>\n" +
        "    <value value=\"loinc-cholesterol-inl\"/>\n" +
        "  </identifier>\n" +
        "  <version value=\"20150622\"/>\n" +
        "  <name value=\"ACME Codes for Cholesterol in Serum/Plasma\"/>\n" +
        "  <status value=\"draft\"/>\n" +
        "  <experimental value=\"true\"/>\n" +
        "  <publisher value=\"HL7 International\"/>\n" +
        "  <contact>\n" +
        "    <name value=\"FHIR project team\"/>\n" +
        "    <telecom>\n" +
        "      <system value=\"other\"/>\n" +
        "      <value value=\"http://hl7.org/fhir\"/>\n" +
        "    </telecom>\n" +
        "  </contact>\n" +
        "  <date value=\"2015-06-22\"/>\n" +
        "  <description value=\"This is an example value set that includes all the ACME codes for serum/plasma cholesterol from v2.36.\"/>\n" +
        "  <codeSystem>\n" +
        "    <system value=\"http://acme.com/config/fhir/codesystems/cholesterol\"/>\n" +
        "    <version value=\"4.2.3\"/>\n" +
        "    <caseSensitive value=\"true\"/>\n" +
        "    <concept>\n" +
        "      <code value=\"chol-mmol\"/>\n" +
        "      <display value=\"SChol (mmol/L)\"/>\n" +
        "      <definition value=\"Serum Cholesterol, in mmol/L\"/>\n" +
        "      <designation>\n" +
        "        <use>\n" +
        "          <system value=\"http://acme.com/config/fhir/codesystems/internal\"/>\n" +
        "          <code value=\"internal-label\"/>\n" +
        "        </use>\n" +
        "        <value value=\"From ACME POC Testing\"/>\n" +
        "      </designation>\n" +
        "    </concept>\n" +
        "    <concept>\n" +
        "      <code value=\"chol-mass\"/>\n" +
        "      <display value=\"SChol (mg/L)\"/>\n" +
        "      <definition value=\"Serum Cholesterol, in mg/L\"/>\n" +
        "      <designation>\n" +
        "        <use>\n" +
        "          <system value=\"http://acme.com/config/fhir/codesystems/internal\"/>\n" +
        "          <code value=\"internal-label\"/>\n" +
        "        </use>\n" +
        "        <value value=\"From Paragon Labs\"/>\n" +
        "      </designation>\n" +
        "    </concept>\n" +
        "    <concept>\n" +
        "      <code value=\"chol\"/>\n" +
        "      <display value=\"SChol\"/>\n" +
        "      <definition value=\"Serum Cholesterol\"/>\n" +
        "      <designation>\n" +
        "        <use>\n" +
        "          <system value=\"http://acme.com/config/fhir/codesystems/internal\"/>\n" +
        "          <code value=\"internal-label\"/>\n" +
        "        </use>\n" +
        "        <value value=\"Obdurate Labs uses this with both kinds of units...\"/>\n" +
        "      </designation>\n" +
        "    </concept>\n" +
        "  </codeSystem>\n" +
        "</ValueSet>";
        
        
    public ResourceCacheTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getResource method, of class ResourceCache.
     */
    @Test
    public void testGetResource() {
        System.out.println("getResource");
        String identifier = "http://random.url.here/path/filename.xml";

        // First we check that getting a random name gives us null.
        DomainResource expResult = null;
        DomainResource result = ResourceCache.getResource(identifier);
        assertEquals(expResult, result);
                
        // Now create a resource which we'll put in
        ValueSet vs = FhirContext.forDstu2Hl7Org().newXmlParser().parseResource(ValueSet.class, VALUESET);
        DomainResource resource = (DomainResource) vs;
        ResourceCache.putResource(identifier, resource);
        
        // And check that that gives us a resource
        result = ResourceCache.getResource(identifier);
        assertEquals(vs, result);
    }

    /**
     * Test of putResource method, of class ResourceCache.
     */
    @Test
    public void testPutResource() {
        System.out.println("putResource");
        String key = "http://made.up.host/path/name.json";

        // Before we start, we check that getting the resource we're going to cache gets null
        DomainResource result = ResourceCache.getResource(key);
        assertEquals(result, null);
        
        // Create a valid resource to be cached
        ValueSet vs = FhirContext.forDstu2Hl7Org().newXmlParser().parseResource(ValueSet.class, VALUESET);
        DomainResource resource = (DomainResource) vs;
        
        // Now put the resource in the cache
        ResourceCache.putResource(key, resource);
        
        // Get it back and see if we get the same thing back
        result = ResourceCache.getResource(key);
        assertEquals(result, resource);
    }
    
}
