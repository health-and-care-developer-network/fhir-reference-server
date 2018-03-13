package uk.nhs.fhir.data.structdef.tree.cache;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.SlicingResolver;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;

public class SlicingDiscriminatorCacher<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {
	
	private final FhirTreeData<T, U> treeData;
	
	public SlicingDiscriminatorCacher(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}
	
	public void resolve() {
		for (U node : treeData.nodes()) {
			if (SlicingResolver.getSlicingSibling(node).isPresent()) {
				cacheSlicingDiscriminator(node);
			}
		}
	}
	
	private void cacheSlicingDiscriminator(U node) {
		Set<String> discriminatorPaths = 
			SlicingResolver
				.getSlicingSibling(node).get()
				.getData()
				.getSlicingInfo().get()
				.getDiscriminatorPaths();
		
		if (discriminatorPaths.size() > 1) {
			//  Return a map? Consider ordering for node key?
			throw new IllegalStateException("Need to implement handling multiple discriminators");
		}

		String discriminatorPath;
		try {
			discriminatorPath = discriminatorPaths.iterator().next();
		} catch (NoSuchElementException ex) {
			String warning = "Didn't find any discriminators to identify " + node.getPath() + " (likely caused by previous error)";
			EventHandlerContext.forThread().event(RendererEventType.NO_DISCRIMINATORS_FOUND, warning);
			node.getData().setDiscriminatorValue("<missing>");
			return;
		}

		List<String> discriminators = findDiscriminators(node, discriminatorPath);
		
		if (discriminators.size() == 0) {
			if (!node.getData().getSliceName().isPresent()) {
				String warning = "Didn't find any discriminators to identify " + node.getPath() + " (likely caused by previous error)";
				EventHandlerContext.forThread().event(RendererEventType.NO_DISCRIMINATORS_FOUND, warning);
			}
			
			node.getData().setDiscriminatorValue("<missing>");
		} else if (discriminators.size() > 1) {
			throw new IllegalStateException("Found multiple discriminators: [" + String.join(", ", discriminators) + " ] for node " + node.getPath());
		} else {
			node.getData().setDiscriminatorValue(discriminators.get(0));
		}
	}

	private List<String> findDiscriminators(U node, String discriminatorPath) {
		
		// From http://hl7.org/fhir/stu3/elementdefinition.html#slicing
		// "An element with a cardinality of 0..1 and a choice of multiple types can be sliced by type.
		// This is to specify different constraints for different types. In this case, the discriminator SHALL be "@type""
		if (discriminatorPath.endsWith("@type")) {
			return findTypeDiscriminators(node, discriminatorPath);
		}

		// Check for an extension type link first. Otherwise we default to an actual child node 'url' as normal
		if (node.getData().getPathName().equals("extension") 
		  && discriminatorPath.equals("url")) {
			
			Optional<String> extensionUrlDiscriminator = findExtensionUrlDiscriminator(node);
			
			if (extensionUrlDiscriminator.isPresent()) {
				return Lists.newArrayList(extensionUrlDiscriminator.get());
			}
			// (otherwise carry on)
		} 

		return findDiscriminatorsByBindingOrFixedValue(node, discriminatorPath);
	}

	private List<String> findTypeDiscriminators(U node, String discriminatorPath) {
		List<String> discriminators = Lists.newArrayList();
		
		U discriminatorNode;
		if (discriminatorPath.equals("@type")) {
			discriminatorNode = node;
		} else {
			String relativePath = discriminatorPath.substring(0, discriminatorPath.length() - 1 - "@type".length());
			Optional<U> descendant = node.findUniqueDescendantMatchingPath(relativePath);
			
			if (descendant.isPresent()) {
				discriminatorNode = descendant.get();
			} else {
				throw new IllegalStateException("Couldn't resolve discriminatorPath '" + discriminatorPath + "' for node " + node.getPath());
			}
		}
		
		// if the element is a reference type, we need to look at the type it is a reference to. Otherwise it's just the type string.
		LinkDatas discriminatorNodeTypeLinks = discriminatorNode.getData().getTypeLinks();
		for (Entry<LinkData, List<LinkData>> discriminatorNodeTypeLink : discriminatorNodeTypeLinks.links()) {
			if (discriminatorNodeTypeLink.getValue().isEmpty()) {
				discriminators.add(discriminatorNodeTypeLink.getKey().getText());
			} else {
				throw new IllegalStateException("Don't yet handle @type discriminator node with nested type links (" + node.getPath() + " -> " + discriminatorPath + ")");
			}
		}
		
		return discriminators;
	}
	
	private Optional<String> findExtensionUrlDiscriminator(U node) {
		Set<FhirURL> extensionUrlDiscriminators =
			node.getData()
				.getTypeLinks()
				.links()
				.stream()
				.filter(typeLink -> typeLink.getKey().getText().equals("Extension"))
				.flatMap(typeLink -> typeLink.getValue().isEmpty() ? Lists.newArrayList(typeLink.getKey()).stream() : typeLink.getValue().stream())
				.map(link -> link.getURL())
				.collect(Collectors.toSet());
		
		if (extensionUrlDiscriminators.size() == 0) {
			EventHandlerContext.forThread().event(RendererEventType.UNRESOLVED_DISCRIMINATOR, 
				"Missing extension URL discriminator node (" + node.getPath() + "). "
				  + "If slicing node is removed, we may not know to include a disambiguator in fhirTreeNode.getKeySegment()");
			return Optional.of("<missing>");
		}
		
		if (extensionUrlDiscriminators.size() > 1) {
			throw new IllegalStateException("Don't yet handle multiple extension url discriminators. Consider ordering so that keys are consistent?");
		}
		
		FhirURL extensionUrlDiscriminator = extensionUrlDiscriminators.iterator().next();
		if (!extensionUrlDiscriminator.equals(FhirURL.buildOrThrow("http://hl7.org/fhir/stu3/extensibility.html#Extension", node.getData().getVersion()))){
			return Optional.of(extensionUrlDiscriminator.toFullString());
		}
		
		return Optional.empty();
	}

	private List<String> findDiscriminatorsByBindingOrFixedValue(U node, String discriminatorPath) {
		List<String> discriminators = Lists.newArrayList();
		
		Optional<U> descendantNode = node.findUniqueDescendantMatchingPath(discriminatorPath);
		if (!descendantNode.isPresent()) {
			throw new IllegalStateException("Couldn't resolve discriminatorPath '" + discriminatorPath + "' for node " + node.getPath());
		}
		
		U discriminatorNode = descendantNode.get();

		if (discriminatorNode.getData().isFixedValue()) {
			Optional<String> discriminatorValue = discriminatorNode.getData().getFixedValue();
			discriminators.add(discriminatorValue.get());
		} else if (discriminatorNode.getData().hasBinding()) {
			BindingInfo bindingInfo = discriminatorNode.getData().getBinding().get();
			if (bindingInfo.getUrl().isPresent()) {
				discriminators.add(bindingInfo.getDescription().get());
			} else {
				discriminators.add(bindingInfo.getUrl().get().toFullString());
			}
		} else {
			if (!node.getData().getSliceName().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.UNRESOLVED_DISCRIMINATOR, 
					"Expected Fixed Value or Binding on discriminator node at " + discriminatorPath + " for sliced node " + node.getPath());
			}
			discriminators.add("<missing>");
		}
		
		return discriminators;
	}
}
