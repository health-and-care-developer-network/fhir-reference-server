package uk.nhs.fhir.makehtml.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import uk.nhs.fhir.makehtml.NewMain;

/**
 * Class to allow fine-grained configuration of how the app responds to errors.
 * This config is overridden by newMain.STRICT
 */

public class RendererError {
	public enum Key {
		EMPTY_TYPE_LINKS,
		MISNAMED_SNAPSHOT_CHOICE_NODE,
		DUPLICATE_CONSTRAINT_KEYS,
		MISSING_CARDINALITY,
		MISSING_TYPE_LINK,
		EXTENSION_FILE_NOT_FOUND,
		BINDING_WITHOUT_DESC_OR_URL,
		STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED,
		CONSTRAINT_WITHOUT_CONDITION, 
		HL7_URL_WITHOUT_DSTU2, 
		LINK_WITH_LOGICAL_URL, 
		HL7_ORG_UK_HOST,
		COMPLEX_EXTENSION_WITH_CHILDREN,
		LINK_REFERENCES_ITSELF,
		MISSING_REFERENCED_NODE,
		FIXEDVALUE_WITH_LINKED_NODE,
		MISSING_TYPE_LINKS_KNOWN_ISSUE;
	}
	
	private static final Map<Key, ErrorResponse> responses = new HashMap<>();

	static {
		responses.put(Key.MISNAMED_SNAPSHOT_CHOICE_NODE, ErrorResponse.IGNORE);
		responses.put(Key.DUPLICATE_CONSTRAINT_KEYS, ErrorResponse.IGNORE);
		responses.put(Key.CONSTRAINT_WITHOUT_CONDITION, ErrorResponse.IGNORE);
		responses.put(Key.HL7_ORG_UK_HOST, ErrorResponse.IGNORE);
		responses.put(Key.COMPLEX_EXTENSION_WITH_CHILDREN, ErrorResponse.IGNORE);

		responses.put(Key.MISSING_TYPE_LINK, ErrorResponse.LOG_WARNING);
		
		responses.put(Key.BINDING_WITHOUT_DESC_OR_URL, ErrorResponse.LOG_WARNING);
		responses.put(Key.HL7_URL_WITHOUT_DSTU2, ErrorResponse.LOG_WARNING);
		responses.put(Key.MISSING_TYPE_LINKS_KNOWN_ISSUE, ErrorResponse.LOG_WARNING);

		responses.put(Key.EMPTY_TYPE_LINKS, ErrorResponse.THROW);
		responses.put(Key.LINK_WITH_LOGICAL_URL, ErrorResponse.THROW);
		responses.put(Key.MISSING_CARDINALITY, ErrorResponse.THROW);
		responses.put(Key.EXTENSION_FILE_NOT_FOUND, ErrorResponse.THROW);
		responses.put(Key.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED, ErrorResponse.THROW);
		responses.put(Key.LINK_REFERENCES_ITSELF, ErrorResponse.THROW);
		responses.put(Key.MISSING_REFERENCED_NODE, ErrorResponse.THROW);
	}
	
	public static void handle(Key errorType, String logInfo) {
		handle(errorType, logInfo, Optional.empty());
	}
	
	public static void handle(Key errorType, String logInfo, Optional<Throwable> throwable) {
		if (NewMain.STRICT) {
			handleThrow(logInfo, throwable);
		}
		
		if (!responses.containsKey(errorType)) {
			throw new IllegalStateException("Missing RendererError key: " + errorType);
		}
		
		switch (responses.get(errorType)) {
			case IGNORE:
				return;
			case LOG_WARNING:
				handleLog(logInfo, throwable);
				return;
			case THROW:
				handleThrow(logInfo, throwable);
				return;
			default:
				throw new IllegalStateException("Unexpected default for " + errorType);
		}
	}
	
	private static void handleLog(String info, Optional<Throwable> throwable) {
		System.out.println(info);
		if (throwable.isPresent()) {
			throwable.get().printStackTrace();
		}
	}
	
	private static void handleThrow(String info, Optional<Throwable> throwable) {
		if (throwable.isPresent()) {
			throw new IllegalStateException(info, throwable.get());
		} else {
			throw new IllegalStateException(info);
		}
	}

}

enum ErrorResponse {
	IGNORE,
	LOG_WARNING,
	THROW;
}