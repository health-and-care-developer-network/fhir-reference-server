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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;




/**
 * Class used to add CORS headers for responses to allow them to be used across origins
 *
 * @author Adam Hatherly
 */
public class CORSInterceptor extends InterceptorAdapter {

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	addCORSResponseHeaders(theServletResponse);
    	return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}
    
    // Removed in HAPI 3.0.0
    //@Override
	/*public boolean outgoingResponse(RequestDetails theRequestDetails, Bundle theResponseObject,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
			throws AuthenticationException {
    	addCORSResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}*/

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
		resp.addHeader("Access-Control-Expose-Headers", "Content-Location,Location");
	}
}
