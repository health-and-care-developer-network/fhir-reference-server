package uk.nhs.fhir.util;

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

}
