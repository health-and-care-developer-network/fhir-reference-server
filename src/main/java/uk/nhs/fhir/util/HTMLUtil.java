package uk.nhs.fhir.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class HTMLUtil {
	public static final String HTML_NS = "http://www.w3.org/1999/xhtml";

    public static final String UTF8_BOM = "\uFEFF";
	
	public static String docToString(Document document, boolean prettyPrint, boolean xmlDeclaration) throws IOException {
		StringWriter writer = new StringWriter();
		Format format = prettyPrint ? Format.getPrettyFormat() : Format.getCompactFormat();
		format.setOmitDeclaration(!xmlDeclaration);
		format.setIndent("  ");
		
		format.setLineSeparator(prettyPrint ? "\n" : "");
		
		XMLOutputter outputter = new XMLOutputter(format);
        outputter.output(document, writer);
        return writer.toString();
	}

    public static Document readFile(String filename) throws JDOMException, IOException {
    	String xml = new String(Files.readAllBytes(Paths.get(filename)));
    	return parseString(xml);
    }
    
    public static Document parseString(String xml) throws JDOMException, IOException {
    	SAXBuilder builder = new SAXBuilder();
        if (xml.startsWith(UTF8_BOM)) {
        	xml = xml.substring(1);
        }
        return builder.build(new StringReader(xml));
    }
}
