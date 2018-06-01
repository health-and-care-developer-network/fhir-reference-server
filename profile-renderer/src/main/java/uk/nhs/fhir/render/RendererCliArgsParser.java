package uk.nhs.fhir.render;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import uk.nhs.fhir.util.cli.CliArgSpec;
import uk.nhs.fhir.util.cli.CliArgSpecBuilder;
import uk.nhs.fhir.util.cli.CliArgsParser;
import uk.nhs.fhir.util.cli.CliPathArg;
import uk.nhs.fhir.util.cli.CliStringArg;
import uk.nhs.fhir.util.cli.CliStringSetArg;
import uk.nhs.fhir.util.cli.ParsedArgs;

public class RendererCliArgsParser extends CliArgsParser<RendererCliArgs> {
	
	public static final CliPathArg ARG_INPUT = new CliPathArg("input-dir", "Specification source directory");
	public static final CliPathArg ARG_OUTPUT = new CliPathArg("output-dir", "Rendered artefact destination directory");
	public static final CliStringArg ARG_NEW_PATH = new CliStringArg("newbaseurl", "New base URL for resources", "base-url", "b");
	public static final CliStringSetArg ARG_MISSING_EXT = new CliStringSetArg("extprefix", 
		"If an extension with this prefix is depended on but unavailable, the renderer will try to continue rendering without it",
		"missing-ext-prefix", "p");
	public static final CliStringSetArg ARG_LOCAL_DOMAINS = new CliStringSetArg("localdomain", 
		"Local domains (for resources hosted on this FHIR server)", "local-domains", "l");
	public static final CliPathArg ARG_HTTP_CACHE = new CliPathArg("httpcache", 
		"Directory to use to hold the HTTP cache, used when retrieving Git history (to avoid unecessary calls to Git)", "http-cache", "c");
	
	private static final CliArgSpec RENDERER_ARG_SPEC = 
		new CliArgSpecBuilder()
			.requiredPositional()
			.addArg(ARG_INPUT)
			.addArg(ARG_OUTPUT)
		.optionalFlagged()
			.addArg(ARG_NEW_PATH)
			.addArg(ARG_MISSING_EXT)
			.addArg(ARG_LOCAL_DOMAINS)
			.addArg(ARG_HTTP_CACHE)
			.build();
	
	public RendererCliArgsParser() {
		super(RENDERER_ARG_SPEC);
	}

	protected RendererCliArgs extractArgs(ParsedArgs parsedArgs) {
		Path inputDir = parsedArgs.get(ARG_INPUT);
		Path outputDir = parsedArgs.get(ARG_OUTPUT);
        Optional<Set<String>> allowedMissingExtensionPrefixes = Optional.ofNullable(parsedArgs.get(ARG_MISSING_EXT));
        Optional<String> newBaseUrl = Optional.ofNullable(parsedArgs.get(ARG_NEW_PATH));
        Optional<Set<String>> localDomains = Optional.ofNullable(parsedArgs.get(ARG_LOCAL_DOMAINS));
		
        Optional<Path> httpCacheDirectory = Optional.ofNullable(parsedArgs.get(ARG_HTTP_CACHE));
        
		return new RendererCliArgs(inputDir, outputDir, newBaseUrl,
							allowedMissingExtensionPrefixes, httpCacheDirectory,
							localDomains);
	}
}