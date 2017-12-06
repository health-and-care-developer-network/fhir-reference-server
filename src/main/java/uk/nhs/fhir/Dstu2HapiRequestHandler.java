package uk.nhs.fhir;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.google.common.collect.Lists;

import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"", "/*"}, name = FhirBrowserRequestServlet.DSTU2_SERVLET_NAME, displayName = "DSTU2 FHIR Request Handler", loadOnStartup = 2)
public class Dstu2HapiRequestHandler extends HapiRequestHandler {
	
	public Dstu2HapiRequestHandler() {
		super(FhirVersion.DSTU2);
	}

	@Override
	protected List<IResourceProvider> getResourceProvidersList() {
		return Lists.newArrayList(
			new uk.nhs.fhir.resourcehandlers.dstu2.StructureDefinitionProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ValueSetProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.OperationDefinitionProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ImplementationGuideProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ConformanceProvider(dataSource));
	}

}
