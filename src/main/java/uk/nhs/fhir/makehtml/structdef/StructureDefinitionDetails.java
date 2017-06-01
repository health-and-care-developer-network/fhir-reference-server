package uk.nhs.fhir.makehtml.structdef;

import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.data.SlicingInfo;
import uk.nhs.fhir.makehtml.html.CSSRule;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class StructureDefinitionDetails {
	
	private final String key;
	private final Optional<String> definition;
	private final String cardinality;
	private final Optional<BindingInfo> binding;
	private final List<LinkData> typeLinks;
	private final Optional<String> requirements;
	private final List<String> aliases;
	private final ResourceFlags resourceFlags;
	private final Optional<String> comments;
	private final Optional<SlicingInfo> slicing;
	private final List<ConstraintInfo> inheritedConstraints;
	private final List<ConstraintInfo> profileConstraints;
	
	public StructureDefinitionDetails(String key, Optional<String> definition, String cardinality, Optional<BindingInfo> binding, 
			List<LinkData> typeLinks, Optional<String> requirements, List<String> aliases, ResourceFlags resourceFlags,
			Optional<String> comments, Optional<SlicingInfo> slicing, List<ConstraintInfo> inheritedConstraints, 
			List<ConstraintInfo> profileConstraints) {
		this.key = key;
		this.definition = definition;
		this.cardinality = cardinality;
		this.binding = binding;
		this.typeLinks = typeLinks;
		this.requirements = requirements;
		this.aliases = aliases;
		this.resourceFlags = resourceFlags;
		this.comments = comments;
		this.slicing = slicing;
		this.inheritedConstraints = inheritedConstraints;
		this.profileConstraints = profileConstraints;
	}

	public void addContent(List<Element> tableContent) {
		tableContent.add(getHeaderRow(key));
		
		addDataIfPresent(tableContent, "Definition", definition);
		addData(tableContent, "Cardinality", cardinality);
		addBindingRowIfPresent(tableContent, binding);
		tableContent.add(getLinkRow("Type", typeLinks));
		addDataIfPresent(tableContent, "Requirements", requirements);
		addListDataIfPresent(tableContent, "Alternate Names", aliases);
		addResourceFlags(tableContent, resourceFlags);
		addDataIfPresent(tableContent, "Comments", comments);
		addConstraints(tableContent);
		addSlicing(tableContent);
	}

	private Element getHeaderRow(String header) {
		return Elements.withAttributeAndChild("tr", 
			new Attribute("class", "fhir-details-header-row"), 
			Elements.withAttributesAndChildren("td",
				Lists.newArrayList(
					new Attribute("class", "fhir-details-header-cell"), 
					new Attribute("colspan", "2")), 
				Lists.newArrayList(
					Elements.withAttribute("a", 
						new Attribute("name", header)),
					new Text(header))));
	}

	private void addDataIfPresent(List<Element> tableContent, String label, Optional<String> content) {
		if (content.isPresent()) {
			addData(tableContent, label, content.get());
		}
	}

	private void addData(List<Element> tableContent, String label, String content) {
		tableContent.add(simpleStringDataRow(label, content)); 
	}
	
	private Element simpleStringDataRow(String title, String content) {
		
		Element labelCell = dataCell(title, "fhir-details-data-cell");
		Element dataCell = dataCell(content, "fhir-details-data-cell");
		
		return getDataRow(labelCell, dataCell);
	}
	
	private Element dataCell(String content, String classString) {
		return Elements.withAttributeAndText("td",
			new Attribute("class", classString),
			content);
	}
	
	private Element getDataRow(Element labelCell, Element dataCell) {
		return Elements.withAttributeAndChildren("tr", 
			new Attribute("class", "fhir-details-data-row"),
				Lists.newArrayList(
					labelCell,
					dataCell));
	}

	private void addBindingRowIfPresent(List<Element> tableContent, Optional<BindingInfo> binding) {
		if (binding.isPresent()) {
			BindingInfo info = binding.get();

			String bindingInfo = "";
			
			boolean hasUrl = info.getUrl().isPresent();
			boolean hasDesc = info.getDescription().isPresent();
			
			if (hasUrl) {
				String fullUrl = info.getUrl().get().toString();
				String hyphenatedUrlName = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);
				String urlName = StringUtil.hyphenatedToPascalCase(hyphenatedUrlName);
				bindingInfo += urlName;
			}
			
			if (hasUrl && hasDesc) {
				bindingInfo += ": ";
			}
			
			if (hasDesc) {
				bindingInfo += info.getDescription().get();
			}
			
			bindingInfo += " (" + info.getStrength() + ")";
			
			addData(tableContent, "Binding", bindingInfo);
		}
	}
	
	private Element getLinkRow(String title, List<LinkData> linkDatas) {
		return Elements.withAttributeAndChildren("tr", 
			new Attribute("class", "fhir-details-data-row"),
				Lists.newArrayList(
					dataCell(title, "fhir-details-data-cell"),
					linkCell(linkDatas)));
	}
	
	private Element linkCell(List<LinkData> linkDatas) {
		return new LinkCell(linkDatas, Sets.newHashSet("fhir-details-data-cell"), Sets.newHashSet(), false, false).makeCell();
	}

	private void addListDataIfPresent(List<Element> tableContent, String label, List<String> listData) {
		if (!aliases.isEmpty()) {
			addData(tableContent, label, String.join("; ", listData));
		}
	}

	private void addResourceFlags(List<Element> tableContent, ResourceFlags resourceFlags) {
		addDataIfTrue(tableContent, "Summary", resourceFlags.isSummary());
		addDataIfTrue(tableContent, "Modifier", resourceFlags.isModifier());
		//addDataIfTrue(tableContent, "Is Constrained", resourceFlags.isConstrained());
		addDataIfTrue(tableContent, "Must-Support", resourceFlags.isMustSupport());
	}

	private void addDataIfTrue(List<Element> tableContent, String label, boolean condition) {
		if (condition) {
			addData(tableContent, label, "True");
		}
	}
	
	private void addConstraints(List<Element> tableContent) {
		if (!profileConstraints.isEmpty() || !inheritedConstraints.isEmpty()) {
			
			List<Content> constraintInfos = Lists.newArrayList();
			addConstraintInfos(constraintInfos, profileConstraints, "Defined on this element");
			addConstraintInfos(constraintInfos, inheritedConstraints, "Affect this element");
			
			tableContent.add(
				getDataRow(
					dataCell("Invariants", "fhir-details-data-cell"), 
					Elements.withAttributeAndChildren("td", 
						new Attribute("class", "fhir-details-data-cell"), 
						constraintInfos)));
		}
	}
	
	private void addConstraintInfos(List<Content> constraintInfos, List<ConstraintInfo> profileConstraints, String label) {
		if (!profileConstraints.isEmpty()) {
			if (!constraintInfos.isEmpty()) {
				constraintInfos.add(Elements.newElement("br"));
			}
			constraintInfos.add(Elements.withText("b", label));
			
			for (ConstraintInfo constraint : profileConstraints) {
				constraintInfos.add(Elements.newElement("br"));
				
				constraintInfos.add(Elements.withText("b", constraint.getKey() + ": "));
				String constraintContent = constraint.getDescription();
				if (constraint.getRequirements().isPresent()) {
					constraintContent += ". " + constraint.getRequirements().get();
				}
				constraintContent += " (xpath: " + constraint.getXPath() + ")";
				constraintContent += " severity: " + constraint.getSeverity();
				constraintInfos.add(new Text(constraintContent));
			}
		}
	}

	private void addSlicing(List<Element> tableContent) {
		if (slicing.isPresent()) {
			SlicingInfo slicingInfo = slicing.get();
			
			List<Content> renderedSlicingInfo = Lists.newArrayList();
			String description = slicingInfo.getDescription();
			if (!Strings.isNullOrEmpty(description)) {
				addSlicingInfo(renderedSlicingInfo, slicingInfo.getDescription());
			}
			addSlicingInfo(renderedSlicingInfo, slicingInfo.getOrderedDesc());
			addSlicingInfo(renderedSlicingInfo, slicingInfo.getRules());
			addSlicingInfo(renderedSlicingInfo, "discriminators: " + String.join(", ", slicingInfo.getDiscriminatorPaths()));
			
			tableContent.add(
				getDataRow(
					dataCell("Slicing", "fhir-details-data-cell"),
					Elements.withAttributeAndChildren("td", 
						new Attribute("class", "fhir-details-data-cell"), 
						Lists.newArrayList(
							new Text("This element introduces a set of slices. The slicing rules are:"),
							Elements.withAttributeAndChildren("ul", 
								new Attribute("class", "fhir-list"), 
								renderedSlicingInfo)))));
		}
	}

	private void addSlicingInfo(List<Content> renderedSlicingInfo, String data) {
		if (!Strings.isNullOrEmpty(data)) {
			renderedSlicingInfo.add(Elements.withText("li", data));
		}
	}

	public Optional<String> getDefinition() {
		return definition;
	}

	public String getCardinality() {
		return cardinality;
	}

	public Optional<BindingInfo> getBindingInfo() {
		return binding;
	}

	public List<LinkData> getTypeLinks() {
		return typeLinks;
	}

	public Optional<String> getRequirements() {
		return requirements;
	}

	public  List<String> getAliases() {
		return aliases;
	}

	public ResourceFlags getResourceFlags() {
		return resourceFlags;
	}

	public Optional<String> getComments() {
		return comments;
	}

	public List<ConstraintInfo> getInheritedConstraints() {
		return inheritedConstraints;
	}

	public List<ConstraintInfo> getProfileConstraints() {
		return profileConstraints;
	}

	public void assertEqualTo(StructureDefinitionDetails detail) {

		if (!getDefinition().equals(detail.getDefinition())) {
			throw new IllegalStateException("Same key, different definition (" + key + ").");
		}
		if (!getCardinality().equals(detail.getCardinality())) {
			throw new IllegalStateException("Same key, different cardinality (" + key + ").");
		}
		if (!getBindingInfo().equals(detail.getBindingInfo())) {
			throw new IllegalStateException("Same key, different binding info (" + key + ").");
		}
		if (!getTypeLinks().stream().allMatch(link -> detail.getTypeLinks().contains(link))) {
			throw new IllegalStateException("Same key, different types info (" + key + ").");
		}
		if (!getRequirements().equals(detail.getRequirements())) {
			throw new IllegalStateException("Same key, different requirements info (" + key + ").");
		}
		if (!getAliases().stream().allMatch(alias -> detail.getAliases().contains(alias))) {
			throw new IllegalStateException("Same key, different alias info (" + key + ").");
		}
		if (!getResourceFlags().equals(detail.getResourceFlags())) {
			throw new IllegalStateException("Same key, different resource flags info (" + key + ").");
		}
		if (!getComments().equals(detail.getComments())) {
			throw new IllegalStateException("Same key, different comments info (" + key + ").");
		}
		if (!getInheritedConstraints().stream().allMatch(constraint -> detail.getInheritedConstraints().contains(constraint))) {
			throw new IllegalStateException("Same key, different inherited constraints info (" + key + ").");
		}
		if (!getProfileConstraints().stream().allMatch(constraint -> detail.getProfileConstraints().contains(constraint))) {
			throw new IllegalStateException("Same key, different profile constraints info (" + key + ").");
		}
	}

	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> iconStyles = Lists.newArrayList();
		
		iconStyles.add(
				new CSSStyleBlock(Lists.newArrayList(".fhir-list"),
					Lists.newArrayList(
						new CSSRule("margin", "0px"))));
			
		return iconStyles;
	}
	
}
