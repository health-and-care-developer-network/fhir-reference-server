package uk.nhs.fhir.makehtml.html;

import java.io.IOException;
import java.util.Optional;

import org.jdom2.Document;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.util.HTMLUtil;

public class TestFhirTreeTable {
	@Test
	public void testAsTable() throws IOException {
		FhirTreeNode node = new FhirTreeNode(
			FhirIcon.ELEMENT,
			Optional.of("test"),
			new ResourceFlags(),
			0,
			"1",
			Lists.newArrayList(new SimpleLinkData("#", "testlink")),
			"root info",
			Lists.newArrayList(),
			"path.to.resource");
		FhirTreeData data = new FhirTreeData(node);
		Table table = new FhirTreeTable(data).asTable(false, Optional.empty());
		
		String output = HTMLUtil.docToString(new Document(table.makeTable()), true, false);
		System.out.println(output);
	}
}