package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;

/**
 * Takes each ElementDefinition from a snapshot and builds up a tree structure from that data, 
 * based primarily on their 'path' attributes.
 * @author jon
 */
public class FhirTreeBuilder {

	protected final FhirTreeNodeBuilder nodeBuilder;
	
	private List<String> path = Lists.newArrayList();
	private int removedLevel = -1;
	private FhirTreeNode rootNode = null;
	private FhirTreeNode currentNode = null;
	
	public FhirTreeBuilder() {
		this(new FhirTreeNodeBuilder());
	}
	public FhirTreeBuilder(FhirTreeNodeBuilder nodeBuilder) {
		this.nodeBuilder = nodeBuilder;
	}
	
	public void addElementDefinition(ElementDefinitionDt elementDefinition) {
		String newElementPath = elementDefinition.getPath();
		List<String> pathElements = Lists.newArrayList(newElementPath.split("\\."));
		String elementPathName = pathElements.remove(pathElements.size() - 1);
		
		stepOutTo(pathElements);
		if (removedLevel > path.size()) {
			//stepped out of the removed node
			removedLevel = -1;
		}
		
		FhirTreeNode newNode = nodeBuilder.fromElementDefinition(elementDefinition);
		
		if (currentNode == null) {
			rootNode = newNode;
			currentNode = newNode;
		} else {
			currentNode.addChild(newNode);
			currentNode = newNode;
		}
		path.add(elementPathName);
	}
	
	private void stepOutTo(List<String> targetPath) {
		if (!pathIsSubpath(path, targetPath)) {
			throw new IllegalArgumentException("Cannot step out from " + String.join(".", path) + " to " + String.join(".", targetPath));
		}
		int stepOutCount = path.size() - targetPath.size();
		for (int i=0; i<stepOutCount; i++) {
			stepOut();
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
