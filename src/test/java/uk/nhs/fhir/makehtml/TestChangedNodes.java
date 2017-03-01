package uk.nhs.fhir.makehtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestChangedNodes {

	Document document;
    Element element;
    Element elementReference;
    Element elementQuantity;
    NodeList difflist;
    
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
        difflist = document.getElementsByTagName("differential");
        Element snapshotNode = (Element) difflist.item(0);
        NodeList elements = snapshotNode.getElementsByTagName("element");
        element = (Element) elements.item(0);
        elementReference = (Element) elements.item(2);
        elementQuantity =  (Element) elements.item(3);
    }
	
    /**
     * Test of GetChangedNodes method, of class NewMain.
     */
    @Test
    public void testGetChangedNodes() {
        System.out.println("GetChangedNodes");
        HTMLMaker instance = new DummyHTMLMaker();

        ArrayList<String> result = instance.GetChangedNodes(document);
        assertEquals(2, result.size());
        
        assertTrue(result.contains("Account"));
        
        assertTrue(result.contains("Account.subject"));
        
        assertFalse(result.contains("Should.Not.Find.Me"));
    }
}
