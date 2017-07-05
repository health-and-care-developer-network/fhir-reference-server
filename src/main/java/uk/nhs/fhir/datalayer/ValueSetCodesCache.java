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
package uk.nhs.fhir.datalayer;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.CodeSystemConcept;

/**
 * A singleton object which holds a cached set of all codes in all ValueSets
 * hosted on this server.
 * 
 * @author tim.coates@hscic.gov.uk
 * @author Adam Hatherly
 */
public class ValueSetCodesCache {
    private static List<cacheObject> _cache;
    private static ValueSetCodesCache _instance = null;

    /**
     * Method to get our one instance of this object.
     * 
     * @return the singleton instance of our cache.
     */
    public static ValueSetCodesCache getInstance() {
        if(_instance == null) {
            _instance = new ValueSetCodesCache();
        }
        return _instance;
    }
    
    /**
     * Method to add all of the codes defined in a ValueSet to our cache.
     * 
     * @param theSet A ValueSet object which contains items we want to have cached.
     */
    public void cacheValueSet(ValueSet theSet) {
        
        // First we should remove any items previously added from this ValueSet
        for(cacheObject cacheItem : _cache) {
            if(cacheItem._valueSetID.equals(theSet.getId().getIdPart())) {
                _cache.remove(cacheItem);
            }
        }
        
        // Now we simply iterate through the ValueSet, adding each code we come across.
        List<CodeSystemConcept> codes = theSet.getCodeSystem().getConcept();
        for(CodeSystemConcept code : codes) {
            _cache.add(new cacheObject(code.getCode(), theSet.getId().getIdPart()));
        }
    }
    
    /**
     * Constructor which is never directly called, instead, getInstance should be called.
     */
    private ValueSetCodesCache() {
        _cache = new ArrayList();
    }   
    
    /**
     * Method to return a list of the ValueSets which contain a definition of the
     * requested code.
     * 
     * @param code String value for example 'R' in the below concept:

     * 	<codeSystem>
     *      <system value="http://fhir.nhs.net/ValueSet/registration-type-1"/>
     *          <concept>
     *              <code value="R"/>
     *              <display value="Fully Registered"/>
     *          </concept>
     * 
     * @return A List of ValueSet IDs, where this code was found.
     */
    public static List<String> findCode(String code) {
        List<String> matches = new ArrayList<String>();
        
        for(cacheObject cacheItem : _cache) {
            if(cacheItem._code.equals(code)) {
                matches.add(cacheItem._valueSetID);
            }
        }               
        return matches;
    }
    
    /**
     * Private internal class, to represent the items we're caching. Each on is simply a pair of the
     * code, and the ValueSet ID.
     * 
     */
    private class cacheObject {
        protected String _code;
        protected String _valueSetID;

        /**
         * Constructor.
         * 
         * @param newCode
         * @param newValueSetID
         */
        public cacheObject(String newCode, String newValueSetID) {
            this._code = newCode;
            this._valueSetID = newValueSetID;
        }
    }
}
