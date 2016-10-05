/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.validator;

import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class Validator {
    
    public List<String> validateResource(BaseResource resource, String url) {
        List<String> probs = new ArrayList<String>();
        return probs;
    }
    
}
