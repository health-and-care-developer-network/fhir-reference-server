package uk.nhs.fhir.server_renderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.function.Supplier;

import javax.swing.JFrame;

public class RunRendererActionListener implements ActionListener {

	private final Supplier<Path> filePathSupplier;
	private final Path outputDirectory;
	private final RendererSupervisor renderer;
	private final JFrame mainAppWindow;
	
	public RunRendererActionListener(JFrame mainAppWindow, Supplier<Path> filePathSupplier, RendererOutputDisplay output, Path outputDirectory, RendererListener... listeners) {
		if (filePathSupplier == null) {
			throw new NullPointerException("File path supplier cannot be null");
		}
		if (output == null) {
			throw new NullPointerException("Output display cannot be null");
		}
		if (outputDirectory == null) {
			throw new NullPointerException("Output display cannot be null");
		}
		
		this.mainAppWindow = mainAppWindow;
		this.filePathSupplier = filePathSupplier;
		this.outputDirectory = outputDirectory;
		
		this.renderer = new RendererSupervisor(output);
		for (RendererListener listener : listeners) {
			renderer.addListener(listener);
		}
	}
	
	public void actionPerformed(ActionEvent event) {
		renderer.tryStartRendering(filePathSupplier.get(), outputDirectory, mainAppWindow);
	}
}
