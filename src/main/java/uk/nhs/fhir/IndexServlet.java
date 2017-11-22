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

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.page.home.HomePageTemplate;
import uk.nhs.fhir.page.home.ResourceCountsProvider;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.ServletUtils;

@WebServlet(urlPatterns = {"/index.html", ""}, displayName = "FHIR Server Home Page", loadOnStartup = 1)
public class IndexServlet extends javax.servlet.http.HttpServlet {
	
	private static final long serialVersionUID = -7060628622645267225L;
	private static final Logger LOG = LoggerFactory.getLogger(IndexServlet.class.getName());
	private static final ResourceCountsProvider resourceCountsProvider = new ResourceWebHandler(SharedDataSource.get());
	
	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.info("IndexServlet Requested URL: " + req.getRequestURL());
		
		/* Check if this is a ReST request (e.g. paging retrieval), and if so delegate back to the RestfulServlet */
		ClientType clientType = ClientType.getTypeFromHeaders(req);
		if (clientType == ClientType.NON_BROWSER) {
			RequestDispatcher rd = getServletContext().getNamedDispatcher("uk.nhs.fhir.RestfulServlet");
			rd.forward(req, resp);
			return;
		}
    	
		// Render the home page
		String baseUrl = req.getContextPath();
    	HashMap<String, Integer> resourceCounts = resourceCountsProvider.getResourceTypeCounts();
		String content = new HomePageTemplate(baseUrl, resourceCounts).getHtml();
    	
		ServletUtils.setResponseContentForSuccess(resp, "text/html", content);
	}
}
