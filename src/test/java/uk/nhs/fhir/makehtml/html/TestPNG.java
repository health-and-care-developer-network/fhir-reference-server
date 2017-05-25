package uk.nhs.fhir.makehtml.html;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;

public class TestPNG {
	
	@Ignore
	@Test
	public void testWritePNG() {
		TablePNGGenerator png = new TablePNGGenerator();
		String base64 = png.getBase64(Style.DOTTED, new boolean[]{true, false});
		System.out.println(base64);
	}
	
	@Ignore
	@Test
	public void displayGenerated() throws ParserConfigurationException, IOException {
		// one of the backgrounds from the HL7 FHIR website
		//String base64Input = "iVBORw0KGgoAAAANSUhEUgAAAyAAAAACCAYAAACg/LjIAAAAMElEQVR42u3QwQkAMAwDsezq/WdoskKgFAoy6HkfV5LamJ1tc7MHAAD+5QQAAOCZBkurQFbnaRSlAAAAAElFTkSuQmCC";

		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		TablePNGGenerator pngGenerator = new TablePNGGenerator();
		
		/*
		 * Simple 1-line background, with dotted style matching website
		 */
		addDemoBackgroundSection(doc, "dotted-1line", pngGenerator.getBase64(Style.DOTTED, new boolean[]{true}));
		
		/*
		 * Two lines, using dashed style
		 */
		addDemoBackgroundSection(doc, "dashed-2line", pngGenerator.getBase64(Style.DASHED, new boolean[]{true, true}));
		
		/*
		 * More complicated line layout, with solid style
		 */
		addDemoBackgroundSection(doc, "solid-3line", pngGenerator.getBase64(Style.SOLID, new boolean[]{true, false, true, true}));
		
		/*
		 * Test trailing 'false' 
		 */
		addDemoBackgroundSection(doc, "dashed-2line-trailingfalse", pngGenerator.getBase64(Style.DOTTED, new boolean[]{true, false, true, false}));
		
		Document html = doc.getHTML();
		String htmlString = HTMLUtil.docToString(html, true, false);
		System.out.println(htmlString);
	}

	/**
	 * Outputs some HTML which can be pasted into a .html file and viewed in a browser to test output
	 */
	private void addDemoBackgroundSection(SectionedHTMLDoc doc, String desc, String base64Input) throws ParserConfigurationException, IOException {
		doc.addStyle(new CSSStyleBlock(Lists.newArrayList(".demo_" + desc), Lists.newArrayList(
			new CSSRule("background-image", "url(data:image/png;base64," + base64Input + ")"),
			new CSSRule("background-repeat", "repeat-y"),
			new CSSRule("height", "80"),
			new CSSRule("width", "200"),
			new CSSRule("clear", "both")
			)));
		doc.addBodyElement(
			Elements.withAttributeAndText("div",
				new Attribute("class", "demo_" + desc),
				"\u00A0"));
	}
}
