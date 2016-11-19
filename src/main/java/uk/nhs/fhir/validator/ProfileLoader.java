/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.validator;

import ca.uhn.fhir.context.FhirContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author tim
 */
public class ProfileLoader implements IValidationSupport {
    private static final Logger LOG = Logger.getLogger(ProfileLoader.class.getName());

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fc, ValueSet.ConceptSetComponent csc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext fc, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method to fetch a non-standard resource on demand, to be used as part of the validation.
     * 
     * @param <T>       The class of the resource being requested, eg StructureDefinition or similar
     * @param fc        The FHIRContext we're working with.
     * @param type      The class type.
     * @param string    The 'name' of the resource, which should be a URL.
     * @return          A Resource.
     */
    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fc, Class<T> type, String string) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(string);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (ProtocolException ex) {
            LOG.severe("Trying to fetch a profile - ProtocolException: " + ex.getMessage());
        } catch (MalformedURLException ex) {
            LOG.severe("Trying to fetch a profile - MalformedURLException: " + ex.getMessage());
        } catch (IOException ex) {
            LOG.severe("Trying to fetch a profile - IOException: " + ex.getMessage());
        }
        IBaseResource T = fc.newXmlParser().parseResource(result.toString());
        return (T) T;
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext fc, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeValidationResult validateCode(FhirContext fc, String string, String string1, String string2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
