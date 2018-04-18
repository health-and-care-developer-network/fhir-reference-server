package uk.nhs.fhir.render.html.style;

/**
 * A pair of strings representing a CSS rule.
 * e.g. for rule "height: 80", name="height" and arguments="80"
 * Arguments may be a space-delimited set of arguments in a single string.
 */
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

	public String getArguments() {
		return arguments;
	}
	
	public String toFormattedString() {
		return new StringBuilder()
			.append(name)
			.append(": ")
			.append(arguments)
			.toString();
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + arguments.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof CSSRule)) {
			return false;
		} else {
			CSSRule other = (CSSRule)o;
			return name.equals(other.getName())
			  && arguments.equals(other.getArguments());
		}
	}
}