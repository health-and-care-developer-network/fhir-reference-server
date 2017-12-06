package uk.nhs.fhir;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.google.common.collect.Lists;

import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/3.0.1/*", "/STU3/*"}, name = FhirBrowserRequestServlet.STU3_SERVLET_NAME, displayName = "STU3 FHIR Request Handler", loadOnStartup = 2)
public class Stu3HapiRequestHandler extends HapiRequestHandler {

	public Stu3HapiRequestHandler() {
		super(FhirVersion.STU3);
	}

	@Override
	protected List<IResourceProvider> getResourceProvidersList() {
		return Lists.newArrayList(
	        new uk.nhs.fhir.resourcehandlers.stu3.StructureDefinitionProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ValueSetProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.OperationDefinitionProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ImplementationGuideProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.CodeSystemProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ConceptMapProvider(dataSource));
	}

}
