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
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PageTemplateHelper;
import uk.nhs.fhir.util.PropertyReader;

@WebServlet(urlPatterns = {"/index.html", ""}, displayName = "FHIR Server Home Page", loadOnStartup = 1)
public class IndexServlet extends javax.servlet.http.HttpServlet {
	
	private static final long serialVersionUID = -7060628622645267225L;
	private static final Logger LOG = Logger.getLogger(IndexServlet.class.getName());
	PageTemplateHelper templateHelper = null;
	private static ResourceWebHandler myWebHandler = null;
	private static String templateDirectory = PropertyReader.getProperty("templateDirectory");
	
	protected static void setResourceHandler(ResourceWebHandler webHandler) {
		myWebHandler = webHandler;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		templateHelper = new PageTemplateHelper();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter outputStream = null;
		String baseURL = req.getContextPath();
		StringBuffer content = new StringBuffer();
		
		LOG.info("IndexServlet Requested URL: " + req.getRequestURL());
		
		/* Check if this is a ReST request (e.g. paging retrieval), and if so delegate back to the RestfulServlet */
		ClientType clientType = ClientType.getTypeFromHeaders(req);
		if (clientType == clientType.NON_BROWSER) {
			RequestDispatcher rd = getServletContext().getNamedDispatcher("uk.nhs.fhir.RestfulServlet");
			rd.forward(req, resp);
			return;
		}
		
		// Load home page template
		VelocityContext context = new VelocityContext();
    	
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate(templateDirectory + "home.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	// Put content into template
    	context.put( "baseURL", baseURL );
    	context.put( "counts", myWebHandler.getResourceTypeCounts() );
    	
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	content.append(sw.toString());
		
		// Initialise the output
        resp.setStatus(200);
        resp.setContentType("text/html");
		outputStream = resp.getWriter();
		
		// Put the content into our template and stream to the response
		outputStream.append(templateHelper.wrapContentInTemplate(null, null, content, baseURL));
	}
}
