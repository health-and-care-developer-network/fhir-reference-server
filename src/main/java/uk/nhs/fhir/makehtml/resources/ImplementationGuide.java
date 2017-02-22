package uk.nhs.fhir.makehtml.resources;

import static uk.nhs.fhir.makehtml.XMLParserUtils.getFirstNamedChildValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.MarkdownProcessor;

public class ImplementationGuide {
	
	private static final Logger LOG = Logger.getLogger(ImplementationGuide.class.getName());
	
    /**
     * Make the html narrative section for the ImplementationGuide described in this XML
     *
     * @param thisDoc   The XML Document as an org.w3c.dom.Document
     *
     * @return          Valid xhtml fully describing the ImplementationGuide
     */
    @SuppressWarnings("unchecked")
	public static String makeHTMLForImplementationGuide(Document thisDoc, File folder) {
    	
    	StringBuilder sb = new StringBuilder();

        Element root = (Element) thisDoc.getFirstChild();

        sb.append("<div style='font-family: sans-serif;' xmlns='http://www.w3.org/1999/xhtml'>\n");

        
        // Load in the relevant markdown file and inject it here.
        // TODO: Add support for other mime types and child pages - is this required?
        
        NodeList pageSet = thisDoc.getElementsByTagName("page");
        if(pageSet.getLength() > 0) {
        	// Only one page at the top level
        	Element pageElement = (Element) pageSet.item(0);
        	String source = getFirstNamedChildValue(pageElement, "source");
        	String name = getFirstNamedChildValue(pageElement, "name");
        	String kind = getFirstNamedChildValue(pageElement, "kind");
        	String format = getFirstNamedChildValue(pageElement, "format");
        	
        	String content_to_inject = null;
        	if (source != null) {
        		// Go and retrieve the content from the source URL - assume it is a file in the same directory!
        		File srcUrl = new File(folder.getAbsolutePath() + "/" + source);
        		content_to_inject = FileLoader.loadFile(srcUrl);
        	}
        	
        	if (format.equalsIgnoreCase("text/markdown")) {
        		// Compile and Inject markdown content
        		String html = MarkdownProcessor.renderMarkdown(content_to_inject);
        		sb.append("<div class='markdown_page'>").append(html).append("</div>");
        	} else if (format.startsWith("text")) {
        		// Inject the text straignt in
        		sb.append("<pre>").append(content_to_inject).append("</pre>");
        	} else {
        		LOG.info("Unrecognised page format: " + format);
        	}
        }
        
        
        NodeList packageSet = thisDoc.getElementsByTagName("package");
        if(packageSet.getLength() > 0) {
        	
        	sb.append("<table style='font-family: sans-serif;' width='100%'>");
        	
        	for(int i = 0; i < packageSet.getLength(); i++) {
        		Element packageElement = (Element) packageSet.item(i);
        		String packageName = cleanPackageName(getFirstNamedChildValue(packageElement, "name"));
        		
            	sb.append("<tr><th colspan='3' bgcolor='#f0f0f0'>")
            					.append(packageName).append("</th></tr>");
            	sb.append("<tr><th>Name</th><th>Type</th><th>Description and Constraints</th></tr>");
            	
            	NodeList resourceSet = packageElement.getElementsByTagName("resource");
            	ArrayList<ResourceRow> resourceList = new ArrayList<ResourceRow>(); 
            	
            	if(resourceSet.getLength() > 0) {
                	for(int j = 0; j < resourceSet.getLength(); j++) {
                		Element resourceElement = (Element) resourceSet.item(j);
                		String resourceName = cleanPackageName(getFirstNamedChildValue(resourceElement, "name"));
                		String resourceDescription = cleanPackageName(getFirstNamedChildValue(resourceElement, "description"));
                		String resourcePurpose = cleanPackageName(getFirstNamedChildValue(resourceElement, "purpose"));
                		String resourceUri = cleanPackageName(getFirstNamedChildValue(resourceElement, "sourceUri"));
                		
                		// Look through the extensions
                		int publishOrder = 1000;
                		NodeList extensionSet = thisDoc.getElementsByTagName("extension");
                		if(extensionSet.getLength() > 0) {
                        	for(int k = 0; k < extensionSet.getLength(); k++) {
                        		Element extensionElement = (Element) extensionSet.item(k);
                        		String extensionUrl = extensionElement.getAttribute("url");
                        		if (extensionUrl.equals("urn:hscic:publishOrder")) {
                        			publishOrder = Integer.parseInt(getFirstNamedChildValue(extensionElement, "valueInteger"));
                        		}
                        	}
                		}
                		
                		if (resourcePurpose.equals("example")) {
                			// Ignore examples for now!
                		} else {
                			resourceList.add(new ResourceRow(resourceName, resourceDescription, resourceUri, resourcePurpose, publishOrder));
                		}
                	}
                }
            	
            	// Output the sorted list of resources
            	Collections.sort(resourceList);
            	for (ResourceRow row : resourceList) {
            		row.writeResource(sb);
            	}
        		
        	}
        	
        	sb.append("</table>");
        	
        }
        
        // TODO: Update the below for ImplementationGuide!
        
        
        /*
        sb.append("<table style='font-family: sans-serif;'><tr><th>Name</th><th>Value</th></tr>");
        
        NodeList composeSet = thisDoc.getElementsByTagName("compose");
        if(composeSet.getLength() > 0) {
            sb.append("<h4>Composed from</h4>");

            Element composeElement = (Element) composeSet.item(0);

            // The compose can be one or more of Import, Inlude and Exclude sections
            NodeList composeImports = composeElement.getElementsByTagName("import");
            NodeList composeIncludes = composeElement.getElementsByTagName("include");
            NodeList composeExcludes = composeElement.getElementsByTagName("exclude");

            // Imports is dead easy...
            for(int i = 0; i < composeImports.getLength(); i++) {
                Element importRef = (Element) composeImports.item(i);
                sb.append("<p><b>Import:</b> " + importRef.getAttribute("value") + "<br /></p>");
            }

            // Includes is more tricky...
            for(int i = 0; i < composeIncludes.getLength(); i++) {
                Element includeRef = (Element) composeIncludes.item(i);

                sb.append("<p><table><tr><td><b>Code System:</b></td><td>" + getFirstNamedChildValue(includeRef, "system") + "</td></tr>");

                NodeList filterList = includeRef.getElementsByTagName("filter");
                if (filterList.getLength()>0) {
                	sb.append("<tr><td colspan='2'><b>Filters:</b></td></tr>");
                }
                for(int j = 0; j < filterList.getLength(); j++) {
                    Element theFilter = (Element) filterList.item(j);
                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                
                NodeList includeList = includeRef.getElementsByTagName("concept");
                if (includeList.getLength()>0) {
                	sb.append("<tr><td colspan='2'><b>Includes:</b></td></tr>");
                }
                for(int j = 0; j < includeList.getLength(); j++) {
                    Element theInclude = (Element) includeList.item(j);
                    sb.append("<tr><td colspan='2'><li>");
                    sb.append("<b>code:</b> " + getFirstNamedChildValue(theInclude, "code"));
                    sb.append(": ");
                    sb.append("<b>display:</b> " + getFirstNamedChildValue(theInclude, "display"));
                    sb.append("</li></td></tr>");
                    
                }
                
                sb.append("</table></p>");
            }

            // Excludes is identical to Includes...
            for(int i = 0; i < composeExcludes.getLength(); i++) {
                Element excludeRef = (Element) composeExcludes.item(i);

                sb.append("<p><table><tr><td><b>Exclude:</b></td><td>" + getFirstNamedChildValue(excludeRef, "system") + "</td></tr>");

                NodeList filterList = excludeRef.getElementsByTagName("filter");
                for(int j = 0; j < filterList.getLength(); j++) {
                    Element theFilter = (Element) filterList.item(j);

                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                sb.append("</table></p>");
            }

        }*/
        
        LOG.info("\n=========================================\nhtml generated\n=========================================");
        sb.append("</div>\n");
        return sb.toString();
    }
    
    /**
     * The package names in the ImplementationGuide seem to be in the form Profile.GetRecordQueryRequest
     * but we want to publish them without the prefix, so remove it if there is one.
     * @param val
     * @return
     */
    private static String cleanPackageName(String val) {
    	if (val == null)
    		return "";
    	int idx = val.indexOf('.');
    	if (idx > -1) {
    		return val.substring(idx+1);
    	} else {
    		return val;
    	}
    }

}
