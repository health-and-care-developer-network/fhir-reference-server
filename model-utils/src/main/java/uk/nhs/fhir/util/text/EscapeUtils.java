package uk.nhs.fhir.util.text;

/* 
 * This selection is due to method org.hl7.fhir.utilities.xhtml.XhtmlComposer.writeText(XhtmlNode node)
 * This method outputs HTML entities that are not valid XML. An XML parser reading them, specifically when contained in a narrative section, falls over.
 */
public class EscapeUtils {

	public static String escapeTextSection(String textSection) {
		textSection = escapeCopyright(textSection);
		textSection = escapeRegistered(textSection);
		textSection = escapeNbsp(textSection);
		textSection = escapeSectionSign(textSection);
		textSection = escapeTrademark(textSection);
		textSection = escapeMu(textSection);
		
		return textSection;
	}

	private static String escapeRegistered(String toEscape) {
        String escaped = toEscape.replace("®", "&#174;");
        escaped = escaped.replace("\\u00ae", "&#174;");
        return escaped;
	}

	public static String escapeCopyright(String toEscape) {
        String escaped = toEscape.replace("©", "&#169;");
        escaped = escaped.replace("\\u00a9", "&#169;");
        return escaped;
	}
	
	public static String escapeNbsp(String toEscape) {
		String escaped = toEscape.replace(Character.toString((char)0xa0), "&#160;");
		escaped = escaped.replace("\\u00a0", "&#160;");
		return escaped;
	}
	
	public static String escapeSectionSign(String toEscape) {
		String escaped = toEscape.replace("§", "&#167;");
		escaped = escaped.replace("\\u00a7", "&#167;");
		return escaped;
	}
	
	public static String escapeTrademark(String toEscape) {
		String escaped = toEscape.replace("™", "&#8482;");
		escaped = escaped.replace("\\u2122", "&#8482;");
		return escaped;
	}
	
	public static String escapeMu(String toEscape) {
		String escaped = toEscape.replace("μ", "&#956;");
		escaped = escaped.replace("\\u03bc", "&#956;");
		return escaped;
	}
}
