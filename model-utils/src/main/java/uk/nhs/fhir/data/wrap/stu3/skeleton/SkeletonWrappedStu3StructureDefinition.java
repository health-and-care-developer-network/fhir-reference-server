package uk.nhs.fhir.data.wrap.stu3.skeleton;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3StructureDefinition;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FhirParsingFailedException;
import uk.nhs.fhir.util.FhirVersion;

public class SkeletonWrappedStu3StructureDefinition extends WrappedStu3StructureDefinition {

	private static final Logger LOG = LoggerFactory.getLogger(SkeletonWrappedStu3StructureDefinition.class.getName());
	private final FhirFileParser parser = new FhirFileParser();
	
	private String url = null;
	private String version = null;
	private String name = null;
	private String constrainedType = null;
	private FhirCardinality rootCardinality = null;
	private List<String> useLocationContexts = null;
	private String differentialRootDescription = null;
	private String differentialRootDefinition = null;
	private String extensionBaseTypeDesc = null;
	private String status = null;
	private ExtensionType extensionType = null;
	private String kind = null;
	private String kindDisplay = null;
	private Boolean isAbstract = null;
	private boolean missingSnapshot;
	
	private File originalFile = null;
	
	/*
	private final StructureDefinition definition;
	
	*/
	public SkeletonWrappedStu3StructureDefinition(StructureDefinition definition, File originalFile) {
		super();

		this.originalFile = originalFile;		
		this.url = definition.getUrl();
		this.version = definition.getVersion();
		this.name = definition.getName();
		this.constrainedType = definition.getType();
		
		Integer min = definition.getSnapshot().getElementFirstRep().getMin();
		String max = definition.getSnapshot().getElementFirstRep().getMax();
		this.rootCardinality = new FhirCardinality(min, max);
		
		this.useLocationContexts = Lists.newArrayList();
		for (StringType context : definition.getContext()) {
			this.useLocationContexts.add(context.getValue());
		}
		
		this.differentialRootDescription = definition.getDifferential().getElementFirstRep().getShort();
		this.differentialRootDefinition = definition.getDifferential().getElementFirstRep().getDefinition();
		
		
		List<ElementDefinition> diffElements = definition.getDifferential().getElement();
		if (diffElements.size() == 3
		  && diffElements.get(1).getPath().equals("Extension.url")) {
			// It is a simple extension, so we can also find a type
			List<TypeRefComponent> typeList = diffElements.get(2).getType();
			if (typeList.size() == 1) {
				this.extensionBaseTypeDesc = typeList.get(0).getCode();
			} else {
				this.extensionBaseTypeDesc = "(choice)";
			}
		} else {
			this.extensionBaseTypeDesc = "(complex)";
		}
		
		this.status = definition.getStatus().getDisplay();
		
		if (isExtension()) {
			if (definition
					.getSnapshot()
					.getElement()
					.stream()
					.anyMatch(element -> element.getPath().equals("Extension.extension.url"))) {
				this.extensionType = ExtensionType.COMPLEX;
			} else {
				this.extensionType = ExtensionType.SIMPLE;
			}
		}
		
		this.kind = definition.getKind().toCode();
		this.kindDisplay = definition.getKind().getDisplay();
		this.isAbstract = definition.getAbstract();
		this.missingSnapshot = definition.getSnapshot().isEmpty();
	}
	
	public WrappedStu3StructureDefinition upgradeToFullWrappedResource() {
		LOG.info("Upgrading cached skeleton resource to full resource!");
		IBaseResource parsedFile;
		try {
			parsedFile = parser.parseFile(this.originalFile);
			return new WrappedStu3StructureDefinition((StructureDefinition)parsedFile);
		} catch (FhirParsingFailedException e) {
			return null;
		}
	}
	
	@Override
	public IBaseResource getWrappedResource() {
		LOG.info("Re-parsing StructureDefinition to return full wrapped resource object");
		WrappedStu3StructureDefinition fullWrappedResource = this.upgradeToFullWrappedResource();
		return fullWrappedResource.getWrappedResource();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(this.url);
	}
	
	@Override
	public String getKind() {
		return this.kind;
	}

	@Override
	public String getKindDisplay() {
		return this.kindDisplay;
	}

	@Override
	public String getStatus() {
		return this.status;
	}
	
	@Override
	public Boolean getAbstract() {
		return this.isAbstract;
	}
	
	@Override
	public Optional<String> getConstrainedType() {
		return Optional.ofNullable(this.constrainedType);
	}
	
	@Override
	public String getBase() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}
	
	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(this.version);
	}

	@Override
	public Optional<String> getDisplay() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<String> getPublisher() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<Date> getDate() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<String> getCopyright() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<String> getDescription() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<String> getFhirVersion() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public Optional<String> getContextType() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public List<FhirContacts> getContacts() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public List<String> getUseContexts() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}
	
	@Override
	public void checkUnimplementedFeatures() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public List<FhirMapping> getMappings() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public List<String> getUseLocationContexts() {
		return this.useLocationContexts;
	}

	@Override
	public IBaseMetaType getSourceMeta() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	protected void setCopyright(String copyRight) {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public void setUrl(String url) {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public void addHumanReadableText(String textSection) {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public boolean missingSnapshot() {
		return this.missingSnapshot;
	}

	@Override
	public ExtensionType getExtensionType() {
		if (!isExtension()) {
			return null;
		}
		return this.extensionType;
	}
	
	@Override
	public FhirCardinality getRootCardinality() {
		return this.rootCardinality;
	}
	
	@Override
	public Optional<String> getDifferentialRootDescription() {		
		return Optional.ofNullable(this.differentialRootDescription);
	}
	
	@Override
	public String getDifferentialRootDefinition() {
		return this.differentialRootDefinition;
	}

	@Override
	public Optional<String> extensionBaseTypeDesc() {
		if (!isExtension()) {
			return Optional.empty();
		}
		return Optional.of(this.extensionBaseTypeDesc);
	}
	
}
