package uk.nhs.fhir.enums;

import java.util.logging.Logger;

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
	
	// The code in the renderer for OD artefacts is: uk.nhs.fhir.data.wrap.WrappedOperationDefinition
	OPERATION_DETAILS("render.html", ResourceType.OPERATIONDEFINITION, "Details", 0, false), // Taken from resource text section
	
	// The code in the renderer for VS artefacts is: uk.nhs.fhir.data.wrap.WrappedValueSet
	VALUESET_DETAILS("render.html", ResourceType.VALUESET, "Details", 0, false), // Taken from resource text section
	
	// The code in the renderer for CM artefacts is: uk.nhs.fhir.data.wrap.WrappedConceptMap
	CONCEPT_MAP_METADATA("metadata.html", ResourceType.CONCEPTMAP, "Metadata", -1, true),
	CONCEPT_MAP_FULL("full.html", ResourceType.CONCEPTMAP, "Details", 0, false), // Taken from resource text section
	CONCEPT_MAP_MAPPINGS("mappings.html", ResourceType.CONCEPTMAP, "Mappings", 10, false),
	
	// The code in the renderer for CS artefacts is: uk.nhs.fhir.data.wrap.WrappedCodeSystem
	CODESYSTEM_METADATA("metadata.html", ResourceType.CODESYSTEM, "Metadata", -1, true),
	//CODESYSTEM_DETAILS("codesystem-full.html", ResourceType.CODESYSTEM, "Details", 0, false),
	CODESYSTEM_CONCEPTS("concepts.html", ResourceType.CODESYSTEM, "Concepts", 0, false),
	CODESYSTEM_FILTERS("filters.html", ResourceType.CODESYSTEM, "Filters", 10, false),
	;
	
	private static final Logger LOG = Logger.getLogger(ArtefactType.class.getName());
	
	private ArtefactType(String filename, ResourceType relatesToResourceType, String displayName, int weight, boolean metadata) {
		this.displayName = displayName;
		this.relatesToResourceType = relatesToResourceType;
		this.filename = filename;
		this.weight = weight;
		this.metadata = metadata;
	}
	
	private String filename = null;
	private ResourceType relatesToResourceType = null;
	private String displayName = null;
	private int weight = 0;
	private boolean metadata = false;
	
	/**
	 * Takes a filename and resourceType and returns a matching ArtefactType (or null if not found)
	 * @param resourceType
	 * @param filename
	 * @return
	 */
    public static ArtefactType getFromFilename(ResourceType resourceType, String filename) {
    	if (filename == null || resourceType == null) { 
    		LOG.fine("Found artefact - can't determine type - filename: " + filename);
    		return null;
    	} else {
    		for (ArtefactType type : ArtefactType.values()) {
    			if (type.relatesToResourceType.equals(resourceType) && type.filename.equalsIgnoreCase(filename)) {
    				LOG.fine("Detected artefact of type: " + type);
    				return type;
    			}
    		}
    	}
    	LOG.fine("Found artefact - can't determine type - filename: " + filename);
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
		int idx = filename.indexOf('.');
		if (idx == -1) {
			return filename;
		} else {
			return filename.substring(0, idx);
		}
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public ResourceType getRelatesToResourceType() {
		return relatesToResourceType;
	}

	public void setRelatesToResourceType(ResourceType relatesToResourceType) {
		this.relatesToResourceType = relatesToResourceType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}
}
