package uk.nhs.fhir.util.cli;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ParsedArgs {
	private Map<CliArg<?>, String> map = Maps.newHashMap();
	private Set<CliFlag> flags = Sets.newHashSet();
	
	public void put(CliArg<?> argSpec, String arg) {
		map.put(argSpec, arg);
	}
	
	public void foundFlag(CliFlag flag) {
		flags.add(flag);
	}
	
	public boolean get(CliFlag flag) {
		return flags.contains(flag);
	}

	public <T> T get(CliArg<T> argSpec) {
		if (map.containsKey(argSpec)) {
			String arg = map.get(argSpec);
			return argSpec.convert(arg);
		} else {
			return null;
		}
	}
}
