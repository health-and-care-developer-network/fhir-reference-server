package uk.nhs.fhir.page.namingsystem;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.datalayer.SharedDataSource;

import uk.nhs.fhir.page.namingsystem.NamingSystemListPageTemplate;

import uk.nhs.fhir.page.namingsystem.NamingSystemListProvider;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.ServletUtils;

public class NamingSystemListRenderer {

	private static final String NAMINGSYSTEM_CRAWLER_DESCRIPTION = "FHIR Server Naming System Registry";
	
	private static final Logger LOG = LoggerFactory.getLogger(NamingSystemListRenderer.class.getName());
	
	
	private static NamingSystemListProvider namingSystemListProvider = new ResourceWebHandler(SharedDataSource.get());
	
	
	
	public static void loadNamingSystem(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.debug("Requested URL: " + req.getRequestURL());
		
		String baseUrl = req.getContextPath();
		List<ResourceMetadata> namingsystems = namingSystemListProvider.getNamingSystem();
		NamingSystemListPageTemplate namingSystemPage = new NamingSystemListPageTemplate(baseUrl, namingsystems);
		String renderedNamingSystemPage = namingSystemPage.getHtml(NAMINGSYSTEM_CRAWLER_DESCRIPTION);
		
		ServletUtils.setResponseContentForSuccess(resp, "text/html", renderedNamingSystemPage);
	}

}