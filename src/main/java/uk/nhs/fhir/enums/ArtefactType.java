package uk.nhs.fhir.enums;

import java.util.logging.Logger;

import ca.uhn.fhir.rest.method.RequestDetails;

/**
 * This is an enum to hold the type of a supporting artefact that sits alongside a FHIR resource
 * @author Adam Hatherly
 *
 */
public enum ArtefactType {
	
	METADATA("metadata.html", ResourceType.STRUCTUREDEFINITION, "Metadata"),
	//SNAPSHOT("snapshot.html", ResourceType.STRUCTUREDEFINITION, "Snapshot"),
	BINDINGS("bindings.html", ResourceType.STRUCTUREDEFINITION, "Bindings"),
	DETAILS("details.html", ResourceType.STRUCTUREDEFINITION, "Details"),
	DIFFERENTIAL("differential.html", ResourceType.STRUCTUREDEFINITION, "Differential"),
	
	//OPERATION_RENDER("render.html", ResourceType.OPERATIONDEFINITION, "Details"),
	
	//VALUESET_RENDER("render.html", ResourceType.VALUESET, "Details"),
	;
	
	private static final Logger LOG = Logger.getLogger(ArtefactType.class.getName());
	
	private ArtefactType(String filename, ResourceType relatesToResourceType, String displayName) {
		this.displayName = displayName;
		this.relatesToResourceType = relatesToResourceType;
		this.filename = filename;
	}
	
	private String filename = null;
	private ResourceType relatesToResourceType = null;
	private String displayName = null;
	
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
}
