package uk.nhs.fhir.resourcehandlers.stu3;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;



/**
 * Allows us to customise the server Conformance resoure returned for a metadata request
 * @author adam
 *
 */
public class CustomServerConformanceProvider extends ServerCapabilityStatementProvider {
	
	public CustomServerConformanceProvider() {
		super.setCache(false);
	}
	
	public CapabilityStatement getServerConformance(HttpServletRequest request) {
		CapabilityStatement conformance = super.getServerConformance(request);
		/*
		List<ConformanceRestComponent> restList = conformance.getRest();
		for (ConformanceRestComponent rest : restList) {
			List<ConformanceRestResourceComponent> resourceList = rest.getResource();
			List<ConformanceRestResourceComponent> newResourceList = new ArrayList<ConformanceRestResourceComponent>();
			
			for (ConformanceRestResourceComponent resource : resourceList) {
				List<ResourceInteractionComponent> interactions = resource.getInteraction();
				if (interactions.isEmpty()) {
					// We've found a problem - an empty list of interactions - this isn't valid!
					// This seems to happen because the HAPI library isn't adding the validate interaction
					// for resources that only implement validate. It is however adding the validate operation, so				
					// I'm not entirely sure whether we should also see an interaction for it or not..
					
					// If we wanted to add the interaction we could do this:
					
					//RestResourceInteraction validateInteraction = new RestResourceInteraction();
					//validateInteraction.setCode(TypeRestfulInteractionEnum.VALIDATE);
					//interactions.add(validateInteraction);
					
					// Otherwise we could just remove the resource from the list...
					
				} else {
					// Only retain the resources we have interactions on.
					newResourceList.add(resource);
				}
			}
			rest.setResource(newResourceList);
		}
		*/
		return conformance;
	}
}
