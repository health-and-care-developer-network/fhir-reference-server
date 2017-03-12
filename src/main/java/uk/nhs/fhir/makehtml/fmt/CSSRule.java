package uk.nhs.fhir.makehtml.fmt;

public class CSSRule {
	
	private final String name;
	private final String arguments;
	
	public CSSRule(String name, String string) {
		this.name = name;
		this.arguments = string;
	}
	
	public String getName() {
		return name;
	}
	
	public String toFormattedString() {
		return new StringBuilder()
			.append(name)
			.append(": ")
			.append(arguments)
			.toString();
	}
}