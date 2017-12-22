package uk.nhs.fhir.server_renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import uk.nhs.fhir.render.NewMain;

public class RendererSupervisor {

	private final RendererOutputDisplay output;
	private final AtomicBoolean isRendering;
	private final ArrayList<RendererListener> listeners;
	
	public RendererSupervisor(RendererOutputDisplay output) {
		this.output = output;
		
		isRendering = new AtomicBoolean(false);
		listeners = new ArrayList<>();
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
			
			if (!isRendering.compareAndSet(false, true)) {
				// rendering already in progress
				output.displayUpdate("Tried to start rendering but it was already in progress");
			} else {
				renderer.start();
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
					
					NewMain renderer = new NewMain(sourceDirectory, destinationDirectory, new DeferredDialogEventAccumulator(parentWindow));
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
