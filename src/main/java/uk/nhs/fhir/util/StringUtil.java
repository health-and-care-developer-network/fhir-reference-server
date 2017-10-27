package uk.nhs.fhir.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

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
	
	/*
	 *  Not necessary while it's a single-threaded application, but avoids difficult-to-debug errors later
	 *  if that ever changes.
	 */
	private static final ThreadLocal<? extends DateFormat> dateFormat = 
		ThreadLocal.withInitial(
			new Supplier<SimpleDateFormat>(){
				@Override public SimpleDateFormat get() { 
					return new SimpleDateFormat("dd/MM/yy");
				}
			}
		);
	
	public static String dateRange(Date dateStart, Date dateEnd) {
		return dateToString(dateStart) + " - " + dateToString(dateEnd);
	}
	
	public static String dateToString(Date date) {
		return dateFormat.get().format(date);
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

	@SafeVarargs
	public static <T> Optional<T> firstPresent(Optional<T>... options) {
		for (Optional<T> option : options) {
			if (option.isPresent()) {
				return option;
			}
		}
		
		return Optional.empty();
	}
	
	public static String nChars(int n, char c) {
		char[] charArray = new char[n];
		Arrays.fill(charArray, c);
		return new String(charArray);
	}

	public static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		return stacktrace;
	}

	public static String commonStringStart(String commonFilepathStart, String filePath) {
		char[] chars1 = commonFilepathStart.toCharArray();
		char[] chars2 = filePath.toCharArray();
		
		int matchingCharsIndex = 0;
		while (chars1.length > matchingCharsIndex
		  && chars2.length > matchingCharsIndex
		  && chars1[matchingCharsIndex] == chars2[matchingCharsIndex]) {
			matchingCharsIndex++;
		}
		
		char[] resultChars = Arrays.copyOf(chars1, matchingCharsIndex);
		String result = new String(resultChars);
		
		return result;
	}

	public static String getTrimmedFileName(File xmlFile) {
		String fullFileName = xmlFile.getName();
		
		String trimmedFileName = removeExtension(fullFileName);
		
		return trimmedFileName;
	}
	
	public static String getLastPartOfUrlWithoutExtension(String url) {
		String[] split = url.split("/");
		String lastPart = split[split.length - 1];
		String trimmedLastPart = removeExtension(lastPart);
		return trimmedLastPart;
	}
	
	// trims everything after first '.'
	private static String removeExtension(String s) {
		if (s.contains(".")) {
			return s.substring(0, s.indexOf('.'));
		} else {
			return s;
		}
	}
}
