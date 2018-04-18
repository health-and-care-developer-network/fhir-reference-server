package uk.nhs.fhir.servlet.hapi;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.servlet.browser.FhirBrowserRequestServlet;
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
			createResourceProvider(ResourceType.STRUCTUREDEFINITION, StructureDefinition.class),
			createResourceProvider(ResourceType.VALUESET, ValueSet.class),
			createResourceProvider(ResourceType.OPERATIONDEFINITION, OperationDefinition.class),
			createResourceProvider(ResourceType.IMPLEMENTATIONGUIDE, ImplementationGuide.class),
			createResourceProvider(ResourceType.CONFORMANCE, Conformance.class));
	}

}
