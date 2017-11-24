package uk.nhs.fhir.datalayer;

public class SharedDataSource {
	
	private static FilesystemIF sharedDataSource = null;
	
	public static void set(FilesystemIF newSharedDataSource) {
		sharedDataSource = newSharedDataSource;
	}
	
	public static FilesystemIF get() {
		if (sharedDataSource == null) {
			initDefault();
		}
		return sharedDataSource;
	}

	private synchronized static void initDefault() {
		if (sharedDataSource == null) {
			sharedDataSource = new FilesystemIF();
		}
	}
}
