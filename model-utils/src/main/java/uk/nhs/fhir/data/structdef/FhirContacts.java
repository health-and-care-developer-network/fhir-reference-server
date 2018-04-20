package uk.nhs.fhir.data.structdef;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof FhirContacts)) {
			return false;
		}
		
		FhirContacts otherFhirContacts = (FhirContacts)other;
		
		return individualName.equals(otherFhirContacts.getName())
		  && telecoms.equals(otherFhirContacts.getTelecoms());
	}

	@Override
	public int hashCode() {
		return individualName.hashCode() * telecoms.hashCode();
	}

	@Override
	public String toString() {
		return "FhirContacts" + telecoms.stream().map(FhirContact::toString).collect(Collectors.joining(", ", "[", "]"));
	}
}
