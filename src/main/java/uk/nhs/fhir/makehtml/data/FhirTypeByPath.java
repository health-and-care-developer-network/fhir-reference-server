package uk.nhs.fhir.makehtml.data;

import java.util.Map;

import com.google.common.collect.Maps;

public class FhirTypeByPath {

	private static final Map<String, LinkData> typeByPath = Maps.newHashMap();
	

	public static boolean recognisedPath(String path) {
		return typeByPath.containsKey(path);
	}

	public static LinkData forPath(String path) {
		return typeByPath.get(path);
	}

}
