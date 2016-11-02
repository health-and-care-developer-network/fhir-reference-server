package uk.nhs.fhir.datalayer;

import uk.nhs.fhir.util.PropertyReader;

public class DataSourceFactory {
	public static Datasource getDataSource() {
		String dataSourceType = PropertyReader.getProperty("dataSourceType");
		if (dataSourceType.equalsIgnoreCase("mongodb")) {
			return new MongoIF();
		} else {
			return new FilesystemIF();
		}
	}
}
