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
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.FhirVersion;

public class HAPIUtils {
	private static final FhirContext DSTU2_CONTEXT = FhirContext.forDstu2();
	private static final FhirContext DSTU3_CONTEXT = FhirContext.forDstu3();
	
	// never instantiated
	private HAPIUtils(){}
	
	public static FhirContext fhirContext(FhirVersion fhirVersion) {
		switch (fhirVersion) {
		case DSTU2:
			return DSTU2_CONTEXT;
		case STU3:
			return DSTU3_CONTEXT;
		default:
			throw new IllegalStateException("No context/parser available for " + fhirVersion.toString());
		}
	}

	public static IParser xmlParser(FhirVersion fhirVersion) {
		return fhirContext(fhirVersion).newXmlParser();
	}
	
	public static IParser dstu2XmlParser() {
		return DSTU2_CONTEXT.newXmlParser();
	}

	public static IParser dstu3XmlParser() {
		return DSTU3_CONTEXT.newXmlParser();
	}
	
	public static String resolveDatatypeValue(IDatatype datatype) {
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
