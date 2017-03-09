package uk.nhs.fhir.makehtml.old;

import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getFirstNamedChildValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

import uk.nhs.fhir.makehtml.resources.ResourceRow;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;
import uk.nhs.fhir.util.MarkdownProcessor;

public class ImplementationGuideHTMLMakerOLD extends HTMLMakerOLD {
	
	private static final Logger LOG = Logger.getLogger(ImplementationGuideHTMLMakerOLD.class.getName());
	
	private final File folder;
	private final String outPath;
	
	public ImplementationGuideHTMLMakerOLD(File folder, String outPath) {
		this.folder = folder;
		this.outPath = outPath;
	}
	
    /**
     * Make the html narrative section for the ImplementationGuide described in this XML
     *
     * @param thisDoc   The XML Document as an org.w3c.dom.Document
     *
     * @return          Valid xhtml fully describing the ImplementationGuide
     */
    @Override
	public String makeHTML(Document thisDoc) {
    	
    	StringBuilder sb = new StringBuilder();

        //Element root = (Element) thisDoc.getFirstChild();

        sb.append("<div style='font-family: sans-serif;' xmlns='http://www.w3.org/1999/xhtml'>\n");

        // Load in the relevant markdown file and inject it here.
        // TODO: Add support for other mime types and child pages - is this required?
        
        IteratorIterable<Element> pageSet = thisDoc.getDescendants(new ElementFilter("page"));
        if(pageSet.hasNext()) {
        	// Only one page at the top level
        	Element pageElement = pageSet.next();
        	String source = getFirstNamedChildValue(pageElement, "source");
        	//String name = getFirstNamedChildValue(pageElement, "name");
        	//String kind = getFirstNamedChildValue(pageElement, "kind");
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
        
        
        IteratorIterable<Element> packageSet = thisDoc.getDescendants(new ElementFilter("package"));
        if (packageSet.hasNext()) {
        	sb.append("<h1>Constrained FHIR Models</h1>");
        	
        	sb.append("<table style='font-family: sans-serif;' width='100%'>");
        	
        	for(Element packageElement : packageSet) {
        		String packageName = cleanPackageName(getFirstNamedChildValue(packageElement, "name"));
        		
            	sb.append("<tr><th colspan='3' bgcolor='#f0f0f0'>")
            					.append(packageName).append("</th></tr>");
            	sb.append("<tr><th class='nameCol'>Name</th><th class='typeCol'>Type</th>");
            	sb.append("<th class='descCol'>Description and Constraints</th></tr>");

            	ArrayList<ResourceRow> resourceList = new ArrayList<ResourceRow>();
            	for (Element resourceElement : packageElement.getDescendants(new ElementFilter("resource"))) {
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
    /*private static String reformatResourceDescriptions(String val) {
    	if (val == null) {
    		return "";
    	}
    	System.out.println("Before: " + val);
    	String result = val.replaceAll("\\r\\n|\\r|\\n", "<br/>");
    	System.out.println("After : " + result);
    	return result;
    }*/

}
