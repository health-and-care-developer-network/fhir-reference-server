package uk.nhs.fhir.makehtml.html.jdom2;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class HTMLUtil {
	
	public static String docToString(Document document, boolean prettyPrint, boolean xmlDeclaration) throws IOException {
		StringWriter writer = new StringWriter();
		Format format = prettyPrint ? Format.getPrettyFormat() : Format.getCompactFormat();
		format.setOmitDeclaration(!xmlDeclaration);
		format.setIndent("  ");
		
		format.setLineSeparator(prettyPrint ? "\n" : "");
		
		XMLOutputter outputter = new XMLOutputter(format);
        outputter.output(document, writer);
        
        //fix html entities which will have had their apersands escaped
        return writer.toString().replaceAll("&amp;#([\\dA-Fa-f]+);", "&#$1;");
	}
}
