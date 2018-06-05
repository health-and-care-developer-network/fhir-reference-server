package uk.nhs.fhir.server_renderer;

import java.util.Optional;

import uk.nhs.fhir.util.RendererCliArg;
import uk.nhs.fhir.util.cli.CliArgSpec;
import uk.nhs.fhir.util.cli.CliArgSpecBuilder;
import uk.nhs.fhir.util.cli.CliArgsParser;
import uk.nhs.fhir.util.cli.CliFlag;
import uk.nhs.fhir.util.cli.CliStringSetArg;
import uk.nhs.fhir.util.cli.ParsedArgs;

public class ServerRendererCliArgsParser extends CliArgsParser<ServerRendererArgs> {
	
	private static final CliStringSetArg ARG_MISSING_EXT = RendererCliArg.ARG_MISSING_EXT;
	private static final CliStringSetArg ARG_LOCAL_DOMAINS = RendererCliArg.ARG_LOCAL_DOMAINS;
	private static final CliFlag ARG_LARGE_TEXT = new CliFlag("large-text", "enlarge windows and text for high density displays", "large-text", "t");
	
	private static final CliArgSpec SERVER_RENDERER_ARGS =
		new CliArgSpecBuilder()
			.optionalFlagged()
				.addArg(ARG_MISSING_EXT)
				.addArg(ARG_LOCAL_DOMAINS)
			.booleanFlag()
				.addFlag(ARG_LARGE_TEXT)
			.build();
	
	public ServerRendererCliArgsParser() {
		super(SERVER_RENDERER_ARGS);
	}

	@Override
	protected ServerRendererArgs extractArgs(ParsedArgs parsedArgs) {
		return new ServerRendererArgs(
			parsedArgs.get(ARG_LARGE_TEXT),
			Optional.ofNullable(parsedArgs.get(ARG_LOCAL_DOMAINS)), 
			Optional.ofNullable(parsedArgs.get(ARG_MISSING_EXT)));
	}

}
