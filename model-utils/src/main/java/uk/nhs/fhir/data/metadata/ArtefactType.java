package uk.nhs.fhir.data.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an enum to hold the type of a supporting artefact that sits alongside a FHIR resource
 * @author Adam Hatherly
 *
 */
public enum ArtefactType {
	
	// Note: To stop an artefact being displayed, use a weight of -1
	
	// The code in the renderer for SD artefacts is: uk.nhs.fhir.data.wrap.WrappedStructureDefinition
	METADATA("metadata.html", ResourceType.STRUCTUREDEFINITION, "Metadata", -1, true),
	SNAPSHOT("snapshot.html", ResourceType.STRUCTUREDEFINITION, "Snapshot", 0, false), // Taken from resource text section
	BINDINGS("bindings.html", ResourceType.STRUCTUREDEFINITION, "Bindings", 30, false),
	DETAILS("details.html", ResourceType.STRUCTUREDEFINITION, "Detailed Descriptions", 20, false),
	DIFFERENTIAL("differential.html", ResourceType.STRUCTUREDEFINITION, "Differential", 10, false),
	GITHUB_HISTORY("git-history.html", ResourceType.STRUCTUREDEFINITION, "Git history", 40, false),
	STRUCTURE_DEFINITION_FULL("full.html", ResourceType.STRUCTUREDEFINITION, "Full", -1, false),
	
	// The code in the renderer for OD artefacts is: uk.nhs.fhir.data.wrap.WrappedOperationDefinition
	OPERATION_DETAILS("render.html", ResourceType.OPERATIONDEFINITION, "Details", 0, false), // Taken from resource text section
	OPERATION_GITHUB_HISTORY("git-history.html", ResourceType.OPERATIONDEFINITION, "Git history", 40, false),
	
	// The code in the renderer for VS artefacts is: uk.nhs.fhir.data.wrap.WrappedValueSet
	VALUESET_DETAILS("render.html", ResourceType.VALUESET, "Details", 0, false), // Taken from resource text section
	VALUESET_GITHUB_HISTORY("git-history.html", ResourceType.VALUESET, "Git history", 40, false),
	
	// The code in the renderer for CM artefacts is: uk.nhs.fhir.data.wrap.WrappedConceptMap
	CONCEPT_MAP_METADATA("metadata.html", ResourceType.CONCEPTMAP, "Metadata", -1, true),
	CONCEPT_MAP_FULL("full.html", ResourceType.CONCEPTMAP, "Details", -1, false), // Taken from resource text section
	CONCEPT_MAP_MAPPINGS("mappings.html", ResourceType.CONCEPTMAP, "Mappings", 10, false),
	CONCEPT_MAP_GITHUB_HISTORY("git-history.html", ResourceType.CONCEPTMAP, "Git history", 40, false),
	
	// The code in the renderer for CS artefacts is: uk.nhs.fhir.data.wrap.WrappedCodeSystem
	CODESYSTEM_METADATA("metadata.html", ResourceType.CODESYSTEM, "Metadata", -1, true),
	CODESYSTEM_DETAILS("codesystem-full.html", ResourceType.CODESYSTEM, "Details", -1, false),
	CODESYSTEM_CONCEPTS("concepts.html", ResourceType.CODESYSTEM, "Concepts", 0, false),
	CODESYSTEM_FILTERS("filters.html", ResourceType.CODESYSTEM, "Filters", 10, false),
	CODESYSTEM_GITHUB_HISTORY("git-history.html", ResourceType.CODESYSTEM, "Git history", 40, false),
	
	MESSAGE_METADATA("metadata.html", ResourceType.MESSAGEDEFINITION, "Metadata", -1, true),
	MESSAGE_FOCUS("focus.html", ResourceType.MESSAGEDEFINITION, "Message Payload", 0, false),
	MESSAGE_GITHUB_HISTORY("git-history.html", ResourceType.MESSAGEDEFINITION, "Git history", 40, false),
	
	SEARCH_PARAM_METADATA("metadata.html", ResourceType.SEARCHPARAMETER, "Metadata", -1, true),
	SEARCH_PARAM_DETAILS("details.html", ResourceType.SEARCHPARAMETER, "Definition", 0, false),
	SEARCH_PARAM_GITHUB_HISTORY("git-history.html", ResourceType.SEARCHPARAMETER, "Git history", 40, false),
	
	NAMING_SYSTEM("render.html",  ResourceType.NAMINGSYSTEM , "Details", 0, false), // Taken from resource text section
	NAMING_SYSTEM_GITHUB_HISTORY("git-history.html", ResourceType.NAMINGSYSTEM, "Git history", 40, false)	
	;
	
	private static final Logger LOG = LoggerFactory.getLogger(ArtefactType.class.getName());
	
	private ArtefactType(String filename, ResourceType relatesToResourceType, String displayName, int weight, boolean metadata) {
		this.filename = filename;
		this.relatesToResourceType = relatesToResourceType;
		this.displayName = displayName;
		this.weight = weight;
		this.metadata = metadata;
	}
	
	private final String filename;
	private final ResourceType relatesToResourceType;
	private final String displayName;
	private final int weight;
	private final boolean metadata;
	
	/**
	 * Takes a filename and resourceType and returns a matching ArtefactType (or null if not found)
	 * @param resourceType
	 * @param filename
	 * @return
	 */
    public static ArtefactType getFromFilename(ResourceType resourceType, String filename) {
    	if (filename == null 
    	  || resourceType == null) { 
    		LOG.debug("Found artefact - can't determine type - filename: " + filename);
    		return null;
    	} else {
    		for (ArtefactType type : ArtefactType.values()) {
    			if (type.relatesToResourceType.equals(resourceType) 
    			  && type.filename.equalsIgnoreCase(filename)) {
    				LOG.debug("Detected artefact of type: " + type);
    				return type;
    			}
    		}
    	}
    	LOG.debug("Found artefact - can't determine type - filename: " + filename);
    	return null;
    }
    
    /**
     * Get the Metadata type for the specified resource type (if there is one)
     * @param resourceType
     * @return
     */
    /*public static ArtefactType getMetadataTypeForResourceType(ResourceType resourceType) {
    	if (resourceType == null) { 
    		LOG.fine("Can't determine metadata for resource type: " + resourceType);
    		return null;
    	} else {
    		for (ArtefactType type : ArtefactType.values()) {
    			if (type.relatesToResourceType.equals(resourceType) && type.isMetadata()) {
    				LOG.fine("Found metadata type: " + type);
    				return type;
    			}
    		}
    	}
		LOG.fine("Can't determine metadata for resource type: " + resourceType);
    	return null;
    }*/
	
	@Override
	public String toString() {
		return this.displayName;
	}

	public String getFilename() {
		return filename;
	}
	
	public String getFilenameWithoutExtension() {
		if (filename.contains(".")) {
			int dotIndex = filename.indexOf('.');
			return filename.substring(0, dotIndex);
		} else {
			return filename;
		}
	}

	public ResourceType getRelatesToResourceType() {
		return relatesToResourceType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isMetadata() {
		return metadata;
	}
}
