package uk.nhs.fhir.makehtml.old;

import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getDescription;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getElementCardinality;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getElementName;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getElementTypeList;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getElementTypeName;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getFlags;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getQuantityType;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getReferenceTypes;
import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getTitle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.DataTypes;
import uk.nhs.fhir.makehtml.MyElement;

public class StructureDefinitionHTMLMakerOLD extends HTMLMakerOLD {
    private static final Logger LOG = Logger.getLogger(StructureDefinitionHTMLMakerOLD.class.getName());

   /**
    * Process a specific file.
    *
    * @param filename
    */
	@Override
    public String makeHTML(Document document) {

       StringBuilder sb = new StringBuilder();
       sb.append(TABLESTART);

       List<MyElement> elementList = Lists.newArrayList();
       Element snapshotNode = document.getDescendants(new ElementFilter("snapshot")).next();

       List<Element> elements = Lists.newArrayList();
       for (Element e : snapshotNode.getDescendants(new ElementFilter("element"))){
    	   elements.add(e);
       }
       int snapshotElementCount = elements.size();

       // Now get a list of the names of elements which show as having been changed by this profile...
       ArrayList<String> changedNodes = GetChangedNodes(document);


       // First we process all the elements...
       for(int i = 0; i < snapshotElementCount; i++) {
           Element element = (Element) elements.get(i);
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

               switch (typeName) {
                   case "Multiple_Type_Choice":
                	   List<String> types = getElementTypeList(element);
                       elementList.add(new MyElement(elementName, cardinality, typeName, typeName, flags, description, hoverText, hasChanged));
                       for(String type : types) {
                           elementList.add(new MyElement(elementName + "." + type, "", type, type, flags, "", "", hasChanged));
                       }
                       break;
                   case "Reference":
                	   String referenceTypeName = getReferenceTypes(element);
                       elementList.add(new MyElement(elementName, cardinality, typeName, referenceTypeName, flags, description, hoverText, hasChanged));
                       break;
                   case "Quantity":
                	   String quantityTypeName = getQuantityType(element);
                       elementList.add(new MyElement(elementName, cardinality, typeName, quantityTypeName, flags, description, hoverText, hasChanged));
                       break;
                   default:
                	   elementList.add(new MyElement(elementName, cardinality, typeName, typeName, flags, description, hoverText, hasChanged));
                	   break;
               }
               /*if(typeName.equals("Multiple_Type_Choice")) {
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
               }*/
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
       
       boolean fieldHasBeenRemovedInProfile = false;
       int levelWhereFieldWasRemoved = 1;
       
       // Build HTML table
       for(int i = 0; i < elementList.size(); i++) {
           MyElement item = (MyElement) elementList.get(i);
           
           int level = item.getLevel();
           
           // If this field has a cardinality of 0..0 it has been removed in this
           // profile, make a note of this so we can also remove any child fields
           if (fieldHasBeenRemovedInProfile == true) {
           	if (level > levelWhereFieldWasRemoved) {
           		// Do nothing - we are in a child of a removed field, so keep this field removed too
           	} else {
           		// We are back up to a level where we need to check again if the field is removed
           		fieldHasBeenRemovedInProfile = "0..0".equals(item.getCardinality());
           		if (fieldHasBeenRemovedInProfile) {
           			levelWhereFieldWasRemoved = level;
           		}
           	}
           } else {
           	// Check if the field is removed
       		fieldHasBeenRemovedInProfile = "0..0".equals(item.getCardinality());
       		if (fieldHasBeenRemovedInProfile) {
       			levelWhereFieldWasRemoved = level;
       		}
           }

           if(item.isDisplay()) {

               //if (fieldHasBeenRemovedInProfile) {
//               	sb.append(START_TABLE_ROW_REMOVED_FIELD);
               //} else {
               	sb.append(START_TABLE_ROW);
//               }

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

//Now figure out whether there are more level one elements to come, so do we continue the very left leg of the tree?
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

               if(item.isChanged() && !fieldHasBeenRemovedInProfile) {
                   sb.append("<b>");
                   sb.append(item.getNiceTitle(fieldHasBeenRemovedInProfile));
                   sb.append("</b>");
               } else {
                   sb.append(item.getNiceTitle(fieldHasBeenRemovedInProfile));
               }
               sb.append(END_TABLE_CELL);

               // Now the flags column
               sb.append(item.getHTMLWrappedFlags(fieldHasBeenRemovedInProfile));

               // Now the Cardinality column
               sb.append(item.getHTMLWrappedCardinality(fieldHasBeenRemovedInProfile));

               // Now the type column
               if (fieldHasBeenRemovedInProfile)
               	sb.append(START_TABLE_CELL_REMOVED_FIELD);
               else
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
               sb.append(item.getHTMLWrappedDescription(fieldHasBeenRemovedInProfile));

               sb.append(END_TABLE_ROW);
           }
       }
       sb.append(" </table>\n");
       sb.append("</div>");

       LOG.info("\n=========================================\nhtml generated\n=========================================");
       return sb.toString();
   }

}
