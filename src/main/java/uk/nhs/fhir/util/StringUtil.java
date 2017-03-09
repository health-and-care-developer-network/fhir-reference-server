package uk.nhs.fhir.util;

public class StringUtil {
	public static boolean hasUpperCaseChars(String value) {
		for (char c : value.toCharArray()) {
			if (Character.isUpperCase(c)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * If the string only contains lowercase letters, capitalise the first letter.
	 */
	public static String capitaliseLowerCase(String value) {
		if (!hasUpperCaseChars(value)) {
			return value.substring(0,1).toUpperCase() + value.substring(1);
		} else {
			return value;
		}
	}
}
