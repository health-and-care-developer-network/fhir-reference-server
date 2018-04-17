package uk.nhs.fhir.server_renderer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.render.NewMain;

public class RendererSupervisor {

	private final RendererOutputDisplay output;
	private final AtomicBoolean isRendering;
	private final List<RendererListener> listeners;
	
	public RendererSupervisor(RendererOutputDisplay output) {
		this.output = output;
		
		isRendering = new AtomicBoolean(false);
		listeners = Lists.newArrayList();
	}

	public void tryStartRendering(Path sourceDirectory, Path destinationDirectory, JFrame mainAppWindow) {
		final String outputDesc = " -> " + destinationDirectory.toString();
		
		if (sourceDirectory == null) {
			output.displayUpdate("CLICKED WITHOUT FILEPATH" + outputDesc);
		} else if (!sourceDirectory.toFile().exists()){
			output.displayUpdate("Directory " + sourceDirectory.toString() + " doesn't exist.");
		} else if (!sourceDirectory.toFile().isDirectory()){
			output.displayUpdate(sourceDirectory.toString() + " is not a directory.");
		} else {
			Thread renderer = createRenderMainThread(sourceDirectory, destinationDirectory, mainAppWindow);
			
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

	private Thread createRenderMainThread(final Path sourceDirectory, final Path destinationDirectory, final JFrame parentWindow) {
		return new Thread(new Runnable() {
			public void run() {
				try {
					for (RendererListener listener : listeners) {
						listener.startRender();
					}
					
					Optional<Set<String>> allowedMissingExtensionPrefix = 
						Optional.ofNullable(System.getProperty(WrappedElementDefinition.SYS_PROP_PERMITTED_MISSING_EXTENSION))
							.map(prop -> Sets.newHashSet(prop));
					NewMain renderer = new NewMain(sourceDirectory, destinationDirectory, allowedMissingExtensionPrefix, new DeferredDialogEventAccumulator(parentWindow));
					renderer.setContinueOnFail(true);
					renderer.setAllowCopyOnError(true);
					
					renderer.process();
					
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
