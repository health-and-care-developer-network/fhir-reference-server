package uk.nhs.fhir.data.wrap.dstu2.skeleton;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2StructureDefinition;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FhirParsingFailedException;
import uk.nhs.fhir.util.FhirVersion;

public class SkeletonWrappedDstu2StructureDefinition extends WrappedDstu2StructureDefinition {

	private static final Logger LOG = LoggerFactory.getLogger(SkeletonWrappedDstu2StructureDefinition.class.getName());
	private final FhirFileParser parser = new FhirFileParser();
	
	private Optional<String> url = null;
	private String name = null;
	private Optional<String> version = null;
	private Optional<String> constrainedType = null;
	private FhirCardinality rootCardinality = null;
	private List<String> useLocationContexts = null;
	private Optional<String> differentialRootDescription = null;
	private String differentialRootDefinition = null;
	private Optional<String> extensionBaseTypeDesc = null;
	private String status = null;
	private String kind = null;
	private ExtensionType extensionType = null;
	private boolean missingSnapshot;
	private File originalFile = null;
	
	public SkeletonWrappedDstu2StructureDefinition(StructureDefinition definition, File originalFile) {
		super();

		this.originalFile = originalFile;		
		
		WrappedDstu2StructureDefinition fullWrapped = new WrappedDstu2StructureDefinition(definition);
		
		this.url = fullWrapped.getUrl();
		this.name = fullWrapped.getName();
		this.constrainedType = fullWrapped.getConstrainedType();
		this.rootCardinality = fullWrapped.getRootCardinality();
		this.useLocationContexts = fullWrapped.getUseLocationContexts();
		this.differentialRootDescription = fullWrapped.getDifferentialRootDescription();
		this.differentialRootDefinition = fullWrapped.getDifferentialRootDefinition();
		this.extensionBaseTypeDesc = fullWrapped.extensionBaseTypeDesc();
		this.version = fullWrapped.getVersion();
		this.status = fullWrapped.getStatus();
		this.kind = fullWrapped.getKind();
		this.extensionType = fullWrapped.getExtensionType();
		this.missingSnapshot = fullWrapped.missingSnapshot();
	}
	
	public WrappedDstu2StructureDefinition upgradeToFullWrappedResource() {
		LOG.debug("Upgrading cached skeleton resource to full resource!");
		IBaseResource parsedFile;
		try {
			parsedFile = parser.parseFile(this.originalFile);
			return new WrappedDstu2StructureDefinition((StructureDefinition)parsedFile);
		} catch (FhirParsingFailedException e) {
			return null;
		}
	}
	
	@Override
	public IBaseResource getWrappedResource() {
		LOG.debug("Re-parsing StructureDefinition to return full wrapped resource object");
		WrappedDstu2StructureDefinition fullWrappedResource = this.upgradeToFullWrappedResource();
		return fullWrappedResource.getWrappedResource();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Optional<String> getUrl() {
		return this.url;
	}
	
	@Override
	public String getKind() {
		return this.kind;
	}

	@Override
	public String getKindDisplay() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}

	@Override
	public String getStatus() {
		return this.status;
	}
	
	@Override
	public Boolean getAbstract() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}
	
	@Override
	public Optional<String> getConstrainedType() {
		return this.constrainedType;
	}
	
	@Override
	public String getBase() {
		throw new NotImplementedException("This is a Skeleton resource only so this method is not supported!");
	}
	
	@Override
	public Optional<String> getVersion() {
		return this.version;
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
		return this.extensionType;
	}
	
	@Override
	public FhirCardinality getRootCardinality() {
		return this.rootCardinality;
	}
	
	@Override
	public Optional<String> getDifferentialRootDescription() {		
		return this.differentialRootDescription;
	}
	
	@Override
	public String getDifferentialRootDefinition() {
		return this.differentialRootDefinition;
	}

	@Override
	public Optional<String> extensionBaseTypeDesc() {
		return this.extensionBaseTypeDesc;
	}
	
}
