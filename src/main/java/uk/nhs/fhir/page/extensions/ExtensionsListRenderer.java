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
package uk.nhs.fhir.page.extensions;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.ServletUtils;

public class ExtensionsListRenderer {
	
	private static final Logger LOG = Logger.getLogger(ExtensionsListRenderer.class.getName());
	
	private static ExtensionsListProvider extensionsListProvider = null;
	
	public static void setResourceHandler(ExtensionsListProvider webHandler) {
		extensionsListProvider = webHandler;
	}
	
	public static void loadExtensions(HttpServletRequest req, HttpServletResponse resp,
								FHIRVersion fhirVersion, ResourceWebHandler webHandler) throws ServletException, IOException {
		LOG.fine("Requested URL: " + req.getRequestURL());
		
		String baseUrl = req.getContextPath();
		List<ResourceMetadata> extensions = extensionsListProvider.getExtensions();
		ExtensionsListPageTemplate extensionsPage = new ExtensionsListPageTemplate(baseUrl, extensions);
		String renderedExtensionsPage = extensionsPage.getHtml();
		
		ServletUtils.setResponseContentForSuccess(resp, "text/html", renderedExtensionsPage);
	}
}
