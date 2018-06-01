package uk.nhs.fhir.util.cli;

import java.util.Optional;

public class CliStringArg extends CliArg<String> {
	public CliStringArg(String id, String desc) {
		super(id, desc);
	}
	public CliStringArg(String id, String desc, String label, String flag) throws InvalidConfiguration {
		super(id, desc, Optional.ofNullable(label), Optional.ofNullable(flag));
	}
	
	@Override
	public String convert(String arg) {
		return arg;
	}
	
	@Override
	public String example() {
		return "argString";
	}
}
