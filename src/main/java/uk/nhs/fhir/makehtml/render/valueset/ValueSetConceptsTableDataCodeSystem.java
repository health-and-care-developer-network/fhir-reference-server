package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.url.FhirURL;

public class ValueSetConceptsTableDataCodeSystem {
	private final FhirURL codeSystem;
	private final List<ValueSetConceptsTableData> concepts = Lists.newArrayList();
	
	public ValueSetConceptsTableDataCodeSystem(FhirURL codeSystem) {
		this.codeSystem = codeSystem;
	}
	
	public void addConcept(String code, Optional<String> display, Optional<String> definition, Optional<String> mapping) {
		concepts.add(new ValueSetConceptsTableData(code, display, definition, mapping));
	}
	
	public FhirURL getCodeSystem() {
		return codeSystem;
	}
	
	public List<ValueSetConceptsTableData> getConcepts() {
		concepts.sort((data1, data2) -> data1.getCode().compareTo(data2.getCode()));
		return concepts;
	}
}
