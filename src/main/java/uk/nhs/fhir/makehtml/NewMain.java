/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;
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

    void run() {

        StringBuilder sb = new StringBuilder();
        sb.append("<table border='0' cellpadding='0'>\n");
        sb.append(" <tr>\n");
        sb.append("  <td>Name</td>\n");
        sb.append("  <td>Flags</td>\n");
        sb.append("  <td>Card</td>\n");
        sb.append("  <td>Type</td>\n");
        sb.append("  <td>Description & Constraints</td>\n");
        sb.append(" </tr>\n");
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("C:\\Users\\tico3\\NetBeansProjects\\MakeHTML\\src\\main\\resources\\uk\\nhs\\fhir\\makehtml\\allergyintolerance.profile.xml"));

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
                if(i == elements.getLength() -1) {
                    sb.append(MakeRow(element, resourceName, true));
                } else {
                    sb.append(MakeRow(element, resourceName, false));
                }
            }
        } catch (SAXException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        sb.append("</table>\n");
        System.out.println(sb.toString());
    }

    String MakeRow(Element element, String resName, boolean lastItem) {
        StringBuilder sb = new StringBuilder();

        if(element.getNodeType() == Node.ELEMENT_NODE) {

            // Get the name of it...
            NodeList path = element.getElementsByTagName("path");
            String elementName = getElementValue((Element) path.item(0));
            
            if(elementName.equals(resName + ".id")
                    || elementName.equals(resName + ".meta")
                    || elementName.equals(resName + "implicitRules")
                    || elementName.equals(resName + ".language")
                    || elementName.equals(resName + ".text")
                    || elementName.equals(resName + ".contained")
                    || elementName.equals(resName + ".extension")
                    || elementName.equals(resName + ".modifierExtension")
                    || elementName.equals(resName + ".implicitRules")) {
                return "";
            }

            // Get the type...
            NodeList type = element.getElementsByTagName("type");
            Element typeElement = (Element) type.item(0);
            NodeList codeElement = typeElement.getElementsByTagName("code");
            Element typecodeElement = (Element) codeElement.item(0);
            String typeName = getElementValue(typecodeElement);

            // Get the cardinality...
            NodeList minList = element.getElementsByTagName("min");
            Element minElement = (Element) minList.item(0);
            String min = getElementValue(minElement);
            NodeList maxList = element.getElementsByTagName("max");
            Element maxElement = (Element) maxList.item(0);
            String max = getElementValue(maxElement);
            
            sb.append(" <tr>\n");
            sb.append("  <td>");
            
            // Here we draw the tree line items.
            if(lastItem) {
                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC\" />");
            } else {
                if(elementName.equals(resName) == false) {
                    sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzI3XJ6V3QAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+2RsQ0AIAzDav7/2VzQwoCY4iWbZSmo1QGoUgNMghvWaIejPQW/CrrNCylIwcOCDYfLNRcNer4SAAAAAElFTkSuQmCC\" />");
                }
            }

            if(elementName.equals(resName)) {
                sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Resource\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJBSURBVDjLhdKxa5NBGMfx713yvkmbJnaoFiSF4mJTh06Kg4OgiyCCRXCof4YIdXdxFhQVHPo3OFSoUx0FySQttaVKYq2NbdO8ed/L3fM4JG3tYPvAcfBw9+HHPWdUlf/V0tLSqKo+EpEHInJFRIohhDUR+RBCeDM7O7ua55QSkRfVanVufHyckZERrLV0Op2Zra2tmXq9fg+YsmcAdyYnJykUCke9OI6ZmJgghHAZ4KwE3ntPs9mkVCohIjQaDWq1GiEEAM5KoHEcY62lVCrRarUoFotUKpUjIL/y/uqXYmV62ph/LSVrr30P4bEFcM4B0Ov1jk547/uAUTs1ceNdZIwB7V/GGHz6+9LXxY96eDiEgHMOY8xJAK8p4grZz5cElwNbwZgyxYu3EFM01lriOCZJEqIoIooiALIsGwA9Y1UcwcWoKNLdpLu9zvbnBWqNBhuvn5EDUmB0EH/1E2TZw5U+YLQovkun+Ytsaw1xCbnCOap334LC7s4Oe/ttvA+ICLmhMXRxDufczUECS37oAuevPwUEVFFp4/eXkXSdYc2IopSepnjtUh5/wg9gfn6+OQBUNaRIUkfDHhraSLoBKqikIF3yHJDLHaAkFOLciVHnyVAVj/S2Ub/XRyQD9aAZKgkaOohvo6ENgykcA07VEFDfQv1uf4W9Y8y30bCPhg4qKZJtMnjTPqBO/vhkZ7h3EJeRslWNQMqgY2jIAIfa/m5sIKSpqpPsGEiz599e3b+GchtD+bSvjQJm2SG6cNj6C+QmaxAek5tyAAAAAElFTkSuQmCC\" />");
            } else {
                elementName = elementName.substring(elementName.indexOf(".")+1);
                
                // If we're a child element, we add a spacer image before the node image...
                if(elementName.contains(".")) {
                    sb.append("<img xmlns=\"http://www.w3.org/1999/xhtml\" style=\"background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=\" />");
                }
                
                
                if(typeName.equals("string")
                        || typeName.equals("code")
                        || typeName.equals("dateTime") ) {
                    sb.append("<img title=\"Primitive Data Type\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3gYBFzI0BrFQCwAAAERJREFUOMtj/P//PwMlgImBQjDwBrCcOnWKokBgYWBgYDCU+06W5i8MUggvnH/EOVJjAW4AuQHJ+O75LYqikXE0LzAAALePEntTkEoSAAAAAElFTkSuQmCC\">");
                }
                if(typeName.equals("Identifier")
                        || typeName.equals("CodeableConcept")
                        || typeName.equals("Coding")
                        || typeName.equals("Quantity")
                        || typeName.equals("Money")
                        || typeName.equals("Period")
                        || typeName.equals("Money")
                        || typeName.equals("Annotation") ) {
                    sb.append("<img title=\"Data Type\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,R0lGODlhEAAQAOZ/APrkusOiYvvfqbiXWaV2G+jGhdq1b8GgYf3v1frw3vTUlsWkZNewbcSjY/DQkad4Hb6dXv3u0f3v1ObEgfPTlerJiP3w1v79+e7OkPrfrfnjuNOtZPrpydaxa+/YrvvdpP779ZxvFPvnwKKBQaFyF/369M2vdaqHRPz58/HNh/vowufFhfroxO3OkPrluv779tK0e6JzGProwvrow9m4eOnIifPTlPDPkP78+Naxaf3v0/zowfXRi+bFhLWUVv379/rnwPvszv3rye3LiPvnv+3MjPDasKiIS/789/3x2f747eXDg+7Mifvu0tu7f+/QkfDTnPXWmPrjsvrjtPbPgrqZW+/QlPz48K2EMv36866OUPvowat8Ivvgq/Pbrvzgq/PguvrgrqN0Gda2evfYm9+7d/rpw9q6e/LSku/Rl/XVl/LSlfrkt+zVqe7Wqv3x1/bNffbOf59wFdS6if3u0vrqyP3owPvepfXQivDQkO/PkKh9K7STVf779P///////yH5BAEAAH8ALAAAAAAQABAAAAfNgH+Cg36FfoOIhH4JBxBghYl/hQkNAV0IVT5GkJKLCwtQaSsSdx9aR26Gcwt2IkQaNRI6dBERIzCFDSgWSW8WCDkbBnoOQ3uFARc/JQJfCAZlT0x4ZFyFBxdNQT9ZCBNWKQoKUQ+FEDgcdTIAV14YDmg2CgSFA0hmQC5TLE4VRTdrKJAoxOeFCzZSwsw4U6BCizwUQhQyEaAPiAwCVNCY0FCNnA6GPAwYoETIFgY9loiRA4dToTYnsOxg8CBGHE6ICvEYQ4AKzkidfgoKBAA7\">");
                }
                if(typeName.equals("Reference")) {
                    sb.append("<img title=\"Reference to another Resource\" style=\"background-color: white; background-color: inherit\" alt=\".\" class=\"hierarchy\" src=\"data: image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAadEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My41LjEwMPRyoQAAAFxJREFUOE/NjEEOACEIA/0o/38GGw+agoXYeNnDJDCUDnd/gkoFKhWozJiZI3gLwY6rAgxhsPKTPUzycTl8lAryMyMsVQG6TFi6cHULyz8KOjC7OIQKlQpU3uPjAwhX2CCcGsgOAAAAAElFTkSuQmCC\">");
                }
            }
            sb.append(elementName)
                    .append("</td>\n");
            sb.append("  <td>Flags</td>\n")
                    
                    ;
            sb.append("  <td>");
            sb.append(min)
                    .append("..")
                    .append(max);
            
            sb.append("</td>\n");
            sb.append("  <td>")
                    .append(typeName)
                    .append("</td>\n");
            sb.append("<td>");
            sb.append("Description");
            sb.append("</td>");
            sb.append(" </tr>\n");
        }

        return sb.toString();
    }
    
    // Get the 'value' attribute of an element
    String getElementValue(Element item) {
        NamedNodeMap typeAttributes = item.getAttributes();
        String value = typeAttributes.getNamedItem("value").getTextContent();
        return value;
    }
}
