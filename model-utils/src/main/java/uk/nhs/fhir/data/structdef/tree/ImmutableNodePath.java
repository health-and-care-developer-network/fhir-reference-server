package uk.nhs.fhir.data.structdef.tree;

import java.util.List;

public class ImmutableNodePath extends AbstractNodePath {

	public ImmutableNodePath(String path) {
		super(path);
	}
	
	public ImmutableNodePath(List<String> pathParts) {
		super(pathParts);
	}

}
