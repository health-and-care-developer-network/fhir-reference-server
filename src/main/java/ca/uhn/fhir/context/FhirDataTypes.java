package ca.uhn.fhir.context;

import java.util.List;

public interface FhirDataTypes<TYPE> {

	public List<TYPE> knownTypes(List<TYPE> type);

}
