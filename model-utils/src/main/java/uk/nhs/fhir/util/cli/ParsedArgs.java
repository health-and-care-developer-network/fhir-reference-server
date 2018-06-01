package uk.nhs.fhir.util.cli;

import java.util.Map;

import com.google.common.collect.Maps;

public class ParsedArgs {
	private Map<CliArg<?>, String> map = Maps.newHashMap();
	
	public void put(CliArg<?> argSpec, String arg) {
		map.put(argSpec, arg);
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
