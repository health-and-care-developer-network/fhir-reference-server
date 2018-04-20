package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.structdef.tree.CloneableFhirTreeData;
import uk.nhs.fhir.data.structdef.tree.DifferentialData;
import uk.nhs.fhir.data.structdef.tree.DifferentialTreeNode;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.structdef.tree.StructureDefinitionTreeDataProvider;
import uk.nhs.fhir.util.FhirFileRegistry;
import uk.nhs.fhir.util.ResourceNotAvailableException;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public abstract class WrappedStructureDefinition extends WrappedResource<WrappedStructureDefinition> {
	
	public abstract void checkUnimplementedFeatures();

	public abstract String getName();
	public abstract String getKind();
	public abstract String getKindDisplay();
	public abstract String getStatus();
	public abstract Boolean getAbstract();

	public abstract Optional<String> getConstrainedType();
	public abstract String getBase();
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Optional<Date> getDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getFhirVersion();
	public abstract Optional<String> getContextType();
	public abstract Optional<String> getDescription();

	public abstract List<FhirContacts> getContacts();
	public abstract List<String> getUseContexts();
	public abstract List<FhirMapping> getMappings();
	public abstract List<String> getUseLocationContexts();
	
	public abstract ExtensionType getExtensionType();

	public abstract boolean missingSnapshot();
	
	protected abstract void setCopyright(String updatedCopyRight);

	public abstract FhirCardinality getRootCardinality();
	public abstract Optional<String> getDifferentialRootDescription();
	public abstract String getDifferentialRootDefinition();
	public abstract Optional<String> extensionBaseTypeDesc();

	public boolean isExtension() {
		if (getConstrainedType().isPresent()) {
			return getConstrainedType().get().equals("Extension");
		} else {
			throw new IllegalStateException("Not sure whether this is an extension - no constrained type present");
		}
	}
	
	public void fixHtmlEntities() {
		//Optional<String> copyRight = getCopyright();
	    //if (copyRight.isPresent()) {
	    //    setCopyright(EscapeUtils.escapeCopyright(copyRight.get()));
	    //}
	    
	}
	
	public ResourceMetadata getMetadataImpl(File source) {
		String name = getName();
		boolean isExtension = isExtension();
		
		String baseType;
    	String extensionCardinality;
    	List<String> extensionContexts;
    	String extensionDescription;
		if (isExtension) {
			// Extra metadata for extensions
			extensionCardinality = getRootCardinality().toString();
			extensionContexts = getUseLocationContexts();
    		
			extensionDescription = getDifferentialRootDescription().orElse(getDifferentialRootDefinition());
    		
			baseType = extensionBaseTypeDesc().get();
		} else {
			extensionCardinality = null;
    		extensionContexts = Lists.newArrayList();
			extensionDescription = null;
    		
    		baseType = getConstrainedType().get();
		}
		
		String url = getUrl().get();
        String resourceID = getIdFromUrl().orElse(name);
        String displayGroup = baseType;
        VersionNumber versionNo = parseVersionNumber();
        
        String status = getStatus();
        
        return new ResourceMetadata(name, source, ResourceType.STRUCTUREDEFINITION,
        		isExtension, Optional.of(baseType), displayGroup, false,
				resourceID, versionNo, status, null, extensionCardinality,
				extensionContexts, extensionDescription, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.STRUCTUREDEFINITION;
	}
	
	/* Build once and copy as required */
	
	private Optional<CloneableFhirTreeData<SnapshotData, SnapshotTreeNode>> snapshotTree = Optional.empty();
	public CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> getSnapshotTree(Optional<StructureDefinitionRepository> structureDefinitions,
			Set<String> permittedMissingExtensionPrefixes) {
		if (missingSnapshot()) {
			throw new IllegalStateException("No snapshot information present");
		}
		
		if (!snapshotTree.isPresent()) {
			StructureDefinitionTreeDataProvider snapshotProvider = new StructureDefinitionTreeDataProvider(this);
			snapshotTree = Optional.of(snapshotProvider.getSnapshotTreeData(structureDefinitions, permittedMissingExtensionPrefixes));
		}
		
		return snapshotTree.get().shallowCopy();
	}

	private Optional<CloneableFhirTreeData<DifferentialData, DifferentialTreeNode>> differentialTree = Optional.empty();
	public CloneableFhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTree(Optional<StructureDefinitionRepository> structureDefinitions,
			Set<String> permittedMissingExtensionPrefixes) {
		if (missingSnapshot()) {
			throw new IllegalStateException("No snapshot information present");
		}
		
		if (!differentialTree.isPresent()) {
			CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> snapshotTree = getSnapshotTree(structureDefinitions, permittedMissingExtensionPrefixes);
			StructureDefinitionTreeDataProvider differentialProvider = new StructureDefinitionTreeDataProvider(this);
			differentialTree = Optional.of(differentialProvider.getDifferentialTreeData(snapshotTree, structureDefinitions, permittedMissingExtensionPrefixes));
		}
		
		return differentialTree.get().shallowCopy();
	}

	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse("FHIR Server: StructureDefinition " + getName());
	}

	public Map<SnapshotTreeNode, List<SnapshotTreeNode>> getExtensionsWithBindings(FhirFileRegistry fileRegistry,
			Set<String> permittedMissingExtensionPrefixes) {
		Map<SnapshotTreeNode, List<SnapshotTreeNode>> extensionsWithBindings = Maps.newHashMap();
		
		for (SnapshotTreeNode node : getSnapshotTree(Optional.of(fileRegistry), permittedMissingExtensionPrefixes).nodes()) {
			try {
				Optional<WrappedStructureDefinition> extensionDefinition = 
					node.getData()
						.getLinkedStructureDefinitionUrl()
						.map(url -> fileRegistry.getStructureDefinitionIgnoreCase(getImplicitFhirVersion(), url));
			
			
				if (extensionDefinition.isPresent()) {
					WrappedStructureDefinition definition = extensionDefinition.get();
					String extensionUrl = definition.getUrl().get();
	
					List<SnapshotTreeNode> extensionNodesWithBindings = Lists.newArrayList();
					
					for (SnapshotTreeNode extensionNode : definition.getSnapshotTree(Optional.of(fileRegistry), permittedMissingExtensionPrefixes).nodes()) {
						if (extensionNode.getData().getLinkedStructureDefinitionUrl().isPresent()) {
							throw new IllegalStateException("Structure definition " + getUrl() + " refers to extension " + extensionUrl 
							  + " which contains a nested extension at " + extensionNode.getNodeKey() + " but nested extensions are not currently supported");
						}
						
						if (!extensionNode.isRemovedByProfile()
		              	  && extensionNode.getData().getBinding().isPresent()) {
							extensionNodesWithBindings.add(extensionNode);
						}
					}
					
					if (!extensionNodesWithBindings.isEmpty()) {
						extensionsWithBindings.put(node, extensionNodesWithBindings);
					}
				}
			} catch (ResourceNotAvailableException e) {
				// couldn't find the resource, so cannot gather bindings from it
			}
		}
		
		return extensionsWithBindings;
	}
}
