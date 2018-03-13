package uk.nhs.fhir.datalayer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;

public class VersionedFilePreprocessorTest {

	private static final Path testResourcesDir = Paths.get(".", "src", "test", "resources");
	private static final Path incomingDir = testResourcesDir.resolve("TestIncoming");
	private static final Path outgoingDir = testResourcesDir.resolve("TestOutgoing");
	private static final Path outputDir = outgoingDir.resolve("outfile-versioned-1");

	private static final File incomingFile = incomingDir.resolve("CareConnect-GPC-MedicationOrder-1.xml").toFile();
	private static final File outgoingFile = outgoingDir.resolve("outfile-versioned-1.xml").toFile();
	private static final File outputDirFile = outputDir.toFile();
	private static final File expectedFile1 = outputDir.resolve("bindings.html").toFile();
	private static final File expectedFile2 = outputDir.resolve("details.html").toFile();
	
	@Test
	public void testCopyOtherResources() throws IOException {
		new VersionedFilePreprocessor(new AbstractFhirFileLocator() {
			@Override
			public Path getSourceRoot(FhirVersion fhirVersion) {
				throw new NotImplementedException();
			}
			
			@Override
			public Path getDestinationPathForResourceType(ResourceType type, FhirVersion version) {
				throw new NotImplementedException();
			}
		}).copyOtherResources(incomingFile.toPath(), outgoingFile.toPath());
		
		// Check the supporting resources have been copied properly
		assertTrue(expectedFile1.exists());
		assertTrue(expectedFile2.exists());
		
		// Clean up
		FileUtils.deleteDirectory(outputDirFile);
	}
}
