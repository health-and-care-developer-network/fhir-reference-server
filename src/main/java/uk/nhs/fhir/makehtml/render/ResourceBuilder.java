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
package uk.nhs.fhir.makehtml.render;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.makehtml.prep.ResourcePreparer;
import uk.nhs.fhir.util.HAPIUtils;

public class ResourceBuilder {
	private final ResourcePreparer<StructureDefinition> structureDefinitionPreparer;
	private final ResourcePreparer<ValueSet> valueSetPreparer;
	private final ResourcePreparer<OperationDefinition> operationDefinitionPreparer;
	private final ResourcePreparer<ImplementationGuide> implementationGuidePreparer;

	public ResourceBuilder(
		ResourcePreparer<StructureDefinition> structureDefinitionPreparer,
		ResourcePreparer<ValueSet> valueSetPreparer,
		ResourcePreparer<OperationDefinition> operationDefinitionPreparer,
		ResourcePreparer<ImplementationGuide> implementationGuideSanitizer) {

		Preconditions.checkNotNull(structureDefinitionPreparer);
		Preconditions.checkNotNull(valueSetPreparer);
		Preconditions.checkNotNull(operationDefinitionPreparer);
		Preconditions.checkNotNull(implementationGuideSanitizer);
		
		this.structureDefinitionPreparer = structureDefinitionPreparer;
		this.valueSetPreparer = valueSetPreparer;
		this.operationDefinitionPreparer = operationDefinitionPreparer;
		this.implementationGuidePreparer = implementationGuideSanitizer;
	}
	
	protected String addTextSection(String resourceXML, String textSection, String newBaseURL) throws Exception {
		FhirContext ctx = HAPIUtils.sharedFhirContext();
		BaseResource resource = parseResource(ctx, resourceXML);
		addHumanReadableText(resource, textSection);
        prepareResource(resource, newBaseURL);
		
		String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        serialised = serialised.replace("Σ", "&#931;");
        return serialised;
	}

	private void addHumanReadableText(BaseResource resource, String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        resource.setText(textElement);
	}

	private static BaseResource parseResource(FhirContext ctx, String resourceXML) throws Exception {
		IBaseResource iResource = ctx.newXmlParser().parseResource(resourceXML);
		
		if (iResource instanceof BaseResource) {
			return (BaseResource)iResource;
		} else {
			throw new Exception("Parsed object wasn't an instance of BaseResource");
		}
	}
	
	private void prepareResource(BaseResource resource, String newBaseURL) throws Exception {
		if (resource instanceof StructureDefinition) {
			StructureDefinition structureDefinition = (StructureDefinition)resource;
			structureDefinitionPreparer.prepare(structureDefinition, newBaseURL);
		} else if (resource instanceof ValueSet) {
			ValueSet valueSet = (ValueSet)resource;
			valueSetPreparer.prepare(valueSet, newBaseURL);
		} else if (resource instanceof OperationDefinition) {
			OperationDefinition operationDefinition = (OperationDefinition)resource;
			operationDefinitionPreparer.prepare(operationDefinition, newBaseURL);
		} else if (resource instanceof ImplementationGuide) {
			ImplementationGuide implementationGuide = (ImplementationGuide)resource;
			implementationGuidePreparer.prepare(implementationGuide, newBaseURL);
		} else {
			throw new Exception("Deserialised file was not any recognised type");
		}
	}
}
