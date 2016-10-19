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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.exceptions.FHIRException;
import org.xml.sax.SAXException;
import static uk.nhs.fhir.validator.Validator.DEFINITIONSBUFFER;
/**
 *
 * @author damian
 */
public class ValidatorManager {
    private static final Logger LOG = Logger.getLogger(ValidatorManager.class.getName());
    protected static final String VALIDATORDEFINITIONS = "validation.xml.zip";
    private static final String VALIDATORPOOLSIZE = "uk.nhs.fhir.validator.poolsize";
    private static final int DEFAULTPOOLSIZE = 3;
    private static ValidatorManager validatorFactory = new ValidatorManager();
    private static Throwable bootError = null;
    
    private LinkedList<Validator> free = null;
    private HashMap<Integer,Validator> used = null;
    
    public static ValidatorManager getInstance() 
            throws Throwable
    {
        LOG.info("ValidatorManager instance requested");
        if (bootError != null)
            throw bootError;
        return validatorFactory; 
    }
    
    public synchronized Validator getValidator()
            throws RuntimeException
    {
        LOG.info("Validator requested");
        if (free.isEmpty()) {
            LOG.warning("NONE AVAILABLE");
            throw new RuntimeException("No validator instances available, try again later");
        }
        
        Validator v = free.removeFirst();
        used.put(v.getIdentifier(), v);
        LOG.log(Level.INFO, "returning validator: #{0}", v.getIdentifier());
        return v;
    }
    
    synchronized void recycleValidator(Integer i) 
    {
        LOG.log(Level.INFO, "Recycling validator: #{0}", i);
        Validator v = used.get(i);
        if (v == null)
            return;
        free.add(used.remove(i));
    }
    
    private ValidatorManager()
    {
        LOG.info("Creating new ValidatorManager with pool size: " + VALIDATORPOOLSIZE);
        try {
            free = new LinkedList<>();
            used = new HashMap<>();
            String p = System.getProperty(VALIDATORPOOLSIZE);
            int poolSize = (p == null) ? DEFAULTPOOLSIZE : Integer.parseInt(p);
            byte[] buffer = new byte[DEFINITIONSBUFFER];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedInputStream bis = new BufferedInputStream(getClass().getResourceAsStream(VALIDATORDEFINITIONS));
            int r = -1;
            while ((r = bis.read(buffer, 0, DEFINITIONSBUFFER)) != -1) {
                os.write(buffer, 0, r);
            }
            byte[] validationFileData = os.toByteArray();
            LOG.log(Level.INFO, "Read validation file of size : {0} bytes into memory", validationFileData.length);
            
            for (int i = 0; i < poolSize; i++) {
                LOG.info("Requesting a new validator to add to my pool...");
                Validator v = new Validator(i, this, validationFileData);
                free.add(v);
            }
        } catch (IOException | SAXException | URISyntaxException | FHIRException ex) {
            LOG.log(Level.SEVERE, "Exception caught in ValidatorManager constructor: {0}", ex.getMessage());
            bootError = ex;
        }
    }
}
