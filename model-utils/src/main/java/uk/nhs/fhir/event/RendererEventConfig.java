package uk.nhs.fhir.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to allow fine-grained configuration of how the app responds to errors.
 * This config is overridden by newMain.STRICT
 */

public class RendererEventConfig {
	
	private static final Map<RendererEventType, RendererEventResponse> responses = new ConcurrentHashMap<>();

	static {
		// Forge bug
		responses.put(RendererEventType.MISNAMED_SNAPSHOT_CHOICE_NODE, RendererEventResponse.IGNORE); // should ideally be a warning so we chase Forge, but out of our control
		responses.put(RendererEventType.DIFFERENTIAL_NODE_MISSING_ID, RendererEventResponse.IGNORE); // should be a warning so we chase Forge, but too many & maybe out of our control
		responses.put(RendererEventType.DIFFERENTIAL_CHOICE_NODE_WRONG_ID, RendererEventResponse.IGNORE); // should be a warning so we chase Forge
		
		// Error in profile
		responses.put(RendererEventType.DUPLICATE_CONSTRAINT_KEYS, RendererEventResponse.IGNORE); // should be a warning, but too many
		responses.put(RendererEventType.MESSAGE_ASSET_TYPE_CODING, RendererEventResponse.LOG_WARNING); // should be CodeType not CodingType
		
		// Perhaps valid?
		responses.put(RendererEventType.MISSING_TYPE_LINK, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.TYPELINK_STRING_WITH_PROFILE, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.UNRECOGNISED_MESSAGE_ASSET_VERSION, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.UNRECOGNISED_SEARCH_PARAM_FEATURE, RendererEventResponse.LOG_WARNING);
		
		responses.put(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.CONSTRAINT_WITHOUT_CONDITION, RendererEventResponse.IGNORE);

		// Uses placeholder with the expectation that the node is going to be removed before display
		// If the placeholder is going to be displayed, we hit STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED
		responses.put(RendererEventType.BINDING_WITHOUT_DESC_OR_URL, RendererEventResponse.IGNORE);

		// special case workarounds (unlikely to go away)
		responses.put(RendererEventType.IGNORABLE_MAPPING_ID, RendererEventResponse.IGNORE);
		responses.put(RendererEventType.DEFAULT_TO_SIMPLE_EXTENSION, RendererEventResponse.IGNORE); // should be a warning, but too many
		responses.put(RendererEventType.DIFFERENTIAL_MISSING_SLICE_NAME, RendererEventResponse.LOG_WARNING);

		// hit by DSTU2 (GPConnect)
		responses.put(RendererEventType.VERSION_NOT_AVAILABLE, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.METADATA_NOT_AVAILABLE, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.MISSING_CARDINALITY, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.NO_DISCRIMINATORS_FOUND, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.SLICING_WITHOUT_DISCRIMINATOR, RendererEventResponse.LOG_WARNING);
		responses.put(RendererEventType.DSTU2_PARSE_VERSION_NUMBER_FAILURE, RendererEventResponse.LOG_WARNING);
		
		// Currently not hit
		responses.put(RendererEventType.FILE_WITH_BOM, RendererEventResponse.LOG_WARNING);
		
		responses.put(RendererEventType.EMPTY_VALUE_SET, RendererEventResponse.THROW);
		responses.put(RendererEventType.FIX_MISSING_TYPE_LINK, RendererEventResponse.THROW);
		responses.put(RendererEventType.RESOURCE_WITHOUT_SNAPSHOT, RendererEventResponse.THROW);
		responses.put(RendererEventType.UNRESOLVED_DISCRIMINATOR, RendererEventResponse.THROW);
		responses.put(RendererEventType.HL7_URL_WITHOUT_DSTU2, RendererEventResponse.THROW);
		responses.put(RendererEventType.EMPTY_TYPE_LINKS, RendererEventResponse.THROW);
		responses.put(RendererEventType.LINK_WITH_LOGICAL_URL, RendererEventResponse.THROW);
		responses.put(RendererEventType.EXTENSION_FILE_NOT_FOUND, RendererEventResponse.THROW);
		responses.put(RendererEventType.LINK_REFERENCES_ITSELF, RendererEventResponse.THROW);
		responses.put(RendererEventType.MISSING_REFERENCED_NODE, RendererEventResponse.THROW);
		responses.put(RendererEventType.USER_TYPE_WITHOUT_CONSTRAINED_TYPE, RendererEventResponse.THROW);
	}
	
	public static RendererEventResponse getResponse(RendererEventType error) {
		return responses.get(error);
	}

}