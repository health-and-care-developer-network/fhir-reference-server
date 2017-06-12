package uk.nhs.fhir.datalayer.collections;

import java.io.File;

import uk.nhs.fhir.enums.ArtefactType;

/**
 * This represents a supporting artefact that sits alongside a resource - for example a
 * html page showing a diff view, bindings, etc.
 * @author Adam Hatherly
 *
 */
public class SupportingArtefact {
	private File filename = null;
	private ArtefactType artefactType = null;
	
	public SupportingArtefact(File filename, ArtefactType artefactType) {
		super();
		this.filename = filename;
		this.artefactType = artefactType;
	}

	public File getFilename() {
		return filename;
	}

	public void setFilename(File filename) {
		this.filename = filename;
	}

	public ArtefactType getArtefactType() {
		return artefactType;
	}

	public void setArtefactType(ArtefactType artefactType) {
		this.artefactType = artefactType;
	}
}
