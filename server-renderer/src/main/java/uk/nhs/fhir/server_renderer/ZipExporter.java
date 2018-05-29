package uk.nhs.fhir.server_renderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.render.format.SectionedHTMLDoc;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.HTMLUtil;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;

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
				indexFileBytes = Optional.of(buildIndexFile(toZip).getBytes("UTF-8"));
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

	private String buildIndexFile(File toZip) throws IOException, ParserConfigurationException {
		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		
		TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files = findRenderedFiles(toZip);
		
		for (Entry<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> e : files.entrySet()) {
			String fhirVersion = e.getKey();
			List<Element> resourceSections = Lists.newArrayList();
			Path fhirVersionpath = Paths.get(fhirVersion);
			
			for (Entry<String, TreeMap<String, TreeSet<File>>> e2 : e.getValue().entrySet()) {
				String resourceType = e2.getKey();
				Path resourceTypePath = fhirVersionpath.resolve(resourceType);
	
				List<Element> resourcesForSection = Lists.newArrayList();
				for (Entry<String, TreeSet<File>> resource : e2.getValue().entrySet()) {
					String resourceName = resource.getKey();
					Path resourcePath = resourceTypePath.resolve(resourceName);
					
					List<Element> artefactsForResource = Lists.newArrayList();
					for (File artefact : resource.getValue()) {
						String name = artefact.getName();
						String url = resourcePath.resolve(name).toString();
						
						Element artefactElement =
							Elements.withChild("li",
								Elements.withAttributesAndText("a", Lists.newArrayList(new Attribute("href", url)), name));
						
						artefactsForResource.add(artefactElement);
					}
					
					Element resourceElement =
						Elements.withAttributeAndChild("li", new Attribute("id", resourceName), 
							Elements.withChildren("ul", artefactsForResource));
					
					resourcesForSection.add(resourceElement);
				}
	
				Element resourceSection = 
					Elements.withChildren("div", Lists.newArrayList(
						Elements.withText("h2", resourceType),
						Elements.withChildren("ul", resourcesForSection)));
				resourceSections.add(resourceSection);
			}
			Element fhirVersionSection =
				Elements.withChildren("h1", resourceSections);
			doc.addBodyElement(fhirVersionSection);
		}
		
		doc.addStyle(new CSSStyleBlock(Lists.newArrayList("h1"), Lists.newArrayList(new CSSRule("font-weight", "500"))));
		
		return HTMLUtil.docToString(doc.getHTML(), true, false);
	}
	
	private TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> findRenderedFiles(File root) {
		TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files = Maps.newTreeMap();
		findRenderedFiles(toZip, files);
		return files;
	}

	private void findRenderedFiles(File toZip, TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files) {
		for (File f : toZip.listFiles()) {
			if (f.isDirectory()) {
				findRenderedFiles(f, files);
			} else if (f.isFile()
			  && f.getParentFile().getName().toLowerCase().contains("-versioned-")) {
				String resourceName = f.getParentFile().getName();
				String resourceTypeName = f.getParentFile().getParentFile().getName();
				String fhirVersion = f.getParentFile().getParentFile().getParentFile().getName();
				
				TreeMap<String, TreeMap<String, TreeSet<File>>> fhirVersionMap = files.putIfAbsent(fhirVersion, Maps.newTreeMap()); 
				fhirVersionMap = fhirVersionMap == null ? files.get(fhirVersion) : fhirVersionMap;
				TreeMap<String, TreeSet<File>> resourceTypeMap = fhirVersionMap.putIfAbsent(resourceTypeName, Maps.newTreeMap());
				resourceTypeMap = resourceTypeMap == null ? fhirVersionMap.get(resourceTypeName) : resourceTypeMap;
				TreeSet<File> resourceArtefactList = resourceTypeMap.putIfAbsent(resourceName, Sets.newTreeSet(Comparator.comparing(File::getName)));
				resourceArtefactList = resourceArtefactList == null ? resourceTypeMap.get(resourceName) : resourceArtefactList;
				resourceArtefactList.add(f);
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
