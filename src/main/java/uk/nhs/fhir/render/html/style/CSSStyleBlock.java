package uk.nhs.fhir.render.html.style;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * A set of selectors (each of which may consist of multiple parts) and a set of style rules to be applied to the selected HTML elements.
 */
public class CSSStyleBlock {
	private final List<String> selectors;
	private final List<CSSRule> rules;
	
	public CSSStyleBlock() {
		this(Lists.newArrayList(), Lists.newArrayList());
	}
	
	public CSSStyleBlock(List<String> selectors, List<CSSRule> rules) {
		if (selectors == null || selectors.isEmpty()) {
			throw new IllegalArgumentException("selectors cannot be null or empty [" + selectors + "]");
		}
		if (rules == null || rules.isEmpty()) {
			throw new IllegalArgumentException("rules cannot be null or empty [" + rules + "]");
		}
		
		this.selectors = selectors;
		this.rules = rules;
	}
	
	public void addSelector(String selector) {
		selectors.add(selector);
	}
	
	public void addRule(CSSRule rule) {
		rules.add(rule);
	}
	
	public List<String> getSelectors() {
		return selectors;
	}
	
	public List<CSSRule> getRules() {
		return rules;
	}
	
	public String toFormattedString() {
		
		List<String> ruleTexts = Lists.newArrayList();
		for (CSSRule rule : rules) {
			ruleTexts.add(rule.toFormattedString());
		}

		return new StringBuilder()
			.append(String.join(",\n", selectors)) 
			.append(" {\n\t")
			.append(rules.stream().map(rule -> rule.toFormattedString()).collect(Collectors.joining(";\n\t")))
			.append("\n}")
			.toString();
	}
}
