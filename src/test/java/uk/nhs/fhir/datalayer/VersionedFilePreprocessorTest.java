package uk.nhs.fhir.datalayer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class VersionedFilePreprocessorTest {

	private static final File incomingFile = new File("./src/test/resources/TestIncoming/CareConnect-GPC-MedicationOrder-1.xml");
	private static final File outgoingFile = new File("./src/test/resources/TestOutgoing/outfile-versioned-1.xml");

	private static final File expectedFile1 = new File("./src/test/resources/TestOutgoing/outfile-versioned-1/bindings.html");
	private static final File expectedFile2 = new File("./src/test/resources/TestOutgoing/outfile-versioned-1/details.html");
	
	private static final File cleanUpDir = new File("./src/test/resources/TestOutgoing/outfile-versioned-1");
	
	@Test
	public void testCopyOtherResources() throws IOException {
		VersionedFilePreprocessor.copyOtherResources(incomingFile, outgoingFile);
		
		// Check the supporting resources have been copied properly
		assertTrue(expectedFile1.exists());
		assertTrue(expectedFile2.exists());
		
		// Clean up
		FileUtils.deleteDirectory(cleanUpDir);
	}
}
