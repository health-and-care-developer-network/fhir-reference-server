package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.data.metadata.ArtefactType;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ConceptMap;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2OperationDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2StructureDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ValueSet;
import uk.nhs.fhir.data.wrap.dstu2.skeleton.SkeletonWrappedDstu2StructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3CodeSystem;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ConceptMap;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3MessageDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3OperationDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3SearchParameter;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3StructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ValueSet;
import uk.nhs.fhir.data.wrap.stu3.skeleton.SkeletonWrappedStu3StructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3NamingSystem;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

/*
 * ADDING NEW RESOURCE TYPES
 * 
 * To add a new resource type, create a new subclass of this class.
 * That subclass should be of the form WrappedX where X is the FHIR resource name.
 * This should then be extended by subclasses of the form WrappedStuNX where n is the FHIR version (following the existing pattern).
 * 
 * The type needs adding to the ResourceType enum.
 * On the implementation class itself, all fields that are not present in NHS Digital examples should be checked for and warned if encountered.
 * fromBaseResource() needs updating below
 * Formatters need creating and adding to ResourceFormatterFactory
 * ArtefactType needs updating with each new artefact
 * FhirBrowserRequestServlet INDEXED_TYPES needs the type to be added
 * The reference server hl7-velocity-templates/home.vm and velocity-templates/home.vm need updating
 * ResourcePageRenderer getFirstTabName() needs an entry adding
 * The appropriate [FHIRVERSION]HapiRequestHandler class(es) will need handlers adding for non-browser requests
 */

public abstract class WrappedResource<T extends WrappedResource<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(WrappedResource.class.getName());
	
	public abstract IBaseResource getWrappedResource();
	public abstract IBaseMetaType getSourceMeta();
	public abstract FhirVersion getImplicitFhirVersion();
	public abstract Optional<String> getUrl();
	public abstract void setUrl(String url);
	public abstract String getCrawlerDescription();
	
	public abstract Optional<String> getVersion();
	
	public Optional<VersionNumber> getVersionNo() {
		return getVersion().map(version -> new VersionNumber(version));
	}
	
	// Name as used in the resource's URL
	public abstract String getName();
	
	public abstract void addHumanReadableText(String textSection);
	public abstract void clearHumanReadableText();

	public Class<? extends IBaseResource> getFhirClass() {
		return getWrappedResource().getClass();
	}

	public String getOutputFolderName() {
		return getResourceType().getFolderName();
	}
	
	protected abstract ResourceMetadata getMetadataImpl(File source);
	
	public abstract ResourceType getResourceType();
    
    public ResourceMetadata getMetadata(File source) {
    	ResourceMetadata resourceMetadata = getMetadataImpl(source);
    	
    	List<SupportingArtefact> artefacts = getArtefacts(source);

		Collections.sort(artefacts, SupportingArtefact.BY_WEIGHT);
		
		resourceMetadata.setArtefacts(artefacts);
		
		return resourceMetadata;
	}
    
	private List<SupportingArtefact> getArtefacts(File source) {
		List<SupportingArtefact> artefacts = Lists.newArrayList();
		
		String resourceFilename = FileLoader.removeFileExtension(source.getName());
		File dir = new File(source.getParent());
		File artefactDir = new File(dir.getAbsolutePath() + File.separator + resourceFilename);
		
		LOG.debug("Looking for artefacts in directory:" + artefactDir.getAbsolutePath());
		
		if (artefactDir.exists() 
		  && artefactDir.isDirectory()) { 
			// Now, loop through and find any artefact files
            File[] fileList = artefactDir.listFiles();
            if (fileList != null) {
    	        for (File thisFile : fileList) {
    	        	// Add this to our list of artefacts (if we can identify what it is!
    	        	ArtefactType type = ArtefactType.getFromFilename(getResourceType(), thisFile.getName());
    	        	if (type != null) {
    	        		SupportingArtefact artefact = new SupportingArtefact(thisFile, type); 
    	        		artefacts.add(artefact);
    	        	}
    	        }
            }
		}

		return artefacts;
	}
    
	public boolean isDstu2() {
		return getImplicitFhirVersion().equals(FhirVersion.DSTU2);
	};
	public boolean isStu3() {
		return getImplicitFhirVersion().equals(FhirVersion.STU3);
	};
	
	public Optional<String> getIdFromUrl() {
		Optional<String> url = getUrl();
		
		if (url.isPresent()
		  && url.get().contains("/")
		  && !url.get().endsWith("/")) {
			String[] urlParts = url.get().split("/"); 
			String lastPart = urlParts[urlParts.length-1];
			return Optional.of(lastPart);
		}
		
		return Optional.empty();
	}
	
	private Optional<IBaseMetaType> getMeta() {
		IBaseMetaType metaInfo = getSourceMeta();
		if (!metaInfo.isEmpty()) {
			return Optional.of(metaInfo);
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getVersionId() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			return Optional.ofNullable(metaInfo.get().getVersionId());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<String> getLastUpdated() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			Date lastUpdated = metaInfo.get().getLastUpdated();
			if (lastUpdated != null) {
				return Optional.of(StringUtil.dateToString(lastUpdated));
			}
		}
		
		return Optional.empty();
	}
	
	public static WrappedResource<?> fromBaseResourceAsSkeletonIfAvailable(IBaseResource resource, File originalFile) {
		/*if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return new SkeletonWrappedDstu2StructureDefinition((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource, originalFile);
		} else*/ if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return new SkeletonWrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)resource, originalFile);
		} else {
			// No skeleton, so just create a full wrapped resource
			return fromBaseResource(resource);
		}
	}
	
	public static WrappedResource<?> fromBaseResource(IBaseResource resource) {
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return new WrappedDstu2StructureDefinition((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return new WrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			return new WrappedDstu2ValueSet((ca.uhn.fhir.model.dstu2.resource.ValueSet)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			return new WrappedStu3ValueSet((org.hl7.fhir.dstu3.model.ValueSet)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			return new WrappedDstu2OperationDefinition((ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.OperationDefinition) {
			return new WrappedStu3OperationDefinition((org.hl7.fhir.dstu3.model.OperationDefinition)resource);
		}
		
		else if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
			return new WrappedStu3CodeSystem((org.hl7.fhir.dstu3.model.CodeSystem)resource);
		}
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ConceptMap) {
			return new WrappedDstu2ConceptMap((ca.uhn.fhir.model.dstu2.resource.ConceptMap)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ConceptMap) {
			return new WrappedStu3ConceptMap((org.hl7.fhir.dstu3.model.ConceptMap)resource);
		}
		
		else if (resource instanceof org.hl7.fhir.dstu3.model.MessageDefinition) {
			return new WrappedStu3MessageDefinition((org.hl7.fhir.dstu3.model.MessageDefinition)resource);
		}
		
		else if (resource instanceof org.hl7.fhir.dstu3.model.SearchParameter) {
			return new WrappedStu3SearchParameter((org.hl7.fhir.dstu3.model.SearchParameter)resource);
		}
		else if (resource instanceof org.hl7.fhir.dstu3.model.NamingSystem ) {
            return new WrappedStu3NamingSystem((org.hl7.fhir.dstu3.model.NamingSystem)resource);
        }
		else {
			throw new IllegalStateException("Couldn't make a WrappedResource for " + resource.getClass().getCanonicalName());
		}
	}
	
	public static Optional<WrappedResource<?>> getFullWrappedResourceIfSkeleton(Optional<WrappedResource<?>> wrappedResource) {
		if (wrappedResource.isPresent()) {
			if (wrappedResource.get() instanceof uk.nhs.fhir.data.wrap.stu3.skeleton.SkeletonWrappedStu3StructureDefinition) {
				return Optional.of(((uk.nhs.fhir.data.wrap.stu3.skeleton.SkeletonWrappedStu3StructureDefinition) wrappedResource.get()).upgradeToFullWrappedResource());
			} else if (wrappedResource.get() instanceof uk.nhs.fhir.data.wrap.dstu2.skeleton.SkeletonWrappedDstu2StructureDefinition) {
				return Optional.of(((uk.nhs.fhir.data.wrap.dstu2.skeleton.SkeletonWrappedDstu2StructureDefinition) wrappedResource.get()).upgradeToFullWrappedResource());
			}
		}
		return wrappedResource;
	}
	
	public IParser newXmlParser() {
		return FhirContexts.xmlParser(getImplicitFhirVersion());
	}

	protected VersionNumber parseVersionNumber() {
		if (!getVersion().isPresent()
		  && getImplicitFhirVersion().equals(FhirVersion.DSTU2)) {
			EventHandlerContext.forThread().event(RendererEventType.DSTU2_PARSE_VERSION_NUMBER_FAILURE, "No version number present for " + getName());
    		return new VersionNumber("1.0.0");
		}
		
    	try {
    		return new VersionNumber(getVersion().get());
    	} catch (Exception e) {
        	throw new IllegalStateException("Failed to load " + getResourceType().getDisplayName() + " version number", e);
    	}
	}

	protected void checkNoInfoPresent(Object o) {
		if (o instanceof Collection<?>) {
			if (!((Collection<?>) o).isEmpty()) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		} else if (o instanceof Base) {
			if (!((Base) o).isEmpty()) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		} else {
			if (o != null) {
				throw new IllegalStateException("Expected " + o.toString() + " to be empty");
			}
		}
	}

	protected void checkNoInfoPresent(Object o, String path) {
		if (o instanceof Collection<?>) {
			if (!((Collection<?>) o).isEmpty()) {
				throw new IllegalStateException("Expected " + path + " to be empty");
			}
		} else if (o instanceof Base) {
			if (!((Base) o).isEmpty()) {
				throw new IllegalStateException("Expected " + path + " to be empty");
			}
		} else {
			if (o != null) {
				throw new IllegalStateException("Expected " + path + " to be empty");
			}
		}
	}
	
	protected void checkInfoPresent(Object o, String path) {
		if (o instanceof Collection<?>) {
			if (((Collection<?>)o).isEmpty()) {
				throw new IllegalStateException("Expected " + path + " to contain some data");
			}
		} else if (o == null) {
			throw new IllegalStateException("Expected " + path + " to be present");
		}
	}
}
