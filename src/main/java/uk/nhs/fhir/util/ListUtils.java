package uk.nhs.fhir.util;

import java.util.List;
import java.util.Optional;

public class ListUtils {
	
	public static <T> T expectUnique(List<T> list, String itemsDesc) {
		if (list.isEmpty()) {
			throw new IllegalStateException("Expected 1 " + itemsDesc + " but found none");
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			int size = list.size();
			throw new IllegalStateException("Found multiple (" + size + ")" + itemsDesc + " (expected 1)");
		}
	}
	
	public static <T> Optional<T> uniqueIfPresent(List<T> list, String itemsDesc) {
		if (list.isEmpty()) {
			return Optional.empty();
		} else if (list.size() == 1) {
			return Optional.of(list.get(0));
		} else {
			int size = list.size();
			throw new IllegalStateException("Found multiple (" + size + ")" + itemsDesc + " (expected 0 or 1)");
		}
	}
}
