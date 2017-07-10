package uk.nhs.fhir.makehtml.html;

import java.io.IOException;
import java.util.Optional;

import org.jdom2.Document;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;

public class TestFhirTreeTable {
	@Test
	public void testAsTable() throws IOException {
		FhirTreeNode node = new FhirTreeNode(
			FhirIcon.ELEMENT,
			Optional.of("test"),
			new ResourceFlags(),
			0,
			"1",
			Lists.newArrayList(new SimpleLinkData(FhirURL.buildOrThrow("http://www.example.com"), "testlink")),
			"root info",
			Lists.newArrayList(),
			"path.to.resource",
			FhirDataType.ELEMENT);
		FhirTreeData data = new FhirTreeData(node);
		
		FhirTreeTable fhirTreeTable = new FhirTreeTable(data);
		fhirTreeTable.stripRemovedElements();
		Table table = fhirTreeTable.asTable();
		
		String output = HTMLUtil.docToString(new Document(table.makeTable()), true, false);
		System.out.println(output);
	}
}