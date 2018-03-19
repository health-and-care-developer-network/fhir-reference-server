package uk.nhs.fhir.makehtml.html;

import java.io.IOException;
import java.util.Optional;

import org.jdom2.Document;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.structdef.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.RemovedElementStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.UnwantedConstraintRemover;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.html.HTMLUtil;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.FhirTreeTable;
import uk.nhs.fhir.util.FhirVersion;

public class TestFhirTreeTable {
	@Test
	public void testAsTable() throws IOException {
		SnapshotData node = new SnapshotData(
			Optional.empty(),
			Optional.of("test"),
			new ResourceFlags(),
			0,
			"1",
			new LinkDatas(new LinkData(FhirURL.buildOrThrow("http://www.example.com", FhirVersion.DSTU2), "testlink")),
			"root info",
			Lists.newArrayList(),
			new ImmutableNodePath("path.to.resource"),
			FhirElementDataType.ELEMENT,
			FhirVersion.DSTU2);
		
		FhirTreeData<SnapshotData, SnapshotTreeNode> data = new FhirTreeData<>(new SnapshotTreeNode(node));
		
		FhirTreeTable<SnapshotData, SnapshotTreeNode> fhirTreeTable = new FhirTreeTable<>(data, FhirVersion.DSTU2);

		new RemovedElementStripper<>(data).process();
		new ExtensionsSlicingNodesRemover<>(data).process();
		new UnwantedConstraintRemover<>(data).process();
		new ComplexExtensionChildrenStripper<>(data).process();
		
		Table table = fhirTreeTable.asTable();
		
		String output = HTMLUtil.docToString(new Document(table.makeTable()), true, false);
		System.out.println(output);
	}
}