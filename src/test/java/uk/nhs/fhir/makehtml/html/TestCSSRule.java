package uk.nhs.fhir.makehtml.html;

import org.junit.Test;

import junit.framework.Assert;
import uk.nhs.fhir.render.html.style.CSSRule;

public class TestCSSRule {
	@Test
	public void testWriteCSSRule() {
		Assert.assertEquals("test1: test2", new CSSRule("test1", "test2").toFormattedString());
	}
}
