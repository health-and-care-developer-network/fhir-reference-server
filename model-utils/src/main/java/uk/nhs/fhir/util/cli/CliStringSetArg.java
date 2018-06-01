package uk.nhs.fhir.util.cli;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

public class CliStringSetArg extends CliArg<Set<String>> {
	public CliStringSetArg(String id, String desc) {
		super(id, desc);
	}
	public CliStringSetArg(String id, String desc, String label, String flag) throws InvalidConfiguration {
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
	
	@Override
	public String example() {
		return "string1;string2;string3";
	}
}
