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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Tim Coates
 */
public class ProfileLoader implements IValidationSupport {

    private static final Logger LOG = Logger.getLogger(ProfileLoader.class.getName());

    /**
     * Need to be clear that this method is effectively stubbed out, pending
     * some more details of what it's expected or required to do.
     *
     *
     * @param fc
     * @param csc
     * @return
     */
    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fc, ValueSet.ConceptSetComponent csc) {
        return null;
    }

    /**
     * Method to fetch a remote ValueSet which is required for validation.
     *
     * @param fc Our current FHIR Context
     * @param string The URL of the ValueSet
     *
     * @return A ValueSet resource
     */
    @Override
    public ValueSet fetchCodeSystem(FhirContext fc, String string) {
        ValueSet theVS = new ValueSet();

        theVS = (ValueSet) ResourceCache.getResource(string);
        if (theVS != null) {
            LOG.fine("CodeSystem: " + string + " Was in cache.");
        } else {
            LOG.fine("CodeSystem: " + string + " Was NOT in cache, will fetch it...");

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(string);
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                } catch (IOException ex) {
                    LOG.severe("Trying to fetch a ValueSet, IOException caught when trying to connect to : " + string);
                }
                if (conn != null) {
                    conn.setRequestMethod("GET");
                    try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                        int httpStatus = conn.getResponseCode();
                        if (httpStatus == 200) {
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
            LOG.fine("Adding ValueSet to cache");
            ResourceCache.putResource(string, theVS);
        }
        LOG.fine("ValueSet fetched.");
        return theVS;
    }

    /**
     * Method to fetch a non-standard resource on demand, to be used as part of
     * the validation.
     *
     * @param <T> The class of the resource being requested, likely to be a
     * StructureDefinition or ValueSet
     * @param fc The FHIRContext we're working with.
     * @param type The class type.
     * @param string The 'name' of the resource, which should be a URL.
     * @return A Resource.
     */
    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fc, Class<T> type, String string) {
        // NB: We need to decide here whether we should inspect the url, and fetch the file locally, or
        // just http fetch it even if we're fetching it from this server.
        DomainResource theResource = null;

        theResource = ResourceCache.getResource(string);

        LOG.fine("Checking for Resource in cache...");
        if (theResource != null) {
            LOG.fine("Resource: " + string + " Was in cache.");
        } else {
            LOG.fine("Resource: " + string + " Was NOT in cache, will fetch it...");

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(string);
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                } catch (IOException ex) {
                    LOG.severe("Trying to fetch a Resource, IOException caught when trying to connect to : " + string);
                }
                if (conn != null) {
                    conn.setRequestMethod("GET");
                    try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                        int httpStatus = conn.getResponseCode();
                        if (httpStatus == 200) {
                            String line;
                            while ((line = rd.readLine()) != null) {
                                result.append(line);
                            }
                        } else {
                            LOG.warning("Got http status code: " + httpStatus + " when requesting Resource: " + string);
                        }
                    } catch (IOException ex) {
                        LOG.warning("IOException when trying to read from URL: " + string);
                    }
                }
            } catch (ProtocolException ex) {
                LOG.severe("Trying to fetch a Resource - ProtocolException: " + string);
            } catch (MalformedURLException ex) {
                LOG.severe("Trying to fetch a Resource - MalformedURLException: " + string);
            }
            
            String xmlFileContents = result.toString();
            if(xmlFileContents.equals("")) {
                LOG.severe("Empty string, won't try to parse or cache it.");
            } else {
                try {
                    FhirContext fcHL7 = FhirContext.forDstu2Hl7Org();
                    theResource = (DomainResource) fcHL7.newXmlParser().parseResource(type, xmlFileContents);
                    LOG.fine("Fetched Resource: " + string + " adding it to cache.");
                    ResourceCache.putResource(string, (DomainResource) theResource);
                } catch(Exception ex) {
                    LOG.severe("Exception thrown parsing resource: " + string);
                    LOG.severe(ex.getMessage());
                }
            }
        }
        return (T) theResource;
    }

    /**
     * We simply assume that if the CodeSystem begins with http://fhir.nhs.uk
     * then we know about this CodeSyatem, and will be able to validate it's
     * component parts.
     *
     * @param fc A FHIR Context
     * @param string The URL of the CodeSystem
     * @return
     */
    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
        if (theSystem.startsWith("http://fhir.nhs.uk/")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method checks whether the code and display are acceptable pair in
     * the specified system.
     *
     * @param theContext Our FHIR Context
     * @param theCodeSystem The System we think the code and display exist in
     * @param theCode The code we're testing
     * @param theDisplay The display value we think matches that code
     *
     * @return
     */
    @Override
    public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
        CodeValidationResult result;
        ValueSet vs = fetchResource(theContext, ValueSet.class, theCodeSystem);
        // Here see if we can find the code in the ValueSet...
        ValueSet.ConceptDefinitionComponent definition = new ValueSet.ConceptDefinitionComponent();
        definition.setDisplay(theDisplay);
        List<ValueSet.ConceptDefinitionComponent> concepts = vs.getCodeSystem().getConcept();
        for (ValueSet.ConceptDefinitionComponent concept : concepts) {
            if (concept.getCode().equals(theCode)) {
                if (concept.getDisplay().equals(theDisplay)) {
                    result = new CodeValidationResult(definition);
                    return result;
                }
            }
        }
        return new CodeValidationResult(IssueSeverity.ERROR, "Code or display not matched: " + theCodeSystem + " / " + theCode + " / " + theDisplay);
    }
}
