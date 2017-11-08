package uk.nhs.fhir;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.util.UrlPathTokenizer;
import ca.uhn.fhir.util.UrlUtil;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.DataLoaderMessages;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.extensions.ExtensionsListRenderer;
import uk.nhs.fhir.page.list.ResourceListTemplate;
import uk.nhs.fhir.page.rendered.ResourcePageRenderer;
import uk.nhs.fhir.page.searchresults.SearchResultsTemplate;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRenderer;
import uk.nhs.fhir.servlethelpers.ServletStreamArtefact;
import uk.nhs.fhir.servlethelpers.ServletStreamExample;
import uk.nhs.fhir.servlethelpers.ServletStreamRawFile;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

@SuppressWarnings("serial")
@WebServlet(
	urlPatterns = {
		"/CodeSystem/*", "/ConceptMap/*", "/StructureDefinition/*", "/OperationDefinition/*", "/Extensions/*", "/ValueSets/*",	// index pages
		"/*", 						//DSTU2 delegated paths
		"/3.0.1/*", "/STU3/*"		//STU3 delegated paths
		}, 
	displayName = "FHIR Servlet", 
	loadOnStartup = 1)
public class FhirRequestServlet extends HttpServlet {
	private static final Logger LOG = LoggerFactory.getLogger(FhirRequestServlet.class);
	
	private final Map<FhirVersion, FhirRequestHandler> delegateHandlers = new ConcurrentHashMap<>();
	
	private final ResourceWebHandler data;
	private final RawResourceRenderer myRawResourceRenderer;
	private final ResourcePageRenderer myResourcePageRenderer;
	private final ServletStreamArtefact myArtefactStreamer;
	private final ServletStreamExample exampleStreamer;
	
	public FhirRequestServlet() {
		FilesystemIF dataSource = SharedDataSource.get();
		
		data = new ResourceWebHandler(dataSource);
		myRawResourceRenderer = new RawResourceRenderer(data);
		myResourcePageRenderer = new ResourcePageRenderer(data);
		myArtefactStreamer = new ServletStreamArtefact(dataSource);
		exampleStreamer = new ServletStreamExample(dataSource);
		
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			FhirRequestHandler requestHandler = new FhirRequestHandler(version);
			try {
				// sharing config for now. Could use servlet holders at this level if necessary to give each its own context.
				requestHandler.init(getServletConfig());
				delegateHandlers.put(version, requestHandler);
			} catch (ServletException se) {
				LOG.error("Error initialising FHIR request handler for {} requests", version.toString(), se);
			}
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    	LOG.debug("Received request for {}", request.getRequestURI());
    	
    	if(request.getRequestURI().endsWith(".css")) {
            // Stylesheets
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "text/css", request.getRequestURI());
        	return;
        } else if (request.getRequestURI().endsWith("favicon.ico")) {
        	// favicon.ico
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "image/x-icon", FhirServerProperties.getProperty("faviconFile"));
        	return;
        } else if (request.getRequestURI().startsWith("/images/") 
          || request.getRequestURI().startsWith("/js/")) {
        	// Image and JS files
        	ServletStreamRawFile.streamRawFileFromClasspath(response, null, request.getRequestURI());
        	return;
        }
    	
    	switch (ClientType.getTypeFromHeaders(request)) {
    	case NON_BROWSER:
    		delegateToAppropriateFhirServlet(request, response);
    		break;
    	case BROWSER:
            LOG.debug("This appears to be a browser, generate some HTML to return.");
    		serviceBrowserRequest(request, response);
    		break;
    	}
	}

	// Types for which index pages exist
	// TODO: can we include Extensions here?
	private static final ResourceType[] INDEXED_TYPES = new ResourceType[] {
		ResourceType.STRUCTUREDEFINITION,
		ResourceType.VALUESET,
		ResourceType.OPERATIONDEFINITION,
		ResourceType.CONCEPTMAP,
		ResourceType.CODESYSTEM};
	
	public static ResourceType[] getIndexedTypes() {
		return INDEXED_TYPES;
	}

	private void serviceBrowserRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String fullUri = request.getRequestURI();
		if (fullUri.equals("/dataLoadStatusReport")) {
        	String profileLoadMessages = DataLoaderMessages.getProfileLoadMessages();
			ServletUtils.setResponseContentForSuccess(response, "text/plain", profileLoadMessages);
			return;
        }
		
		if (fullUri.equals("/Extensions")) {
        	try {
				ExtensionsListRenderer.loadExtensions(request, response);
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
        	return;
        }
		
		if (fullUri.equals("/metadata")) {
			// just supply DSTU2 info for now
			try {
				delegateHandlers.get(FhirVersion.DSTU2).service(request, response);
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		serviceVersionedRequest(request, response, fullUri);
	}

	private void serviceVersionedRequest(HttpServletRequest request, HttpServletResponse response, String fullUri) {
		String serverBase = "";
		FhirVersion requestVersion = FhirVersion.DSTU2;
		String uriAfterBase = fullUri;
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			if (fullUri.startsWith("/" + version.toString())) {
				serverBase = "/" + version.toString();
				requestVersion = version;
				uriAfterBase = fullUri.substring(serverBase.length());
				break;
			}
		}
		
		if (uriAfterBase.startsWith("/artefact")) {
        	try {
				myArtefactStreamer.streamArtefact(request, response, requestVersion);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	return;
        }
		
		if (uriAfterBase.startsWith("/Examples/")) {
        	try {
				exampleStreamer.streamExample(uriAfterBase, response, requestVersion, myRawResourceRenderer);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	return;
		}

		Map<String, String[]> params = getSafeParameterMap(request);
		
		// Pages that display a list of resources, whether as an index or the results of a search
		for (ResourceType type : getIndexedTypes()) {
			if (uriAfterBase.equals("/" + type.getHAPIName())) {
				showListPage(requestVersion, request, response, type, params);
				return;
			}
		}
		
		RequestData theRequestDetails = extractData(requestVersion, uriAfterBase);
		String typeInRequest = theRequestDetails.getResourceName().get();
		Optional<String> requestOperation = theRequestDetails.getOperation();
		Optional<IIdType> requestId = theRequestDetails.getId();
		
    	LOG.debug("Detecting type of resource: " + typeInRequest);
    	
		ResourceType resourceType = ResourceType.getTypeFromHAPIName(typeInRequest);
		
        LOG.info("Request received - operation: " + requestOperation.toString() + ", type: " + resourceType.toString());
        
        String content;
		if (!requestOperation.isPresent()) {
    		ResourceMetadata resourceEntityByID = data.getResourceEntityByID(requestVersion, requestId.get());
    		String resourceName = resourceEntityByID.getResourceName();

        	String[] formatParams = params.get("_format");
        	String formatParam = "";
        	if (formatParams != null
        	  && formatParams.length > 0) {
        		formatParam = formatParams[0];
        	}
        	
        	MimeType mimeType = MimeType.getTypeFromHeader(formatParam);
            LOG.debug("Format to return to browser: " + mimeType.toString());
            
            switch(mimeType) {
	            case XML:
	            case JSON:
	            	IBaseResource resource = data.getResourceByID(requestVersion, requestId.get());
	                content = myRawResourceRenderer.renderSingleWrappedRAWResourceWithoutText(resource, requestVersion, resourceName, resourceType, serverBase, mimeType);
	                break;
	            case UNKNOWN_MIME:
	        	default:
	                content = myResourcePageRenderer.renderSingleResource(requestVersion, serverBase, requestId.get(), resourceName, resourceType);
            }
        } else {
        	// TODO: show a proper error page rather than stacktrace
            throw new IllegalStateException("Don't know what to do for operation " + requestOperation.toString() 
        		+ " from URL [" + request.getRequestURL() + "]"
            	+ " with query string [" + request.getQueryString() + "]");
        }

        ServletUtils.setResponseContentForSuccess(response, "text/html", content);
	}

	private void showListPage(FhirVersion version, HttpServletRequest request, HttpServletResponse response, ResourceType resourceType,
			Map<String, String[]> params) {
		String content;
		// We are showing a list of matching resources for the specified name query
		if (params.containsKey("name")) {
			List<ResourceMetadata> list = data.getAllNames(FhirVersion.DSTU2, resourceType, params.get("name")[0]);
			content = new SearchResultsTemplate(resourceType, list).getHtml();
        } else if (params.containsKey("name:contains")) {
        	List<ResourceMetadata> list = data.getAllNames(FhirVersion.DSTU2, resourceType, params.get("name:contains")[0]);
        	content = new SearchResultsTemplate(resourceType, list).getHtml();
        } else {
        	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
        	HashMap<String, List<ResourceMetadata>> groupedResources = data.getAGroupedListOfResources(resourceType);
        	content = new ResourceListTemplate(resourceType, groupedResources).getHtml();
        }
		
		ServletUtils.setResponseContentForSuccess(response, "text/html", content);
	}

	private void delegateToAppropriateFhirServlet(HttpServletRequest request, HttpServletResponse response) {
		String fullUri = request.getRequestURI();
		
		FhirVersion requestVersion = FhirVersion.DSTU2;
		//String uriAfterBase = fullUri;
		
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			if (fullUri.startsWith("/" + version.toString())) {
				//String serverBase = "/" + version.toString();
				requestVersion = version;
				//uriAfterBase = fullUri.substring(serverBase.length());
				break;
			}
		}
		
		try {
			delegateHandlers.get(requestVersion).service(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}
	
	// logic lifted from RestfulServer.populateRequestDetailsFromRequestPath()
	public static RequestData extractData(FhirVersion version, String theRequestPath) {
		StringTokenizer tok = new UrlPathTokenizer(theRequestPath);
		
		String resourceName = null;
		IIdType id = null;
		String operation = null;
		String compartment = null;
		// Secondary is for things like ..../_tags/_delete
		String secondaryOperation = null;
		
		// first can be operation, else is resourceName
		if (tok.hasMoreTokens()) {
			resourceName = tok.nextToken();
			if (partIsOperation(resourceName)) {
				operation = resourceName;
				resourceName = null;
			}
		}
	
		// second can overwrite operation without error
		// else is id
		if (tok.hasMoreTokens()) {
			String nextString = tok.nextToken();
			if (partIsOperation(nextString)) {
				operation = nextString;
			} else {
				id = FhirContexts.forVersion(version).getVersion().newIdType();
				id.setParts(null, resourceName, UrlUtil.unescape(nextString), null);
			}
		}

		// third may be special string _history, 
			// - in that case if there is an additional token it is used to update the id's versionId (must be already set so second couldn't be operation) 
			// and operation is not set
			// - if there are no additional tokens, the id is not updated and the operation is set to _history
		// otherwise if it is an operation string, provided operation wasn't already set, it can now be set
		// otherwise it sets compartment
		if (tok.hasMoreTokens()) {
			String nextString = tok.nextToken();
			if (nextString.equals(Constants.PARAM_HISTORY)) {
				if (tok.hasMoreTokens()) {
					String versionString = tok.nextToken();
					if (id == null) {
						throw new InvalidRequestException("Don't know how to handle request path: " + theRequestPath);
					}
					id.setParts(null, resourceName, id.getIdPart(), UrlUtil.unescape(versionString));
				} else {
					operation = Constants.PARAM_HISTORY;
				}
			} else if (partIsOperation(nextString)) {
				if (operation != null) {
					throw new InvalidRequestException("URL Path contains two operations: " + theRequestPath);
				}
				operation = nextString;
			} else {
				compartment = nextString;
			}
		}
	
		// if operation hasn't been set, it can now be set, and so can an additional secondary operation
		// if both operation and secondaryOperation are set, there shouldn't be any more tokens
		while (tok.hasMoreTokens()) {
			String nextString = tok.nextToken();
			if (operation == null) {
				operation = nextString;
			} else if (secondaryOperation == null) {
				secondaryOperation = nextString;
			} else {
				throw new InvalidRequestException("URL path has unexpected token '" + nextString + "' at the end: " + theRequestPath);
			}
		}
	
		return new RequestData(
			Optional.ofNullable(resourceName), 
			Optional.ofNullable(id),
			Optional.ofNullable(operation),
			Optional.ofNullable(compartment),
			Optional.ofNullable(secondaryOperation));
	}
	
	private static boolean partIsOperation(String nextString) {
		return nextString.length() > 0 
		  && (nextString.charAt(0) == '_' 
		  	|| nextString.charAt(0) == '$' 
		  	|| nextString.equals(Constants.URL_TOKEN_METADATA));
	}
	
	// Logic lifted from RestfulServer
	// Fortunately for now, we don't need to handle POST requests - otherwise we'd need to consume and parse the message body
	private Map<String, String[]> getSafeParameterMap(HttpServletRequest request) {
		//StringBuffer requestUrl = request.getRequestURL();
		//request.getQueryString();
		
		//String contentType = request.getHeader(Constants.HEADER_CONTENT_TYPE);
		
		String queryString = request.getQueryString();
		
		if (Strings.isNullOrEmpty(queryString)) {
			return Maps.newHashMap();
		}
		/*else if  (request.getMethod().toLowerCase().equals("post") 
		  && isNotBlank(contentType)
		  && contentType.startsWith(Constants.CT_X_FORM_URLENCODED)) {
			String requestBody = new String(requestDetails.loadRequestContents(), Constants.CHARSET_UTF8);
			params = UrlUtil.parseQueryStrings(theRequest.getQueryString(), requestBody);
		}*/
		else if (request.getMethod().toLowerCase().equals("get")) {
			return UrlUtil.parseQueryString(queryString);
		} else {
			return Maps.newHashMap();
		}
	}
	
}

class RequestData {
	private final Optional<String> resourceName;
	private final Optional<IIdType> id;
	private final Optional<String> operation;
	private final Optional<String> compartment;
	private final Optional<String> secondaryOperation;
	
	public RequestData(Optional<String> resourceName, Optional<IIdType> id, Optional<String> operation, 
			Optional<String> compartment, Optional<String> secondaryOperation) {
		this.resourceName = resourceName;
		this.id = id;
		this.operation = operation;
		this.compartment = compartment;
		this.secondaryOperation = secondaryOperation;
	}
	
	public Optional<String> getResourceName() {
		return resourceName;
	}
	
	public Optional<IIdType> getId() {
		return id;
	}
	
	public Optional<String> getOperation() {
		return operation;
	}
	
	public Optional<String> getCompartment() {
		return compartment;
	}
	
	public Optional<String> getSecondaryOperation() {
		return secondaryOperation;
	}
}
