package uk.nhs.fhir.enums;

import java.util.Comparator;
import java.util.logging.Logger;

import ca.uhn.fhir.rest.method.RequestDetails;

/**
 * This is an enum to hold the type of a supporting artefact that sits alongside a FHIR resource
 * @author Adam Hatherly
 *
 */
public enum ArtefactType {
	
	// Note: To stop an artefact being displayed, use a weight of -1
	METADATA("metadata.html", ResourceType.STRUCTUREDEFINITION, "Metadata", -1),
	//SNAPSHOT("snapshot.html", ResourceType.STRUCTUREDEFINITION, "Snapshot"),
	BINDINGS("bindings.html", ResourceType.STRUCTUREDEFINITION, "Bindings", 30),
	DETAILS("details.html", ResourceType.STRUCTUREDEFINITION, "Detailed Descriptions", 20),
	DIFFERENTIAL("differential.html", ResourceType.STRUCTUREDEFINITION, "Differential", 10),
	
	//OPERATION_RENDER("render.html", ResourceType.OPERATIONDEFINITION, "Details"),
	
	//VALUESET_RENDER("render.html", ResourceType.VALUESET, "Details"),
	;
	
	private static final Logger LOG = Logger.getLogger(ArtefactType.class.getName());
	
	private ArtefactType(String filename, ResourceType relatesToResourceType, String displayName, int weight) {
		this.displayName = displayName;
		this.relatesToResourceType = relatesToResourceType;
		this.filename = filename;
		this.weight = weight;
	}
	
	private String filename = null;
	private ResourceType relatesToResourceType = null;
	private String displayName = null;
	private int weight = 0;
	
	/**
	 * Takes a filename and resourceType and returns a matching ArtefactType (or null if not found)
	 * @param resourceType
	 * @param filename
	 * @return
	 */
    public static ArtefactType getFromFilename(ResourceType resourceType, String filename) {
    	if (filename == null || resourceType == null) { 
    		LOG.info("Found artefact - can't determine type");
    		return null;
    	} else {
    		for (ArtefactType type : ArtefactType.values()) {
    			if (type.relatesToResourceType.equals(resourceType) && type.filename.equalsIgnoreCase(filename)) {
    				LOG.info("Detected artefact of type: " + type);
    				return type;
    			}
    		}
    	}
    	LOG.info("Found artefact - can't determine type");
    	return null;
    }
	
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
}
