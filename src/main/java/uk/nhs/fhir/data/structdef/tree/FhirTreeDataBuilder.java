package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;

/**
 * Builds up a tree structure from a list of FhirTreeNodes, based primarily on their 'path' attributes.
 */
public class FhirTreeDataBuilder<T, U extends TreeNode<T, U>> {
	
	private final MutableNodePath path = new MutableNodePath();
	private final Optional<EmptyNodeFactory<T, U>> emptyNodeFactory;
	
	private U rootNode = null;
	private U currentNode = null;
	
	public FhirTreeDataBuilder() {
		this(Optional.empty());
	}
	
	public FhirTreeDataBuilder(EmptyNodeFactory<T, U> emptyNodeFactory) {
		this(Optional.of(emptyNodeFactory));
	}
	
	public FhirTreeDataBuilder(Optional<EmptyNodeFactory<T, U>> emptyNodeFactory) {
		this.emptyNodeFactory = emptyNodeFactory;
	}
	
	public void addFhirTreeNode(U node) {
		MutableNodePath nodeParentPathParts = node.getPath().mutableCopy();
		
		// pop the last part of the path (node name) off the end, leaving the parent path 
		String nodeName = nodeParentPathParts.stepOut();
		
		// update state so that we are ready to append the next node
		stepTo(nodeParentPathParts);
		
		appendNode(node);
		path.stepInto(nodeName);
	}
	
	protected void appendNode(U newNode) {
		if (currentNode == null) {
			rootNode = newNode;
			currentNode = newNode;
		} else {
			currentNode.addChild(newNode);
			currentNode = newNode;
		}
	}
	
	private void stepTo(MutableNodePath targetPath) {
		if (path.isSubpath(targetPath)) {
			stepOutToAncestorPath(targetPath);
		} else {
			stepToNonAncestorPath(targetPath);
		}
		
		sanityCheck();
	}
	
	private void stepToNonAncestorPath(MutableNodePath targetPath) {
		if (emptyNodeFactory.isPresent()) {
			// trim path back until it only has nodes in common with the target path
			while (!targetPath.isSubpath(path)) {
				stepOut();
			}
			
			// add dummy nodes until we reach the target path
			EmptyNodeFactory<T, U> emptyNodefactory = emptyNodeFactory.get();
			for (int i=path.size(); i<targetPath.size(); i++) {
				path.stepInto(targetPath.getPart(i));
	
				appendNode(emptyNodefactory.create(currentNode, path.immutableCopy()));
			}
		} else {
			throw new IllegalArgumentException("Cannot step to " + targetPath + " from " + path + " (no dummy node factory)");
		}
	}

	private void stepOutToAncestorPath(MutableNodePath ancestorPath) {
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
		  && currentNode.getPathString().split("\\.").length != path.size()) {
			throw new IllegalStateException("Failed sanity check: current node path = " + currentNode.getPath() + ", current path = " + path.toString());
		}
	}
	
	public FhirTreeData<T, U> getTree() {
		return new FhirTreeData<>(rootNode);
	}
}