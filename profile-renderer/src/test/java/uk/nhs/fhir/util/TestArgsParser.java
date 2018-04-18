package uk.nhs.fhir.util;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import uk.nhs.fhir.render.RendererCliArgs;
import uk.nhs.fhir.render.RendererCliArgsParser;

public class TestArgsParser {
	@Test
	public void testNullForZeroArgs() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();
		
		String[] args = new String[] {};
		
		Assert.assertEquals(null, rendererCliArgsParser.parseArgs(args));
	}

	@Test
	public void testNullForOneArg() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();
		
		String[] args = new String[] {"my_input"};
		
		Assert.assertEquals(null, rendererCliArgsParser.parseArgs(args));
	}

	@Test
	public void testContentsForTwoArgs() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();

		String[] args = new String[] {"my_input", "my_output"};
		
		RendererCliArgs parsedArgs = rendererCliArgsParser.parseArgs(args);
		Assert.assertEquals(Paths.get("my_input"), parsedArgs.getInputDir());
		Assert.assertEquals(Paths.get("my_output"), parsedArgs.getOutputDir());
		Assert.assertEquals(Optional.empty(), parsedArgs.getNewBaseUrl());
		Assert.assertEquals(Optional.empty(), parsedArgs.getLocalDomains());
	}

	@Test
	public void testNullForUnrecognisedFlag() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();

		String unrecognisedFlag = "-a";
		String[] args = new String[] {"my_input", "my_output", unrecognisedFlag};
		
		Assert.assertEquals(null, rendererCliArgsParser.parseArgs(args));
	}
	
	@Test
	public void testNullForUnrecognisedLabel() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();

		String unrecognisedLabel = "--aa";
		String[] args = new String[] {"my_input", "my_output", unrecognisedLabel};
		
		Assert.assertEquals(null, rendererCliArgsParser.parseArgs(args));
	}
	
	@Test
	public void testParseFlag() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();

		String baseUrlLabel = "--base-url";
		String[] args = new String[] {"my_input", "my_output", baseUrlLabel, "my_base_url"};
		
		RendererCliArgs parsedArgs = rendererCliArgsParser.parseArgs(args);
		Assert.assertEquals(Optional.of("my_base_url"), parsedArgs.getNewBaseUrl());
	}
	
	@Test
	public void testParseLabel() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();

		String allowedMissingExtensionPrefix = "-p";
		String[] args = new String[] {"my_input", "my_output", allowedMissingExtensionPrefix, "my_prefix"};
		
		RendererCliArgs parsedArgs = rendererCliArgsParser.parseArgs(args);
		Assert.assertEquals(Optional.of(Sets.newHashSet("my_prefix")), parsedArgs.getAllowedMissingExtensionPrefixes());
	}
	
	@Test
	public void testParseLabelAndFlag() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();
		
		String allowedMissingExtensionPrefix = "-p";
		String baseUrlLabel = "--base-url";
		String[] args = new String[] {"my_input", "my_output", allowedMissingExtensionPrefix, "my_prefix", baseUrlLabel, "my_base_url"};
		
		RendererCliArgs parsedArgs = rendererCliArgsParser.parseArgs(args);
		Assert.assertEquals(Optional.of(Sets.newHashSet("my_prefix")), parsedArgs.getAllowedMissingExtensionPrefixes());
		Assert.assertEquals(Optional.of("my_base_url"), parsedArgs.getNewBaseUrl());
	}
	
	@Test
	public void testParseStringSetArg() {
		RendererCliArgsParser rendererCliArgsParser = new RendererCliArgsParser();
		
		String localDomains = "-l";
		String[] args = new String[] {"my_input", "my_output", localDomains, "domain1;http://some.domain;https://my.other.domain"};
		RendererCliArgs parsedArgs = rendererCliArgsParser.parseArgs(args);
		
		Set<String> expected = Sets.newHashSet("domain1", "http://some.domain", "https://my.other.domain");
		Assert.assertEquals(Optional.of(expected), parsedArgs.getLocalDomains());
	}
}
