package uk.nhs.fhir.data.wrap;

import java.util.List;
import java.util.Set;

import uk.nhs.fhir.data.structdef.ConstraintInfo;

public interface HasConstraints {

	public Set<String> getConditionIds();
	public List<ConstraintInfo> getConstraintInfos();
	public String getIdentifierString();

}
