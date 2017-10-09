package uk.nhs.fhir.datalayer;

public class SharedDataSource {
	
	private static FilesystemIF sharedDataSource;
	static {
		sharedDataSource = new FilesystemIF();
	}
	
	public static void set(FilesystemIF newSharedDataSource) {
		sharedDataSource = newSharedDataSource;
	}
	
	public static FilesystemIF get() {
		return sharedDataSource;
	}
}
