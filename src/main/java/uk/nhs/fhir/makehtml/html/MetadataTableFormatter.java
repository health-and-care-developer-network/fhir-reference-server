package uk.nhs.fhir.makehtml.html;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.util.StringUtil;

public abstract class MetadataTableFormatter extends ResourceFormatter {
	
	protected static final String VERSION_DATE = "Version date";
	
	protected static final String BLANK = "";
	
	protected Element labelledValueCell(String label, Optional<String> value, int colspan) {
		String displayValue = value.isPresent() ? value.get() : BLANK;
		return labelledValueCell(label, displayValue, colspan);
	}
	
	protected Element labelledValueCell(String label, String value, int colspan) {
		return labelledValueCell(label, value, colspan, false);
	}
		
	protected Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig) {
		Preconditions.checkNotNull(value, "value data");
		
		List<Element> cellSpans = Lists.newArrayList();
		if (label.length() > 0) {
			cellSpans.add(labelSpan(label, value.isEmpty()));
		}
		if (value.length() > 0) {
			cellSpans.add(valueSpan(value, alwaysBig));
		}
		
		return cell(cellSpans, colspan);
	}
	
	protected Element cell(List<? extends Content> content, int colspan) {
		return Elements.withAttributesAndChildren("td", 
			Lists.newArrayList(
				new Attribute("class", FhirCSS.METADATA_CELL),
				new Attribute("colspan", Integer.toString(colspan))),
			content);
	}
	
	protected Element labelSpan(String label, boolean valueIsEmpty) {
		String cssClass = FhirCSS.METADATA_LABEL;
		if (valueIsEmpty) {
			cssClass += " " + FhirCSS.METADATA_LABEL_EMPTY;
		}
		
		if (label.length() > 0) {
			label += ": ";
		} else {
			// if the content is entirely empty, the title span somehow swallows the value span
			// so use a zero-width space character.
			label = "&#8203;";
		}
		
		return Elements.withAttributeAndText("span", 
			new Attribute("class", cssClass), 
			label);
	}
	
	protected Optional<String> getVersionId(BaseResource source) {
		Optional<IBaseMetaType> metaInfo = getMeta(source);
		if (metaInfo.isPresent()) {
			return Optional.ofNullable(metaInfo.get().getVersionId());
		} else {
			return Optional.empty();
		}
	}
	
	protected Optional<String> getLastUpdated(BaseResource source) {
		Optional<IBaseMetaType> metaInfo = getMeta(source);
		if (metaInfo.isPresent()) {
			Date lastUpdated = metaInfo.get().getLastUpdated();
			if (lastUpdated != null) {
				return Optional.of(StringUtil.dateToString(lastUpdated));
			}
		}
		
		return Optional.empty();
	}
	
	private Optional<IBaseMetaType> getMeta(BaseResource source) {
		IBaseMetaType metaInfo = source.getMeta();
		if (!metaInfo.isEmpty()) {
			return Optional.of(metaInfo);
		} else {
			return Optional.empty();
		}
	}
	
	protected Element valueSpan(String value, boolean alwaysLargeText) {
		boolean url = (value.startsWith("http://") || value.startsWith("https://"));
		boolean largeText = alwaysLargeText || value.length() < 25;
		String fhirMetadataClass = FhirCSS.METADATA_VALUE;
		if (!largeText) fhirMetadataClass += " " + FhirCSS.METADATA_VALUE_SMALLTEXT;
		
		if (url) {
			return Elements.withAttributeAndChild("span", 
				new Attribute("class", fhirMetadataClass), 
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("class", FhirCSS.LINK), 
						new Attribute("href", value)), 
					value));
			
		} else {
			return Elements.withAttributeAndText("span", 
				new Attribute("class", fhirMetadataClass), 
				value);
		}
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.METADATA_CELL),
				Lists.newArrayList(
					new CSSRule("border", "1px solid #f0f0f0"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_LABEL, "." + FhirCSS.TELECOM_NAME),
					Lists.newArrayList(
						new CSSRule("color", "#808080"),
						new CSSRule("font-weight", "bold"),
						new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_LABEL_EMPTY),
					Lists.newArrayList(
						new CSSRule("color", "#D0D0D0"),
						new CSSRule("font-weight", "normal"))));
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.METADATA_VALUE, "." + FhirCSS.TELECOM_VALUE),
				Lists.newArrayList(
					new CSSRule("color", "#000000"),
					new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_VALUE_SMALLTEXT),
					Lists.newArrayList(
						new CSSRule("font-size", "10"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_BLOCK_TITLE),
					Lists.newArrayList(
							new CSSRule("color", "#808080"),
							new CSSRule("font-weight", "bold"),
							new CSSRule("text-decoration", "underline"),
							new CSSRule("font-size", "13"))));
		
		return styles;
	}

}
