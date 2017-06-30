package uk.nhs.fhir.datalayer.collections;

import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.StringDt;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact.OrderByWeight;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;

public class ResourceEntity implements Comparable<ResourceEntity> {
	private String resourceName = null;
	private String resourceID = null;
	private File resourceFile = null;
	private String group = null;
	private ResourceType resourceType;
	private boolean extension = false;
	private String baseType = null;
	private String displayGroup = null;
	private boolean example = false;
	private VersionNumber versionNo = null;
	private String status = null;
	ArrayList<SupportingArtefact> artefacts = null;
	private String extensionCardinality = null;
	ArrayList<String> extensionContexts = null;
	private String extensionDescription = null;
	
	/**
	 * Extract the metadata from the resource file
	 * @param fhirVersion
	 * @param resource
	 */
	public ResourceEntity(FHIRVersion fhirVersion, File thisFile) {
		
		// Different parsing for different FHIR versions...
		if (fhirVersion.equals(FHIRVersion.DSTU2)) {
            if (resourceType == STRUCTUREDEFINITION) {
            	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(thisFile);
            	resourceName = profile.getName();
            	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
                
            	if (!extension) {
            		baseType = profile.getConstrainedType();
            	} else {
            		// Extra metadata for extensions
            		int min = profile.getSnapshot().getElementFirstRep().getMin();
            		String max = profile.getSnapshot().getElementFirstRep().getMax();
            		extensionCardinality = min + ".." + max;
            		
            		extensionContexts = new ArrayList<String>();
            		List<StringDt> contextList = profile.getContext();
            		for (StringDt context : contextList) {
            			extensionContexts.add(context.getValueAsString());
            		}
            		
            		extensionDescription = profile.getDifferential().getElementFirstRep().getShort();
            		
            		List<ElementDefinitionDt> diffElements = profile.getDifferential().getElement();
            		boolean isSimple = false;
            		if (diffElements.size() == 3) {
            			if (diffElements.get(1).getPath().equals("Extension.url")) {
            				isSimple = true;
            				// It is a simple extension, so we can also find a type
            				List<Type> typeList = diffElements.get(2).getType();
            				if (typeList.size() == 1) {
            					baseType = typeList.get(0).getCode();
            				} else {
            					baseType = "(choice)";
            				}
            			}
            		}
            		if (!isSimple) {
            			baseType = "(complex)";
            		}
            	
            	}
                String url = profile.getUrl();
                resourceID = getResourceIDFromURL(url, resourceName);
                displayGroup = baseType;
                versionNo = new VersionNumber(profile.getVersion());
                status = profile.getStatus();
            } else if (resourceType == VALUESET) {
            	displayGroup = "Code List";
            	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(thisFile);
            	resourceName = profile.getName();
            	String url = profile.getUrl();
            	resourceID = getResourceIDFromURL(url, resourceName);
            	if (FHIRUtils.isValueSetSNOMED(profile)) {
            		displayGroup = "SNOMED CT Code List";
            	}
            	versionNo = new VersionNumber(profile.getVersion());
            	status = profile.getStatus();
            } else if (resourceType == OPERATIONDEFINITION) {
            	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(thisFile);
            	resourceName = operation.getName();
            	String url = operation.getUrl();
                resourceID = getResourceIDFromURL(url, resourceName);
                displayGroup = "Operations";
                versionNo = new VersionNumber(operation.getVersion());
                status = operation.getStatus();
            } else if (resourceType == IMPLEMENTATIONGUIDE) {
            	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(thisFile);
            	resourceName = guide.getName();
            	String url = guide.getUrl();
                resourceID = getResourceIDFromURL(url, resourceName);
                displayGroup = "Implementation Guides";
                versionNo = new VersionNumber(guide.getVersion());
                status = guide.getStatus();
            }
		}
	}
	
	/**
	 * Create some metadata for the resource
	 * @param resourceName
	 * @param resourceFile
	 * @param resourceType
	 * @param extension
	 * @param baseType
	 * @param displayGroup
	 * @param example
	 * @param resourceID
	 * @param versionNo
	 * @param status
	 * @param artefacts
	 * @param cardinality
	 * @param extensionContexts
	 * @param extensionDescription
	 */
	public ResourceEntity(String resourceName, File resourceFile, ResourceType resourceType,
							boolean extension, String baseType, String displayGroup, boolean example,
							String resourceID, VersionNumber versionNo, String status,
							ArrayList<SupportingArtefact> artefacts, String cardinality,
							ArrayList<String> extensionContexts, String extensionDescription) {
		this.resourceName = resourceName;
		this.resourceFile = resourceFile;
		this.resourceType = resourceType;
		this.extension = extension;
		this.baseType = baseType;
		this.displayGroup = displayGroup;
		this.example = example;
		this.resourceID = resourceID;
		this.versionNo = versionNo;
		this.status = status;
		this.artefacts = artefacts;
		this.extensionCardinality = cardinality;
		this.extensionContexts = extensionContexts;
		this.extensionDescription = extensionDescription;
		
	}
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public File getResourceFile() {
		return resourceFile;
	}
	public void setResourceFile(File resourceFile) {
		this.resourceFile = resourceFile;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isExtension() {
		return extension;
	}
	public void setExtension(boolean extension) {
		this.extension = extension;
	}

	public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	public String getDisplayGroup() {
		return displayGroup;
	}

	public void setDisplayGroup(String displayGroup) {
		this.displayGroup = displayGroup;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public boolean isExample() {
		return example;
	}

	public void setExample(boolean example) {
		this.example = example;
	}

	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}
	
	public VersionNumber getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(VersionNumber versionNo) {
		this.versionNo = versionNo;
	}

	/**
	 * Allow resources to be sorted by name
	 */
	@Override
	public int compareTo(ResourceEntity other) {
		if (this.resourceName.equals(other.resourceName) && this.resourceType == other.resourceType) {
			return 0;
		} else {
			return this.resourceName.compareTo(other.resourceName);
		}
	}

	@Override
	public String toString() {
		int artefactCount = 0;
		if (artefacts != null) {
			artefactCount = artefacts.size();			
		}
		return "      ResourceEntity [ID=" + resourceID + ", version=" + versionNo + ", artefacts=" + artefactCount + "]";
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersionedUrl(String baseURL) {
		StringBuilder url = new StringBuilder();
    	url.append(baseURL).append("/");
    	url.append(getResourceType().getHAPIName());
    	url.append("/").append(getResourceID());
    	url.append("/_history/").append(getVersionNo());
    	return url.toString();
	}

	public ArrayList<SupportingArtefact> getArtefacts() {
		return artefacts;
	}

	public void setArtefacts(ArrayList<SupportingArtefact> artefacts) {
		this.artefacts = artefacts;
	}

	public String getExtensionCardinality() {
		return extensionCardinality;
	}

	public void setExtensionCardinality(String extensionCardinality) {
		this.extensionCardinality = extensionCardinality;
	}

	public ArrayList<String> getExtensionContexts() {
		return extensionContexts;
	}

	public void setExtensionContexts(ArrayList<String> extensionContexts) {
		this.extensionContexts = extensionContexts;
	}

	public String getExtensionDescription() {
		return extensionDescription;
	}

	public void setExtensionDescription(String extensionDescription) {
		this.extensionDescription = extensionDescription;
	}
}
