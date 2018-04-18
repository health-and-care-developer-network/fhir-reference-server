package uk.nhs.fhir.makehtml.html;

import org.junit.Test;
import org.junit.Assert;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;

public class TestCSSRule {
	@Test
	public void testWriteCSSRule() {
		Assert.assertEquals("test1: test2", new CSSRule("test1", "test2").toFormattedString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCSSStyleBlockMustHaveNonNullSelectors() {
		new CSSStyleBlock(
			null,
			Lists.newArrayList(new CSSRule("blah", "blah"))
		);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCSSStyleBlockMustHaveNonEmptySelectors() {
		new CSSStyleBlock(
			Lists.newArrayList(),
			Lists.newArrayList(new CSSRule("blah", "blah"))
		);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCSSStyleBlockMustHaveNonNullRules() {
		new CSSStyleBlock(
			Lists.newArrayList(),
			null
		);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCSSStyleBlockMustHaveNonEmptyRules() {
		new CSSStyleBlock(
			Lists.newArrayList("blah"),
			Lists.newArrayList()
		);
	}

	/*
	 * selector {
	 * 	name: value
	 * }
	 */
	@Test
	public void testWriteCSSStyleBlock1Selector1Rule() {
		CSSStyleBlock blockToWrite = new CSSStyleBlock(
			Lists.newArrayList("selector"),
			Lists.newArrayList(new CSSRule("name", "value"))
		);
		
		Assert.assertEquals("selector {\n\tname: value\n}", blockToWrite.toFormattedString());
	}

	/*
	 * selector1,
	 * selector2 {
	 * 	name: value
	 * }
	 */
	@Test
	public void testWriteCSSStyleBlock2Selectors1Rule() {
		CSSStyleBlock blockToWrite = new CSSStyleBlock(
			Lists.newArrayList("selector1", "selector2"),
			Lists.newArrayList(new CSSRule("name", "value"))
		);
		
		Assert.assertEquals("selector1,\nselector2 {\n\tname: value\n}", blockToWrite.toFormattedString());
	}

	/*
	 * selector1,
	 * selector2,
	 * multipart selector,
	 * dotted.selector,
	 * hashed#selector {
	 * 	name: value
	 * }
	 */
	@Test
	public void testWriteCSSStyleBlockManySelectors1Rule() {
		CSSStyleBlock blockToWrite = new CSSStyleBlock(
			Lists.newArrayList("selector1", "selector2", "multipart selector", "dotted.selector", "hashed#selector"),
			Lists.newArrayList(new CSSRule("name", "value"))
		);
		
		Assert.assertEquals("selector1,\nselector2,\nmultipart selector,\ndotted.selector,\nhashed#selector {\n\tname: value\n}", blockToWrite.toFormattedString());
	}

	/*
	 * selector1 {
	 * 	name1: value1;
	 * 	name2: value2
	 * }
	 */
	@Test
	public void testWriteCSSStyleBlock1Selector2Rules() {
		CSSStyleBlock blockToWrite = new CSSStyleBlock(
			Lists.newArrayList("selector1"),
			Lists.newArrayList(new CSSRule("name1", "value1"), new CSSRule("name2", "value2"))
		);
		
		Assert.assertEquals("selector1 {\n\tname1: value1;\n\tname2: value2\n}", blockToWrite.toFormattedString());
	}

	/*
	 * selector1 {
	 * 	name1: value1;
	 * 	name2: value2
	 * }
	 */
	@Test
	public void testWriteCSSStyleBlock1SelectorManyRules() {
		CSSStyleBlock blockToWrite = new CSSStyleBlock(
			Lists.newArrayList("selector1"),
			Lists.newArrayList(new CSSRule("name1", "value1"), new CSSRule("name2", "value2"), new CSSRule("hyphenated-name", "0px 1px 2px 3px"))
		);
		
		Assert.assertEquals("selector1 {\n\tname1: value1;\n\tname2: value2;\n\thyphenated-name: 0px 1px 2px 3px\n}", blockToWrite.toFormattedString());
	}
	
}
