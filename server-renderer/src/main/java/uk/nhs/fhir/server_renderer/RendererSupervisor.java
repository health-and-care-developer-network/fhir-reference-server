package uk.nhs.fhir.server_renderer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.FhirProfileRenderer;
import uk.nhs.fhir.render.RendererExitStatus;

public class RendererSupervisor {

	private final RendererOutputDisplay output;
	private final AtomicBoolean isRendering;
	private final List<RendererListener> listeners;
	
	public RendererSupervisor(RendererOutputDisplay output) {
		this.output = output;
		
		isRendering = new AtomicBoolean(false);
		listeners = Lists.newArrayList();
	}

	public void tryStartRendering(Path sourceDirectory, Path destinationDirectory, Path githubCacheDir, Optional<Path> logFileDir, ServerRendererWindow mainAppWindow) {
		Optional<Set<String>> allowedMissingExtensionPrefixes = mainAppWindow.getAllowedMissingExtensionPrefixes();
		Optional<Set<String>> localDomains = mainAppWindow.getLocalDomains();
		
		final String outputDesc = " -> " + destinationDirectory.toString();
		
		if (sourceDirectory == null) {
			output.displayUpdate("CLICKED WITHOUT FILEPATH" + outputDesc);
		} else if (!sourceDirectory.toFile().exists()){
			output.displayUpdate("Directory " + sourceDirectory.toString() + " doesn't exist.");
		} else if (!sourceDirectory.toFile().isDirectory()){
			output.displayUpdate(sourceDirectory.toString() + " is not a directory.");
		} else {
			long currentTimeMillis = System.currentTimeMillis();
			Thread renderer = createRenderMainThread(sourceDirectory, destinationDirectory, githubCacheDir, 
				logFileDir.map(dir -> dir.resolve("render" + currentTimeMillis + ".log")), mainAppWindow, allowedMissingExtensionPrefixes,
				localDomains);
			
			if (isRendering.compareAndSet(false, true)) {
				renderer.start();
			} else {
				// rendering already in progress
				output.displayUpdate("Tried to start rendering but it was already in progress");
			}
		}
	}
	
	public void addListener(RendererListener listener) {
		this.listeners.add(listener);
	}

	private Thread createRenderMainThread(final Path sourceDirectory, final Path destinationDirectory, final Path githubCacheDir, final Optional<Path> logFile, 
		final JFrame parentWindow, final Optional<Set<String>> allowedMissingExtensionPrefixes, final Optional<Set<String>> localDomains) {
		return new Thread(new Runnable() {
			public void run() {
				try {
					for (RendererListener listener : listeners) {
						listener.startRender();
					}
					
					Optional<String> newBaseUrl = Optional.empty();
					
					FhirProfileRenderer renderer = new FhirProfileRenderer(sourceDirectory, destinationDirectory, newBaseUrl, allowedMissingExtensionPrefixes, 
							Optional.of(githubCacheDir), new DeferredDialogEventAccumulator(parentWindow, logFile.map(Path::toFile)),
							localDomains);
					renderer.setContinueOnFail(true);
					renderer.setAllowCopyOnError(true);
					
					RendererExitStatus exitStatus = renderer.process();
					output.displayUpdate("Renderer exited with code " + exitStatus.exitCode());
				} catch (Exception e) {
					output.displayUpdate("Caught exception while rendering: ");
					e.printStackTrace();
				} finally {
					for (RendererListener listener : listeners) {
						try {
							listener.finishRender();
						} catch (Exception e) {
							output.displayException(e);
						}
					}
					
					isRendering.set(false);
				}
				
			}}, "Renderer-main");
	}

}
