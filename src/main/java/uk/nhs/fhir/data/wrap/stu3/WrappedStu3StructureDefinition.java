package uk.nhs.fhir.data.wrap.stu3;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.dstu3.model.UsageContext;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeDataBuilder;
import uk.nhs.fhir.data.structdef.tree.FhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeNodeBuilder;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.util.FhirRelease;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3StructureDefinition extends WrappedStructureDefinition {

	private final StructureDefinition definition;
	
	public WrappedStu3StructureDefinition(StructureDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getKind() {
		return definition.getKind().getDisplay();
	}

	@Override
	public String getStatus() {
		return definition.getStatus().getDisplay();
	}

	@Override
	public Boolean getAbstract() {
		return definition.getAbstract();
	}

	@Override
	public Optional<String> getConstrainedType() {
		return Optional.of(definition.getType());
	}

	@Override
	public String getBase() {
		return definition.getBaseDefinition();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public Optional<String> getDisplay() {
		return Optional.ofNullable(definition.getName());
	}

	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(definition.getPublisher());
	}

	@Override
	public Optional<Date> getDate() {
		return Optional.ofNullable(definition.getDate());
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(definition.getDescription());
	}

	@Override
	public Optional<String> getFhirVersion() {
		Optional<String> fhirVersionDesc = Optional.empty();
		
		if (!Strings.isNullOrEmpty(definition.getFhirVersion())) {
			fhirVersionDesc = Optional.of(FhirRelease.forString(definition.getFhirVersion()).getDesc());
		}
		
		return fhirVersionDesc;
	}

	@Override
	public Optional<String> getContextType() {
		if (definition.hasContextType()) {
			return Optional.of(definition.getContextType().getDisplay());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public List<FhirContacts> getContacts() {
		return new Stu3FhirContactConverter().convertList(definition.getContact());
	}

	@Override
	public List<String> getUseContexts() {
		List<String> useContexts = Lists.newArrayList();
		
		for (UsageContext useContext : definition.getUseContext()) {
			if (useContext.getValue() != null) {
				throw new NotImplementedException("Don't know what to do with use context value: " + useContext.getValue().toString());
			}
			
			String text = useContext.getCode().getDisplay();
			useContexts.add(text);
		}
		
		return useContexts;
	}
	
	@Override
	public void checkUnimplementedFeatures() {
		//List<List<Content>> identifierCells = Lists.newArrayList();
		if (!definition.getIdentifier().isEmpty()) {
			throw new NotImplementedException("Identifier");
		}
		/*for (IdentifierDt identifier : definition.getIdentifier()) {
			List<Content> identifierCell = Lists.newArrayList();
			identifierCells.add(identifierCell);
			
			Optional<String> use = Optional.ofNullable(identifier.getUse());
			Optional<String> type = Optional.ofNullable(identifier.getType().getText());
			Optional<String> system = Optional.ofNullable(identifier.getSystem());
			Optional<String> value = Optional.ofNullable(identifier.getValue());
			Optional<PeriodDt> period = Optional.ofNullable(identifier.getPeriod());
			ResourceReferenceDt assigner = identifier.getAssigner();
		}*/
		
				
	}

	@Override
	public List<FhirMapping> getMappings() {
		List<FhirMapping> mappings = Lists.newArrayList();
		
		for (StructureDefinitionMappingComponent mapping : definition.getMapping()) {
			mappings.add(new FhirMapping(mapping.getIdentity(), mapping.getUri(), mapping.getName(), mapping.getComment()));
		}
		
		return mappings;
	}

	@Override
	public List<String> getUseLocationContexts() {
		List<String> useLocationContexts = Lists.newArrayList();
		
		for (StringType context : definition.getContext()) {
			useLocationContexts.add(context.getValue());
		}
		
		return useLocationContexts;
	}

	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	private static final FhirTreeNodeBuilder treeNodeBuilder = new FhirTreeNodeBuilder();
	
	@Override
	public FhirTreeData getSnapshotTree(FhirFileRegistry otherResources) {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		
		for (ElementDefinition element : definition.getSnapshot().getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element, otherResources));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}

	@Override
	public FhirTreeData getDifferentialTree(FhirFileRegistry otherResources) {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		fhirTreeDataBuilder.permitDummyNodes();
		
		for (ElementDefinition element : definition.getDifferential().getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element, otherResources));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}

	@Override
	protected void setCopyright(String copyRight) {
		definition.setCopyright(copyRight);
	}

	@Override
	public void setUrl(String url) {
		definition.setUrl(url);
	}

	@Override
	public void addHumanReadableText(String textSection) {
		try {
			Narrative textElement = Factory.newNarrative(NarrativeStatus.GENERATED, textSection);
	        definition.setText(textElement);
		} catch (IOException | FHIRException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean missingSnapshot() {
		return definition.getSnapshot().isEmpty();
	}

	@Override
	public ExtensionType getExtensionType() {
		if (!isExtension()) {
			return null;
		}
		
		if (definition
				.getSnapshot()
				.getElement()
				.stream()
				.anyMatch(element -> element.getPath().equals("Extension.extension.url"))) {
			return ExtensionType.COMPLEX;
		} else {
			return ExtensionType.SIMPLE;
		}
		
	}

	@Override
	public FhirCardinality getRootCardinality() {
		Integer min = definition.getSnapshot().getElementFirstRep().getMin();
		String max = definition.getSnapshot().getElementFirstRep().getMax();
		return new FhirCardinality(min, max);
	}

	@Override
	public Optional<String> getDifferentialRootDescription() {
		String differentialShort = definition.getDifferential().getElementFirstRep().getShort();
		return Optional.ofNullable(differentialShort);
	}

	@Override
	public String getDifferentialRootDefinition() {
		return definition.getDifferential().getElementFirstRep().getDefinition();
	}

	@Override
	public Optional<String> extensionBaseTypeDesc() {
		if (!isExtension()) {
			return Optional.empty();
		}

		List<ElementDefinition> diffElements = definition.getDifferential().getElement();
		if (diffElements.size() == 3
		  && diffElements.get(1).getPath().equals("Extension.url")) {
			// It is a simple extension, so we can also find a type
			List<TypeRefComponent> typeList = diffElements.get(2).getType();
			if (typeList.size() == 1) {
				return Optional.of(typeList.get(0).getCode());
			} else {
				return Optional.of("(choice)");
			}
		}
		
		return Optional.of("(complex)");
	}
}
