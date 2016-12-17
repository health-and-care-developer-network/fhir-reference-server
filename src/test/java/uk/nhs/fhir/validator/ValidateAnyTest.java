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
import static ca.uhn.fhir.model.dstu2.resource.Condition.SEVERITY;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.api.MethodOutcome;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class ValidateAnyTest {

//<editor-fold defaultstate="collapsed" desc="Set up a MINIMAL patient">
    String MINIMAL_PATIENT = "<Patient xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"http://hl7.org/fhir/StructureDefinition/Patient\" /></meta><id value=\"pat1\"/></Patient>";
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Set up a MINIMAL patient with a text block">
    String MINIMAL_PATIENT_TEXT = "<Patient xmlns=\"http://hl7.org/fhir\">" +
                        "<meta><profile value=\"http://hl7.org/fhir/StructureDefinition/Patient\" /></meta>" +
                        "<id value=\"pat1\"/>" +
                        "  <text>\n" +
                        "    <status value=\"generated\"/>\n" +
                        "    <div xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "      <table>\n" +
                        "        <tbody>\n" +
                        "          <tr>\n" +
                        "            <td>Name</td>\n" +
                        "            <td>John Smith</td>\n" +
                        "          </tr>\n" +
                        "          <tr>\n" +
                        "            <td>Address</td>\n" +
                        "            <td>534 Erewhon, Pleasantville, Vic, 3999</td>\n" +
                        "          </tr>\n" +
                        "        </tbody>\n" +
                        "      </table>    \n" +
                        "    </div>\n" +
                        "  </text>\n" +
                        "</Patient>";
//</editor-fold>


    
//<editor-fold defaultstate="collapsed" desc="Set up a MINIMAL patient which HAS NO CONTENT">
    String MINIMAL_PATIENT_NO_CONTENTS = "<Patient xmlns=\"http://hl7.org/fhir\" />";
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Set up a MINIMAL BAD patient">
    String MINIMAL_BAD_PATIENT = "<Patient xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"http://hl7.org/fhir/StructureDefinition/Patient\" /></meta><id value=\"pat1\"/><active value=\"true\"/><active value=\"false\"/><gender value=\"nothinglikeacode\" /></Patient>";
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Set up a HL7 patient">
    String HL7_PATIENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Patient xmlns=\"http://hl7.org/fhir\">\n" +
                        "  <id value=\"example\"/>\n" +
                        "  <text>\n" +
                        "    <status value=\"generated\"/>\n" +
                        "    <div xmlns=\"http://www.w3.org/1999/xhtml\">      \n" +
                        "      <table>\n" +
                        "        <tbody>\n" +
                        "          <tr>\n" +
                        "            <td>Name</td>\n" +
                        "            <td>Peter James \n" +
                        "              <b>Chalmers</b> (&quot;Jim&quot;)\n" +
                        "            </td>\n" +
                        "          </tr>\n" +
                        "          <tr>\n" +
                        "            <td>Address</td>\n" +
                        "            <td>534 Erewhon, Pleasantville, Vic, 3999</td>\n" +
                        "          </tr>\n" +
                        "          <tr>\n" +
                        "            <td>Contacts</td>\n" +
                        "            <td>Home: unknown. Work: (03) 5555 6473</td>\n" +
                        "          </tr>\n" +
                        "          <tr>\n" +
                        "            <td>Id</td>\n" +
                        "            <td>MRN: 12345 (Acme Healthcare)</td>\n" +
                        "          </tr>\n" +
                        "        </tbody>\n" +
                        "      </table>    \n" +
                        "    </div>\n" +
                        "  </text>\n" +
                        "  <identifier>\n" +
                        "    <use value=\"usual\"/>\n" +
                        "    <type>\n" +
                        "      <coding>\n" +
                        "        <system value=\"http://hl7.org/fhir/v2/0203\"/>\n" +
                        "        <code value=\"MR\"/>\n" +
                        "      </coding>\n" +
                        "    </type>\n" +
                        "    <system value=\"urn:oid:1.2.36.146.595.217.0.1\"/>\n" +
                        "    <value value=\"12345\"/>\n" +
                        "    <period>\n" +
                        "      <start value=\"2001-05-06\"/>\n" +
                        "    </period>\n" +
                        "    <assigner>\n" +
                        "      <display value=\"Acme Healthcare\"/>\n" +
                        "    </assigner>\n" +
                        "  </identifier>\n" +
                        "  <active value=\"true\"/>\n" +
                        "  <name>\n" +
                        "    <use value=\"official\"/>\n" +
                        "    <family value=\"Chalmers\"/>\n" +
                        "    <given value=\"Peter\"/>\n" +
                        "    <given value=\"James\"/>\n" +
                        "  </name>\n" +
                        "  <telecom>\n" +
                        "    <system value=\"phone\"/>\n" +
                        "    <value value=\"(03) 5555 6473\"/>\n" +
                        "    <use value=\"work\"/>\n" +
                        "  </telecom>\n" +
                        "  <gender value=\"male\"/>\n" +
                        "  <birthDate value=\"1974-12-25\">\n" +
                        "    <extension url=\"http://hl7.org/fhir/StructureDefinition/patient-birthTime\">\n" +
                        "      <valueDateTime value=\"1974-12-25T14:35:45-05:00\"/>\n" +
                        "    </extension>\n" +
                        "  </birthDate>\n" +
                        "  <deceasedBoolean value=\"false\"/>\n" +
                        "  <address>\n" +
                        "    <use value=\"home\"/>\n" +
                        "    <type value=\"both\"/>\n" +
                        "    <line value=\"534 Erewhon St\"/>\n" +
                        "    <city value=\"PleasantVille\"/>\n" +
                        "    <district value=\"Rainbow\"/>\n" +
                        "    <state value=\"Vic\"/>\n" +
                        "    <postalCode value=\"3999\"/>\n" +
                        "    <period>\n" +
                        "      <start value=\"1974-12-25\"/>\n" +
                        "    </period>\n" +
                        "  </address>\n" +
                        "  <contact>\n" +
                        "    <relationship>\n" +
                        "      <coding>\n" +
                        "        <system value=\"http://hl7.org/fhir/patient-contact-relationship\"/>\n" +
                        "        <code value=\"partner\"/>\n" +
                        "      </coding>\n" +
                        "    </relationship>\n" +
                        "    <name>\n" +
                        "      <family value=\"Marché\"/>\n" +
                        "      <given value=\"Bénédicte\"/>\n" +
                        "    </name>\n" +
                        "    <telecom>\n" +
                        "      <system value=\"phone\"/>\n" +
                        "      <value value=\"+33 (237) 998327\"/>\n" +
                        "    </telecom>\n" +
                        "    <gender value=\"female\"/>\n" +
                        "    <period>\n" +
                        "      <start value=\"2012\"/>\n" +
                        "    </period>\n" +
                        "  </contact>\n" +
                        "  <managingOrganization>\n" +
                        "    <reference value=\"Organization/1\"/>\n" +
                        "  </managingOrganization>\n" +
                        "</Patient>";
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Set up a GP Connect Patient resource">
    String GPCONNECT_PATIENT = "<Patient xmlns=\"http://hl7.org/fhir\">\n" +
                        "    <id value=\"07e2071f-509f-4ac1-82e5-2f74a8915379\" />\n" +
                        "    <meta>\n" +
                        "        <profile value=\"http://fhir.nhs.uk/fhir/StructureDefinition/GPConnect-Register-Patient-1.xml\" />\n" +
                        "    </meta>\n" +
                        "    <extension url=\"http://fhir.nhs.uk/fhir/Extension/Extension-Registration-Period-1.xml\">\n" +
                        "        <valuePeriod>\n" +
                        "            <start value=\"1991-01-01\" />\n" +
                        "        </valuePeriod>\n" +
                        "    </extension>\n" +
                        "    <extension url=\"http://fhir.nhs.uk/fhir/Extension/Extension-Registration-Status-1.xml\">\n" +
                        "        <valueCodeableConcept>\n" +
                        "            <coding>\n" +
                        "                <system value=\"http://fhir.nhs.uk/fhir/registration-status-code-1\" />\n" +
                        "                <code value=\"A\" />\n" +
                        "                <display value=\"Active\" />\n" +
                        "            </coding>\n" +
                        "        </valueCodeableConcept>\n" +
                        "    </extension>\n" +
                        "    <extension url=\"http://fhir.nhs.uk/fhir/Extension/Extension-Registration-Type-1.xml\">\n" +
                        "        <valueCodeableConcept>\n" +
                        "            <coding>\n" +
                        "                <system value=\"http://fhir.nhs.uk/fhir/ValueSet/registration-type-1.xml\" />\n" +
                        "                <code value=\"R\" />\n" +
                        "                <display value=\"Fully Registered\" />\n" +
                        "            </coding>\n" +
                        "        </valueCodeableConcept>\n" +
                        "    </extension>\n" +
                        "    <name>\n" +
                        "        <use value=\"usual\" />\n" +
                        "        <family value=\"Taylor\" />\n" +
                        "        <given value=\"Sally\" />\n" +
                        "    </name>\n" +
                        "    <identifier><system value=\"http://fhir.nhs.net/Id/nhs-number\"/><value value=\"9900002831\"/></identifier>\n" +
                        "    <gender value=\"female\" />\n" +
                        "    <birthDate value=\"1947-06-09\" />\n" +
                        "</Patient>";
//</editor-fold>
    static FhirContext ctx;
    
    public ValidateAnyTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ctx = FhirContext.forDstu2();
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
     * Test of validateStructureDefinition method, of class ValidateAny.
     */
    @Test
    public void testValidateStructureDefinition_MINIMAL() {
        System.out.println("testValidateStructureDefinition_MINIMAL");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, MINIMAL_PATIENT);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(0, errorCount);
    }

    /**
     * Test of validateStructureDefinition method, of class ValidateAny.
     */
    @Test
    public void testValidateStructureDefinition_MINIMAL_TEXT() {
        System.out.println("testValidateStructureDefinition_MINIMAL_TEXT");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, MINIMAL_PATIENT_TEXT);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(0, errorCount);
    }

    

    /**
     * Test of validateStructureDefinition method, of class ValidateAny.
     */
    @Test
    public void testValidateStructureDefinition_MINIMAL_PATIENT_NO_CONTENTS() {
        System.out.println("testValidateStructureDefinition_MINIMAL_PATIENT_NO_CONTENTS");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, MINIMAL_PATIENT_NO_CONTENTS);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(2, errorCount);
    }
    
    /**
     * Tests how we react when (in this example) we have a patient with a gender value which isn't in
     * the known list of genders.
     * 
     */
    @Test
    public void testValidateStructureDefinition_MINIMAL_BAD() {
        System.out.println("testValidateStructureDefinition_MINIMAL_BAD");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, MINIMAL_BAD_PATIENT);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(2, errorCount);
    }
    
    /**
     * Tests the validation of a HL7 base Patient resource.
     * 
     */
    @Test
    public void testValidateStructureDefinition_HL7() {
        System.out.println("testValidateStructureDefinition_HL7");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, HL7_PATIENT);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(0, errorCount);
    }
    
    /**
     * Tests the validation of a GP Connect defined locally profiled Patient resource.
     * The resource claims it conforms to the profile:
     * 
     * http://fhir.nhs.uk/fhir/StructureDefinition/GPConnect-Register-Patient-1.xml
     * 
     */
    @Test
    public void testValidateStructureDefinition_GP_CONNECT() {
        System.out.println("testValidateStructureDefinition_GP_CONNECT");
        int errorCount = 0;
        
        Patient pat = ctx.newXmlParser().parseResource(Patient.class, GPCONNECT_PATIENT);
        IBaseResource resourceToTest = pat;
        MethodOutcome methodOutcome = ValidateAny.validateStructureDefinition(ctx, resourceToTest);
        OperationOutcome opOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        List<Issue> issueList = opOutcome.getIssue();
        for(Issue thisIssue : issueList) {
            String sev = thisIssue.getSeverity().toLowerCase();
            if(!sev.equals("information")) {
                errorCount++;
            }
            System.out.println("+++Severity: [" + sev + "] Diagnostic message: [" + thisIssue.getDiagnosticsElement().toString() + "]");
        }        
        assertEquals(1, errorCount);
    }
}
