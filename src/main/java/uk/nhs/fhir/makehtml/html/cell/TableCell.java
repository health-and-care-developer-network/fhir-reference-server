package uk.nhs.fhir.makehtml.html.cell;

import java.util.Set;

import org.jdom2.Element;

import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.html.style.FhirCSS;

public abstract class TableCell {
	
	public abstract Element makeCell();
	
	protected final Set<String> cellClasses = Sets.newHashSet(FhirCSS.TREE_CELL);
	
	public TableCell() {
		this(false, false, false);
	}
	
	public TableCell(boolean bordered, boolean faded, boolean strikethrough) {
		if (bordered) {
			cellClasses.add(FhirCSS.DATA_CELL);
		}
		if (faded) {
			cellClasses.add(FhirCSS.TEXT_FADED);
		}
		if (strikethrough) {
			cellClasses.add(FhirCSS.TEXT_STRIKETHROUGH);
		}
	}
}
