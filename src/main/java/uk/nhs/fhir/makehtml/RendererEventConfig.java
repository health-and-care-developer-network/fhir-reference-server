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

public class RendererEventConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RendererEventConfig.class);
	
	public static boolean STRICT = false;
	
	private static final Map<RendererEventType, RendererEventResponse> responses = new ConcurrentHashMap<>();

	static {
		// Forge bug
		responses.put(RendererEventType.MISNAMED_SNAPSHOT_CHOICE_NODE, RendererEventResponse.IGNORE);
		
		// Error in profile
		responses.put(RendererEventType.HL7_ORG_UK_HOST, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.SLICING_WITHOUT_DISCRIMINATOR, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.UNRESOLVED_DISCRIMINATOR, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.NO_DISCRIMINATORS_FOUND, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.MISSING_CARDINALITY, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.FIX_MISSING_TYPE_LINK, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.RESOURCE_WITHOUT_SNAPSHOT, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.DUPLICATE_CONSTRAINT_KEYS, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.IGNORABLE_MAPPING_ID, RendererEventResponse.IGNORE);
		
		// Perhaps valid?
		responses.put(RendererEventType.EMPTY_VALUE_SET, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.MISSING_TYPE_LINK, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.TYPELINK_STRING_WITH_PROFILE, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.CONSTRAINT_WITHOUT_CONDITION, RendererEventResponse.IGNORE);

		// Uses placeholder with the expectation that the node is going to be removed before display
		// If the placeholder is going to be displayed, we hit STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED
		responses.put(RendererEventType.BINDING_WITHOUT_DESC_OR_URL, RendererEventResponse.IGNORE);
		
		responses.put(RendererEventType.DEFAULT_TO_SIMPLE_EXTENSION, RendererEventResponse.LOG_WARNING);

		responses.put(RendererEventType.METADATA_NOT_AVAILABLE, RendererEventResponse.THROW);
		responses.put(RendererEventType.VERSION_NOT_AVAILABLE, RendererEventResponse.THROW);
		
		// Currently not hit
		responses.put(RendererEventType.HL7_URL_WITHOUT_DSTU2, RendererEventResponse.THROW);
		responses.put(RendererEventType.EMPTY_TYPE_LINKS, RendererEventResponse.THROW);
		responses.put(RendererEventType.LINK_WITH_LOGICAL_URL, RendererEventResponse.THROW);
		responses.put(RendererEventType.EXTENSION_FILE_NOT_FOUND, RendererEventResponse.THROW);
		responses.put(RendererEventType.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED, RendererEventResponse.THROW);
		responses.put(RendererEventType.LINK_REFERENCES_ITSELF, RendererEventResponse.THROW);
		responses.put(RendererEventType.MISSING_REFERENCED_NODE, RendererEventResponse.THROW);
	}
	
	public static RendererEventResponse getResponse(RendererEventType error) {
		return responses.get(error);
	}
	
	public static void handle(RendererEventType errorType, String logInfo) {
		handle(errorType, logInfo, Optional.empty());
	}
	
	public static void handle(RendererEventType errorType, String logInfo, Optional<Throwable> throwable) {
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