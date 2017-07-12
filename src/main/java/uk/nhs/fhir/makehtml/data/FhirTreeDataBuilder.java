package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Builds up a tree structure from a list of FhirTreeNodes, based primarily on their 'path' attributes.
 * @author jon
 */
public class FhirTreeDataBuilder {
	
	private List<String> path = Lists.newArrayList();
	private FhirTreeTableContent rootNode = null;
	private FhirTreeTableContent currentNode = null;
	private boolean allowDummyNodes = false;
	
	public void permitDummyNodes() {
		this.allowDummyNodes = true;
	}
	
	public void addFhirTreeNode(FhirTreeNode node) {
		String nodePath = node.getPath();
		List<String> pathElements = Lists.newArrayList(nodePath.split("\\."));
		String elementPathName = pathElements.remove(pathElements.size() - 1);
		
		stepOutTo(pathElements);
		
		appendNode(node);
		path.add(elementPathName);
	}
	
	private void appendNode(FhirTreeTableContent newNode) {
		if (currentNode == null) {
			rootNode = newNode;
			currentNode = newNode;
		} else {
			currentNode.addChild(newNode);
			currentNode = newNode;
		}
	}
	private void stepOutTo(List<String> targetPath) {
		if (pathIsSubpath(path, targetPath)) {
			int stepOutCount = path.size() - targetPath.size();
			for (int i=0; i<stepOutCount; i++) {
				stepOut();
			}
		} else if (allowDummyNodes) {
			// trim path back until it only has nodes in common with the target path
			while (!pathIsSubpath(targetPath, path)) {
				stepOut();
			}
			
			// add dummy nodes until we reach the target path
			for (int i=path.size(); i<targetPath.size(); i++) {
				String nextPathSegment = targetPath.get(i);
				path.add(nextPathSegment);
				String nextFullPath = String.join(".", path);
				FhirTreeTableContent dummyNode = new DummyFhirTreeNode(currentNode, nextFullPath);
				appendNode(dummyNode);
			}
		} else {
			throw new IllegalArgumentException("Cannot step out from " + String.join(".", path) + " to " + String.join(".", targetPath));
		}
		
		if (currentNode != null
		  && currentNode.getPath().split("\\.").length != path.size()) {
			throw new IllegalStateException("Failed sanity check: current node path = " + currentNode.getPath() + ", current path = " + String.join(", ", path));
		}
	}

	private void stepOut() {
		currentNode = currentNode.getParent();
		path.remove(path.size() - 1);
	}
	
	private static boolean pathIsSubpath(List<String> path, List<String> ancestorPath) {
		if (ancestorPath.size() > path.size()) {
			return false;
		}
		
		for (int i=0; i<ancestorPath.size(); i++) {
			if (!path.get(i).equals(ancestorPath.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	public FhirTreeData getTree() {
		return new FhirTreeData(rootNode); 
	}
}