package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class FhirContact {
	private final Optional<String> individualName;
	private final List<String> telecoms = Lists.newArrayList();
	
	public FhirContact(String individualName) {
		this.individualName = Optional.ofNullable(individualName);
	}
	
	public void addTelecom(String telecom) {
		telecoms.add(telecom);
	}

	public Optional<String> getName() {
		return individualName;
	}

	public List<String> getTelecoms() {
		return telecoms;
	}
}
