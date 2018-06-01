package uk.nhs.fhir.util.cli;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

public class CliArgSpec {
	private final List<CliArg<?>> requiredPositionalArgs;
	private final List<CliArg<?>> optionalPositionalArgs;
	private final List<CliArg<?>> optionalArgs;
	private final List<CliFlag> booleanFlags;
	
	protected CliArgSpec(
			List<CliArg<?>> requiredPositionalArgs,
			List<CliArg<?>> optionalPositionalArgs,
			List<CliArg<?>> optionalArgs,
			List<CliFlag> booleanFlags) {
		this.requiredPositionalArgs = ImmutableList.copyOf(requiredPositionalArgs);
		this.optionalPositionalArgs = ImmutableList.copyOf(optionalPositionalArgs);
		this.optionalArgs = ImmutableList.copyOf(optionalArgs);
		this.booleanFlags = booleanFlags;
	}
	
	private Map<String, CliConfig> getOptionalFlaggedArgs() {
		return Streams.concat(optionalArgs.stream(), booleanFlags.stream())
			.filter(arg -> arg.getFlag().isPresent())
			.collect(Collectors.toMap(
				arg -> arg.getFlag().get(), 
				arg -> arg));
	}
	
	private Map<String, CliConfig> getOptionalLabeledArgs() {
		return Streams.concat(optionalArgs.stream(), booleanFlags.stream())
			.filter(arg -> arg.getLabel().isPresent())
			.collect(Collectors.toMap(
				arg -> arg.getLabel().get(), 
				arg -> arg));
	}

	String getUsage() {
		StringBuilder usage = new StringBuilder();
		usage.append("Usage: \n");
		usage.append("cmd");
		for (CliArg<?> requiredPositionalArg : requiredPositionalArgs) {
			usage.append(" ").append(requiredPositionalArg.getId());
		}
		
		for (CliArg<?> optionalPositionalArg : optionalPositionalArgs) {
			usage.append(" [").append(optionalPositionalArg.getId()).append("]");
		}
		
		for (CliArg<?> optionalArg : optionalArgs) {
			usage.append(" [");
			
			if (optionalArg.getFlag().isPresent()) {
				usage.append(optionalArg.getFlag().get());
			}
			if (optionalArg.getLabel().isPresent() && optionalArg.getFlag().isPresent()) {
				usage.append("|");
			}
			if (optionalArg.getLabel().isPresent()) {
				usage.append(optionalArg.getLabel().get());
			}
			
			usage.append(" ").append(optionalArg.example());
			usage.append("]");
		}
		
		usage.append("\n\n");
		
		int tabWidth = 40;
		
		for (CliArg<?> requiredPositionalArg : requiredPositionalArgs) {
			String id = requiredPositionalArg.getId();
			usage.append(id);
			usage.append(nSpaces(tabWidth - id.length()));
			usage.append(requiredPositionalArg.getDesc());
			usage.append("\n");
		}
		
		for (CliArg<?> optionalPositionalArg : optionalPositionalArgs) {
			String id = optionalPositionalArg.getId();
			usage.append(id);
			usage.append(nSpaces(tabWidth - id.length()));
			usage.append(optionalPositionalArg.getDesc());
			usage.append("\n");
		}
		
		for (CliArg<?> optionalArg : optionalArgs) {
			StringBuilder optionalArgLabelFlag = new StringBuilder();
			if (optionalArg.getFlag().isPresent()) {
				optionalArgLabelFlag.append(optionalArg.getFlag().get());
			}
			if (optionalArg.getLabel().isPresent() && optionalArg.getFlag().isPresent()) {
				optionalArgLabelFlag.append("|");
			}
			if (optionalArg.getLabel().isPresent()) {
				optionalArgLabelFlag.append(optionalArg.getLabel().get());
			}
			
			usage.append(optionalArgLabelFlag.toString());
			usage.append(nSpaces(tabWidth - optionalArgLabelFlag.toString().length()));
			usage.append(optionalArg.getDesc());
			usage.append("\n");
		}
		
		return usage.toString();
	}
	
	private String nSpaces(int n) {
		StringBuilder spaces = new StringBuilder();
		for (int i=0; i<n; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}

	protected ParsedArgs parseArgs(String... args) throws ArgParsingFailed {
		if (args.length < requiredPositionalArgs.size()) {
			throw new ArgParsingFailed("Expected at least " + requiredPositionalArgs.size() + " arguments but found " + args.length);
		}
		
		ParsedArgs parsedArgs = new ParsedArgs();
		
		// find required positional args
		int ix = 0;
		for (;ix < requiredPositionalArgs.size(); ix++) {
			CliArg<?> argSpec = requiredPositionalArgs.get(ix);
			
			String arg = args[ix];
			if (isRecognisedFlag(arg)) {
				throw new ArgParsingFailed("Expected required positional arg " + argSpec.getId() + " but found recognised flag \"" + arg + "\"");
			}
			
			argSpec.validate(arg);
			parsedArgs.put(argSpec, arg);
		}
		
		// find optional positional args until we find a recognised label or flag
		int optionalPositionalIx = 0;
		while (ix < args.length
		  && optionalPositionalIx < optionalPositionalArgs.size()) {
			String arg = args[ix];
			CliArg<?> argSpec = optionalPositionalArgs.get(optionalPositionalIx);
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
		while (ix < args.length) {
			String arg = args[ix];
			ix++;
			if (arg.startsWith("--")) {
				CliConfig labelSpec = getOptionalLabeledArgs().get(arg);
				if (labelSpec == null) {
					throw new ArgParsingFailed("Unrecognised label " + arg);
				}
				if (labelSpec instanceof CliArg) {
					CliArg<?> argLabelSpec = (CliArg<?>)labelSpec;
					if (ix >= args.length) {
						throw new ArgParsingFailed("Expected a value for label " + arg + " but there was no more input");
					}
					
					String value = args[ix];
					ix++;
					argLabelSpec.validate(value);
					parsedArgs.put(argLabelSpec, value);
				} else if (labelSpec instanceof CliFlag) {
					CliFlag flagLabelSpec = (CliFlag)labelSpec;
					parsedArgs.foundFlag(flagLabelSpec);
				} else {
					throw new IllegalStateException("Unexpected config class: " + labelSpec.getClass().getSimpleName());
				}
				
			} else if (arg.startsWith("-")) {
				Map<String, CliConfig> optionalFlaggedArgs = getOptionalFlaggedArgs();
				CliConfig flagSpec = optionalFlaggedArgs.get(arg);
				if (flagSpec == null) {
					throw new ArgParsingFailed("Unrecognised flag " + arg);
				}
				
				if (flagSpec instanceof CliArg) {
					CliArg<?> argFlagSpec = (CliArg<?>)flagSpec;
					if (ix >= args.length) {
						throw new ArgParsingFailed("Expected a value for flag " + arg + " but there was no more input");
					}
					
					String value = args[ix];
					ix++;
					argFlagSpec.validate(value);
					parsedArgs.put(argFlagSpec, value);
				} else if (flagSpec instanceof CliFlag) {
					CliFlag booleanFlagSpec = (CliFlag)flagSpec;
					parsedArgs.foundFlag(booleanFlagSpec);
				} else {
					throw new IllegalStateException("Unexpected config class: " + flagSpec.getClass().getSimpleName());
				}
			} else {
				throw new ArgParsingFailed("Expected flag or label, found: " + arg);
			}
		}
		
		return parsedArgs;
	}

	private boolean isRecognisedFlag(String arg) {
		Stream<CliConfig> argsStream = 
			Streams.concat(
				requiredPositionalArgs.stream(), 
				optionalPositionalArgs.stream(), 
				optionalArgs.stream(),
				booleanFlags.stream());
		
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
