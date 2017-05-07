package uk.nhs.fhir;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.Conformance.Rest;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResource;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResourceInteraction;

/**
 * Allows us to customise the server Conformance resoure returned for a metadata request
 * @author adam
 *
 */
public class CustomServerConformanceProvider extends ca.uhn.fhir.rest.server.provider.dstu2.ServerConformanceProvider {
	
	public CustomServerConformanceProvider() {
		super.setCache(false);
	}
	
	public Conformance getServerConformance(HttpServletRequest request) {
		Conformance conformance = super.getServerConformance(request);
		List<Rest> restList = conformance.getRest();
		for (Rest rest : restList) {
			List<RestResource> resourceList = rest.getResource();
			List<RestResource> newResourceList = new ArrayList<RestResource>();
			
			for (RestResource resource : resourceList) {
				List<RestResourceInteraction> interactions = resource.getInteraction();
				if (interactions.isEmpty()) {
					// We've found a problem - an empty list of interactions - this isn't valid!
					// This seems to happen because the HAPI library isn't adding the validate interaction
					// for resources that only implement validate. It is however adding the validate operation, so				
					// I'm not entirely sure whether we should also see an interaction for it or not..
					
					// If we wanted to add the interaction we could do this:
					/*
					RestResourceInteraction validateInteraction = new RestResourceInteraction();
					validateInteraction.setCode(TypeRestfulInteractionEnum.VALIDATE);
					interactions.add(validateInteraction);
					*/
					
					// Otherwise we could just remove the resource from the list...
					
				} else {
					// Only retain the resources we have interactions on.
					newResourceList.add(resource);
				}
			}
			rest.setResource(newResourceList);
		}
		return conformance;
	}
}
