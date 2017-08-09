package uk.nhs.fhir.makehtml.html.tree;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;

/**
 * Icons used to display the type of an element in a FHIR tree
 * @author jon
 */
public enum FhirTreeIcon {
	BLANK("tbl_blank.png", FhirTreeIcon.blankBase64, FhirTreeIcon.BLANK_CLASS),
	SPACER("tbl_spacer.png", FhirTreeIcon.spacerBase64, FhirTreeIcon.SPACER_CLASS),
	VJOIN("tbl_vjoin.png", FhirTreeIcon.vjoinBase64, FhirTreeIcon.VJOIN_CLASS),
	VJOIN_END("tbl_vjoin_end.png", FhirTreeIcon.vjoinEndBase64, FhirTreeIcon.VJOIN_END_CLASS),
	VLINE("tbl_vline.png", FhirTreeIcon.vlineBase64, FhirTreeIcon.VLINE_CLASS);
	
	private final String filename;
	private final String base64;
	private final String cssClass;
	
	FhirTreeIcon(String filename, String base64, String cssClass) {
		this.filename = filename;
		this.base64 = base64;
		this.cssClass = cssClass;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public String getBase64() {
		return base64;
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public static List<CSSStyleBlock> getCssRules() {
		List<CSSStyleBlock> rules = Lists.newArrayList();
		/*for (FhirTreeIcon icon : FhirTreeIcon.values()) {
			String filename = icon.getFileName();
			String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
			
			//Chrome
			rules.add(
				new CSSStyleBlock(
					Lists.newArrayList("img." + icon.getCssClass()), 
					Lists.newArrayList(
						new CSSRule("background", "url('data:image/" + fileExtension + ";base64," + icon.getBase64() + "')"),
						new CSSRule("background-repeat", "no-repeat"),
						//new CSSRule("background-color", "white"),
						new CSSRule("border", "0"),
						new CSSRule("width", "16"),
						new CSSRule("height", "22"))));
			
			//Firefox/IE
			/*rules.add(
				new CSSStyleBlock(
					Lists.newArrayList("img." + icon.getCssClass() + "::after"), 
					Lists.newArrayList(
						new CSSRule("content", "url(data:image/" + fileExtension + ";base64," + icon.getBase64() + ")"),
						new CSSRule("background-color", "white"),
						new CSSRule("border", "0"),
						new CSSRule("width", "16"),
						new CSSRule("height", "22"))));
		}*/
		
		rules.add(
			new CSSStyleBlock(
				Lists.newArrayList("img." + FhirCSS.TREE_ICON), 
				Lists.newArrayList(
					new CSSRule("background-color", "white"),
					new CSSRule("border", "0"),
					new CSSRule("width", "16"),
					new CSSRule("height", "22"))));
		
		return rules;
	}
	
	private static final String blankBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIZgEiYEgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAIElEQVQ4y2P8//8/AyWAiYFCMGrAqAGjBowaMGoAAgAALL0DKYQ0DPIAAAAASUVORK5CYII=";
	private static final String spacerBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAAWCAYAAAABxvaqAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzIs1vtcMQAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAE0lEQVQI12P4//8/AxMDAwNdCABMPwMo2ctnoQAAAABJRU5ErkJggg==";
	//Regenerated this one using a recommended tool which supposedly creates pngs that should render ok in Firefox. Trying to work out why these icons won't display in Firefox, thought it might be an encoding issue.
	private static final String vjoinBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzI3XJ6V3QAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+2RsQ0AIAzDav7/2VzQwoCY4iWbZSmo1QGoUgNMghvWaIejPQW/CrrNCylIwcOCDYfLNRcNer4SAAAAAElFTkSuQmCC";
	//private static final String vjoinBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzI3XJ6V3QAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+2RsQ0AIAzDav7/2VzQwoCY4iWbZSmo1QGoUgNMghvWaIejPQW/CrrNCylIwcOCDYfLNRcNer4SAAAAAElFTkSuQmCC";
	private static final String vjoinEndBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzME+lXFigAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAANklEQVQ4y+3OsRUAIAjEUOL+O8cJABttJM11/x1qZAGqRBEVcNIqdWj1efDqQbb3HwwwwEfABmQUHSPM9dtDAAAAAElFTkSuQmCC";
	private static final String vlineBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAWCAYAAADJqhx8AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wYeFzMPbYccAgAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAAAMElEQVQ4y+3OQREAIBDDwAv+PQcFFN5MIyCzqHMKUGVCpMFLK97heq+gggoq+EiwAVjvMhFGmlEUAAAAAElFTkSuQmCC";

	private static final String BLANK_CLASS = "blank-fhir-tree-icon";
	private static final String SPACER_CLASS = "spacer-fhir-tree-icon";
	private static final String VJOIN_CLASS = "vjoin-fhir-tree-icon";
	private static final String VJOIN_END_CLASS = "vjoin-end-fhir-tree-icon";
	private static final String VLINE_CLASS = "vline-fhir-tree-icon";

	public String getNhsSrc() {
		return FhirURLConstants.NHS_FHIR_IMAGES_DIR + "/" + getFileName();
	}
}
