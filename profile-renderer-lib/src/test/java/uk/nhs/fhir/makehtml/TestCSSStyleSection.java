package uk.nhs.fhir.makehtml;

import org.junit.Test;
import org.junit.Assert;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSStyleSection;

public class TestCSSStyleSection {

	@Test
	public void testAddRule() {
		CSSStyleSection s1 = new CSSStyleSection();
		s1.addBlock(
			new CSSStyleBlock(
				Lists.newArrayList("a"), 
				Lists.newArrayList(new CSSRule("rule1", "args1"))));
		
		Assert.assertEquals(1, s1.getBlocks().size());
		
		CSSStyleBlock cssStyleBlock = s1.getBlocks().get(0);
		Assert.assertEquals("a", cssStyleBlock.getSelectors().get(0));
		
		Assert.assertEquals(1, cssStyleBlock.getRules().size());
		
		CSSRule cssRule = cssStyleBlock.getRules().get(0);
		Assert.assertEquals("rule1", cssRule.getName());
		Assert.assertEquals("args1", cssRule.getArguments());
	}
	
	@Test
	public void testAddDuplicateRule() {
		CSSStyleSection s1 = new CSSStyleSection();
		
		s1.addBlock(
				new CSSStyleBlock(
					Lists.newArrayList("a"), 
					Lists.newArrayList(new CSSRule("rule1", "args1"))));
		
		s1.addBlock(
				new CSSStyleBlock(
					Lists.newArrayList("a"), 
					Lists.newArrayList(new CSSRule("rule1", "args1"))));
		
		Assert.assertEquals(1, s1.getBlocks().size());
		
		CSSStyleBlock cssStyleBlock = s1.getBlocks().get(0);
		Assert.assertEquals("a", cssStyleBlock.getSelectors().get(0));
		
		Assert.assertEquals(1, cssStyleBlock.getRules().size());
		
		CSSRule cssRule = cssStyleBlock.getRules().get(0);
		Assert.assertEquals("rule1", cssRule.getName());
		Assert.assertEquals("args1", cssRule.getArguments());
	}
	
	@Test
	public void testSplitsRuleToAvoidDuplicate() {
		CSSStyleSection s1 = new CSSStyleSection();
		
		s1.addBlock(
				new CSSStyleBlock(
					Lists.newArrayList("a"), 
					Lists.newArrayList(new CSSRule("rule1", "args1"))));
		
		CSSStyleBlock block2 = new CSSStyleBlock(
			Lists.newArrayList("a", "b"),
			Lists.newArrayList(
				new CSSRule("rule1", "args1"),
				new CSSRule("rule2", "args2")));
		s1.addBlock(block2);
		
		Assert.assertEquals(3, s1.getBlocks().size());

		CSSStyleBlock cssStyleBlock1 = s1.getBlocks().get(0);
		Assert.assertEquals(1, cssStyleBlock1.getRules().size());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testThrowsForNonMatchingRule() {
		CSSStyleSection s1 = new CSSStyleSection();
		
		s1.addBlock(
			new CSSStyleBlock(
				Lists.newArrayList("a"), 
				Lists.newArrayList(new CSSRule("rule1", "args1"))));
		
		s1.addBlock(
			new CSSStyleBlock(
				Lists.newArrayList("a"), 
				Lists.newArrayList(new CSSRule("rule1", "args2"))));
	}
}
