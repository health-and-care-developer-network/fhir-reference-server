package uk.nhs.fhir.server_renderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ZipExporter {

	private static final Logger LOG = LoggerFactory.getLogger(ZipExporter.class);
	
	private final File toZip;
	
	public ZipExporter(File toZip) {
		Preconditions.checkNotNull(toZip);
		Preconditions.checkArgument(toZip.isDirectory());
		
		this.toZip = toZip;
	}
	
	public Optional<String> export(File zip) {
		// If no file extension was provided, default to .zip
		if (zip.getName().indexOf('.') == -1) {
			zip = new File(zip.getAbsolutePath() + ".zip");
		}
		
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
			addDirectory(toZip, "", zos);
			zos.flush();
			return Optional.empty();
		} catch (Exception e) {
			LOG.error("Caught error writing zip file. Deleting anything already written to " + zip.getAbsolutePath(), e);
			
			if (zip.exists()) {
				zip.delete();
			}
			
			String message = e.getMessage();
			if (message != null) {
				return Optional.of(e.getMessage());
			} else {
				return Optional.of("See logs for details");
			}
		}
	}

	void addDirectory(File toZip, String parentDirName, ZipOutputStream zos) throws IOException {
		String dirName = parentDirName + toZip.getName() + "/";
		for (File f : toZip.listFiles()) {
			if (f.isFile()) {
				addFile(f, dirName, zos);
			} else if (f.isDirectory()) {
				addDirectory(f, dirName, zos);
			} else {
				throw new IllegalStateException("File " + f.getAbsolutePath() + " was not a file or a directory");
			}
		}
	}

	void addFile(File toZip, String dir, ZipOutputStream zos) throws IOException, FileNotFoundException {
		String entryName = dir + toZip.getName();
		ZipEntry fileEntry = new ZipEntry(entryName);
		
		try (FileInputStream fis = new FileInputStream(toZip)) {
			zos.putNextEntry(fileEntry);
			final byte[] bytes = new byte[1024];
			int bytesRead;
			while((bytesRead = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, bytesRead);
			}
			zos.closeEntry();
		}
	}

}
