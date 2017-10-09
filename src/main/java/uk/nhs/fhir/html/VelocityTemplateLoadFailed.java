package uk.nhs.fhir.html;

@SuppressWarnings("serial")
public class VelocityTemplateLoadFailed extends IllegalStateException {

	public VelocityTemplateLoadFailed(String templateName, Exception e) {
		super(templateName, e);
	}

}