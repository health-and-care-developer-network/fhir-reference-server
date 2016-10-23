/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.makehtml;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class MyElement {

    String localName;
    String fullName;
    boolean isLast;
    String typeName;
    int level;
    boolean display;

    public MyElement(String newName, String resourceName) {
        this.fullName = newName;

        if(fullName.equals(resourceName)) {
            display = false;
            localName = newName;
        } else {
            localName = newName.substring(newName.indexOf(".") + 1);
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
}
