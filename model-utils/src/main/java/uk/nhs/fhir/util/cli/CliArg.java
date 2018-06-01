package uk.nhs.fhir.util.cli;

import java.util.Optional;

public abstract class CliArg<T> extends CliConfig {
	
	public abstract T convert(String arg);
	public abstract String example();

	public CliArg(String id, String desc) {
		this(id, desc, Optional.empty(), Optional.empty());
	}
	
	public CliArg(String id, String desc, Optional<String> label, Optional<String> flag) throws InvalidConfiguration {
		super(id, desc, label, flag);
	}
	
	public void validate(String arg) throws ArgParsingFailed {
		try {
			convert(arg);
		} catch (Exception e) {
			throw new ArgParsingFailed("Arg for type " + getClass().getSimpleName() + " failed to convert: \"" + arg + "\"");
		}
	}
}
