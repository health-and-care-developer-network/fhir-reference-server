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
package uk.nhs.fhir.validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class ValidatorFactoryTest {
    
    public ValidatorFactoryTest() {
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
     * Test of getValidator method, of class ValidatorFactory.
     */
    @Test
    public void testGetValidator() {
        System.out.println("getValidator");
        FhirContext ctx1 = FhirContext.forDstu2();
        FhirContext ctx2 = FhirContext.forDstu2Hl7Org();
        
        // Get a validator...
        FhirValidator result = ValidatorFactory.getValidator(ctx1);
        assertNotNull(result);
        
        /// Get another validator, SHOULD reuse the same one...
        result = ValidatorFactory.getValidator(ctx1);
        assertNotNull(result);
        
        // Get another one for a different context, should bin it and recreate a new one...
        result = ValidatorFactory.getValidator(ctx2);
        assertNotNull(result);
    }
    
}
