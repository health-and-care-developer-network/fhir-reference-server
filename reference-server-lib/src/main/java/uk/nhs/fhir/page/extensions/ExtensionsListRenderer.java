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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.ServletUtils;

public class ExtensionsListRenderer {

	private static final String EXTENSIONS_CRAWLER_DESCRIPTION = "FHIR Server Extensions Registry";
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtensionsListRenderer.class.getName());
	
	private static ExtensionsListProvider extensionsListProvider = new ResourceWebHandler(SharedDataSource.get());
	
	public static void loadExtensions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.debug("Requested URL: " + req.getRequestURL());
		
		String baseUrl = req.getContextPath();
		List<ResourceMetadata> extensions = extensionsListProvider.getExtensions();
		ExtensionsListPageTemplate extensionsPage = new ExtensionsListPageTemplate(baseUrl, extensions);
		String renderedExtensionsPage = extensionsPage.getHtml(EXTENSIONS_CRAWLER_DESCRIPTION);
		
		ServletUtils.setResponseContentForSuccess(resp, "text/html", renderedExtensionsPage);
	}
}
