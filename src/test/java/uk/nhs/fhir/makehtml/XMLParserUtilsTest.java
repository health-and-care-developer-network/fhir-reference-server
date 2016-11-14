/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.makehtml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class XMLParserUtilsTest {
    Document document;
    Element element;
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
                "  </snapshot>\n" +
                "  <differential>\n" +
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
                "  </differential>\n" +
                "</StructureDefinition>";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            document = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLParserUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XMLParserUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLParserUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        NodeList snapshot = document.getElementsByTagName("snapshot");
        Element snapshotNode = (Element) snapshot.item(0);
        NodeList elements = snapshotNode.getElementsByTagName("element");
        element = (Element) elements.item(0);
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
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add("DomainResource");
        ArrayList<String> result = XMLParserUtils.getElementTypeList(element);
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
        String expResult = "A financial tool for tracking value accrued for a particular purpose.  In the healthcare field, used to track charges for a patient, cost centres, etc.\"/>\n";
        String result = XMLParserUtils.getDescription(element);
        assertEquals(expResult, result);
    }

    /**
     * Test of getReferenceTypes method, of class XMLParserUtils.
     */
    @Test
    public void testGetReferenceTypes() {
        System.out.println("getReferenceTypes");
        Element element = null;
        String expResult = "";
        String result = XMLParserUtils.getReferenceTypes(element);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getQuantityType method, of class XMLParserUtils.
     */
    @Test
    public void testGetQuantityType() {
        System.out.println("getQuantityType");
        Element element = null;
        String expResult = "";
        String result = XMLParserUtils.getQuantityType(element);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
