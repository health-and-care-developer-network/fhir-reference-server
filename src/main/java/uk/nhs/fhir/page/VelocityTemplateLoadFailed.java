package uk.nhs.fhir.page;

@SuppressWarnings("serial")
public class VelocityTemplateLoadFailed extends IllegalStateException {

	public VelocityTemplateLoadFailed(String templateName, Exception e) {
		super(templateName, e);
	}

}