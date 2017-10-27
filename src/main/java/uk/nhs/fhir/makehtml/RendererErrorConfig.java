package uk.nhs.fhir.makehtml;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to allow fine-grained configuration of how the app responds to errors.
 * This config is overridden by newMain.STRICT
 */

public class RendererErrorConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RendererErrorConfig.class);
	
	public static boolean STRICT = false;
	
	private static final Map<RendererError, RendererErrorResponse> responses = new ConcurrentHashMap<>();

	static {
		// Forge bug
		responses.put(RendererError.MISNAMED_SNAPSHOT_CHOICE_NODE, RendererErrorResponse.IGNORE);
		
		// Error in profile
		responses.put(RendererError.HL7_ORG_UK_HOST, RendererErrorResponse.IGNORE);
		responses.put(RendererError.SLICING_WITHOUT_DISCRIMINATOR, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.UNRESOLVED_DISCRIMINATOR, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.NO_DISCRIMINATORS_FOUND, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.MISSING_CARDINALITY, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.FIX_MISSING_TYPE_LINK, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.RESOURCE_WITHOUT_SNAPSHOT, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.DUPLICATE_CONSTRAINT_KEYS, RendererErrorResponse.IGNORE);
		responses.put(RendererError.IGNORABLE_MAPPING_ID, RendererErrorResponse.IGNORE);
		
		// Perhaps valid?
		responses.put(RendererError.EMPTY_VALUE_SET, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.MISSING_TYPE_LINK, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.TYPELINK_STRING_WITH_PROFILE, RendererErrorResponse.LOG_WARNING);
		responses.put(RendererError.MULTIPLE_MAPPINGS_SAME_KEY, RendererErrorResponse.IGNORE);
		responses.put(RendererError.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE, RendererErrorResponse.IGNORE);
		responses.put(RendererError.CONSTRAINT_WITHOUT_CONDITION, RendererErrorResponse.IGNORE);

		// Uses a temporary name - if it is going to be displayed, we hit STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED
		responses.put(RendererError.BINDING_WITHOUT_DESC_OR_URL, RendererErrorResponse.IGNORE);
		
		responses.put(RendererError.DEFAULT_TO_SIMPLE_EXTENSION, RendererErrorResponse.LOG_WARNING);

		responses.put(RendererError.METADATA_NOT_AVAILABLE, RendererErrorResponse.THROW);
		responses.put(RendererError.VERSION_NOT_AVAILABLE, RendererErrorResponse.THROW);
		
		// Currently not hit
		responses.put(RendererError.HL7_URL_WITHOUT_DSTU2, RendererErrorResponse.THROW);
		responses.put(RendererError.EMPTY_TYPE_LINKS, RendererErrorResponse.THROW);
		responses.put(RendererError.LINK_WITH_LOGICAL_URL, RendererErrorResponse.THROW);
		responses.put(RendererError.EXTENSION_FILE_NOT_FOUND, RendererErrorResponse.THROW);
		responses.put(RendererError.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED, RendererErrorResponse.THROW);
		responses.put(RendererError.LINK_REFERENCES_ITSELF, RendererErrorResponse.THROW);
		responses.put(RendererError.MISSING_REFERENCED_NODE, RendererErrorResponse.THROW);
	}
	
	public static RendererErrorResponse getResponse(RendererError error) {
		return responses.get(error);
	}
	
	public static void handle(RendererError errorType, String logInfo) {
		handle(errorType, logInfo, Optional.empty());
	}
	
	public static void handle(RendererError errorType, String logInfo, Optional<Throwable> throwable) {
		if (STRICT) {
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
		LOG.error(info);
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