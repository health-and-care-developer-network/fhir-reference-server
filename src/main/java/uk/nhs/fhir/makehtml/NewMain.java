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
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File("c:\\users\\tico3\\account.profile.xml"));
            
            NodeList snapshot = document.getElementsByTagName("snapshot");
            Node node = snapshot.item(0);
            Element node1 = (Element) snapshot.item(0);
            NodeList elements = node1.getElementsByTagName("element");


            // Here we're looping through the elements...
            for(int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                if(element.getNodeType() == Node.ELEMENT_NODE) {
                    // Get the name of it...
                    NodeList path = element.getElementsByTagName("path");
                    NamedNodeMap attributes = path.item(0).getAttributes();
                    System.out.println("[" + attributes.getNamedItem("value").getTextContent() + "]");
                    
                    // Get the type...
                    NodeList type = element.getElementsByTagName("type");
                    Element typeElement = (Element) type.item(0);
                    
                    NodeList codeElement = typeElement.getElementsByTagName("code");
                    Element typecodeElement = (Element) codeElement.item(0);
                    
                    NamedNodeMap typeAttributes = typecodeElement.getAttributes();
                    System.out.println("[" + typeAttributes.getNamedItem("value").getTextContent() + "]\n");
                    
                }
            }

            
        } catch (SAXException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
