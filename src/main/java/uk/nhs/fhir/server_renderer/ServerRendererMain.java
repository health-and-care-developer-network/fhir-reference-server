package uk.nhs.fhir.server_renderer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.RestfulServlet;
import uk.nhs.fhir.datalayer.FileCache;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.SimpleFhirFileLocator;

public class ServerRendererMain 
{
	private static final Logger LOG = LoggerFactory.getLogger(ServerRendererMain.class);
	
    public static void main(String[] args) {
		Path tmpDir = getTmpDir();
		
		registerShutdownHook(tmpDir);

		Path renderedFileDir = tmpDir.resolve("rendered");
		if (!renderedFileDir.toFile().mkdir()) {
			throw new IllegalStateException("Failed to create temp directory at " + tmpDir.toString());
		}
		
		Path importedFileDir = tmpDir.resolve("imported");
		if (!importedFileDir.toFile().mkdir()) {
			throw new IllegalStateException("Failed to create temp directory at " + tmpDir.toString());
		}
		
		// FileCache will look in rendered dir and output to imported dir when it runs an import
    	SimpleFhirFileLocator serverFilePreprocessingLocator = new SimpleFhirFileLocator(renderedFileDir, importedFileDir);
		FileCache.setVersionedFileLocator(serverFilePreprocessingLocator);
    	
		startWindow(renderedFileDir, importedFileDir);
		
    	Server server = makeServer();
    	startServer(server);
    	waitForServer(server);
    }

	private static Path getTmpDir() {
		try {
			String tmpDirName = "FhirServerRenderer" + "-" + System.currentTimeMillis();
			Path tmpDir = FhirFileUtils.makeTempDir(tmpDirName, false);
			return tmpDir;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
    
	private static void registerShutdownHook(final Path tmpDir) {
		Runtime.getRuntime().addShutdownHook(
			new Thread(
				new Runnable(){

			@Override
			public void run() {
				try {
					LOG.info("Deleting temp directory (" + tmpDir.toAbsolutePath().toString() + ")");
					FileUtils.deleteDirectory(tmpDir.toFile());
					LOG.debug("Successfully deleted temp directory");
				} catch (IOException e) {
					LOG.error("Failed to delete temp directory - consider deleting manually");
					e.printStackTrace();
				}
			}
			
		}));
	}

	private static void waitForServer(Server server) {
        try {
            server.join();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	stopServer(server);
        }
	}

	private static void startWindow(Path renderedFileDir, Path importedFileDir) {
		ServerRendererWindow window = new ServerRendererWindow(renderedFileDir, importedFileDir);
		window.setVisible(true);
	}

	private static void startServer(Server server) {
		try {
            server.start();
		} catch (Exception e) {
        	throw new IllegalStateException("FHIR server failed to start", e);
        }
	}

	private static Server makeServer() {
        
		Server server = new Server(8080);
		WebAppContext handler = new WebAppContext();
		handler.setContextPath("/");
		AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration();
		CodeSource codeSource = RestfulServlet.class.getProtectionDomain().getCodeSource();
		URL location = codeSource.getLocation();
		LOG.debug("Configuration resource name: " + location.toString());
		Resource fhirReferenceServerJar = Resource.newResource(location);
		
		try {
			LOG.info("Configuring server based on Servlets found in " + fhirReferenceServerJar.getFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		handler.getMetaData().addContainerResource(fhirReferenceServerJar);
		handler.setConfigurations(new Configuration[]{annotationConfiguration});
		server.setHandler(handler);
        return server;
	}

	private static void stopServer(Server server) {
    	try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
