package uk.nhs.fhir.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.datalayer.FilesystemIF;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/InvalidateCache"}, displayName = "FHIR Server Home Page", loadOnStartup = 4)
public class InvalidateCacheServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getRemoteHost().equals("127.0.0.1")
		  || request.getRemoteHost().equals("0:0:0:0:0:0:0:1")
		  || request.getRemoteHost().equals("localhost")) {
			FilesystemIF.invalidateCache();
		} else {
			response.sendError(403, "Only available on local machine");
		}
	}
}
