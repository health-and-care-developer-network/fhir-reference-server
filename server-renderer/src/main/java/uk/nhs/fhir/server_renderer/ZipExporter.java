package uk.nhs.fhir.server_renderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

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
		
		Optional<byte[]> indexFileBytes = Optional.empty(); 
		if (!toZip.toPath().resolve("index.html").toFile().exists()) {
			try {
				indexFileBytes = Optional.of(new ZipExportIndexPage(toZip).buildIndexFile().getBytes("UTF-8"));
			} catch (IOException | ParserConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		
		// If no file extension was provided, default to .zip
		if (zip.getName().indexOf('.') == -1) {
			zip = new File(zip.getAbsolutePath() + ".zip");
		}
		
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
			if (indexFileBytes.isPresent()) {
				addFromInputStream(new ByteArrayInputStream(indexFileBytes.get()), new ZipEntry("index.html"), zos);
			}
			addRoot(toZip, zos);
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

	/*
	 * Add all contents, but not the folder itself
	 */
	private void addRoot(File toZip, ZipOutputStream zos) throws FileNotFoundException, IOException {
		for (File f : toZip.listFiles()) {
			if (f.isDirectory()) {
				addDirectory(f, "", zos);
			} else if (f.isFile()) {
				addFile(f, "", zos);
			} else {
				LOG.warn("Skipping file " + f.getAbsolutePath() + " from exported zip - not a file or directory");
			}
		}
	}

	void addDirectory(File toZip, String parentDirName, ZipOutputStream zos) throws IOException {
		
		String dirName = parentDirName + toZip.getName() + "/";
		for (File f : toZip.listFiles()) {
			if (f.isFile()) {
				String parentName = f.getParentFile().getName().toLowerCase();
				if (parentName.contains("-versioned-")) {
					addFile(f, dirName, zos);
				}
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
			addFromInputStream(fis, fileEntry, zos);
		}
	}
	
	void addFromInputStream(InputStream is, ZipEntry ze, ZipOutputStream zos) throws IOException {
		zos.putNextEntry(ze);
		final byte[] bytes = new byte[1024];
		int bytesRead;
		while((bytesRead = is.read(bytes)) >= 0) {
			zos.write(bytes, 0, bytesRead);
		}
		zos.closeEntry();
	}

}
