package uk.nhs.fhir.render.tree;

import java.util.List;

import com.google.common.collect.Lists;

public 
//A stack of strings representing the period-separated path of a node within a FHIR tree (the <path> tag of a node).
class NodePath {
	private final List<String> pathParts;
	
	public NodePath() {
		this.pathParts = Lists.newArrayList();
	}
	
	public NodePath(String nodePath) {
		String[] partsArray = nodePath.split("\\.");
		this.pathParts = Lists.newArrayList(partsArray);
	}
	
	public NodePath(List<String> pathParts) {
		this.pathParts = pathParts;
	}
	
	public void stepInto(String newPathPart) {
		pathParts.add(newPathPart);
	}
	
	public String stepOut() {
		return pathParts.remove(pathParts.size() - 1);
	}
	
	public int size() {
		return pathParts.size();
	}
	
	public String getPart(int i) {
		return pathParts.get(i);
	}
	
	// Returns true if this path begin with the entirety of ancestorPath
	public boolean isSubpath(NodePath ancestorPath) {
		if (ancestorPath.size() > size()) {
			return false;
		}
		
		for (int i=0; i<ancestorPath.size(); i++) {
			if (!pathParts.get(i).equals(ancestorPath.getPart(i))) {
				return false;
			}
		}
		return true;
	}
	
	public String toPathString() {
		return String.join(".", pathParts);
	}
	
	public String toString() {
		return toPathString();
	}
}
