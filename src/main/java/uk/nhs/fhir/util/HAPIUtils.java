package uk.nhs.fhir.util;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.primitive.StringDt;

public class HAPIUtils {
	private static final FhirContext context = FhirContext.forDstu2();
	
	private HAPIUtils(){}
	
	public static FhirContext sharedFhirContext() {
		return context;
	}
	
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
	
	public static String periodToString(PeriodDt period) {
		Date start = period.getStart();
		Date end = period.getEnd();
		
		return StringUtil.dateToString(start) + " - " + StringUtil.dateToString(end);
	}

	public static String join(String delimiter, List<StringDt> wrappedStrings) {
		List<String> unwrappedStrings = Lists.newArrayList();
		wrappedStrings.forEach((StringDt wrapped) -> unwrappedStrings.add(wrapped.getValue()));
		return String.join(delimiter, unwrappedStrings);
	}

}
