package uk.nhs.fhir.data.wrap.stu3;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.OperationDefinition.OperationDefinitionParameterComponent;
import org.hl7.fhir.dstu3.model.OperationDefinition.OperationParameterUse;
import org.hl7.fhir.dstu3.model.NamingSystem;
import org.hl7.fhir.dstu3.model.NamingSystem.NamingSystemUniqueIdComponent ;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.ResourceInfoType;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.namingsystem.FhirNamingSystemUniqueId;
import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.structdef.FhirElementDataTypeStu3;
import uk.nhs.fhir.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.wrap.WrappedNamingSystem;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class WrappedStu3NamingSystem extends WrappedNamingSystem{
	private final NamingSystem definition;

	//private static final Stu3FhirDocLinkFactory linkFactory = new Stu3FhirDocLinkFactory();
	
	public WrappedStu3NamingSystem(NamingSystem definition) {
		this.definition = definition;
		//checkForUnexpectedFeatures();
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public String getFhirVersion() {
		return "STU3";
	}
	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}
	
	/*private static ResourceInfo buildBindingResourceInfo(OperationDefinitionParameterBindingComponent binding) {
		String choice = FhirElementDataTypeStu3.resolveValue(binding.getValueSet());
		String strength = binding.getStrength().getDisplay();
		
		return new BindingResourceInfo(Optional.empty(), Optional.of(FhirURL.buildOrThrow(choice, FhirVersion.STU3)), strength);
	}*/

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public String getKind() {
		return StringUtil.capitaliseLowerCase(definition.getKindElement().getValueAsString());
	//	return "this is kind test value";
	}
	
	@Override
	public String getDescription() {
		return StringUtil.capitaliseLowerCase(definition.getDescription());
	}
	
	@Override
	public String getUsage() {
		return StringUtil.capitaliseLowerCase(definition.getUsage());
	}

	@Override
	public String getResponsible() {
		return StringUtil.capitaliseLowerCase(definition.getResponsible());
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatus() {
		return StringUtil.capitaliseLowerCase(String.valueOf(definition.getStatus()));
		
	}

	@Override
	public Optional<String> getDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Optional<String> getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}

	

	@Override
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<String> getVersion() {
		//return Optional.ofNullable(definition.getVersion());
		return Optional.ofNullable("1.0.0");
		
		
		
	}

	@Override
	public void addHumanReadableText(String textSection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<String> getUrl() {
		// TODO Auto-generated method stub
		//return Optional.ofNullable(definition.getUrl());
	//	return Optional.ofNullable("https://fhir.nhs.uk/Id/ods-organization-code");
		return getPreferredUrl();
	}
	

	public Optional<String> getPreferredUrl() {
	
		 List<String> preferredURL = definition.getUniqueId()
				.stream()
				.filter(preferred -> preferred.getPreferred())
				.map(param -> param.getValue())
				.collect(Collectors.toList());
		
		 if (preferredURL.size() == 0) 
			return Optional.empty();
		 else 
			return Optional.of(preferredURL.get(0));
			
	}
	
	
	
	@Override
	public List<FhirNamingSystemUniqueId> getUniqueIds() {
	
		// Adding the Preferred trues first
		List<FhirNamingSystemUniqueId> NaminsSystemUniqueIds = definition.getUniqueId()
				.stream()
				.filter(preferred -> preferred.getPreferred())
				.map(param -> 
					buildParameter("", param))
				.collect(Collectors.toList());
		
		 NaminsSystemUniqueIds.addAll( definition.getUniqueId()
					.stream()
					.filter(preferred -> !preferred.getPreferred())
					.map(param -> 
						buildParameter("", param))
					.collect(Collectors.toList()));
		
		return NaminsSystemUniqueIds;
		
	}

	
	private static FhirNamingSystemUniqueId buildParameter(String namePrefix, NamingSystemUniqueIdComponent param) {
		return new FhirNamingSystemUniqueId(
				String.valueOf(param.getType()), 
				param.getValue(), 
				param.getPreferred(),
				param.getComment());
	}
	
	

}