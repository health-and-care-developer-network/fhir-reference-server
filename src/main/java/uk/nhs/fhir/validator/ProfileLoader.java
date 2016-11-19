/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.validator;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author tim
 */
public class ProfileLoader implements IValidationSupport {

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fc, ValueSet.ConceptSetComponent csc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext fc, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fc, Class<T> type, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
