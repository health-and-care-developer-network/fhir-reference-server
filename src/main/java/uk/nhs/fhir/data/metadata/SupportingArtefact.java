package uk.nhs.fhir.data.metadata;

import java.io.File;
import java.util.Comparator;

import com.google.common.base.Preconditions;

/**
 * This represents a supporting artefact that sits alongside a resource - for example a
 * html page showing a diff view, bindings, etc.
 * @author Adam Hatherly
 *
 */
public class SupportingArtefact {

	public static final Comparator<SupportingArtefact> BY_WEIGHT = 
		Comparator.comparing((SupportingArtefact supportingArtefact) 
			-> supportingArtefact.artefactType.getWeight());
	
	private final File filename;
	private final ArtefactType artefactType;
	
	public SupportingArtefact(File filename, ArtefactType artefactType) {
		Preconditions.checkNotNull(filename);
		Preconditions.checkNotNull(artefactType);
		
		this.filename = filename;
		this.artefactType = artefactType;
	}

	public File getFilename() {
		return filename;
	}

	public ArtefactType getArtefactType() {
		return artefactType;
	}
}
