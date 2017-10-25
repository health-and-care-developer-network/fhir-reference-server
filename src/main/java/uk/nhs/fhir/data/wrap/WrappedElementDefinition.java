package uk.nhs.fhir.data.wrap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ElementDefinition;

import com.google.common.base.Strings;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ElementDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ElementDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.RendererError;
import uk.nhs.fhir.util.FhirVersion;

public abstract class WrappedElementDefinition {

	private static final String SYS_PROP_PERMITTED_MISSING_EXTENSION = "uk.nhs.fhir.permitted_missing_extension_root";

	public abstract String getName();
	public abstract String getPath();
	public abstract LinkDatas getTypeLinks();
	public abstract Set<FhirElementDataType> getDataTypes();
	public abstract ResourceFlags getResourceFlags();
	public abstract Integer getCardinalityMin();
	public abstract String getCardinalityMax();
	public abstract String getShortDescription();
	public abstract Set<String> getConditionIds();
	public abstract List<ConstraintInfo> getConstraintInfos();
	public abstract Optional<String> getDefinition();
	public abstract Optional<SlicingInfo> getSlicing();
	public abstract Optional<String> getFixedValue();
	public abstract List<String> getExamples();
	public abstract Optional<String> getDefaultValue();
	public abstract Optional<BindingInfo> getBinding();
	public abstract Optional<String> getRequirements();
	public abstract Optional<String> getComments();
	public abstract List<String> getAliases();
	public abstract Optional<ExtensionType> getExtensionType();
	public abstract Optional<String> getLinkedNodeName();
	public abstract Optional<String> getLinkedNodePath();
	public abstract List<FhirElementMapping> getMappings();
	public abstract Optional<String> getId();
	public abstract FhirVersion getVersion();
	
	// Introduced with STU3
	public abstract Optional<String> getSliceName();
	
	protected final FhirFileRegistry otherResources;
	
	public WrappedElementDefinition(FhirFileRegistry otherResources) {
		this.otherResources = otherResources;
	}
	
	public static WrappedElementDefinition fromDefinition(Object definition, FhirFileRegistry otherResources) {
		if (definition instanceof ElementDefinitionDt) {
			return new WrappedDstu2ElementDefinition((ElementDefinitionDt)definition, otherResources);
		} else if (definition instanceof ElementDefinition) {
			return new WrappedStu3ElementDefinition((ElementDefinition)definition, otherResources);
		} else {
			throw new IllegalStateException("Can't wrap element definition class " + definition.getClass().getCanonicalName());
		}
	}
	
	public String[] getPathParts() {
		return getPath().split("\\.");
	}
	
	public boolean isRootElement() {
		return getPathParts().length == 1;
	}

	protected ExtensionType lookupExtensionType(String typeProfile) {
		if (typeProfile == null) {
			return ExtensionType.SIMPLE;
		} else {
			WrappedStructureDefinition extensionDefinition;
			try {
				extensionDefinition = otherResources.getStructureDefinitionIgnoreCase(typeProfile);
				return extensionDefinition.getExtensionType();
			} catch (Exception e) {
				String permittedMissingExtensionRoot = System.getProperty(SYS_PROP_PERMITTED_MISSING_EXTENSION);
				
				if (!Strings.isNullOrEmpty(permittedMissingExtensionRoot)
				  && typeProfile.startsWith(permittedMissingExtensionRoot)) {
					String message = "Defaulting type to Simple for missing extension " + typeProfile + " since it begins with \"" + permittedMissingExtensionRoot;
					RendererError.handle(RendererError.Key.DEFAULT_TO_SIMPLE_EXTENSION, message);
					return ExtensionType.SIMPLE;
				} else {
					throw e;
				}
			}
		}
	}
}
