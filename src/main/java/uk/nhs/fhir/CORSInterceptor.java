/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.provider.dstu2.ServerConformanceProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PageTemplateHelper;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Class used to add CORS headers for responses to allow them to be used across origins
 *
 * @author Adam Hatherly
 */
public class CORSInterceptor extends InterceptorAdapter {

    private static final Logger LOG = Logger.getLogger(CORSInterceptor.class.getName());

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	addCORSResponseHeaders(theServletResponse);
    	return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}
    
    @Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, Bundle theResponseObject,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
			throws AuthenticationException {
    	addCORSResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}

	@Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, HttpServletRequest theServletRequest,
			HttpServletResponse theServletResponse) throws AuthenticationException {
		addCORSResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theServletRequest, theServletResponse);
	}

	@Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, TagList theResponseObject,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
			throws AuthenticationException {
		addCORSResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}
	
	protected void addCORSResponseHeaders(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
	}
}
