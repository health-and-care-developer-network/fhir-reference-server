/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getDescription;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getElementCardinality;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getElementName;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getElementTypeList;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getElementTypeName;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getFlags;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getQuantityType;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getReferenceTypes;
import static uk.nhs.fhir.makehtml.XMLParserUtils.getTitle;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain implements Constants {
    private static final String fileExtension = ".xml";
    private static final Logger LOG = Logger.getLogger(NewMain.class.getName());

    /**
     * Main entry point.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    	if((args.length == 2) || (args.length == 3)) {
            String inputDir = args[0];
            String outputDir = args[1];
            String newBaseURL = null;
            if (args.length == 3) {
            	newBaseURL = args[2];
            }
            NewMain instance = new NewMain();
            instance.processDirectory(inputDir, outputDir, newBaseURL);
        }
    }

    /**
     * Process a specific file.
     * 
     * @param filename 
     */
    private String run(Document document) {
        
        StringBuilder sb = new StringBuilder();
        sb.append(TABLESTART);
        
        ArrayList<MyElement> elementList = new ArrayList<MyElement>();
        Node name = document.getElementsByTagName("name").item(0);
        
        NamedNodeMap typeAttributes = name.getAttributes();
        Element snapshotNode = (Element) document.getElementsByTagName("snapshot").item(0);

        NodeList elements = snapshotNode.getElementsByTagName("element");
        int snapshotElementCount = elements.getLength();
        
        // Now get a list of the names of elements which show as having been changed by this profile...
        ArrayList<String> changedNodes = GetChangedNodes(document);
        
        
        // First we process all the elements...
        for(int i = 0; i < snapshotElementCount; i++) {
            Element element = (Element) elements.item(i);
            if(element != null) {
                String elementName = getElementName(element);
                String cardinality = getElementCardinality(element);
                String typeName = getElementTypeName(element);
                String flags = getFlags(element);
                String description = getTitle(element);
                String hoverText = getDescription(element);
                
                boolean hasChanged = changedNodes.contains(elementName);
                
                if(typeName == null) {
                    LOG.info("typeName is NULL for Element: " + elementName);
                    typeName = "see link";
                }

                // Catch vrious elements which can be of multiple types or other oddities...
                if(typeName.equals("Multiple_Type_Choice")) {
                    ArrayList<String> types = getElementTypeList(element);
                    elementList.add(new MyElement(elementName, cardinality, typeName, typeName, flags, description, hoverText, hasChanged));
                    for(String type : types) {
                        elementList.add(new MyElement(elementName + "." + type, "", type, type, flags, "", "", hasChanged));
                    }
                } else {
                    if(typeName.equals("Reference")) {
                        String newtypeName = getReferenceTypes(element);
                        elementList.add(new MyElement(elementName, cardinality, typeName, newtypeName, flags, description, hoverText, hasChanged));
                    } else {
                        if(typeName.equals("Quantity")) {
                            String newtypeName = getQuantityType(element);
                            elementList.add(new MyElement(elementName, cardinality, typeName, newtypeName, flags, description, hoverText, hasChanged));
                        } else {
                            // This is for all other types
                            elementList.add(new MyElement(elementName, cardinality, typeName, typeName, flags, description, hoverText, hasChanged));
                        }
                    }
                }
            }
        }
        
        // Now we start thinking about adding the elements to the output...
        int mutedAtLevel = 100;
        for(MyElement elementList1 : elementList) {
            MyElement item = (MyElement) elementList1;
            if(item.isDisplay() == false) {
                mutedAtLevel = item.getLevel();
            } else {
                if(item.getLevel() > mutedAtLevel) {
                    item.setDisplay(false);
                } else {
                    mutedAtLevel = 100;
                }
            }
        }
        for(int i = 0; i < elementList.size(); i++) {
            MyElement item = (MyElement) elementList.get(i);
            if(item.isDisplay() == false) {
                elementList.remove(i);
            }
        }
        elementList.get(elementList.size() - 1).setIsLast(true);
        for(int i = 0; i < elementList.size(); i++) {
            MyElement item = (MyElement) elementList.get(i);
            
            if(item.isDisplay()) {
                
                sb.append(START_TABLE_ROW);
                
                // Make a cell for the tree images and the name
                sb.append(START_TABLE_CELL);
                // Tree and object type images need to go here
                // Simplest cases...
                //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 1 elements">
                if(item.getLevel() == 1) {
                    if(i == elementList.size() - 1) {
                        // This is the last item, so:
                        sb.append(CORNER);
                    } else {
                        // Here we need to check whether any items after this are at level one, if not it's corner time.
                        boolean l1Continues = false;
                        for(int n = i+1; n < elementList.size(); n++) {
                            if(elementList.get(n).getLevel() == 1) {
                                l1Continues = true;
                            }
                        }
                        if(l1Continues) {
                            sb.append(LINEWITHT);
                        } else {
                            sb.append(CORNER);    
                        }
                    }
                } else {
                    //</editor-fold>
                    boolean oneContinues = false;
                    for(int n = i+1; n < elementList.size(); n++) {
                        int d = elementList.get(n).getLevel();
                        if(d == 1) {
                            // We need to show the level 1 line continuing beside our line
                            oneContinues = true;
                            break;
                        }
                    }
                    //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 2 elements">
                    if(item.getLevel() == 2) {
                        if(i == elementList.size() - 1) {
                            // It's the last item so a spacer then the 'end corner'
                            sb.append(SPACER);
                            sb.append(CORNER);
                        } else {

                            if(oneContinues) {
                                sb.append(LINE);
                            } else {
                                sb.append(SPACER);
                            }
                            if(elementList.get(i + 1).getLevel() == 1) {
                                // We're the last at level 2, so corner
                                sb.append(CORNER);
                            } else {
                                // Here we need to determine whether this is the last at level two
                                boolean l2Continues = false;
                                for(int n = i+1; n < elementList.size(); n++) {
                                    if(elementList.get(n).getLevel() == 1) {
                                        break;
                                    }
                                    if(elementList.get(n).getLevel() == 2) {
                                        l2Continues = true;
                                        break;
                                    }
                                }
                                if(l2Continues) {
                                    sb.append(LINEWITHT);
                                } else {
                                    sb.append(CORNER);
                                }
                            }
                        }
                    } else {
                        //</editor-fold>
                        // Now figure out what level two is doing...
                        boolean twoContinues = false;
                        for(int n = i+1; n < elementList.size(); n++) {
                            int d = elementList.get(n).getLevel();
                            if(d == 2) {
                                twoContinues = true;
                            }
                            if(d == 1) {
                                break;
                            }
                        }
                        //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 3 elements">
                        if(item.getLevel() == 3) {
                            if(i == elementList.size() - 1) {
                                // It's the last item so two spacers then the 'end corner'
                                sb.append(SPACER);
                                sb.append(SPACER);
                                sb.append(CORNER);
                            } else {
                                // Now figure out whether there are more level one elements to come, so do we continue the very left leg of the tree?
                                if(oneContinues) {  // We have more items coming at Level one, so continue the tree...
                                    sb.append(LINE);
                                } else {
                                    // No more at Level one, so we add a spacer...
                                    sb.append(SPACER);
                                }
                                if(twoContinues) {  // We have more items coming at Level two, so continue the tree...
                                    sb.append(LINE);
                                } else {
                                    sb.append(SPACER);
                                }
                                // Now just figure out whether we're the last at Level 3, and add icon...
                                if(elementList.get(i + 1).getLevel() != 3) {
                                    // We're last, add a corner
                                    sb.append(CORNER);
                                } else {
                                    sb.append(LINEWITHT);
                                }
                                
                            }
                        } else {
                            //</editor-fold>
                            // Now figure out what level three is doing
                            boolean threeContinues = false;
                            for(int n = i; n < elementList.size(); n++) {
                                int d = elementList.get(n).getLevel();
                                if(d == 3) {
                                    threeContinues = true;
                                }
                                if(d == 1 || d == 2) {
                                    break;
                                }
                            }
                            //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 4 elements">
                            if(item.getLevel() == 4) {
                                if(i == elementList.size() - 1) {
                                    // It's the last item so two spacers then the 'end corner'
                                    sb.append(SPACER);
                                    sb.append(SPACER);
                                    sb.append(SPACER);
                                    sb.append(CORNER);
                                } else {
                                    
// Now figure out whether there are more level one elements to come, so do we continue the very left leg of the tree?
                                    if(oneContinues) {  // We have more items coming at Level one, so continue the tree...
                                        sb.append(LINE);
                                    } else {
                                        // No more at Level one, so we add a spacer...
                                        sb.append(SPACER);
                                    }
                                    if(twoContinues) {  // We have more items coming at Level two, so continue the tree...
                                        sb.append(LINE);
                                    } else {
                                        sb.append(SPACER);
                                    }
                                    if(threeContinues) {  // We have more items coming at Level two, so continue the tree...
                                        sb.append(LINE);
                                    } else {
                                        sb.append(SPACER);
                                    }
                                    // Now just figure out whether we're the last at Level 4, and add icon...
                                    if(elementList.get(i + 1).getLevel() != 4) {
                                        // We're last, add a corner
                                        sb.append(CORNER);
                                    } else {
                                        sb.append(LINEWITHT);
                                    }
                                }
                            } else {
                                //</editor-fold>
                                // Now figure out what level four is doing
                                boolean fourContinues = false;
                                for(int n = i; n < elementList.size(); n++) {
                                    int d = elementList.get(n).getLevel();
                                    if(d == 4) {
                                        fourContinues = true;
                                    }
                                    if(d == 1 || d == 2 || d == 3) {
                                        break;
                                    }
                                }
                                //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 5 elements">
                                if(item.getLevel() == 5) {
                                    if(i == elementList.size() - 1) {
                                        // It's the last item so two spacers then the 'end corner'
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(CORNER);
                                    } else {
                                        
                                        if(oneContinues) {  // We have more items coming at Level one, s ocontinue the tree...
                                            sb.append(LINE);
                                        } else {
                                            sb.append(SPACER);
                                        }
                                        if(twoContinues) {  // We have more items coming at Level two, s ocontinue the tree...
                                            sb.append(LINE);
                                        } else {
                                            sb.append(SPACER);
                                        }
                                        if(threeContinues) {  // We have more items coming at Level three, s ocontinue the tree...
                                            sb.append(LINE);
                                        } else {
                                            sb.append(SPACER);
                                        }
                                        if(fourContinues) {  // We have more items coming at Level four, s ocontinue the tree...
                                            sb.append(LINE);
                                        } else {
                                            sb.append(SPACER);
                                        }
                                        // Now just figure out whether we're the last at Level 5, and add icon...
                                        if(elementList.get(i + 1).getLevel() != 5) {
                                            // We're last, add a corner
                                            sb.append(CORNER);
                                        } else {
                                            sb.append(LINEWITHT);
                                        }
                                    }
                                } else {
                                    //</editor-fold>
                                    // Now figure out what level five is doing
                                    boolean fiveContinues = false;
                                    for(int n = i; n < elementList.size(); n++) {
                                        int d = elementList.get(n).getLevel();
                                        if(d == 5) {
                                            fiveContinues = true;
                                        }
                                        if(d == 1 || d == 2 || d == 3 || d == 4) {
                                            break;
                                        }
                                    }
                                    //<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 6 elements">
                                    if(i == elementList.size() - 1) {
                                        // It's the last item so two spacers then the 'end corner'
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(SPACER);
                                        sb.append(CORNER);
                                    } else {
                                        if(item.getLevel() == 6) {
                                            if(oneContinues) {  // We have more items coming at Level one, s ocontinue the tree...
                                                sb.append(LINE);
                                            } else {
                                                sb.append(SPACER);
                                            }
                                            if(twoContinues) {  // We have more items coming at Level two, s ocontinue the tree...
                                                sb.append(LINE);
                                            } else {
                                                sb.append(SPACER);
                                            }
                                            if(threeContinues) {  // We have more items coming at Level three, s ocontinue the tree...
                                                sb.append(LINE);
                                            } else {
                                                sb.append(SPACER);
                                            }
                                            if(fourContinues) {  // We have more items coming at Level four, s ocontinue the tree...
                                                sb.append(LINE);
                                            } else {
                                                sb.append(SPACER);
                                            }
                                            if(fiveContinues) {  // We have more items coming at Level five, s ocontinue the tree...
                                                sb.append(LINE);
                                            } else {
                                                sb.append(SPACER);
                                            }
                                            if(elementList.get(i + 1).getLevel() != 6) {
                                                // We're last, add a corner
                                                sb.append(CORNER);
                                            } else {
                                                sb.append(LINEWITHT);
                                            }
                                        }
                                    }
                                    //</editor-fold>
                                }
                            }
                        }
                    }
                }
                
                // Simple case, the base resource node...
                if(item.getLevel() == 0) {
                    sb.append(RESOURCE);
                }
                
                DataTypes thisType = null;
                String thisTypeName = item.getTypeName(); 
                if(thisTypeName != null) {
                    // If a simle datatype...
                    if(Arrays.asList(BASERESOURCETYPES).contains(thisTypeName))
                    {
                        sb.append(BASETYPE);
                        thisType = DataTypes.Simple;
                    }

                    if(Arrays.asList(RESOURCETYPES).contains(thisTypeName))
                    {
                        sb.append(DATATYPE);
                        thisType = DataTypes.Resource;
                    }

                    // If a Resource Type...
                    if(thisTypeName.equals("Reference")) {
                        sb.append(REFERENCE);
                        thisType = DataTypes.Reference;
                    }
                    
                    if(thisTypeName.equals("Multiple_Type_Choice")) {
                        sb.append(CHOICETYPE);
                    }
                } else {
                    // Seems to be a special case, used in eg Bundle resource types
                    sb.append(BUNDLE);
                }
                
                if(item.isChanged()) {
                    sb.append("<b>");
                    sb.append(item.getNiceTitle());
                    sb.append("</b>");
                } else {
                    sb.append(item.getNiceTitle());
                }
                sb.append(END_TABLE_CELL);
                
                // Now the flags column
                sb.append(item.getHTMLWrappedFlags());
                
                // Now the Cardinality column
                sb.append(item.getHTMLWrappedCardinality());
                
                // Now the type column
                sb.append(START_TABLE_CELL);
                if(item.getTypeName().equals("Multiple_Type_Choice") == false) {
                    if(thisType == DataTypes.Resource) {
                        sb.append(decorateResourceName(item.getTypeName()));
                    } else {
                        if(thisType == DataTypes.Reference) {
                            sb.append("<a href='https://www.hl7.org/fhir/references.html'>" + item.getTypeName() + "</a>");
                        } else {
                            sb.append(decorateTypeName(item.getTypeName()));
                        }
                    }
                }
                sb.append(END_TABLE_CELL);
                
                // And now the description
                sb.append(item.getHTMLWrappedDescription());
                
                sb.append(END_TABLE_ROW);
            }
        }
        sb.append(" </table>\n");
        sb.append("</div>");

        LOG.info("\n=========================================\nhtml generated\n=========================================");
        return sb.toString();
    }

    /**
     * Routine to read in an XML file to an org.w3c.dom.Document.
     * 
     * @param filename
     * @return a Document containing the specified file.
     */
    private Document ReadFile(String filename) {
        Document document = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            document = docBuilder.parse(filename);
            return document;
        } catch (ParserConfigurationException ex) {
            LOG.severe("ParserConfigurationException: " + ex.getMessage());
        } catch (SAXException ex) {
            LOG.severe("SAXException: " + ex.getMessage());
        } catch (IOException ex) {
            LOG.severe("IOException: " + ex.getMessage());
        }
        return document;
    }

    /**
     * Process a directory of Profile files.
     * 
     * @param profilePath 
     */
    private void processDirectory(String profilePath, String outPath, String newBaseURL) {        
        File folder = new File(profilePath);
        File[] allProfiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(fileExtension);
            }
        });
        
        for(File thisFile : allProfiles) {
            if(thisFile.isFile()) {
                String inFile = thisFile.getPath();
                String outFilename = outPath + separatorChar + thisFile.getName();
                LOG.info("\n\n=========================================\nProcessing file: " + inFile + "\n=========================================");
                Document thisDoc = ReadFile(inFile);
                String result = run(thisDoc);
                
                String originalResource = FileLoader.loadFile(inFile);
        
                String augmentedResource = ResourceBuilder.addTextSectionToResource(originalResource, result, newBaseURL);
                try {
                    FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
                }
            }
        }
    }

    /**
     * Dress up a type name so it provides a link back to the definition.
     * 
     * @param type
     * @return 
     */
    public String decorateTypeName(String type) {
        if(type.equals("DomainResource")) {
            return "<a href='https://www.hl7.org/fhir/domainresource.html'>" + type + "</a>";
        }
        if(Arrays.asList(BASERESOURCETYPES).contains(type)) {
            return "<a href='https://www.hl7.org/fhir/datatypes.html#" + type + "'>" + type + "</a>";
        } else
            return type;
    }
    
    public String decorateResourceName(String type) {
        return "<a href='https://www.hl7.org/fhir/" + type.toLowerCase() + ".html'>" + type + "</a>";
    }

    /**
     * Method to get a list of the names of the elements which are listed in
     * the differential section of a StructureDefinition, ie those which
     * have been changed.
     * 
     * @param document A org.w3c.dom.Document
     * @return Returns an ArrayList of Strings holding the (full dot separated) element names.
     */
    protected ArrayList<String> GetChangedNodes(Document document) {
        ArrayList<String> names = new ArrayList<String>();
        // Get a list of any elements called differential
        NodeList differential = document.getElementsByTagName("differential");
        
        if(differential.getLength() > 0) {
            // Get the first one (there should only be one!
            Element diffNode = (Element) differential.item(0);
            
            // Get the elements within the differential section
            NodeList diffElements = diffNode.getElementsByTagName("element");

            for(int i = 0; i < diffElements.getLength(); i++) {
                Element diffElement = (Element) diffElements.item(i);
                if(diffElement != null) {
                    String name = getElementName(diffElement);
                    if(name != null) {
                        names.add(name);
                    }
                }
            }
        }
        return names;
    }
}
