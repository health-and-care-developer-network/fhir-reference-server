package uk.nhs.fhir.render.html;

import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class HTMLUtil {
	
	public static String docToString(Document document, boolean prettyPrint, boolean xmlDeclaration) throws IOException {
		Format format = prettyPrint ? Format.getPrettyFormat() : Format.getCompactFormat();
		
		String output = 
			new XMLOutputter(
				format
					.setOmitDeclaration(!xmlDeclaration)
					.setIndent("  ")
					.setLineSeparator(prettyPrint ? "\n" : ""))
					.outputString(document);
		
		//fix html entities which will have had their apersands escaped
        return output.replaceAll("&amp;#([\\dA-Fa-f]+);", "&#$1;");
	}
}
