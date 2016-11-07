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

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class MyElement {

    private String localName;
    private boolean isLast;
    private String typeName;
    private int level;
    private boolean display;
    private String type;

    private String myCardinality;
    private String myFlags;
    private String myDescription;
    private String myHover;
    private String fullName;
    private boolean changed = false;

    public boolean isChanged() {
        return changed;
    }

    public MyElement(String newName, String cardinality, String newType, String typeName, String theFlags, String description, String hover, boolean hasChanged) {        
        // Set what level of indentation we're at...
        this.level = StringUtils.countMatches(newName, ".");
        
        // Set the hover text
        this.myHover = hover;
        
        // Set the cardinality
        this.myCardinality = cardinality;
        
        // Set type name
        this.type = newType;
        this.typeName = typeName;

        // Set the flags
        this.myFlags = theFlags;
        
        // Store the full name
        this.fullName = newName;
        
        // Extract the local name from after the last dot.
        if(level > 0)
            this.localName = newName.substring(StringUtils.lastIndexOf(newName, ".")+1);
        else
            this.localName = newName;
        
        // Set the title
        this.myDescription = description;
        
        // Set whether we've been changed from the base resource...
        this.changed = hasChanged;
        
        // If it's the root resource then clearly we're going to be showing it...
        if(this.level == 0) {
            this.display = true;
        } else {
            // If not, then we display as long as it's not one of these...
            if(localName.equals("id")
                    || localName.equals("meta")
                    || localName.equals("language")
                    || localName.equals("implicitRules")
                    || localName.equals("extension")
                    || localName.equals("modifierExtension")
                    || localName.equals("contained")
                    || localName.equals("text")) {
                display = false;
            } else {
                display = true;
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">    
    public String getType() {
        return type;
    }


        
    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getFullName() {
        return fullName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    public boolean isIsLast() {
        return isLast;
    }
    
    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getFlags() {
        return myFlags;
    }

    public String getCardinality() {
        return myCardinality;
    }

    public boolean isDisplay() {
        return display;
    }
    
    public String getNiceTitle() {
        return "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"" + cleanHoverText(myHover) + "\">" + localName + "</span>";
    }
    
    public String getDescription() {
        return this.myDescription;                
    }
    
    private static String cleanHoverText(String hover) {
    	return hover.replace("\"", "&quot;");
    	
    }
//</editor-fold>
}
