package uk.nhs.fhir.datalayer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class VersionedFilePreprocessorTest {

	private static final File incomingFile = new File("./src/test/resources/TestIncoming/CareConnect-GPC-MedicationOrder-1.xml");
	private static final File outgoingDir = new File("./src/test/resources/TestOutgoing/outfile-versioned-1.xml");

	private static final File expectedFile1 = new File("./src/test/resources/TestOutgoing/outfile-versioned-1/bindings.html");
	private static final File expectedFile2 = new File("./src/test/resources/TestOutgoing/outfile-versioned-1/details.html");
	
	@Test
	public void testCopyOtherResources() throws IOException {
		new VersionedFilePreprocessor(new PropertiesFhirFileLocator()).copyOtherResources(incomingFile.toPath(), outgoingDir.toPath());
		
		// Check the supporting resources have been copied properly
		assertTrue(expectedFile1.exists());
		assertTrue(expectedFile2.exists());
		
		// Clean up
		FileUtils.deleteDirectory(outgoingDir);
	}
}
