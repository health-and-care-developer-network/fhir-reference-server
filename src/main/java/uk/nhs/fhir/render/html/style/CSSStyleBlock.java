package uk.nhs.fhir.render.html.style;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class CSSStyleBlock {
	private final List<String> selectors;
	private final List<CSSRule> rules;
	
	public CSSStyleBlock() {
		this(Lists.newArrayList(), Lists.newArrayList());
	}
	
	public CSSStyleBlock(List<String> selectors, List<CSSRule> rules) {
		this.selectors = Preconditions.checkNotNull(selectors, "selectors cannot be null");
		this.rules = Preconditions.checkNotNull(rules, "rules cannot be null");
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
			.append(String.join(";\n\t", ruleTexts))
			.append("\n}")
			.toString();
	}
}
