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
package uk.nhs.fhir.makehtml;

import java.util.logging.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;

public class ResourceBuilder {

    protected static String addTextSectionToResource(String resourceXML, String textSection, String newBaseURL) {
        String serialised = null;
        FhirContext ctx = FhirContext.forDstu2();
        StructureDefinition structureDefinitionResource = null;
        structureDefinitionResource = (StructureDefinition) ctx.newXmlParser().parseResource(resourceXML);
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        structureDefinitionResource.setText(textElement);

        // Here (while we have the resource in as a StructureDefinition) we resolve any invalid (c) character in the Copyright section too!
        String copyRight = structureDefinitionResource.getCopyrightElement().getValue();
        if(copyRight != null) {
            copyRight = copyRight.replace("©", "&copy;");
            copyRight = copyRight.replace("\\u00a9", "&copy;");
            structureDefinitionResource.setCopyright(copyRight);
        }
        
        if (newBaseURL != null) {
        	String resourceName = structureDefinitionResource.getName();
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	//structureDefinitionResource.setBase(newBaseURL);
        	structureDefinitionResource.setUrl(newBaseURL+"/StructureDefinition/"+resourceName);
        }
        
        serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(structureDefinitionResource);
        serialised = serialised.replace("Σ", "&#931;");
        return serialised;
    }

}
