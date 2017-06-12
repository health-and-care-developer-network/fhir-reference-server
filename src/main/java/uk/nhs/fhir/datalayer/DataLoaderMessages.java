package uk.nhs.fhir.datalayer;

import java.util.ArrayList;

public class DataLoaderMessages {

	private static ArrayList<String> profileLoadMessages = new ArrayList<String>(); 
	
	public static void addMessage(String msg) {
		profileLoadMessages.add(msg);
	}
	
	public static void clearProfileLoadMessages() {
		profileLoadMessages.clear();
	}
	
	public static String getProfileLoadMessages() {
		String messages = "Messages from profile loader:\n\n";
		for (String message : profileLoadMessages) {
			messages = messages + message + "\n";
		}
		return messages;
	}
}
