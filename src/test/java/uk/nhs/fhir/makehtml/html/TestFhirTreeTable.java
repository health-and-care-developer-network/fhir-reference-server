package uk.nhs.fhir.makehtml.html;

import java.io.IOException;

import org.jdom2.Document;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.data.FhirCardinality;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeNodeId;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.util.HTMLUtil;

public class TestFhirTreeTable {
	@Test
	public void testAsTable() throws IOException {
		FhirTreeNode node = new FhirTreeNode(
			new FhirTreeNodeId("test", null, FhirIcon.ELEMENT),
			new ResourceFlags(),
			new FhirCardinality("0", "1"),
			new LinkData("#", "testlink"),
			"root info",
			Lists.newArrayList(),
			"path.to.resource");
		FhirTreeData data = new FhirTreeData(node);
		Table table = new FhirTreeTable(data).asTable(false);
		
		String output = HTMLUtil.docToString(new Document(table.makeTable()), true, false);
		System.out.println(output);
	}
}