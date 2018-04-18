package uk.nhs.fhir.datalayer;

import java.util.List;

import com.google.common.collect.Lists;

public class DataLoaderMessages {

	private static List<String> profileLoadMessages = Lists.newArrayList(); 
	
	public static void addMessage(String msg) {
		profileLoadMessages.add(msg);
	}
	
	public static void clearProfileLoadMessages() {
		profileLoadMessages.clear();
	}
	
	public static String getProfileLoadMessages() {
		StringBuilder messages = new StringBuilder("Messages from profile loader:\n\n");
		
		for (String message : profileLoadMessages) {
			messages.append(message).append("\n");
		}
		
		return messages.toString();
	}
}
