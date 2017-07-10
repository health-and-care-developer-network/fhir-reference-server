package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import uk.nhs.fhir.makehtml.html.RendererError;

public class SlicingInfo {

	private final String description;
	private final Set<String> discriminatorPaths = Sets.newHashSet();
	private final Boolean ordered;
	private final String rules;
	
	public SlicingInfo(Slicing slicing) {
		this(
			slicing.getDescription(),
			slicing.getDiscriminator().stream()
				.map(stringDt -> stringDt.getValue())
				.collect(Collectors.toSet()),
			slicing.getOrdered(),
			slicing.getRules());
	}
	
	public SlicingInfo(String description, Set<String> discriminatorPaths, Boolean ordered, String rules) {
		this.description = description;
		this.discriminatorPaths.addAll(discriminatorPaths);
		this.ordered = ordered;
		this.rules = rules;
	}

	public Set<String> getDiscriminatorPaths() {
		return discriminatorPaths;
	}
	
	public Boolean getOrdered() {
		return ordered;
	}

	public String getOrderedDesc() {
		return (ordered != null && ordered) ? "ordered" : "unordered";
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getRules() {
		return rules;
	}

	/**
	 * @return an Optional<ResourceInfo> summarising the slicing info, or Optional.empty() if no info present
	 */
	public Optional<ResourceInfo> toResourceInfo() {
		List<String> info = Lists.newArrayList();

		if (!Strings.isNullOrEmpty(description)) {
			info.add("Description: " + description); 
		}
		
		if (discriminatorPaths.size() == 1) {
			info.add("Discriminator: " + discriminatorPaths.iterator().next());
		} else if (discriminatorPaths.size() > 1) {
			info.add("Discriminators: [" + String.join(" ", discriminatorPaths) + "]");
		}
		
		if (ordered != null) {
			info.add("Ordering: " + ordered.toString());
		} else {
			info.add("Ordering: false");
		}
		
		if (!Strings.isNullOrEmpty(rules)) {
			info.add("Rules: " + rules);
		}
		
		if (info.isEmpty()) {
			return Optional.empty();
		} else {
			ResourceInfo slicingResourceInfo = new ResourceInfo("Slicing", String.join(", ", info), ResourceInfoType.SLICING);
			return Optional.of(slicingResourceInfo);
		}
	}
	
}
