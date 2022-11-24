package uk.nhs.fhir.servlet.hapi;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.ImplementationGuide;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.NamingSystem;

import com.google.common.collect.Lists;

import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.servlet.browser.FhirBrowserRequestServlet;
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
			createResourceProvider(ResourceType.STRUCTUREDEFINITION, StructureDefinition.class),
			createResourceProvider(ResourceType.VALUESET, ValueSet.class),
			createResourceProvider(ResourceType.OPERATIONDEFINITION, OperationDefinition.class),
			createResourceProvider(ResourceType.IMPLEMENTATIONGUIDE, ImplementationGuide.class),
			//createResourceProvider(ResourceType.CONFORMANCE, Conformance.class),
			createResourceProvider(ResourceType.CODESYSTEM, CodeSystem.class),
			createResourceProvider(ResourceType.CONCEPTMAP, ConceptMap.class),
			createResourceProvider(ResourceType.MESSAGEDEFINITION, MessageDefinition.class),
			createResourceProvider(ResourceType.SEARCHPARAMETER, SearchParameter.class),
			createResourceProvider(ResourceType.NAMINGSYSTEM, NamingSystem.class)
			);
	}

}
