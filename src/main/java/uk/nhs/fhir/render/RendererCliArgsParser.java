package uk.nhs.fhir.render;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

public class RendererCliArgsParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(RendererCliArgsParser.class);
	
	public static final CliPathArg ARG_INPUT = new CliPathArg("input", "Specification source directory");
	public static final CliPathArg ARG_OUTPUT = new CliPathArg("output", "Rendered artefact destination directory");
	public static final CliStringArg ARG_MISSING_EXT = new CliStringArg("extprefix", 
		"If an extension with this prefix is depended on but unavailable, the renderer will try to continue rendering without it",
		"missing-ext-prefix", "p");
	public static final CliStringSetArg ARG_LOCAL_DOMAINS = new CliStringSetArg("localdomain", 
		"Local domains (for resources hosted on this FHIR server)", "local-domains", "l");
	
	public RendererCliArgs parseArgs(String[] args) {
		RendererArgSpec argSpec = getArgSpec();
		ParsedArgs parsedArgs;
		try {
			 parsedArgs = argSpec.parseArgs(args);
		} catch (ArgParsingFailed e) {
			return error(e.getMessage());
		}
		
		Path inputDir = parsedArgs.get(ARG_INPUT);
		Path outputDir = parsedArgs.get(ARG_OUTPUT);
        Optional<String> newBaseURL = Optional.ofNullable(parsedArgs.get(ARG_MISSING_EXT));
        if (newBaseURL.isPresent()) {
        	LOG.info("Using new base URL: " + newBaseURL.get());
        }
        Optional<Set<String>> localDomains = Optional.ofNullable(parsedArgs.get(ARG_LOCAL_DOMAINS));
		
		return new RendererCliArgs(inputDir, outputDir, newBaseURL, localDomains);
	}
	
	private RendererArgSpec getArgSpec() {
		return new RendererArgSpecBuilder()
			.requiredPositional()
				.addArg(ARG_INPUT)
				.addArg(ARG_OUTPUT)
			.optionalFlagged()
				.addArg(ARG_MISSING_EXT)
				.build();
	}

	private RendererCliArgs error(String message) {
		logUsage(message);
		return null;
	}

	private void logUsage(String message) {
		
	}
}

class RendererArgSpecBuilder {
	
	private enum RendererArgSpecBuilderState {
		NEW,
		REQUIRED_POSITIONAL,
		OPTIONAL_POSITIONAL,
		OPTIONAL_FLAGGED
	}
	
	private RendererArgSpecBuilderState state = RendererArgSpecBuilderState.NEW;
	
	private final List<RendererCliArg<?>> requiredPositionalArgs = Lists.newArrayList();
	private final List<RendererCliArg<?>> optionalPositionalArgs = Lists.newArrayList();
	private final List<RendererCliArg<?>> optionalFlaggedArgs = Lists.newArrayList();
	
	public RendererArgSpecBuilder requiredPositional() {
		setState(RendererArgSpecBuilderState.REQUIRED_POSITIONAL);
		return this;
	}
	
	public RendererArgSpecBuilder optionalPositional() {
		setState(RendererArgSpecBuilderState.OPTIONAL_POSITIONAL);
		return this;
	}
	
	public RendererArgSpecBuilder optionalFlagged() {
		setState(RendererArgSpecBuilderState.OPTIONAL_FLAGGED);
		return this;
	}
	
	private RendererArgSpecBuilder setState(RendererArgSpecBuilderState state) {
		this.state = state;
		return this;
	}
	
	public RendererArgSpecBuilder addArg(RendererCliArg<?> arg) {
		boolean hasLabelOrFlag = arg.getLabel().isPresent() || arg.getFlag().isPresent();
		
		boolean expectFlags = state.equals(RendererArgSpecBuilderState.OPTIONAL_FLAGGED);
		if (!expectFlags && hasLabelOrFlag) {
			throw new IllegalStateException("Trying to add arg \"" + arg.getDesc() + "\" but wasn't expecting a label/flag in mode " + state.toString());
		} else if (expectFlags && !hasLabelOrFlag) {
			throw new IllegalStateException("Trying to add arg \"" + arg.getDesc() + "\" but expected a label or flag in mode " + state.toString());
		}
		
		switch (state) {
			case REQUIRED_POSITIONAL:
				requiredPositionalArgs.add(arg);
				break;
			case OPTIONAL_POSITIONAL:
				optionalPositionalArgs.add(arg);
				break;
			case OPTIONAL_FLAGGED:
				optionalFlaggedArgs.add(arg);
				break;
			case NEW:
				throw new IllegalStateException("State was " + state.toString() + ". Arg type needs to be specified.");
			default:
				throw new IllegalStateException("Unexpected state: " + state.toString());
		}
		
		return this;
	}
	
	protected RendererArgSpec build() {
		return new RendererArgSpec(requiredPositionalArgs, optionalPositionalArgs, optionalFlaggedArgs);
	}
}

class RendererArgSpec {
	private final List<RendererCliArg<?>> requiredPositionalArgs;
	private final List<RendererCliArg<?>> optionalPositionalArgs;
	private final Map<String, RendererCliArg<?>> optionalLabeledArgs;
	private final Map<String, RendererCliArg<?>> optionalFlaggedArgs;
	
	protected RendererArgSpec(List<RendererCliArg<?>> requiredPositionalArgs, List<RendererCliArg<?>> optionalPositionalArgs, List<RendererCliArg<?>> optionalFlaggedArgs) {
		this.requiredPositionalArgs = ImmutableList.copyOf(requiredPositionalArgs);
		this.optionalPositionalArgs = ImmutableList.copyOf(optionalPositionalArgs);
		this.optionalFlaggedArgs = 
				optionalFlaggedArgs.stream()
					.filter(arg -> arg.getFlag().isPresent())
					.collect(Collectors.toMap(
						arg -> arg.getFlag().get(), 
						arg -> arg));
		this.optionalLabeledArgs = 
				optionalFlaggedArgs.stream()
					.filter(arg -> arg.getLabel().isPresent())
					.collect(Collectors.toMap(
						arg -> arg.getLabel().get(), 
						arg -> arg));
	}
	
	protected ParsedArgs parseArgs(String... args) {
		if (args.length < requiredPositionalArgs.size()) {
			throw new ArgParsingFailed("Expected at least " + requiredPositionalArgs.size() + " arguments but found " + args.length);
		}
		
		ParsedArgs parsedArgs = new ParsedArgs();
		
		// find required positional args
		int ix = 0;
		for (;ix < requiredPositionalArgs.size(); ix++) {
			RendererCliArg<?> argSpec = requiredPositionalArgs.get(ix);
			
			String arg = args[ix];
			if (isRecognisedFlag(arg)) {
				throw new IllegalStateException("Expected required positional arg " + argSpec.getId() + " but found recognised flag \"" + arg + "\"");
			}
			
			argSpec.validate(arg);
			parsedArgs.put(argSpec, arg);
		}
		
		// find optional positional args until we find a recognised label or flag
		int optionalPositionalIx = 0;
		while (ix < args.length
		  && optionalPositionalIx < optionalPositionalArgs.size()) {
			String arg = args[ix];
			RendererCliArg<?> argSpec = optionalPositionalArgs.get(optionalPositionalIx);
			if (isRecognisedFlag(arg)) {
				// don't increment ix
				break;
			}

			argSpec.validate(arg);
			parsedArgs.put(argSpec, arg);
			
			ix++;
			optionalPositionalIx++;
		}
		
		// resolve any labels or flags until we have consumed all arguments
		
		
		return parsedArgs;
	}

	private boolean isRecognisedFlag(String arg) {
		Stream<RendererCliArg<?>> argsStream = 
			Streams.concat(
				requiredPositionalArgs.stream(), 
				optionalPositionalArgs.stream(), 
				optionalFlaggedArgs.values().stream(),
				optionalLabeledArgs.values().stream());
		
		Optional<String> argForComparison = Optional.of(arg);
		
		if (arg.startsWith("--")) {
			return argsStream.anyMatch(argSpec -> argSpec.getLabel().equals(argForComparison));
		} else if (arg.startsWith("-")) {
			return argsStream.anyMatch(argSpec -> argSpec.getFlag().equals(argForComparison));
		} else {
			return false;
		}
	}
}

class ParsedArgs {
	private Map<RendererCliArg<?>, String> map = Maps.newHashMap();
	
	public void put(RendererCliArg<?> argSpec, String arg) {
		map.put(argSpec, arg);
	}
	
	public <T> T get(RendererCliArg<T> argSpec) {
		if (map.containsKey(argSpec)) {
			String arg = map.get(argSpec);
			return argSpec.convert(arg);
		} else {
			return null;
		}
	}
}

abstract class RendererCliArg<T> {
	
	public abstract T convert(String arg);

	private final String id;
	private final String desc;
	private final Optional<String> label;
	private final Optional<String> flag;

	public RendererCliArg(String id, String desc) {
		this(id, desc, Optional.empty(), Optional.empty());
	}
	
	public RendererCliArg(String id, String desc, Optional<String> label, Optional<String> flag) {
		this.id = id;
		this.desc = desc;
		this.label = validateLabel(label);
		this.flag = validateFlag(flag);
	}
	
	public void validate(String arg) throws ArgParsingFailed{
		try {
			convert(arg);
		} catch (Exception e) {
			throw new ArgParsingFailed("Arg for type " + getClass().getSimpleName() + " failed to convert: \"" + arg + "\"");
		}
	}

	private Optional<String> validateFlag(Optional<String> flag) {
		if (flag.isPresent()) {
			String flagString = flag.get();
			check(flagString.length() == 1, "Flag may only have a single character: \"-" + flagString + "\"");
			check(Character.isAlphabetic(flagString.charAt(1)), "Flag must have an alphabetic character following the hyphen: \"-" + flagString + "\"");
		}
		
		return flag;
	}

	private void check(boolean b, String errorMessage) {
		if (!b) {
			throw new InvalidConfiguration(errorMessage);
		}
	}

	private Optional<String> validateLabel(Optional<String> label) {
		if (label.isPresent()) {
			String labelString = label.get();
			check(!labelString.startsWith("-"), "Labels cannot start with a hypen: \"--" + labelString + "\"");
			check(!labelString.contains(" "), "Labels cannot contain space characters: \"--" + labelString + "\"");
			check(labelString.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '-' || c == '_'), "Labels must conform to [A-Za-z0-9-_]+ : \"--" + labelString + "\"");
		}
		
		return label;
	}
	
	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public Optional<String> getLabel() {
		return label;
	}
	
	public Optional<String> getFlag() {
		return flag;
	}
}

class CliPathArg extends RendererCliArg<Path> {
	public CliPathArg(String id, String desc) {
		super(id, desc);
	}
	public CliPathArg(String id, String desc, Optional<String> label, Optional<String> flag) {
		super(id, desc, label, flag);
	}
	
	@Override
	public Path convert(String arg) {
		return Paths.get(arg);
	}	
}

class CliStringArg extends RendererCliArg<String> {
	public CliStringArg(String id, String desc) {
		super(id, desc);
	}
	public CliStringArg(String id, String desc, String label, String flag) {
		super(id, desc, Optional.ofNullable(label), Optional.ofNullable(flag));
	}
	
	@Override
	public String convert(String arg) {
		return arg;
	}
}

class CliStringSetArg extends RendererCliArg<Set<String>> {
	public CliStringSetArg(String id, String desc) {
		super(id, desc);
	}
	public CliStringSetArg(String id, String desc, String label, String flag) {
		super(id, desc, Optional.ofNullable(label), Optional.ofNullable(flag));
	}
	
	@Override
	public Set<String> convert(String arg) {
		Set<String> strings = Sets.newHashSet();
		
		for (String fragment : arg.split(";")) {
			String trimmedFragment = fragment.trim();
			if (!trimmedFragment.isEmpty()) {
				strings.add(fragment);
			}
		}
		
		return strings;
	}
}