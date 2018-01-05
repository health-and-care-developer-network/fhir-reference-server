package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

/**
 * Builds up a tree structure from a list of FhirTreeNodes, based primarily on their 'path' attributes.
 */
public class FhirTreeDataBuilder<T extends TreeContent<T>> {
	
	private final NodePath path = new NodePath();
	private final Optional<DummyNodeFactory<T>> dummyNodeFactory;
	
	private T rootNode = null;
	private T currentNode = null;
	
	public FhirTreeDataBuilder() {
		this(Optional.empty());
	}
	
	public FhirTreeDataBuilder(DummyNodeFactory<T> dummyNodeFactory) {
		this(Optional.of(dummyNodeFactory));
	}
	
	public FhirTreeDataBuilder(Optional<DummyNodeFactory<T>> dummyNodeFactory) {
		this.dummyNodeFactory = dummyNodeFactory;
	}
	
	public void addFhirTreeNode(T node) {
		NodePath nodeParentPathParts = new NodePath(node.getPath());
		
		// pop the last part of the path (node name) off the end, leaving the parent path 
		String nodeName = nodeParentPathParts.stepOut();
		
		// update state so that we are ready to append the next node
		stepTo(nodeParentPathParts);
		
		appendNode(node);
		path.stepInto(nodeName);
	}
	
	protected void appendNode(T newNode) {
		if (currentNode == null) {
			rootNode = newNode;
			currentNode = newNode;
		} else {
			currentNode.addChild(newNode);
			currentNode = newNode;
		}
	}
	
	private void stepTo(NodePath targetPath) {
		if (path.isSubpath(targetPath)) {
			stepOutToAncestorPath(targetPath);
		} else {
			stepToNonAncestorPath(targetPath);
		}
		
		sanityCheck();
	}
	
	private void stepToNonAncestorPath(NodePath targetPath) {
		if (dummyNodeFactory.isPresent()) {
			// trim path back until it only has nodes in common with the target path
			while (!targetPath.isSubpath(path)) {
				stepOut();
			}
			
			// add dummy nodes until we reach the target path
			DummyNodeFactory<T> unwrappedDummyNodefactory = dummyNodeFactory.get();
			for (int i=path.size(); i<targetPath.size(); i++) {
				path.stepInto(targetPath.getPart(i));
	
				appendNode(unwrappedDummyNodefactory.create(currentNode, path));
			}
		} else {
			throw new IllegalArgumentException("Cannot step to " + targetPath.toPathString() + " from " + path.toPathString() + " (no dummy node factory)");
		}
	}

	private void stepOutToAncestorPath(NodePath ancestorPath) {
		while (path.size() > ancestorPath.size()) {
			stepOut();
		}
	}

	protected void stepOut() {
		currentNode = currentNode.getParent();
		path.stepOut();
	}

	private void sanityCheck() {
		if (currentNode != null
		  && currentNode.getPath().split("\\.").length != path.size()) {
			throw new IllegalStateException("Failed sanity check: current node path = " + currentNode.getPath() + ", current path = " + path.toPathString());
		}
	}
	
	public FhirTreeData<T> getTree() {
		return new FhirTreeData<>(rootNode);
	}
}

// A stack of strings representing the period-separated path of a node within a FHIR tree (the <path> tag of a node).
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
}