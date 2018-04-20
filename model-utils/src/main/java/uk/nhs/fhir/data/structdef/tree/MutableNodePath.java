package uk.nhs.fhir.data.structdef.tree;

import java.util.List;

import com.google.common.collect.Lists;

public 
//A stack of strings representing the period-separated path of a node within a FHIR tree (the <path> tag of a node).
class MutableNodePath extends AbstractNodePath {
	
	public MutableNodePath() {
		super(Lists.newArrayList());
	}
	
	public MutableNodePath(List<String> pathParts) {
		super(pathParts);
	}
	
	public MutableNodePath(String nodePath) {
		super(nodePath);
	}
	
	public void stepInto(String newPathPart) {
		pathParts.add(newPathPart);
	}
	
	public String stepOut() {
		return pathParts.remove(pathParts.size() - 1);
	}
}
