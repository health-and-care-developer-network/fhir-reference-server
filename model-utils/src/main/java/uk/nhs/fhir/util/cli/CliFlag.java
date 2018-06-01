package uk.nhs.fhir.util.cli;

import java.util.Optional;

public class CliFlag extends CliConfig {

	public CliFlag(String id, String desc, String label, String flag) {
		super(id, desc, Optional.of(label), Optional.of(flag));
	}
}
