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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.dstu2.model.StringType;
import org.hl7.fhir.dstu2.model.OperationOutcome;
import org.hl7.fhir.dstu2.validation.ValidationEngine;
import org.hl7.fhir.exceptions.FHIRException;
import org.xml.sax.SAXException;
        
/**
 *
 * @author damian
 */
public class Validator {
    private static final Logger LOG = Logger.getLogger(Validator.class.getName());
    protected static final String VALIDATORDEFINITIONS = "validation.xml.zip";
    protected static final int DEFINITIONSBUFFER = 2048000;
    protected static final String DEFAULT_FHIR_TERMINOLOGY_SERVER = "http://fhir2.healthintersections.com.au/open";
    
    protected String type = null;
    protected String txUrl = null;
    
    protected static ValidationEngine engine = null;
    
    private Integer identifier = null;
    private ValidatorManager manager = null;
    
    Validator(int n, ValidatorManager m, byte[] definitionsBuffer)
            throws IOException, SAXException, URISyntaxException, FHIRException
    {
        LOG.log(Level.INFO, "Creating validator: #{0}", n);
        identifier = new Integer(n);
        manager = m;
        engine = new ValidationEngine();
        if(engine != null){
            LOG.info("Validation engine created");
        }
        engine.readDefinitions(definitionsBuffer);
        engine.connectToTSServer(txUrl == null ? DEFAULT_FHIR_TERMINOLOGY_SERVER : txUrl);
        LOG.info("Connected to terminology server");
    }
  
    Integer getIdentifier() { return identifier; }
    
    
    private ArrayList<String> validate(boolean xml, String p, byte[] d)
            throws Exception
    {
        LOG.info("validate method called");
        if (engine == null) {
            throw new Exception("No validation engine");
        }
        
        ArrayList<String> results = null;
        engine.reset();
        if (p != null)
            engine.loadProfile(p);
        engine.setSource(d);
        if (xml)
            engine.processXml();
        else
            engine.processJson();
        OperationOutcome outcome = engine.getOutcome();
        if (outcome != null) {
            results = new ArrayList<>();
            for (OperationOutcome.OperationOutcomeIssueComponent ooic : outcome.getIssue()) {
                boolean pass = true;
                StringBuilder octext = new StringBuilder(ooic.getDetails().getText());
                if (ooic.getLocation() != null) {
                    octext.append(" -- Location: ");
                    for (StringType s : ooic.getLocation()) {
                        octext.append(s.toString());
                    }
                }
                results.add(octext.toString());
            }
        }
        manager.recycleValidator(identifier);
        LOG.info("validation completed");
        return results;
    }
    
    public ArrayList<String> validateXml(String p, byte[] d)
            throws Exception
    {
        return validate(true, p, d);
    }
    
    public ArrayList<String> validateXml(String p, String d)
            throws Exception
    {
        return validate(true, p, d.getBytes());
    }

    public ArrayList<String> validateJson(String p, byte[] d)
            throws Exception
    {
        return validate(false, p, d);
    }
    
    public ArrayList<String> validateJson(String p, String d)
            throws Exception
    {
        return validate(false, p, d.getBytes());
    }
    
    private byte[] readStream(InputStream i)
            throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte [] buffer = new byte[DEFINITIONSBUFFER];
        BufferedInputStream bis = new BufferedInputStream(i);
        int r = -1;
        while ((r = bis.read(buffer, 0, DEFINITIONSBUFFER)) != -1) {
            baos.write(buffer, 0, r);
        }
        return baos.toByteArray();
    }
    
    public ArrayList<String> validateXml(String p, InputStream i)
            throws Exception
    {
        return validate(true, p, readStream(i));
    }
    
    public ArrayList<String> validateJson(String p, InputStream i)
            throws Exception
    {
        return validate(false, p, readStream(i));
    }    
}
