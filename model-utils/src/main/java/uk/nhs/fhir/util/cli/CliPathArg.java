package uk.nhs.fhir.util.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CliPathArg extends CliArg<Path> {
	public CliPathArg(String id, String desc) {
		super(id, desc);
	}
	public CliPathArg(String id, String desc, String label, String flag) {
		super(id, desc, Optional.ofNullable(label), Optional.ofNullable(flag));
	}
	
	@Override
	public Path convert(String arg) {
		return Paths.get(arg);
	}
	
	@Override
	public String example() {
		return "/path/to/location";
	}
}
