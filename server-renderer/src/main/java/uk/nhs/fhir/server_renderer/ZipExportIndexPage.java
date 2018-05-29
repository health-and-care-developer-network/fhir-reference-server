package uk.nhs.fhir.server_renderer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.metadata.ArtefactType;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.render.format.SectionedHTMLDoc;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.HTMLUtil;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.util.FhirVersion;

public class ZipExportIndexPage {

	private static final Logger LOG = LoggerFactory.getLogger(ZipExportIndexPage.class);
	
	private static final String RESOURCE_TYPE_CSS_CLASS = "resourceType";
	private static final String FHIR_VERSION_CSS_CLASS = "fhirVersion";
	private static final String RESOURCE_CSS_CLASS = "resource";
	
	private final File root;

	public ZipExportIndexPage(File root) {
		this.root = root;
	}
	
	public String buildIndexFile() throws IOException, ParserConfigurationException {
		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		
		TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files = findRenderedFiles();
		
		for (Entry<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> e : files.entrySet()) {
			String fhirVersion = e.getKey();
			List<Element> resourceSections = Lists.newArrayList();
			resourceSections.add(Elements.withText("h1", fhirVersion));
			Path fhirVersionpath = Paths.get(fhirVersion);
			
			for (Entry<String, TreeMap<String, TreeSet<File>>> e2 : e.getValue().entrySet()) {
				String resourceType = e2.getKey();
				Path resourceTypePath = fhirVersionpath.resolve(resourceType);
				
				List<Element> resourcesForSection = Lists.newArrayList();
				for (Entry<String, TreeSet<File>> resource : e2.getValue().entrySet()) {
					String resourceName = resource.getKey();
					Path resourcePath = resourceTypePath.resolve(resourceName);
					
					Optional<String> displayVersion = Optional.empty();
					String resourceDisplayName = resourceName;
					if (resourceName.contains("-versioned-")) {
						String[] versionedResourceTokens = resourceName.split("-versioned-");
						resourceDisplayName = versionedResourceTokens[0];
						displayVersion = Optional.of(versionedResourceTokens[1]);
					}
					
					List<Element> artefactsForResource = Lists.newArrayList();
					for (File artefact : resource.getValue()) {
						String name = artefact.getName();
						String url = resourcePath.resolve(name).toString();
						
						Element artefactElement =
							Elements.withChild("li",
								Elements.withAttributesAndText("a", Lists.newArrayList(new Attribute("href", url)), name));
						
						artefactsForResource.add(artefactElement);
					}
					
					Element resourceNameElement;
					if (displayVersion.isPresent()) {
						resourceNameElement =
							Elements.withChildren("span",
								Lists.newArrayList(
									Elements.withText("h3", resourceDisplayName),
									Elements.withAttributeAndChild("div", new Attribute("class", "versionText"), 
										Elements.withText("div", "v" + displayVersion.get()))));
					} else {
						resourceNameElement =
							Elements.withChild("span",
								Elements.withText("h3", resourceDisplayName));
					}
					
					Element resourceElement =
						Elements.withAttributeAndChildren("li", 
							new Attribute("class", RESOURCE_CSS_CLASS),
							Lists.newArrayList(
								resourceNameElement,
								Elements.withChildren("ul", artefactsForResource)));
					
					resourcesForSection.add(resourceElement);
				}
	
				Element resourceSection = 
					Elements.withAttributeAndChildren("div", new Attribute("class", RESOURCE_TYPE_CSS_CLASS), Lists.newArrayList(
						Elements.withText("h2", resourceType),
						Elements.withChildren("ul", resourcesForSection)));
				resourceSections.add(resourceSection);
			}
			
			Element fhirVersionSection = Elements.withAttributeAndChildren("div", new Attribute("class", FHIR_VERSION_CSS_CLASS), resourceSections);
			doc.addBodyElement(fhirVersionSection);
		}
		
		doc.addStyles(getStyles());
		
		return HTMLUtil.docToString(doc.getHTML(), true, false);
	}

	private static final Map<ResourceType, Map<String, Integer>> artefactWeightings = Maps.newHashMap();
	static {
		for (FhirVersion v : FhirVersion.getSupportedVersions()) {
			for (ResourceType type : ResourceType.typesForFhirVersion(v)) {
				Map<String, Integer> artefacts = 
					Arrays.asList(ArtefactType.values()).stream()
						.filter(artefactType -> artefactType.getRelatesToResourceType().equals(type))
						.collect(Collectors.toMap(
							artefactType -> artefactType.getFilename(), 
							artefactType -> artefactType.getWeight()));
				artefactWeightings.put(type, artefacts);
			}
		}
	}

	/**
	 * This was built under the assumption that there will always be a folder passed in consisting of at least:
	 * - FHIR version folder (e.g. STU3) > Resource type folder (e.g. StructureDefinition) > Resource artefact folder > artefacts
	 * It is agnostic to other folders nested around these.
	 */
	private TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> findRenderedFiles() {
		TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files = Maps.newTreeMap();
		findRenderedFiles(root, files);
		return files;
	}
	
	/*
	 * Collects all artefacts and orders by weighting as recorded in ArtefactType
	 */
	private void findRenderedFiles(File toZip, TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<File>>>> files) {
		for (File f : toZip.listFiles()) {
			if (f.isDirectory()) {
				findRenderedFiles(f, files);
			} else if (f.isFile()
			  && f.getParentFile().getName().toLowerCase().contains("-versioned-")) {
				String resourceName = f.getParentFile().getName();
				String resourceTypeName = f.getParentFile().getParentFile().getName();
				ResourceType type = ResourceType.getTypeFromHAPIName(resourceTypeName);
				if (type.equals(ResourceType.OTHER)) {
					LOG.warn("Failed to add file " + f.getAbsolutePath() + " to the exported zip index page due to mismatched HAPI name: " + resourceTypeName);
					continue;
				}
				
				String fhirVersion = f.getParentFile().getParentFile().getParentFile().getName();
				if (artefactWeightings.get(type).get(f.getName()) < 0) {
					// not displayed on live site
					continue;
				}
				
				TreeMap<String, TreeMap<String, TreeSet<File>>> fhirVersionMap = files.putIfAbsent(fhirVersion, Maps.newTreeMap()); 
				fhirVersionMap = fhirVersionMap == null ? files.get(fhirVersion) : fhirVersionMap;
				TreeMap<String, TreeSet<File>> resourceTypeMap = fhirVersionMap.putIfAbsent(resourceTypeName, Maps.newTreeMap());
				resourceTypeMap = resourceTypeMap == null ? fhirVersionMap.get(resourceTypeName) : resourceTypeMap;
				TreeSet<File> resourceArtefactList = 
					resourceTypeMap.putIfAbsent(resourceName, 
						Sets.newTreeSet(Comparator.comparing(file -> artefactWeightings.get(type).get(file.getName()))));
				resourceArtefactList = resourceArtefactList == null ? resourceTypeMap.get(resourceName) : resourceArtefactList;
				try {
					resourceArtefactList.add(f);
				} catch (NullPointerException e) {
					LOG.warn("Failed to add file " + f.getAbsolutePath() + " to the exported zip index page. Does it have an associated ArtefactType?");
					e.printStackTrace();
				}
			}
		}
	}
	
	private List<CSSStyleBlock> getStyles() {
		return Lists.newArrayList(
			new CSSStyleBlock(
				Lists.newArrayList("body"),
				Lists.newArrayList(
					new CSSRule("font-family", "\"Ubuntu\",\"Helvetica Neue\", Helvetica, Arial, sans-serif"),
					new CSSRule("background-color", "#f3f3f3"))),
			new CSSStyleBlock(
				Lists.newArrayList("div." + FHIR_VERSION_CSS_CLASS + " h1"), 
				Lists.newArrayList(
					new CSSRule("width", "75%"),
					new CSSRule("margin", "auto"),
					new CSSRule("margin-after", "0.5em"),
					new CSSRule("text-align", "center"),
					new CSSRule("font-weight", "500"),
					new CSSRule("font-size", "2em"),
					new CSSRule("color", "black"),
					new CSSRule("background-color", "lightblue"),
					new CSSRule("border", "3px solid #0272c6"))),
			new CSSStyleBlock(
				Lists.newArrayList("div." + FHIR_VERSION_CSS_CLASS),
				Lists.newArrayList(
					new CSSRule("margin", "auto"),
					new CSSRule("padding", "0.5em"),
					new CSSRule("width", "80%"),
					new CSSRule("max-width", "800px"))),
			new CSSStyleBlock(
				Lists.newArrayList("div." + RESOURCE_TYPE_CSS_CLASS),
				Lists.newArrayList(
					//new CSSRule("box-shadow", "4px 4px 8px 5px rgba(0,0,0,0.3)"),
					new CSSRule("padding", "1em"),
					new CSSRule("background-color", "#ffffff"))),
			new CSSStyleBlock(
				Lists.newArrayList("div." + RESOURCE_TYPE_CSS_CLASS),
				Lists.newArrayList(
					new CSSRule("margin", "1.4em 0em 0em 0em"))),
			new CSSStyleBlock(
				Lists.newArrayList("h2"),
				Lists.newArrayList(
					new CSSRule("margin", "0em 0em 1.0em 0em"))),
			new CSSStyleBlock(
				Lists.newArrayList("h3"),
				Lists.newArrayList(
					new CSSRule("margin", "0em"),
					new CSSRule("font-weight", "normal"))),
			new CSSStyleBlock(
				Lists.newArrayList("li." + RESOURCE_CSS_CLASS + " ul li"),
				Lists.newArrayList(
					new CSSRule("display", "inline-block"),
					new CSSRule("padding", "0.2em 0.2em"),
					new CSSRule("margin", "0.25em"),
					new CSSRule("background-color", "lightblue"))),
			new CSSStyleBlock(
				Lists.newArrayList("a", "a:link", "a:visited"),
				Lists.newArrayList(
					new CSSRule("text-decoration", "none"),
					new CSSRule("color", "black"),
					new CSSRule("background-color", "#c0eaf4"))),
			new CSSStyleBlock(
				Lists.newArrayList("a:hover"),
				Lists.newArrayList(
					new CSSRule("text-decoration", "none"),
					new CSSRule("color", "black"),
					new CSSRule("background-color", "lightgrey"))),
			new CSSStyleBlock(
				Lists.newArrayList(".versionText", "h3"),
				Lists.newArrayList(
					new CSSRule("display", "inline-block"))),
			new CSSStyleBlock(
					Lists.newArrayList(".versionText"),
					Lists.newArrayList(
						new CSSRule("padding", "0.2em 0.3em"),
						new CSSRule("background-color", "#f0f0f0"),
						new CSSRule("vertical-align", "baseline"))),
			new CSSStyleBlock(
					Lists.newArrayList(".versionText div"),
					Lists.newArrayList(
						new CSSRule("font-size", "1.2em"),
						new CSSRule("vertical-align", "baseline"))),
			new CSSStyleBlock(
				Lists.newArrayList("ul"),
				Lists.newArrayList(
					new CSSRule("list-style-type", "none"),
					new CSSRule("margin", "0em"),
					new CSSRule("padding", "0em"))));
	}
}
