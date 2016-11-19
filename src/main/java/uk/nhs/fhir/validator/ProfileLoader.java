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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Tim Coates
 */
public class ProfileLoader implements IValidationSupport {
    private static final Logger LOG = Logger.getLogger(ProfileLoader.class.getName());

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fc, ValueSet.ConceptSetComponent csc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method to fetch a remote ValueSet which is required for validation.
     * 
     * @param fc        Our current FHIR Context
     * @param string    The URL of the ValueSet
     * 
     * @return          A ValueSet resource
     */
    @Override
    public ValueSet fetchCodeSystem(FhirContext fc, String string) {
        ValueSet theVS = new ValueSet();

        LOG.info("Requesting ValueSet: " + string);

                StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(string);
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                LOG.severe("Trying to fetch a ValueSet, IOException caught when trying to connect to : " + string);
            }
            if(conn != null) {   
                conn.setRequestMethod("GET");
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    
                    int httpStatus = conn.getResponseCode();
                    if(httpStatus == 200) {
                        String line;
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }
                    } else {
                        LOG.warning("Got http status code: " + httpStatus + " when requesting: " + string);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProfileLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ProtocolException ex) {
            LOG.severe("Trying to fetch a ValueSet - ProtocolException: " + string);
        } catch (MalformedURLException ex) {
            LOG.severe("Trying to fetch a ValueSet - MalformedURLException: " + string);
        }
        theVS = (ValueSet) fc.newXmlParser().parseResource(result.toString());
        return theVS;
    }

    /**
     * Method to fetch a non-standard resource on demand, to be used as part of the validation.
     * 
     * @param <T>       The class of the resource being requested, likely to
     *                      be a StructureDefinition or ValueSet
     * @param fc        The FHIRContext we're working with.
     * @param type      The class type.
     * @param string    The 'name' of the resource, which should be a URL.
     * @return          A Resource.
     */
    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fc, Class<T> type, String string) {        
// NB: We need to decide here whether we should inspect the url, and fetch the file locally, or
// just http fetch it even if we're fetching it from this server.
        LOG.info("Requesting resource: " + string);
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(string);
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                LOG.severe("Trying to fetch a profile, IOException caught when trying to connect to : " + string);
            }
            if(conn != null) {   
                conn.setRequestMethod("GET");
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    
                    int httpStatus = conn.getResponseCode();
                    if(httpStatus == 200) {
                        String line;
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }
                    } else {
                        LOG.warning("Got http status code: " + httpStatus + " when requesting: " + string);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProfileLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ProtocolException ex) {
            LOG.severe("Trying to fetch a profile - ProtocolException: " + string);
        } catch (MalformedURLException ex) {
            LOG.severe("Trying to fetch a profile - MalformedURLException: " + string);
        }
        IBaseResource T = fc.newXmlParser().parseResource(result.toString());
        return (T) T;
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext fc, String string) {
        throw new UnsupportedOperationException("ProfileLoader.isCodeSystemSupported() Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeValidationResult validateCode(FhirContext fc, String string, String string1, String string2) {
        throw new UnsupportedOperationException("ProfileLoader.validateCode() Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
