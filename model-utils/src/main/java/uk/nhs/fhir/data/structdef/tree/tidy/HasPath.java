package uk.nhs.fhir.data.structdef.tree.tidy;

import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;

public interface HasPath {
	public ImmutableNodePath getPath();
}
