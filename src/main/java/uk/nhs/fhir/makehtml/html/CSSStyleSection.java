package uk.nhs.fhir.makehtml.html;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.makehtml.CSSStyleBlock;

/**
 * Holds a document's styles.
 * Preserves the order of original addition (at the level of a CSSStyleBlock) while avoiding duplication and checking consistency.  
 */
public class CSSStyleSection {
	private final Map<String, Map<String, String>> rulesAdded = Maps.newHashMap();
	private final List<CSSStyleBlock> styleBlocks = Lists.newArrayList();

	public List<CSSStyleBlock> getBlocks() {
		return styleBlocks;
	}
	
	public void addStylesSection(CSSStyleSection styles) {
		addStyles(styles.getBlocks());
	}
	
	public void addStyles(List<CSSStyleBlock> styles) {
		for (CSSStyleBlock block : styles) {
			addBlock(block);
		}
	}
	
	public void addBlock(CSSStyleBlock block) {
		boolean allExist = true;
		boolean noneExist = true;
		
		Map<CSSRule, List<String>> selectorsForRules = Maps.newHashMap();
		
		for (CSSRule rule : block.getRules()) {
			for (String selector : block.getSelectors())  {
				if (ruleExists(selector, rule)) {
					noneExist = false;
					
					assertMatchesExistingRule(selector, rule);
				} else { 
					allExist = false;
					
					if (!selectorsForRules.containsKey(rule)) {
						selectorsForRules.put(rule, Lists.newArrayList());
					}
					List<String> selectorsForRule = selectorsForRules.get(rule);
					
					selectorsForRule.add(selector);
					addRule(selector, rule);
				}
			}
		}
		
		if (allExist) {
			return;
		} else if (noneExist) {
			styleBlocks.add(block);
			return;
		} else {
			combineAndAddRules(selectorsForRules);
		}
	}

	void combineAndAddRules(Map<CSSRule, List<String>> selectorsForRules) {
		while (!selectorsForRules.isEmpty()) {
			List<String> nextSelectors = selectorsForRules.entrySet().iterator().next().getValue();
			
			List<CSSRule> matchingRules = Lists.newArrayList();
			for (Entry<CSSRule, List<String>> e : selectorsForRules.entrySet()) {
				if (e.getValue().equals(nextSelectors)) {
					matchingRules.add(e.getKey());
				}
			}
			
			// remove matches from remaining
			for (CSSRule rule : matchingRules) {
				selectorsForRules.remove(rule);
			}

			CSSStyleBlock newBlock = new CSSStyleBlock(
				Lists.<String>newArrayList(nextSelectors), 
				Lists.<CSSRule>newArrayList(matchingRules));
			nextSelectors.forEach(newBlock::addSelector);
			matchingRules.forEach(newBlock::addRule);
			
			styleBlocks.add(newBlock);
		}
	}

	private void addRule(String selector, CSSRule rule) {
		if (!rulesAdded.containsKey(selector)) {
			rulesAdded.put(selector, Maps.newHashMap());
		}		
		Map<String, String> rulesForSelector = rulesAdded.get(selector);
		
		if (rulesForSelector.containsKey(rule)) {
			throw new IllegalStateException("Overwriting rule for selector " + selector + ": " + rule.getName());
		} else {
			rulesForSelector.put(rule.getName(), rule.getArguments());
		}
	}

	private void assertMatchesExistingRule(String selector, CSSRule rule) {
		String existingArguments = rulesAdded.get(selector).get(rule.getName());
		String newArguments = rule.getArguments();
		if (!existingArguments.equals(newArguments)) {
			throw new IllegalStateException("New rule for " + selector + " " + rule.getName() + ": " + newArguments
				+ " would overwrite existing rule: " + existingArguments);
		}
	}

	private boolean ruleExists(String selector, CSSRule rule) {
		if (!rulesAdded.containsKey(selector)) {
			return false;
		}
		
		Map<String, String> rulesForSelector = rulesAdded.get(selector);
		return rulesForSelector.containsKey(rule.getName());
	}
}
