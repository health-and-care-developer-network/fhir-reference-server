package uk.nhs.fhir.makehtml;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.html.CSSRule;

public class CSSStyleBlock {
	private final List<String> selectors;
	private final List<CSSRule> rules;
	
	public CSSStyleBlock(List<String> selectors, List<CSSRule> rules) {
		Preconditions.checkNotNull(selectors, "selectors cannot be null");
		Preconditions.checkArgument(!selectors.isEmpty(), "must have at least one selector");
		Preconditions.checkNotNull(rules, "rules cannot be null");
		Preconditions.checkArgument(!rules.isEmpty(), "rules cannot be empty");
		
		this.selectors = selectors;
		this.rules = rules;
	}
	
	public void addSelector(String selector) {
		selectors.add(selector);
	}
	
	public void addRule(CSSRule rule) {
		rules.add(rule);
	}
	
	public String toFormattedString() {
		
		List<String> ruleTexts = Lists.newArrayList();
		for (CSSRule rule : rules) {
			ruleTexts.add(rule.toFormattedString());
		}

		return new StringBuilder()
			.append(String.join(",\n", selectors)) 
			.append(" {\n\t")
			.append(String.join(";\n\t", ruleTexts))
			.append("\n}")
			.toString();
	}
}
