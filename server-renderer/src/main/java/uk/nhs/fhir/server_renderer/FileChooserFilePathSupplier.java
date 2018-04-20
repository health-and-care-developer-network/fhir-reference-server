package uk.nhs.fhir.server_renderer;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

import javax.swing.JFileChooser;

public class FileChooserFilePathSupplier implements Supplier<Path> {

	private final JFileChooser chooserToInspect;
	
	public FileChooserFilePathSupplier(JFileChooser chooserToInspect) {
		this.chooserToInspect = chooserToInspect;
	}
	
	public Path get() {
		File selectedFile = chooserToInspect.getSelectedFile();
		if (selectedFile == null) {
			return null;
		} else {
			return selectedFile.toPath();
		}
	}

}
