package uk.nhs.fhir.render;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import uk.nhs.fhir.util.RendererCliArg;
import uk.nhs.fhir.util.cli.CliArgSpec;
import uk.nhs.fhir.util.cli.CliArgSpecBuilder;
import uk.nhs.fhir.util.cli.CliArgsParser;
import uk.nhs.fhir.util.cli.CliFlag;
import uk.nhs.fhir.util.cli.CliPathArg;
import uk.nhs.fhir.util.cli.CliStringArg;
import uk.nhs.fhir.util.cli.CliStringSetArg;
import uk.nhs.fhir.util.cli.ParsedArgs;

public class RendererCliArgsParser extends CliArgsParser<RendererCliArgs> {
	
	private static final CliPathArg ARG_INPUT = RendererCliArg.ARG_INPUT;
	private static final CliPathArg ARG_OUTPUT = RendererCliArg.ARG_OUTPUT;
	private static final CliStringArg ARG_NEW_PATH = RendererCliArg.ARG_NEW_PATH;
	private static final CliStringSetArg ARG_MISSING_EXT = RendererCliArg.ARG_MISSING_EXT;
	private static final CliStringSetArg ARG_LOCAL_DOMAINS = RendererCliArg.ARG_LOCAL_DOMAINS;
	private static final CliPathArg ARG_HTTP_CACHE = RendererCliArg.ARG_HTTP_CACHE;
	private static final CliFlag FLAG_COPY_ON_ERROR = RendererCliArg.FLAG_COPY_ON_ERROR;
	
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
			.booleanFlag()
				.addFlag(FLAG_COPY_ON_ERROR)
			.build();
	
	public RendererCliArgsParser() {
		super(RENDERER_ARG_SPEC);
	}

	protected RendererCliArgs extractArgs(ParsedArgs parsedArgs) {
		Path inputDir = parsedArgs.get(ARG_INPUT);
		Path outputDir = parsedArgs.get(ARG_OUTPUT);
        Optional<Set<String>> allowedMissingExtensionPrefixes = parsedArgs.get(ARG_MISSING_EXT);
        Optional<Path> httpCacheDirectory = Optional.ofNullable(parsedArgs.get(ARG_HTTP_CACHE));
        Optional<String> newBaseUrl = Optional.ofNullable(parsedArgs.get(ARG_NEW_PATH));
        Optional<Set<String>> localDomains = parsedArgs.get(ARG_LOCAL_DOMAINS);
        boolean copyOnError = parsedArgs.get(FLAG_COPY_ON_ERROR);
		
		return new RendererCliArgs(inputDir, outputDir, newBaseUrl,
							allowedMissingExtensionPrefixes, httpCacheDirectory,
							localDomains, copyOnError);
	}
}
