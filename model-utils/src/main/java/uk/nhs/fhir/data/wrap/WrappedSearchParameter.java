package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.structdef.FhirContacts;

public abstract class WrappedSearchParameter extends WrappedResource<WrappedSearchParameter> {

	public abstract String getStatus();
	public abstract Optional<Date> getDate();
	public abstract Optional<String> getPublisher();
	public abstract List<FhirContacts> getContacts();
	public abstract Optional<String> getPurpose();
	public abstract String getUrlCode();
	public abstract List<String> getAssociatedResourceTypes();
	public abstract String getType();
	public abstract String getDescription();
	public abstract Optional<String> getExpression();
	public abstract Optional<String> getXPath();
	public abstract Optional<String> getXPathUsage();
	public abstract List<String> getSupportedComparators();
	public abstract List<String> getModifiers();
	
	public abstract List<String> getInvocations();
}
