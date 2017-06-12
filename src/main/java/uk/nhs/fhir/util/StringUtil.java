package uk.nhs.fhir.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

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
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
	
	public static String dateToString(Date date) {
		return dateFormat.format(date);
	}

	public static void printIfPresent(String desc, Optional<String> s) {
		if (s.isPresent()) {
			System.out.println(desc + ": " + s.get());
		}
	}
	
	public static String hyphenatedToPascalCase(String hyphenated) {
		String[] split = hyphenated.split("-");
		StringBuilder pascalCase = new StringBuilder();
		for (String tok : split) {
			pascalCase.append(capitaliseFirst(tok));
		}
		
		return pascalCase.toString();
	}

	private static String capitaliseFirst(String tok) {
		if (tok.isEmpty()) {
			return tok;
		} else {
			return Character.toUpperCase(tok.charAt(0)) + tok.substring(1);
		}
	}
}
