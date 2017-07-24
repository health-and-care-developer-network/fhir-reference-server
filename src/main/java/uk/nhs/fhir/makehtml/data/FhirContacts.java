package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class FhirContacts {
	private final Optional<String> individualName;
	private final List<FhirContact> telecoms = Lists.newArrayList();
	
	public FhirContacts(String individualName) {
		this.individualName = Optional.ofNullable(individualName);
	}
	
	public void addTelecom(FhirContact telecom) {
		telecoms.add(telecom);
	}

	public Optional<String> getName() {
		return individualName;
	}

	public List<FhirContact> getTelecoms() {
		return telecoms;
	}
}
