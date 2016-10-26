/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
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

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NewMain instance = new NewMain();
        instance.run();
    }
    private static final Logger LOG = Logger.getLogger(NewMain.class.getName());

    void run() {
        
        StructureDefinition structureDefinitionResource = null;
        
        String fileName = "C:\\Users\\tico3\\NetBeansProjects\\MakeHTML\\src\\main\\resources\\uk\\nhs\\fhir\\makehtml\\NRLS-DocumentReference-1-0.xml";
        //String fileName = "C:\\Users\\tico3\\NetBeansProjects\\MakeHTML\\src\\main\\resources\\uk\\nhs\\fhir\\makehtml\\allergyintolerance.profile.xml";

        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n<body>\n");
        sb.append("<div xmlns=\"http://www.w3.org/1999/xhtml\">");
        sb.append(" <table border='0' cellpadding='0' cellspacing='0'>\n");
        sb.append("  <tr>\n");
        sb.append("   <th>Name</th>\n");
        sb.append("   <th>Flags</th>\n");
        sb.append("   <th>Card.</th>\n");
        sb.append("   <th>Type</th>\n");
        sb.append("   <th>Description &amp; Constraints</th>\n");
        sb.append("  </tr>\n");

        ArrayList<MyElement> elementList = new ArrayList<MyElement>();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File(fileName));

            // We already have a text block, abort...
            //NodeList narrative = document.getElementsByTagName("text");
            //if(narrative.getLength() == 0) {

                NodeList names = document.getElementsByTagName("name");
                Node name = names.item(0);
                NamedNodeMap typeAttributes = name.getAttributes();
                String resourceName = typeAttributes.getNamedItem("value").getTextContent();

                NodeList snapshot = document.getElementsByTagName("snapshot");
                Element node = (Element) snapshot.item(0);
                NodeList elements = node.getElementsByTagName("element");

                // Here we're looping through the elements...
                for(int i = 0; i < elements.getLength(); i++) {
                    Element element = (Element) elements.item(i);
                    String elementName = getElementName(element);
                    String cardinality = getElementCardinality(element);
                    String typeName = getElementTypeName(element);
                    String flags = getFlags(element);
                    String description = getTitle(element);
                    String hoverText = getDescription(element);
                    elementList.add(new MyElement(elementName, cardinality, typeName, flags, description, hoverText));
                }

            // We should have a list of ALL Elements, including those we don't display!
                // Set the last item to be 'last'
                elementList.get(elementList.size() - 1).setIsLast(true);

                // Now work through and see if any have 'children...
                for(int i = 0; i < elementList.size(); i++) {
                    MyElement item = (MyElement) elementList.get(i);
                    
                    LOG.info("Level: " + item.getLevel());
                    if(item.isDisplay()) {

                        sb.append("  <tr>\n");

                        // Make a cell for the tree images and the name
                        sb.append("   <td style=\"padding-top: 0px; padding-right: 4px; padding-bottom: 0px; padding-left: 4px;\">");
                    // Tree and object type images need to go here
                        // Simplest cases...
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 1 elements">
                        if(item.getLevel() == 1) {
                            if(i == elementList.size() - 1) {
                                // This is the last item, so:
                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                            } else {
                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzI3XJ6V3QAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+2RsQ0AIAzDav7/2VzQwoCY4iWbZSmo1QGoUgNMghvWaIejPQW/CrrNCylIwcOCDYfLNRcNer4SAAAAAElFTkSuQmCC\" />");
                            }
                        } else {
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 2 elements">
                            if(item.getLevel() == 2) {
                                if(i == elementList.size() - 1) {
                                    // It's the last item so a spacer then the 'end corner'
                                    sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                    sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                                } else {
                                    boolean oneContinues = false;
                                    for(int n = i; n < elementList.size(); n++) {
                                        if(elementList.get(n).getLevel() == 1) {
                                            // We need to show the level 1 line continuing beside our line
                                            oneContinues = true;

                                            break;
                                        }
                                    }
                                    if(oneContinues) {
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzMPbYccAgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAMElEQVQ4y+3OQREAIBDDwAv+PQcFFN5MIyCzqHMKUGVCpMFLK97heq+gggoq+EiwAVjvMhFGmlEUAAAAAElFTkSuQmCC\" />");
                                    } else {
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                    }
                                    if(elementList.get(i + 1).getLevel() == 1) {
                                        // We're the last at level 2, so corner
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                                    } else {
                                        // Here we need to determine whether this is the last at level two
                                        boolean lastOfTwos = false;
                                        for(int n = i; n < elementList.size(); n++) {
                                            if(elementList.get(n).getLevel() == 1) {
                                                lastOfTwos = true;
                                                break;
                                            }
                                        }
                                        if(lastOfTwos) {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                                        } else {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzI3XJ6V3QAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+2RsQ0AIAzDav7/2VzQwoCY4iWbZSmo1QGoUgNMghvWaIejPQW/CrrNCylIwcOCDYfLNRcNer4SAAAAAElFTkSuQmCC\" />");
                                        }
                                    }
                                }
                            } else {
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 3 elements">
                                if(item.getLevel() == 3) {
                                    if(i == elementList.size() - 1) {
                                        // It's the last item so two spacers then the 'end corner'
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                                    } else {
                                        // Now figure out whether there are more level one elements to come, so do we continue the very left leg of the tree?
                                        boolean oneContinues = false;
                                        for(int n = i; n < elementList.size(); n++) {
                                            if(elementList.get(n).getLevel() == 1) {
                                                oneContinues = true;
                                            }
                                        }
                                        if(oneContinues) {
                                            // We have more items coming at Level one, s ocontinue the tree...
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzMPbYccAgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAMElEQVQ4y+3OQREAIBDDwAv+PQcFFN5MIyCzqHMKUGVCpMFLK97heq+gggoq+EiwAVjvMhFGmlEUAAAAAElFTkSuQmCC\" />");
                                        } else {
                                            // No more at Level one, so we add a spacer...
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        }

                                        // Now figure out what level two is doing...
                                        boolean twoContinues = false;
                                        for(int n = i; n < elementList.size(); n++) {
                                            int d = elementList.get(n).getLevel();
                                            if(d == 2) {
                                                twoContinues = true;
                                            }
                                            if(d == 1) {
                                                break;
                                            }
                                        }
                                        if(twoContinues) {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzMPbYccAgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAMElEQVQ4y+3OQREAIBDDwAv+PQcFFN5MIyCzqHMKUGVCpMFLK97heq+gggoq+EiwAVjvMhFGmlEUAAAAAElFTkSuQmCC\" />");
                                        } else {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        }
                                        // Now just figure out whether we're the last at Level 3, and add icon...
                                        if(elementList.get(i + 1).getLevel() != 3) {
                                            // We're last, add a corner
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
                                        } else {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzMPbYccAgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAMElEQVQ4y+3OQREAIBDDwAv+PQcFFN5MIyCzqHMKUGVCpMFLK97heq+gggoq+EiwAVjvMhFGmlEUAAAAAElFTkSuQmCC\" />");
                                        }

                                    }
                                } else {
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 4 elements">
                                    if(item.getLevel() == 4) {
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                    } else {
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 5 elements">
                                        if(item.getLevel() == 5) {
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                        } else {
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Handle tree icons for Level 6 elements">
                                            if(item.getLevel() == 6) {
                                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                                            }
//</editor-fold>
                                        }

                                    }
                                }
                            }
                        }

                        // Simple case, the base resource node...
                        if(item.getLevel() == 0) {
                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Resource\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJBSURBVDjLhdKxa5NBGMfx713yvkmbJnaoFiSF4mJTh06Kg4OgiyCCRXCof4YIdXdxFhQVHPo3OFSoUx0FySQttaVKYq2NbdO8ed/L3fM4JG3tYPvAcfBw9+HHPWdUlf/V0tLSqKo+EpEHInJFRIohhDUR+RBCeDM7O7ua55QSkRfVanVufHyckZERrLV0Op2Zra2tmXq9fg+YsmcAdyYnJykUCke9OI6ZmJgghHAZ4KwE3ntPs9mkVCohIjQaDWq1GiEEAM5KoHEcY62lVCrRarUoFotUKpUjIL/y/uqXYmV62ph/LSVrr30P4bEFcM4B0Ov1jk547/uAUTs1ceNdZIwB7V/GGHz6+9LXxY96eDiEgHMOY8xJAK8p4grZz5cElwNbwZgyxYu3EFM01lriOCZJEqIoIooiALIsGwA9Y1UcwcWoKNLdpLu9zvbnBWqNBhuvn5EDUmB0EH/1E2TZw5U+YLQovkun+Ytsaw1xCbnCOap334LC7s4Oe/ttvA+ICLmhMXRxDufczUECS37oAuevPwUEVFFp4/eXkXSdYc2IopSepnjtUh5/wg9gfn6+OQBUNaRIUkfDHhraSLoBKqikIF3yHJDLHaAkFOLciVHnyVAVj/S2Ub/XRyQD9aAZKgkaOohvo6ENgykcA07VEFDfQv1uf4W9Y8y30bCPhg4qKZJtMnjTPqBO/vhkZ7h3EJeRslWNQMqgY2jIAIfa/m5sIKSpqpPsGEiz599e3b+GchtD+bSvjQJm2SG6cNj6C+QmaxAek5tyAAAAAElFTkSuQmCC\" />");
                        }

                        if(item.getTypeName() != null) {
                            // If a simle datatype...
                            if(item.getTypeName().equals("boolean")
                                    || item.getTypeName().equals("code")
                                    || item.getTypeName().equals("date")
                                    || item.getTypeName().equals("dateTime")
                                    || item.getTypeName().equals("instant")
                                    || item.getTypeName().equals("unsignedInt")
                                    || item.getTypeName().equals("string")
                                    || item.getTypeName().equals("decimal")
                                    || item.getTypeName().equals("uri")
                                    || item.getTypeName().equals("integer")) {
                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Primitive Data Type\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3gYBFzI0BrFQCwAAAERJREFUOMtj/P//PwMlgImBQjDwBrCcOnWKokBgYWBgYDCU+06W5i8MUggvnH/EOVJjAW4AuQHJ+O75LYqikXE0LzAAALePEntTkEoSAAAAAElFTkSuQmCC\" />");
                            }
                            // If a Resource Type...
                            if(item.getTypeName().equals("Identifier")
                                    || item.getTypeName().equals("ContactPoint")
                                    || item.getTypeName().equals("Address")
                                    || item.getTypeName().equals("CodeableConcept")
                                    || item.getTypeName().equals("Attachment")
                                    || item.getTypeName().equals("Resource")
                                    || item.getTypeName().equals("Signature")
                                    || item.getTypeName().equals("BackboneElement")
                                    || item.getTypeName().equals("HumanName")) {
                                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Data Type\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,R0lGODlhEAAQAOZ/APrkusOiYvvfqbiXWaV2G+jGhdq1b8GgYf3v1frw3vTUlsWkZNewbcSjY/DQkad4Hb6dXv3u0f3v1ObEgfPTlerJiP3w1v79+e7OkPrfrfnjuNOtZPrpydaxa+/YrvvdpP779ZxvFPvnwKKBQaFyF/369M2vdaqHRPz58/HNh/vowufFhfroxO3OkPrluv779tK0e6JzGProwvrow9m4eOnIifPTlPDPkP78+Naxaf3v0/zowfXRi+bFhLWUVv379/rnwPvszv3rye3LiPvnv+3MjPDasKiIS/789/3x2f747eXDg+7Mifvu0tu7f+/QkfDTnPXWmPrjsvrjtPbPgrqZW+/QlPz48K2EMv36866OUPvowat8Ivvgq/Pbrvzgq/PguvrgrqN0Gda2evfYm9+7d/rpw9q6e/LSku/Rl/XVl/LSlfrkt+zVqe7Wqv3x1/bNffbOf59wFdS6if3u0vrqyP3owPvepfXQivDQkO/PkKh9K7STVf779P///////yH5BAEAAH8ALAAAAAAQABAAAAfNgH+Cg36FfoOIhH4JBxBghYl/hQkNAV0IVT5GkJKLCwtQaSsSdx9aR26Gcwt2IkQaNRI6dBERIzCFDSgWSW8WCDkbBnoOQ3uFARc/JQJfCAZlT0x4ZFyFBxdNQT9ZCBNWKQoKUQ+FEDgcdTIAV14YDmg2CgSFA0hmQC5TLE4VRTdrKJAoxOeFCzZSwsw4U6BCizwUQhQyEaAPiAwCVNCY0FCNnA6GPAwYoETIFgY9loiRA4dToTYnsOxg8CBGHE6ICvEYQ4AKzkidfgoKBAA7\" />");
                            }
                        } else {
                            // Seems to be a special case, used in eg Bundle resource types
                            sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Reference to another Element\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAKjSURBVDjLrZLdT1JhHMfd6q6L7voT3NrEuQq6aTircWFQ04u4MetCZ4UXGY2J1UoMdCoWxMZWDWtrrqHgylZ54RbkZtkiJ5aAku8MXXqQl3PgAOfb8xwq5LrOzmfnd34vn+d5tqcMQNm/UPZfBMHXx2ZWvI386odLe7jIL7w5EQ68qjhEOFjCmMj+v4LQmCwtCHkSCuSlFOIst4X1KU1mbUqT/kPki57bmL6xEnx55HxRMCqNCTkO6fUBpH5YkFoeBLsyAiHLEFcSQi5B2C38Z3eAPJ8JjcrmigKnLJ7nd8mwDcnFh4h/68T29FVsfW4F4zeCmb0LZqYDO191hOtkZ5sIuY8lioJhKZ9lo2DmbNjx9WDTowW7+YmsGv+9Ov3GijsgxwsNy7iiYOg4L54/nyawQC4lDubYANIRG7g1I9glHVILl5EMNCCXnEfouXSP4JksI+RY5OIfkWXGwf8cQSb6hAz2gV2+BXaxFangBSS/n0PCfxq5xAxCg3sFj2TpPB8Hvz2G3dWneOvqhLnPCIfDgd5uPebfNyAyrUR/t1bMmft7MdR1NiuXyw8UBDYpJ/AMkhsOPLa2wmKxIBqNIhwOw+Px4EG/Hvb7GoSCc2JucnJS7FEqlb2FizRwNMLHFmPvXnQJN/U6+Px+3LvdApVKiebmZlitVuj1ejFWqc7AZNCJEq1WGxMFAVPFtUCPZKhDXZUyGu6IAr+pklOclGNiYgI+nw9erxculws0N2uqjFOBwWDgSu61RCK50tLSwlBBfX39eE1NDa9QKFBXVydCY5qjNSqgvSWCw+RRqVTzZrOZcTqd2263G3a7HW1tbWhvbxdjmqM12kN7SwTl5eX7qqurq2pra5eampqSGo2GI2TUanUj4RSJ4zRHa7SH9v4C8Nrl+GFh7LoAAAAASUVORK5CYII=\" />");
                        }

                        sb.append(item.getNiceTitle());
                        sb.append("</td>\n");

                        // Now the flags column
                        sb.append("    <td style=\"padding-top: 0px; padding-right: 4px; padding-bottom: 0px; padding-left: 4px;\">");
                        sb.append(item.getFlags());
                        sb.append("</td>\n");

                        // Now the Cardinality column
                        sb.append("    <td style=\"padding-top: 0px; padding-right: 4px; padding-bottom: 0px; padding-left: 4px;\">");
                        sb.append(item.getCardinality());
                        sb.append("</td>\n");

                        // Now the type column
                        sb.append("    <td style=\"padding-top: 0px; padding-right: 4px; padding-bottom: 0px; padding-left: 4px;\">");
                        sb.append(item.getTypeName());
                        sb.append("</td>\n");

                        // And now the description
                        sb.append("    <td style=\"padding-top: 0px; padding-right: 4px; padding-bottom: 0px; padding-left: 4px;\">");
                        sb.append(item.getDescription());
                        sb.append("</td>\n");

                        sb.append("   </tr>\n");
                    }
                }
            //}

        } catch (SAXException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        sb.append(" </table>\n");
        sb.append("</div>");
        sb.append("</body>\n</html>\n");
        //System.out.println(sb.toString());
        
        FhirContext ctx = FhirContext.forDstu2();
        
        try {
            structureDefinitionResource = (StructureDefinition) ctx.newXmlParser().parseResource(new BufferedReader(new FileReader(fileName)));
            NarrativeDt textElement = new NarrativeDt();
            textElement.setStatus(NarrativeStatusEnum.GENERATED);
            textElement.setDiv(sb.toString());
            structureDefinitionResource.setText(textElement);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        

    }

    String getElementName(Element item) {
        NodeList pathsList = item.getElementsByTagName("path");
        Element node = (Element) pathsList.item(0);
        return node.getAttribute("value");
    }

    private String getElementCardinality(Element element) {
        NodeList minList = element.getElementsByTagName("min");
        Element node = (Element) minList.item(0);
        String min = node.getAttribute("value");

        NodeList maxList = element.getElementsByTagName("max");
        node = (Element) maxList.item(0);
        String max = node.getAttribute("value");
        return min + ".." + max;
    }

    private String getElementTypeName(Element element) {
        String typeName = null;
        NodeList typesList = element.getElementsByTagName("type");
        if(typesList.getLength() > 0) {
            Element node = (Element) typesList.item(0);
            NodeList codeList = node.getElementsByTagName("code");
            Element subNode = (Element) codeList.item(0);
            typeName = subNode.getAttribute("value");
        }
        return typeName;
    }

    private String getFlags(Element element) {
        String flags = "";

        NodeList summaryList = element.getElementsByTagName("isSummary");
        if(summaryList.getLength() > 0) {
            Element summary = (Element) summaryList.item(0);
            if(summary.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is included in summaries\">&Sigma;</span>\n";
            }
        }

        NodeList conditionList = element.getElementsByTagName("condition");
        if(conditionList.getLength() > 0) {
            Element condition = (Element) conditionList.item(0);
            if(condition.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element has or is affected by some invariants\">I</span>\n";
            }
        }

        NodeList modifierList = element.getElementsByTagName("isModifier");
        if(modifierList.getLength() > 0) {
            Element modifier = (Element) modifierList.item(0);
            if(modifier.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is a modifier element\">?!</span>";
            }
        }
        return flags;
    }

    private String getTitle(Element element) {
        String title = "";
        NodeList titleList = element.getElementsByTagName("short");
        if(titleList.getLength() > 0) {
            Element subNode = (Element) titleList.item(0);
            title = subNode.getAttribute("value");
        }
        return title;
    }

    private String getDescription(Element element) {
        String description = "";
        NodeList descList = element.getElementsByTagName("definition");
        if(descList.getLength() > 0) {
            Element subNode = (Element) descList.item(0);
            description = subNode.getAttribute("value");
        }
        return description;
    }

    private void writeFile(StringBuilder sb) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("C:\\Users\\tico3\\NetBeansProjects\\MakeHTML\\src\\main\\resources\\uk\\nhs\\fhir\\makehtml\\output.html", "UTF-8");
            writer.append(sb.toString());
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }
}
