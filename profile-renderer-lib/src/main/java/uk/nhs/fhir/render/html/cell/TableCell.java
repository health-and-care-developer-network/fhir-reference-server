package uk.nhs.fhir.render.html.cell;

import java.util.Optional;
import java.util.Set;

import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import uk.nhs.fhir.render.html.style.FhirCSS;

public abstract class TableCell {

	public static final String ZERO_WIDTH_CHARACTER = "&#8203;";
	
	private static final TableCell EMPTY_PLAIN = new SimpleTextCell(ZERO_WIDTH_CHARACTER);
	public static TableCell empty() {
		return EMPTY_PLAIN;
	}
	
	private static final TableCell EMPTY_BORDERED = new SimpleTextCell(ZERO_WIDTH_CHARACTER, true);
	public static TableCell emptyBordered() {
		return EMPTY_BORDERED;
	}
			
	public abstract Element makeCell();
	
	protected final Set<String> cellClasses = Sets.newHashSet(FhirCSS.TREE_CELL);
	
	public void addClass(String cssClass) {
		Preconditions.checkArgument(!cssClass.contains(" "), "CSS classes may not contain spaces");
		cellClasses.add(cssClass);
	}
	
	protected Optional<Integer> colspan = Optional.empty();
	
	public void colspan(int span) {
		this.colspan = Optional.of(span);
	}
	
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
