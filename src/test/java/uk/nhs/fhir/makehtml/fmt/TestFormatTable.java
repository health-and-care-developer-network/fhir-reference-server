package uk.nhs.fhir.makehtml.fmt;

import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import junit.framework.Assert;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.LinkData;
import uk.nhs.fhir.util.TableTitle;

public class TestFormatTable {
	@Test
	public void testWriteSimpleCell() throws IOException {
		Element simpleCell = new SimpleTextCell("test_text").makeCell();
		String simpleCellHTML = HTMLUtil.docToString(new Document(simpleCell), false, false);
		String expected = "<td xmlns=\"http://www.w3.org/1999/xhtml\">test_text</td>";
		Assert.assertEquals(expected, simpleCellHTML);
	}
	
	@Test
	public void testWriteLinkCell() throws IOException {
		LinkData data = new LinkData("http://testURL", "test_link_text");
		Element linkCell = new LinkCell(data).makeCell();
		String simpleLinkCellHTML = HTMLUtil.docToString(new Document(linkCell), false, false);
		String expected = "<td xmlns=\"http://www.w3.org/1999/xhtml\">" 
							+ "<a href=\"http://testURL\">" 
								+ "test_link_text" 
							+ "</a>" 
						+ "</td>";
		Assert.assertEquals(expected, simpleLinkCellHTML);
	}
	
	@Test
	public void testWriteFormattedLinkCell() throws IOException {
		LinkData data = new LinkData("http://testURL", "test_link_text");
		Element linkCell = new LinkCell(data, Lists.newArrayList("cell-class-1", "cell-class-2"), Lists.newArrayList("link-class-1 link-class-2")).makeCell();
		String formattedLinkCellHTML = HTMLUtil.docToString(new Document(linkCell), false, false);
		String expected = "<td xmlns=\"http://www.w3.org/1999/xhtml\" class=\"cell-class-1 cell-class-2\">" 
							+ "<a class=\"fhir-link\" href=\"http://testURL\" class=\"link-class-1 link-class-2\">" 
								+ "test_link_text" 
							+ "</a>" 
						+ "</td>";
		Assert.assertEquals(expected, formattedLinkCellHTML);
	}
	
	@Test
	public void testTableRow() throws IOException {
		TableRow row = new TableRow(Lists.newArrayList(new SimpleTextCell("first cell"), new SimpleTextCell("second cell")));
		Element rowElement = row.makeRow();
		String rowHTML = HTMLUtil.docToString(new Document(rowElement), false, false);
		String expected = "<tr xmlns=\"http://www.w3.org/1999/xhtml\">"
							+ "<td>first cell</td>"
							+ "<td>second cell</td>"
						+ "</tr>";
		Assert.assertEquals(expected, rowHTML);
	}
	
	@Test
	public void testSimpleTable() throws IOException {
		List<TableTitle> columns = Lists.newArrayList(
			new TableTitle("ColumnName", "hoverInfo", "50px"), 
			new TableTitle("ColumnName2", "hoverInfo2", "60px"));
		Table table = new Table(columns, Sets.newHashSet("class1"));
		table.addRow(new TableRow(new SimpleTextCell("first cell"), new SimpleTextCell("second cell")));
		Element tableElement = table.makeTable();
		String tableHTML = HTMLUtil.docToString(new Document(tableElement), false, false);
		String expected = "<table xmlns=\"http://www.w3.org/1999/xhtml\">"
							+ "<thead>"
								+ "<tr>"
									+ "<th title=\"hoverInfo\" style=\"width: 50px\">"
										+ "ColumnName"
									+ "</th>"
									+ "<th title=\"hoverInfo2\" style=\"width: 60px\">"
										+ "ColumnName2"
									+ "</th>"
								+ "</tr>"
							+ "</thead>"
							+ "<tbody>"
								+ "<tr>"
									+ "<td>first cell</td>"
									+ "<td>second cell</td>"
								+ "</tr>"
							+ "</tbody>"
						+ "</table>";
		Assert.assertEquals(expected, tableHTML);
	}
	
	/**
	 * This test output can be used to show a table in a browser
	 */
	@Test
	public void testComplexTable() throws IOException {
		List<TableTitle> columns = Lists.newArrayList(
			new TableTitle("Use", "info1", "50px"),
			new TableTitle("Link", "info3", "70px"));
		Table table = new Table(columns, Sets.newHashSet("class1"));
		table.addRow(new TableRow(new SimpleTextCell("Search engine"), new LinkCell(new LinkData("https://www.google.com", "Google"))));
		table.addRow(new TableRow(new SimpleTextCell("News"), new LinkCell(new LinkData("http://news.bbc.co.uk", "BBC News"))));
		table.addRow(new TableRow(new SimpleTextCell("Encyclopedia"), new LinkCell(new LinkData("https://www.wikipedia.org", "Wikipedia"))));
		Element tableElement = table.makeTable();
		String tableHTML = HTMLUtil.docToString(new Document(tableElement), false, false);
		String expected = "<table xmlns=\"http://www.w3.org/1999/xhtml\">"
							+ "<thead>"
								+ "<tr>"
									+ "<th title=\"info1\" style=\"width: 50px\">Use</th>"
									+ "<th title=\"info3\" style=\"width: 70px\">Link</th>"
								+ "</tr>"
							+ "</thead>"
							+ "<tbody>"
								+ "<tr><td>Search engine</td><td><a class=\"fhir-link\" href=\"https://www.google.com\">Google</a></td></tr>"
								+ "<tr><td>News</td><td><a class=\"fhir-link\" href=\"http://news.bbc.co.uk\">BBC News</a></td></tr>"
								+ "<tr><td>Encyclopedia</td><td><a class=\"fhir-link\" href=\"https://www.wikipedia.org\">Wikipedia</a></td></tr>"
							+ "</tbody>"
						+ "</table>";
		Assert.assertEquals(expected, tableHTML);
	}
	
}
