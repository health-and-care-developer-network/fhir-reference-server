package uk.nhs.fhir.data.wrap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ElementDefinition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ElementDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ElementDefinition;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public abstract class WrappedElementDefinition implements HasConstraints {

	private static final String SYS_PROP_PERMITTED_MISSING_EXTENSION = "uk.nhs.fhir.permitted_missing_extension_root";
	
	public abstract String getName();
	public abstract LinkDatas getTypeLinks(Optional<StructureDefinitionRepository> structureDefinitions);
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
	public abstract Optional<ExtensionType> getExtensionType(Optional<StructureDefinitionRepository> structureDefinitions);
	public abstract Optional<String> getLinkedNodeName();
	public abstract Optional<String> getLinkedNodePath();
	public abstract List<FhirElementMapping> getMappings();
	public abstract Optional<String> getId();
	public abstract FhirVersion getVersion();
	public abstract Optional<String> getLinkedStructureDefinitionUrl();
	
	// Introduced with STU3
	public abstract Optional<String> getSliceName();
	
	public static WrappedElementDefinition fromDefinition(Object definition) {
		if (definition instanceof ElementDefinitionDt) {
			return new WrappedDstu2ElementDefinition((ElementDefinitionDt)definition);
		} else if (definition instanceof ElementDefinition) {
			return new WrappedStu3ElementDefinition((ElementDefinition)definition);
		} else {
			throw new IllegalStateException("Can't wrap element definition class " + definition.getClass().getCanonicalName());
		}
	}
	
	private final ImmutableNodePath path;
	
	public ImmutableNodePath getPath() {
		return path;
	}
	
	public WrappedElementDefinition(String path) {
		this.path = new ImmutableNodePath(Preconditions.checkNotNull(path, "path cannot be null"));
	}
	
	public String getIdentifierString() {
		return getId().orElse(getPath().toString());
	}
	
	public boolean isRootElement() {
		return getPath().isRoot();
	}

	protected ExtensionType lookupExtensionType(String typeProfile, Optional<StructureDefinitionRepository> structureDefinitions) {
		if (typeProfile == null) {
			return ExtensionType.SIMPLE;
		} else if (structureDefinitions.isPresent() 
				&& structureDefinitions.get().isCachedPermittedMissingExtension(typeProfile)) {
			return ExtensionType.SIMPLE;
		} else {
			WrappedStructureDefinition extensionDefinition;
			try {
				if (structureDefinitions.isPresent()) {
					extensionDefinition = structureDefinitions.get().getStructureDefinitionIgnoreCase(getVersion(), typeProfile);
					return extensionDefinition.getExtensionType();
				} else {
					throw new IllegalStateException("Cannot find extension type for " + typeProfile + " because there is no StructureDefinitionRepository available");
				}
			} catch (Exception e) {
				String permittedMissingExtensionRoot = System.getProperty(SYS_PROP_PERMITTED_MISSING_EXTENSION);
				
				if (!Strings.isNullOrEmpty(permittedMissingExtensionRoot)
				  && typeProfile.startsWith(permittedMissingExtensionRoot)) {
					
					EventHandlerContext.forThread().event(RendererEventType.DEFAULT_TO_SIMPLE_EXTENSION, 
						"Defaulting type to Simple for missing extension " + typeProfile + " since it begins with \"" + permittedMissingExtensionRoot + "\"");
					
					structureDefinitions.get().addCachedPermittedMissingExtension(typeProfile);
					
					return ExtensionType.SIMPLE;
				} else {
					throw e;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getPath().toString();
	}
}
