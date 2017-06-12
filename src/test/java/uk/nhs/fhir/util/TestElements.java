package uk.nhs.fhir.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.junit.Test;

import junit.framework.Assert;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;

public class TestElements {
	@Test
	public void testCreateWithChild() throws ParserConfigurationException, XMLStreamException, FactoryConfigurationError, TransformerException, IOException {
		Document doc = new Document(Elements.withChild("test",
			Elements.newElement("test2")));
		
		String expected = "<test xmlns=\"http://www.w3.org/1999/xhtml\">\n  <test2 />\n</test>\n";
		String actual = HTMLUtil.docToString(doc, true, false);
		Assert.assertEquals(expected, actual);
	}
}
