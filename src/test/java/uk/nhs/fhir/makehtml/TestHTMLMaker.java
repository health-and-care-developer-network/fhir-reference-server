package uk.nhs.fhir.makehtml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Document;

public class TestHTMLMaker {

    /**
     * Test of decorateTypeName method, of class NewMain.
     */
    @Test
    public void testDecorateTypeName() {
        System.out.println("decorateTypeName");
        HTMLMaker instance = new DummyHTMLMaker();
        String type = "DomainResource";
        String expResult = "<a href='https://www.hl7.org/fhir/domainresource.html'>DomainResource</a>";
        String result = instance.decorateTypeName(type);
        assertEquals(expResult, result);

        type = "boolean";
        expResult = "<a href='https://www.hl7.org/fhir/datatypes.html#boolean'>boolean</a>";
        result = instance.decorateTypeName(type);
        assertEquals(expResult, result);
    
        type = "failSafeValue";
        expResult = type;
        result = instance.decorateTypeName(type);
        assertEquals(expResult, result);
    
    }

    /**
     * Test of decorateResourceName method, of class NewMain.
     */
    @Test
    public void testDecorateResourceName() {
        System.out.println("decorateResourceName");
        String type = "Address";
        HTMLMaker instance = new DummyHTMLMaker();
        String expResult = "<a href='https://www.hl7.org/fhir/address.html'>Address</a>";
        String result = instance.decorateResourceName(type);
        assertEquals(expResult, result);
    }
}

class DummyHTMLMaker extends HTMLMaker {

	@Override
	public String makeHTML(Document doc) {
		return "";
	}
	
}
