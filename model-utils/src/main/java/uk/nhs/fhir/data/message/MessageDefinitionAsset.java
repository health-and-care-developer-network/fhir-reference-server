package uk.nhs.fhir.data.message;

import com.google.common.base.Preconditions;

public class MessageDefinitionAsset {
	private final String code;
	private final String structureDefinitionReference;
	
	// If not a member of WrappedMessageDefinition.PERMITTED_VERSION_STRINGS or a valid version, triggers event UNRECOGNISED_MESSAGE_ASSET_VERSION
	private final String structureDefinitionVersion;
	
	public MessageDefinitionAsset(String code, String structureDefinitionReference, String structureDefinitionVersion) {
		this.code = Preconditions.checkNotNull(code);
		this.structureDefinitionReference = Preconditions.checkNotNull(structureDefinitionReference);
		this.structureDefinitionVersion = Preconditions.checkNotNull(structureDefinitionVersion);
	}
	
	public String getCode() {
		return code;
	}
	public String getStructureDefinitionReference() {
		return structureDefinitionReference;
	}
	public String getStructureDefinitionVersion() {
		return structureDefinitionVersion;
	}
}
