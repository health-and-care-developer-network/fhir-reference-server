package uk.nhs.fhir.util;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.primitive.StringDt;

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
	 * If the string only contains lowercase letters, capitalise the first letter at the start of each list 
	 * of letters.
	 */
	public static String capitaliseLowerCase(String value) {
		if (!hasUpperCaseChars(value)) {
			char[] chars = value.toCharArray();
			
			boolean inLetters = false;
			for (int i=0; i<chars.length; i++) {
				char c = chars[i];
				if (Character.isLetter(c) && !inLetters) {
					chars[i] = Character.toUpperCase(c);
					inLetters = true;
				} else if (!Character.isLetter(c) && inLetters) {
					inLetters = false;
				}
			}
			
			return new String(chars);
		} else {
			return value;
		}
	}

	public static String join(String delimiter, List<StringDt> wrappedStrings) {
		List<String> unwrappedStrings = Lists.newArrayList();
		wrappedStrings.forEach((StringDt wrapped) -> unwrappedStrings.add(wrapped.getValue()));
		return String.join(delimiter, unwrappedStrings);
	}
}
