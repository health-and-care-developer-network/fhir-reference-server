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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Coates
 */
public class MyElementTest {
    
    public MyElementTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of toString method, of class MyElement.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "MyElement [localName=Name, fullName=A.New.Name, isLast=false, typeName=New type name, level=2, display=true, type=newType, myCardinality=1..1, myFlags=FlagsHere, myDescription=The description, myHover=Hover text, changed=true]";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of isChanged method, of class MyElement.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        boolean expResult = true;
        boolean result = instance.isChanged();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class MyElement.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "newType";
        String result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDisplay method, of class MyElement.
     */
    @Test
    public void testSetDisplay() {
        System.out.println("setDisplay");
        boolean display = false;
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        assertEquals(instance.isDisplay(), true);
        instance.setDisplay(display);
        assertEquals(instance.isDisplay(), false);
    }

    /**
     * Test of getFullName method, of class MyElement.
     */
    @Test
    public void testGetFullName() {
        System.out.println("getFullName");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "A.New.Name";
        String result = instance.getFullName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLevel method, of class MyElement.
     */
    @Test
    public void testGetLevel() {
        System.out.println("getLevel");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        int expResult = 2;
        int result = instance.getLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLevel method, of class MyElement.
     */
    @Test
    public void testSetLevel() {
        System.out.println("setLevel");
        int level = 4;
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        instance.setLevel(level);
        int result = instance.getLevel();
        assertEquals(result, level);
    }

    /**
     * Test of getLocalName method, of class MyElement.
     */
    @Test
    public void testGetLocalName() {
        System.out.println("getLocalName");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "Name";
        String result = instance.getLocalName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLocalName method, of class MyElement.
     */
    @Test
    public void testSetLocalName() {
        System.out.println("setLocalName");
        String localName = "DifferentName";
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        instance.setLocalName(localName);
        String result = instance.getLocalName();
        assertEquals(result, localName);
    }

    /**
     * Test of isIsLast method, of class MyElement.
     */
    @Test
    public void testIsIsLast() {
        System.out.println("isIsLast");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        boolean expResult = false;
        boolean result = instance.isIsLast();
        assertEquals(expResult, result);
    }

    /**
     * Test of setIsLast method, of class MyElement.
     */
    @Test
    public void testSetIsLast() {
        System.out.println("setIsLast");
        boolean isLast = true;
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        instance.setIsLast(isLast);
        boolean result = instance.isIsLast();
        assertEquals(isLast, result);
    }

    /**
     * Test of getTypeName method, of class MyElement.
     */
    @Test
    public void testGetTypeName() {
        System.out.println("getTypeName");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "New type name";
        String result = instance.getTypeName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTypeName method, of class MyElement.
     */
    @Test
    public void testSetTypeName() {
        System.out.println("setTypeName");
        String typeName = "TestTypeName";
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        instance.setTypeName(typeName);
        String result = instance.getTypeName();
        assertEquals(typeName, result);
    }

    /**
     * Test of getFlags method, of class MyElement.
     */
    @Test
    public void testGetFlags() {
        System.out.println("getFlags");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "FlagsHere";
        String result = instance.getFlags();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCardinality method, of class MyElement.
     */
    @Test
    public void testGetCardinality() {
        System.out.println("getCardinality");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "1..1";
        String result = instance.getCardinality();
        assertEquals(expResult, result);
    }

    /**
     * Test of isDisplay method, of class MyElement.
     */
    @Test
    public void testIsDisplay() {
        System.out.println("isDisplay");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        boolean expResult = true;
        boolean result = instance.isDisplay();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNiceTitle method, of class MyElement.
     */
    @Test
    public void testGetNiceTitle() {
        System.out.println("getNiceTitle");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"Hover text\">Name</span>";
        String result = instance.getNiceTitle();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class MyElement.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        MyElement instance = new MyElement("A.New.Name", "1..1", "newType", "New type name", "FlagsHere", "The description", "Hover text", true);
        String expResult = "The description";
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }
    
}
