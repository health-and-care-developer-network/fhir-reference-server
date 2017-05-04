/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.makehtml.old;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.HTMLUtil;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class XMLParserUtilsTest {
    Document document;
    Element element;
    Element elementReference;
    Element elementQuantity;
    public XMLParserUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
                "  <snapshot>\n" +
                "    <element>\n" +
                "      <path value=\"Account\"/>\n" +
                "      <short value=\"This Account\"/>\n" +
                "      <definition value=\"A financial tool for tracking value accrued for a particular purpose.  In the healthcare field, used to track charges for a patient, cost centres, etc.\"/>\n" +
                "      <min value=\"0\"/>\n" +
                "      <max value=\"*\"/>\n" +
                "      <type>\n" +
                "        <code value=\"DomainResource\"/>\n" +
                "      </type>\n" +
                "      <isSummary value=\"true\"/>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "      <path value=\"Account.id\"/>\n" +
                "      <short value=\"Logical id of this artifact\"/>\n" +
                "      <definition value=\"The logical id of the resource, as used in the URL for the resource. Once assigned, this value never changes.\"/>\n" +
                "      <comments value=\"The only time that a resource does not have an id is when it is being submitted to the server using a create operation. Bundles always have an id, though it is usually a generated UUID.\"/>\n" +
                "      <min value=\"0\"/>\n" +
                "      <max value=\"1\"/>\n" +
                "      <type>\n" +
                "        <code value=\"id\"/>\n" +
                "      </type>\n" +
                "      <isSummary value=\"true\"/>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "      <path value=\"Account.subject\"/>\n" +
                "      <short value=\"What is account tied to?\"/>\n" +
                "      <definition value=\"Identifies the patient, device, practitioner, location or other object the account is associated with.\"/>\n" +
                "      <alias value=\"target\"/>\n" +
                "      <min value=\"0\"/>\n" +
                "      <max value=\"1\"/>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Patient\"/>\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Device\"/>\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Practitioner\"/>\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Location\"/>\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/HealthcareService\"/>\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Reference\"/>\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Organization\"/>\n" +
                "      </type>\n" +
                "      <isSummary value=\"true\"/>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "      <path value=\"Order.when.schedule.repeat.bounds[x]\" />\n" +
                "      <short value=\"Length/Range of lengths, or (Start and/or end) limits\" />\n" +
                "      <definition value=\"Either a duration for the length of the timing schedule, a range of possible length, or outer bounds for start and/or end limits of the timing schedule.\" />\n" +
                "      <min value=\"0\" />\n" +
                "      <max value=\"1\" />\n" +
                "      <base>\n" +
                "        <path value=\"Timing.repeat.bounds[x]\" />\n" +
                "        <min value=\"0\" />\n" +
                "        <max value=\"1\" />\n" +
                "      </base>\n" +
                "      <type>\n" +
                "        <code value=\"Quantity\" />\n" +
                "        <profile value=\"http://hl7.org/fhir/StructureDefinition/Duration\" />\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Range\" />\n" +
                "      </type>\n" +
                "      <type>\n" +
                "        <code value=\"Period\" />\n" +
                "      </type>\n" +
                "      <isSummary value=\"true\" />\n" +
                "      <mapping>\n" +
                "        <identity value=\"rim\" />\n" +
                "        <map value=\"IVL(TS) used in a QSI\" />\n" +
                "      </mapping>\n" +
                "    </element>\n" +
                "  </snapshot>\n" +
                "  <differential>\n" +
                "    <element>\n" +
                "      <path value=\"Account\"/>\n" +
                "      <definition value=\"A financial tool for tracking value accrued for a particular purpose.  In the healthcare field, used to track charges for a patient, cost centres, etc.\"/>\n" +
                "      <min value=\"0\"/>\n" +
                "      <max value=\"*\"/>\n" +
                "      <type>\n" +
                "        <code value=\"DomainResource\"/>\n" +
                "      </type>\n" +
                "      <isSummary value=\"true\"/>\n" +
                "    </element>\n" +
                "  </differential>\n" +
                "</StructureDefinition>";
            document = HTMLUtil.parseString(xml);
        } catch (IOException | JDOMException ex) {
            Logger.getLogger(XMLParserUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Element> snapshot = XMLParserUtils.descendantsList(document, "snapshot");
        Element snapshotNode = snapshot.get(0);
        List<Element> elements = XMLParserUtils.descendantsList(snapshotNode, "element");
        element = elements.get(0);
        elementReference = elements.get(2);
        elementQuantity =  elements.get(3);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getElementName method, of class XMLParserUtils.
     */
    @Test
    public void testGetElementName() {
        System.out.println("getElementName");
        String expResult = "Account";
        String result = XMLParserUtils.getElementName(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getElementCardinality method, of class XMLParserUtils.
     */
    @Test
    public void testGetElementCardinality() {
        System.out.println("getElementCardinality");
        String expResult = "0..*";
        String result = XMLParserUtils.getElementCardinality(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getElementTypeName method, of class XMLParserUtils.
     */
    @Test
    public void testGetElementTypeName() {
        System.out.println("getElementTypeName");
        String expResult = "DomainResource";
        String result = XMLParserUtils.getElementTypeName(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getElementTypeList method, of class XMLParserUtils.
     */
    @Test
    public void testGetElementTypeList() {
        System.out.println("getElementTypeList");
        List<String> expResult = Lists.newArrayList();
        expResult.add("DomainResource");
        List<String> result = XMLParserUtils.getElementTypeList(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFlags method, of class XMLParserUtils.
     */
    @Test
    public void testGetFlags() {
        System.out.println("getFlags");
        String expResult = "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is included in summaries\">&#931;</span>\n";
        String result = XMLParserUtils.getFlags(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTitle method, of class XMLParserUtils.
     */
    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        String expResult = "This Account";
        String result = XMLParserUtils.getTitle(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class XMLParserUtils.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        String expResult = "A financial tool for tracking value accrued for a particular purpose.  In the healthcare field, used to track charges for a patient, cost centres, etc.";
        String result = XMLParserUtils.getDescription(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getReferenceTypes method, of class XMLParserUtils.
     */
    @Test
    public void testGetReferenceTypes() {
        System.out.println("getReferenceTypes");
        String expResult = "<a href='https://www.hl7.org/fhir/references.html'>Reference</a>("
                + "<a href='http://hl7.org/fhir/StructureDefinition/Patient'>Patient</a> | "
                + "<a href='http://hl7.org/fhir/StructureDefinition/Device'>Device</a> | "
                + "<a href='http://hl7.org/fhir/StructureDefinition/Practitioner'>Practitioner</a> | "
                + "<a href='http://hl7.org/fhir/StructureDefinition/Location'>Location</a> | "
                + "<a href='http://hl7.org/fhir/StructureDefinition/HealthcareService'>HealthcareService</a> | "
                + "<a href='http://hl7.org/fhir/StructureDefinition/Organization'>Organization</a>)";
        String result = XMLParserUtils.getReferenceTypes(elementReference);
        assertEquals(expResult, result);
    }


    /**
     * Test of getQuantityType method, of class XMLParserUtils.
     */
    @Test
    public void testGetQuantityType() {
        System.out.println("getQuantityType");
        String expResult = "<a href='http://hl7.org/fhir/StructureDefinition/Duration'>Duration</a>";
        String result = XMLParserUtils.getQuantityType(elementQuantity);
        assertEquals(expResult, result);
    }
    
}
