package uk.nhs.fhir.util.cli;

import java.util.Optional;

public class CliConfig {

	private final String id;
	private final String desc;
	private final Optional<String> label;
	private final Optional<String> flag;
	
	public CliConfig(String id, String desc, Optional<String> label, Optional<String> flag) throws InvalidConfiguration {
		this.id = id;
		this.desc = desc;
		this.label = validateLabel(label).map(l -> "--" + l);
		this.flag = validateFlag(flag).map(f -> "-" + f);
	}

	private Optional<String> validateFlag(Optional<String> flag) throws InvalidConfiguration {
		if (flag.isPresent()) {
			String flagString = flag.get();
			check(flagString.length() == 1, "Flag may only have a single character: \"-" + flagString + "\"");
			check(Character.isAlphabetic(flagString.charAt(0)), "Flag must have an alphabetic character following the hyphen: \"-" + flagString + "\"");
		}
		
		return flag;
	}

	private Optional<String> validateLabel(Optional<String> label) throws InvalidConfiguration {
		if (label.isPresent()) {
			String labelString = label.get();
			check(!labelString.startsWith("-"), "Labels cannot start with a hypen: \"--" + labelString + "\"");
			check(!labelString.contains(" "), "Labels cannot contain space characters: \"--" + labelString + "\"");
			check(labelString.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '-' || c == '_'), "Labels must conform to [A-Za-z0-9-_]+ : \"--" + labelString + "\"");
		}
		
		return label;
	}

	private void check(boolean b, String errorMessage) throws InvalidConfiguration {
		if (!b) {
			throw new InvalidConfiguration(errorMessage);
		}
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