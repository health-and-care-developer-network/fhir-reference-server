package uk.nhs.fhir.makehtml.fmt;

import org.junit.Test;

import junit.framework.Assert;

public class TestCSSRule {
	@Test
	public void testWriteCSSRule() {
		Assert.assertEquals("test1: test2", new CSSRule("test1", "test2").toFormattedString());
	}
}
