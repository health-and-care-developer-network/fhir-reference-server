package uk.nhs.fhir.interceptor;

import static ca.uhn.fhir.rest.api.RestOperationTypeEnum.METADATA;
import static uk.nhs.fhir.data.metadata.ResourceType.CONFORMANCE;
import static uk.nhs.fhir.enums.ClientType.BROWSER;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.raw.RawResourceTemplate;
import uk.nhs.fhir.servlethelpers.RawResourceRenderer;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

public class ConformanceInterceptor extends InterceptorAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(ConformanceInterceptor.class);

	private static final String CONFORMANCE_CRAWLER_DESCRIPTION = "FHIR Server Conformance Statement";
	
	private final FhirVersion fhirVersion;
    private final RawResourceRenderer myRawResourceRenderer;
	
    public ConformanceInterceptor(FhirVersion fhirVersion) {
    	this.fhirVersion = fhirVersion;
    	this.myRawResourceRenderer = new RawResourceRenderer();
    }
    
	@Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theServletRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theServletRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        
        // If this is a request from a browser for the conformance resource, render and wrap it in HTML
        if (isBrowserConformanceRequest(operation, clientType)) {
    		String baseURL = theRequestDetails.getServerBaseForRequest();
    		String wrappedContent = renderConformance(theResponseObject, mimeType, baseURL);

    		ServletUtils.setResponseContentForSuccess(theServletResponse, "text/html", wrappedContent);
    		return false;
		} else {
			return true;
		}
	}

    private boolean isBrowserConformanceRequest(RestOperationTypeEnum operation, ClientType clientType) {
    	return operation != null && operation.equals(METADATA)
    	  && clientType != null && clientType.equals(BROWSER);
    }
    
    private String renderConformance(IBaseResource conformance, MimeType mimeType, String baseURL) {
    	LOG.debug("Attempting to render conformance statement");
    	String resourceContent = myRawResourceRenderer.getRawResource(conformance, mimeType, fhirVersion);
    	
    	return new RawResourceTemplate(Optional.of(CONFORMANCE.toString()), Optional.empty(), resourceContent, mimeType).getHtml(CONFORMANCE_CRAWLER_DESCRIPTION);
    }
}
