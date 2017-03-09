package uk.nhs.fhir.makehtml.old;

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.LinkData;

public class FormattedTableHTML {
	
	private List<TableRow> rows = Lists.newArrayList();
	
	public Element generateHTML() throws ParserConfigurationException {
		
		return Elements.withAttributeAndChild("div", 
			fontStyleAttribute(), 
			createTable());
	}
	
	private Element createTable() {
		return Elements.withAttributeAndChildren("table",
				fontStyleAttribute(),
				createTableRowsHTML());
	}
	
	private List<Element> createTableRowsHTML() {
		List<Element> rowElements = Lists.newArrayList();
		for (TableRow row : rows) {
			rowElements.add(row.buildHTML());
		}
		return rowElements;
	}

	private Attribute fontStyleAttribute() {
		return new Attribute("style", "font-family: sans-serif;");
	}
	
	private interface TableRow {
		Element buildHTML();
	}

	public void titles(String... titles) {
		rows.add(new TitlesRow(titles));
	}
	
	class TitlesRow implements TableRow {
		List<String> headings = Lists.newArrayList();
		
		public TitlesRow(String... titles) {
			Preconditions.checkNotNull(titles);
			Preconditions.checkArgument(titles.length > 0, "Requires at least one title");
			
			headings.addAll(Arrays.asList(titles));
		}
		
		public Element buildHTML() {
			List<Element> headingElements = Lists.newArrayList();
			for (String heading : headings) {
				headingElements.add(Elements.withText("th", heading));
			}
			
			return Elements.withChildren("tr", headingElements);
		}
	}

	public void borderedRow() {
		rows.add(new HorizontalLineRow());
	}
	
	class HorizontalLineRow implements TableRow {

		@Override
		public Element buildHTML() {
			return Elements.withChild("tr",
				Elements.withAttributes("td",
					Lists.newArrayList(
						new Attribute("colspan", "2"),
						new Attribute("style", "border-bottom: 1px solid #ddd;"))));
		}
		
	}
	
	public void sectionStart(String sectionTitle) {
		borderedRow();
		rows.add(new SectionTitleRow(sectionTitle));
	}
	
	class SectionTitleRow implements TableRow {

		private final String sectionTitle;
		
		SectionTitleRow(String sectionTitle) {
			this.sectionTitle = sectionTitle;
		}

		@Override
		public Element buildHTML() {
			return Elements.withChild("tr",
				Elements.withAttributeAndChild("td",
					new Attribute("colspan", "2"),
					Elements.withText("b", sectionTitle)));
		}
	}
	
	public void dataIfNotNull(Object value, String title, String hover) {
		if (value != null) {
			dataRow(value.toString(), title, hover);
		}
	}
	
	abstract class DataRow<T extends Content> implements TableRow {
		
		abstract List<T> getValueElement();

    	private final String title;
    	private final String hover;

    	public DataRow(String title, String hover) {
    		this.title = title;
    		this.hover = hover;
    	}
    	
		@Override
		public Element buildHTML() {
			return Elements.withChildren("tr", 
        		Lists.newArrayList(
        			Elements.withAttributeAndChild("td",
    						new Attribute("valign", "top"),
    						Elements.withAttributeAndText("span", 
    							new Attribute("title", hover), 
    							title)),
	        		Elements.withChildren("td", getValueElement())
      		));
		}
	}
	
    public void dataRow(String value, String title, String hover) {
        if (value != null) {
        	rows.add(new StringDataRow(value, title, hover));
        }
    }
    
    class StringDataRow extends DataRow<Text> {

    	private final String value;
    	
    	public StringDataRow(String value, String title, String hover) {
    		super(title, hover);
    		this.value = value;
    	}

		@Override
		List<Text> getValueElement() {
			return Lists.newArrayList(new Text(value));
		}
    	
    }
    
    class LinksListDataRow extends DataRow<Content> {

    	private final List<LinkData> links;
    	
		public LinksListDataRow(List<LinkData> links, String title, String hover) {
			super(title, hover);
			this.links = links;
		}

		@Override
		List<Content> getValueElement() {
			List<Content> children = Lists.newArrayList();
			for (int i=0; i<links.size(); i++) {
				LinkData link = links.get(i);
				children.add(
					Elements.withAttributesAndText("a", 
						Lists.newArrayList(new Attribute("href", link.getURL())), 
						link.getText()));
				
				if (i < links.size() - 1) {
					children.add(new Element("br"));
				}
			}
			
			return children;
		}
    }

	public void dataRow(List<LinkData> links, String title, String hover) {
		rows.add(new LinksListDataRow(links, title, hover));
	}
}
