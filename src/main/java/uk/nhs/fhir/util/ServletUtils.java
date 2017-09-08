package uk.nhs.fhir.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class ServletUtils {
	
    /**
     * Simple XML syntax highlight
	 * @see https://coderwall.com/p/rjwkma/simple-java-html-syntax-highlighter-for-xml-code
     * @param source
     * @return syntax highlighted source
     */
    public static final String syntaxHighlight(final String source) {
    	String target = source;
        target = target.replaceAll("<([^>/]*)/>", "&lt;~blue~$1~/~/&gt;");
        target = target.replaceAll("<([^>]*)>", "&lt;~blue~$1~/~&gt;");
        target = target.replaceAll("([\\w]+)=\"([^\"]*)\"", "~red~$1~/~~black~=\"~/~~green~$2~/~~black~\"~/~");
        target = target.replaceAll("~([a-z]+)~", "<span style=\"color: $1;\">");
        target = target.replace("~/~", "</span>");
        return target;
    }
    
    public static String prettyPrintXML(String xml) throws UnsupportedEncodingException, TransformerFactoryConfigurationError, TransformerException {
    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	//initialize StreamResult with File object to save to file
    	StreamResult result = new StreamResult(new StringWriter());
    	Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes("utf-8")));;
    	transformer.transform(source, result);
    	String xmlString = result.getWriter().toString();
    	return xmlString;
    }

}