package uk.nhs.fhir;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;

import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
/*@WebServlet(
	urlPatterns = {
		"/CodeSystem/*", "/ConceptMap/*", "/StructureDefinition/*", "/OperationDefinition/*", "/Extensions/*", "/ValueSets/*",	// index pages
		"/*", 						//DSTU2
		"/3.0.1/*", "/STU3/*"		//STU3
		}, 
	displayName = "FHIR Servlet", 
	loadOnStartup = 1)*/
public class FhirRequestServlet extends HttpServlet {
	
	private final Map<FhirVersion, FhirRequestHandler> delegateHandlers = new ConcurrentHashMap<>();
	
	private static final FhirVersion[] supportedVersions = new FhirVersion[]{FhirVersion.DSTU2, FhirVersion.STU3};
	
	public FhirRequestServlet() {
		FilesystemIF dataSource = SharedDataSource.get();
		
		for (FhirVersion version : supportedVersions) {
			delegateHandlers.put(version, new FhirRequestHandler(version, dataSource));
		}
	}
}
