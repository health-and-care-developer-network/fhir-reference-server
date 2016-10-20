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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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
    
    
    ArrayList<String> doValidate(String p, byte[] d) throws Exception {
        LOG.info("validate method called");
        if (engine == null) {
            throw new Exception("No validation engine");
        }
        
        ArrayList<String> results = null;
        LOG.info("Resetting the engine");
        engine.reset();
        if (p != null) {
            LOG.info("Loading profile");
            engine.loadProfile(p);
        }
        LOG.info("Setting source");
        engine.setSource(d);
        LOG.info("About to ask engine to process data...");
        try {
            engine.processXml();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FHIRException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        LOG.info("Engine has finished processing");
        OperationOutcome outcome = engine.getOutcome();
        if (outcome != null) {
            LOG.info("Faults found, creating results");
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
        } else {
            LOG.info("completed with no faults found");
        }
        LOG.info("Recycling this validator");
        manager.recycleValidator(identifier);
        LOG.info("validation completed");
        return results;
    }    
}
