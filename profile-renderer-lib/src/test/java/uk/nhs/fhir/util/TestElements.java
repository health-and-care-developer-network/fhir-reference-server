package uk.nhs.fhir.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Text;
import org.junit.Assert;
import org.junit.Test;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.HTMLUtil;

public class TestElements {
	@Test
	public void testCreateWithChild() throws ParserConfigurationException, XMLStreamException, FactoryConfigurationError, TransformerException, IOException {
		Document doc = new Document(Elements.withChild("test",
			Elements.newElement("test2")));
		
		String expected = "<test xmlns=\"http://www.w3.org/1999/xhtml\">\n  <test2 />\n</test>\n";
		String actual = HTMLUtil.docToString(doc, true, false);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testValidText() {
		Elements.text("valid string");
	}
	
	@Test
	public void testReplaceControl() {
		String stringWithControl = "My \u0013 text";
		Text text = Elements.text(stringWithControl);
		Assert.assertEquals("My 0x13 text", text.getText());
	}
	
	@Test
	public void testAllowSurrogatePair() {
		String stringWithSurrogate = "My 😀 \u0013text";
		Text text = Elements.text(stringWithSurrogate);
		Assert.assertEquals("My 😀 0x13text", text.getText());
	}
}
