package uk.nhs.fhir.util;

import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;

public class HAPIUtils {
	
	public static String resolveDatatypeValue(IDatatype datatype) {
		if (datatype == null) {
			return null;
		}
		
		if (datatype instanceof BasePrimitive) {
			return ((BasePrimitive<?>) datatype).getValueAsString();
		} else if (datatype instanceof ResourceReferenceDt) {
			return ((ResourceReferenceDt) datatype).getReference().getValueAsString();
		} else {
			throw new IllegalStateException("Unhandled type for datatype: " + datatype.getClass().getName());
		}
	}

}
