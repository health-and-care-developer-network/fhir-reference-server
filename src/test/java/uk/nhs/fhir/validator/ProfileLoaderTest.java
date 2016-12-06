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
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.ValueSet.ConceptSetComponent;
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
public class ProfileLoaderTest {
    
    FhirContext fc;
    ValueSet vs;
    String vsURL = "https://raw.githubusercontent.com/nhsconnect/gpconnect-fhir/develop/ValueSets/gpconnect-error-or-warning-code-1.xml";
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
    
    public ProfileLoaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        fc = FhirContext.forDstu2Hl7Org();
        vs = fc.newXmlParser().parseResource(ValueSet.class, VALUESET);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of expandValueSet method, of class ProfileLoader.
     */
    @Test
    public void testExpandValueSet() {
        System.out.println("expandValueSet");
        ConceptSetComponent csc = new ConceptSetComponent();
        csc.setSystem("http://acme.com/config/fhir/codesystems/internal");
        ProfileLoader instance = new ProfileLoader();
        ValueSet.ValueSetExpansionComponent expResult = null;
        ValueSet.ValueSetExpansionComponent result = instance.expandValueSet(fc, csc);
        assertEquals(expResult, result);
    }

    /**
     * Test of fetchCodeSystem method, of class ProfileLoader.
     */
    @Test
    public void testFetchCodeSystem() {
        System.out.println("fetchCodeSystem");
        ProfileLoader instance = new ProfileLoader();        
        ValueSet result = instance.fetchCodeSystem(fc, vsURL);
        assertEquals(result.getCodeSystem().getSystem(), "http://fhir.nhs.net/ValueSet/gpconnect-error-or-warning-code-1");
    }

    /**
     * Test of fetchResource method, of class ProfileLoader.
     */
    @Test
    public void testFetchResource() {
        System.out.println("fetchResource");
        ProfileLoader instance = new ProfileLoader();
        String expResult = "GP Connect Error or Warning Code";
        ValueSet result = (ValueSet) instance.fetchResource(fc, ValueSet.class, vsURL);
        assertEquals(expResult, result.getName());
    }

    /**
     * Test of isCodeSystemSupported method, of class ProfileLoader.
     */
    @Test
    public void testIsCodeSystemSupported() {
        System.out.println("isCodeSystemSupported");
        ProfileLoader instance = new ProfileLoader();
        String theSystem = "http://fhir.nhs.uk/samplesystem";
        boolean expResult = true;
        boolean result = instance.isCodeSystemSupported(fc, theSystem);
        assertEquals(expResult, result);
        
        theSystem = vsURL;
        expResult = false;
        result = instance.isCodeSystemSupported(fc, theSystem);
        assertEquals(expResult, result);

    }

    /**
     * Test of validateCode method, of class ProfileLoader.
     */
    @Test
    public void testValidateCode() {
        System.out.println("validateCode");
        String theCodeSystem = "https://raw.githubusercontent.com/nhsconnect/gpconnect-fhir/develop/ValueSets/gpconnect-error-or-warning-code-1.xml";
        String theCode = "GPC-001";
        String theDisplay = "Not found";
        ProfileLoader instance = new ProfileLoader();
        IValidationSupport.CodeValidationResult result = instance.validateCode(fc, theCodeSystem, theCode, theDisplay);
        assertTrue(result.isOk());
    }


    
}
