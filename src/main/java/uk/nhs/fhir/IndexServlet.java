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
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.util.FileLoader;

@WebServlet(urlPatterns = {"/index.html", ""}, displayName = "FHIR Server Home Page", loadOnStartup = 1)
public class IndexServlet extends javax.servlet.http.HttpServlet {
	
	private static final Logger LOG = Logger.getLogger(IndexServlet.class.getName());
	private String template = null;
	
	@Override
	public void init() throws ServletException {
		template = FileLoader.loadFileOnClasspath("/template/index.html");
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter outputStream = null;
		StringBuffer content = new StringBuffer();
		
		LOG.info("Requested URL: " + req.getRequestURL());
		
		// Page content
		content.append("<p><a href=\"StructureDefinition\">List all StructureDefinitions</a></p>");
		content.append("<p><a href=\"StructureDefinition?name:contains=ati\">List all StructureDefinitions with 'ati' in their name</a></p>");
		content.append("<p><a href=\"metadata\">Conformance</a></p>");
		
		// Initialise the output
        resp.setStatus(200);
        resp.setContentType("text/html");
		outputStream = resp.getWriter();
		
		// Put the content into our template
	    String outputString = template;
        outputString = outputString.replaceFirst("\\{\\{PAGE-CONTENT\\}\\}", content.toString());
        
        // Send it to the output
        outputStream.append(outputString);
	}
}
