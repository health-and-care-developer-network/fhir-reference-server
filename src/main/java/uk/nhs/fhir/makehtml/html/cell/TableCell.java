package uk.nhs.fhir.makehtml.html.cell;

import org.jdom2.Element;

public abstract class TableCell {
	
	public abstract Element makeCell();
	
	private final boolean faded;
	private final boolean strikethrough;
	
	public TableCell() {
		this(false, false);
	}
	
	public TableCell(boolean faded, boolean strikethrough) {
		this.faded = faded;
		this.strikethrough = strikethrough;
	}
	
	protected boolean getFaded() {
		return faded;
	}
	protected boolean getStrikethrough() {
		return strikethrough;
	}
}
