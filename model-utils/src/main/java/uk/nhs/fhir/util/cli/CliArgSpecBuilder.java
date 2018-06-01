package uk.nhs.fhir.util.cli;

import java.util.List;

import com.google.common.collect.Lists;

public class CliArgSpecBuilder {
	
	private enum ArgSpecBuilderState {
		NEW,
		REQUIRED_POSITIONAL,
		OPTIONAL_POSITIONAL,
		OPTIONAL_FLAGGED
	}
	
	private ArgSpecBuilderState state = ArgSpecBuilderState.NEW;
	
	private final List<CliArg<?>> requiredPositionalArgs = Lists.newArrayList();
	private final List<CliArg<?>> optionalPositionalArgs = Lists.newArrayList();
	private final List<CliArg<?>> optionalFlaggedArgs = Lists.newArrayList();
	
	public CliArgSpecBuilder requiredPositional() {
		setState(ArgSpecBuilderState.REQUIRED_POSITIONAL);
		return this;
	}
	
	public CliArgSpecBuilder optionalPositional() {
		setState(ArgSpecBuilderState.OPTIONAL_POSITIONAL);
		return this;
	}
	
	public CliArgSpecBuilder optionalFlagged() {
		setState(ArgSpecBuilderState.OPTIONAL_FLAGGED);
		return this;
	}
	
	private CliArgSpecBuilder setState(ArgSpecBuilderState state) {
		this.state = state;
		return this;
	}
	
	public CliArgSpecBuilder addArg(CliArg<?> arg) {
		boolean hasLabelOrFlag = arg.getLabel().isPresent() || arg.getFlag().isPresent();
		
		boolean expectFlags = state.equals(ArgSpecBuilderState.OPTIONAL_FLAGGED);
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
	
	public CliArgSpec build() {
		return new CliArgSpec(requiredPositionalArgs, optionalPositionalArgs, optionalFlaggedArgs);
	}
}