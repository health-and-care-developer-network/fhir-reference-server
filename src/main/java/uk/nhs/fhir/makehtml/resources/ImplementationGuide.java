package uk.nhs.fhir.makehtml.resources;

import static uk.nhs.fhir.makehtml.XMLParserUtils.getFirstNamedChildValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.nhs.fhir.makehtml.XMLParserUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;
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
	public static String makeHTMLForImplementationGuide(Document thisDoc, File folder, String outPath) {
    	
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
        		// Also, copy the file to the output path
        		FileWriter.writeFile(outPath + "/" + source, content_to_inject.getBytes());
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
        	
        	sb.append("<h1>Constrained FHIR Models</h1>");
        	
        	sb.append("<table style='font-family: sans-serif;' width='100%'>");
        	
        	for(int i = 0; i < packageSet.getLength(); i++) {
        		Element packageElement = (Element) packageSet.item(i);
        		String packageName = cleanPackageName(getFirstNamedChildValue(packageElement, "name"));
        		
            	sb.append("<tr><th colspan='3' bgcolor='#f0f0f0'>")
            					.append(packageName).append("</th></tr>");
            	sb.append("<tr><th class='nameCol'>Name</th><th class='typeCol'>Type</th>");
            	sb.append("<th class='descCol'>Description and Constraints</th></tr>");
            	
            	NodeList resourceSet = packageElement.getElementsByTagName("resource");
            	ArrayList<ResourceRow> resourceList = new ArrayList<ResourceRow>(); 
            	
            	if(resourceSet.getLength() > 0) {
                	for(int j = 0; j < resourceSet.getLength(); j++) {
                		Element resourceElement = (Element) resourceSet.item(j);
                		String resourceName = getFirstNamedChildValue(resourceElement, "name");
                		String resourceDescription = reformatResourceDescriptionsForListing(
                					getFirstNamedChildValue(resourceElement, "description"));
                		String resourcePurpose = getFirstNamedChildValue(resourceElement, "purpose");
                		String resourceUri = getFirstNamedChildValue(resourceElement, "sourceUri");
                		
                		// Look through the extensions
                		int publishOrder = 1000;
                		String publishOrderStr = XMLParserUtils.getExtensionValue(
                											resourceElement, "urn:hscic:publishOrder", "valueInteger");
                		String resourceType = XMLParserUtils.getExtensionValue(
								resourceElement, "urn:hscic:resourceType", "valueString");
                		
                		if (publishOrderStr != null) {
                			publishOrder = Integer.parseInt(publishOrderStr);
                		}
                		
                		
                		if (resourcePurpose.equals("example")) {
                			// Ignore examples for now!
                		} else {
                			resourceList.add(new ResourceRow(resourceName, resourceDescription, resourceUri, resourceType, publishOrder));
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
    
    /**
     * In the listing in the DMS, the description is truncated from the first newline, excluding any at the very start.
     * @param val
     * @return
     */
    private static String reformatResourceDescriptionsForListing(String val) {
    	if (val == null) {
    		return "";
    	}
    	StringBuilder sb = new StringBuilder();
    	int idx = 0;
    	boolean foundFirstValidCharacter = false;
    	while (idx < val.length()) {
    		char c = val.charAt(idx);
    		if (c == 10 || c == 13) {
    			if (foundFirstValidCharacter) {
    				return sb.toString();
    			}
    		} else {
    			foundFirstValidCharacter = true;
    			sb.append(c);
    		}
    		idx++;
    	}
    	return sb.toString();
    }
    
    /**
     * Replace carriage return and line feed with a html line break
     * @param val
     * @return
     */
    private static String reformatResourceDescriptions(String val) {
    	if (val == null) {
    		return "";
    	}
    	System.out.println("Before: " + val);
    	String result = val.replaceAll("\\r\\n|\\r|\\n", "<br/>");
    	System.out.println("After : " + result);
    	return result;
    }

}
