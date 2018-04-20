package uk.nhs.fhir.data.message;

import uk.nhs.fhir.data.url.FhirURL;

public class MessageResponse {

	private final FhirURL messageDefinitionId;
	
	public MessageResponse(FhirURL messageDefinitionId) {
		this.messageDefinitionId = messageDefinitionId;
	}
	
	public FhirURL getMessageDefinitionId() {
		return messageDefinitionId;
	}
	
}
