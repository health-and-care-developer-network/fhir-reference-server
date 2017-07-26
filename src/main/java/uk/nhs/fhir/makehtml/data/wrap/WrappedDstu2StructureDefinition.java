package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Contact;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Mapping;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirContact;
import uk.nhs.fhir.makehtml.data.FhirContacts;
import uk.nhs.fhir.makehtml.data.FhirMapping;
import uk.nhs.fhir.makehtml.data.FhirRelease;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeDataBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeNodeBuilder;

public class WrappedDstu2StructureDefinition extends WrappedStructureDefinition {
	private final StructureDefinition definition;
	
	public WrappedDstu2StructureDefinition(StructureDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public String getUrl() {
		return definition.getUrl();
	}

	@Override
	public String getKind() {
		return definition.getKind();
	}

	@Override
	public String getStatus() {
		return definition.getStatus();
	}

	@Override
	public Boolean getAbstract() {
		return definition.getAbstract();
	}

	@Override
	public Optional<String> getConstrainedType() {
		return Optional.of(definition.getConstrainedType());
	}

	@Override
	public String getBase() {
		return definition.getBase();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public Optional<String> getDisplay() {
		return Optional.ofNullable(definition.getDisplay());
	}

	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(definition.getPublisher());
	}

	@Override
	public Date getDate() {
		return definition.getDate();
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
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
		return Optional.ofNullable(definition.getContextType());
	}

	@Override
	public List<FhirContacts> getContacts() {
		List<FhirContacts> contacts = Lists.newArrayList();
		
		for (Contact contact : definition.getContact()) {
			FhirContacts fhirContact = new FhirContacts(contact.getName());
			
			for (ContactPointDt telecom : contact.getTelecom()){
				String value = telecom.getValue();
				Integer rank = telecom.getRank();
				fhirContact.addTelecom(new FhirContact(value, rank));
			}
			
			contacts.add(fhirContact);
		}
		
		return contacts;
	}

	@Override
	public List<String> getUseContexts() {
		List<String> useContexts = Lists.newArrayList();
		
		for (CodeableConceptDt useContext : definition.getUseContext()) {
			for (CodingDt coding : useContext.getCoding()) {
				throw new NotImplementedException("Don't know what to do with use context code: " + coding.toString());
			}
			
			String text = useContext.getText();
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
		
		//List<String> indexingCodes = Lists.newArrayList();
		if (!definition.getCode().isEmpty()) {
			throw new NotImplementedException("Code");
		}
		/*for (CodingDt code : definition.getCode()) {
			//indexingCodes.add(code.getCode());
		}*/
		
		if (!definition.getRequirements().isEmpty()) {
			throw new NotImplementedException("NHS Digital StructureDefinitions shouldn't contain requirements");
		}
				
	}

	@Override
	public List<FhirMapping> getMappings() {
		List<FhirMapping> mappings = Lists.newArrayList();
		
		for (Mapping mapping : definition.getMapping()) {
			mappings.add(new FhirMapping(mapping.getIdentity(), mapping.getUri(), mapping.getName(), mapping.getComments()));
		}
		
		return mappings;
	}

	@Override
	public List<String> getUseLocationContexts() {
		List<String> useLocationContexts = Lists.newArrayList();
		
		for (StringDt context : definition.getContext()) {
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
	public FhirTreeData getSnapshotTree() {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		
		for (ElementDefinitionDt element : definition.getSnapshot().getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}

	@Override
	public FhirTreeData getDifferentialTree() {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		fhirTreeDataBuilder.permitDummyNodes();
		
		for (ElementDefinitionDt element : definition.getDifferential().getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
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
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}

	@Override
	public boolean missingSnapshot() {
		return definition.getSnapshot().isEmpty();
	}
}
