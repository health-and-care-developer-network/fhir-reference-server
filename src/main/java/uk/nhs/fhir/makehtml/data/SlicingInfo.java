package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.primitive.StringDt;

public class SlicingInfo {

	private final String description;
	private final List<String> discriminatorPaths = Lists.newArrayList();
	private final Boolean ordered;
	private final String rules;
	
	public SlicingInfo(Slicing slicing) {
		this.description = slicing.getDescription();
		slicing.getDiscriminator().forEach((StringDt discriminator) -> discriminatorPaths.add(discriminator.getValue()));
		this.ordered = slicing.getOrdered();
		this.rules = slicing.getRules();
	}

	public List<String> getDiscriminatorPaths() {
		return discriminatorPaths;
	}

	/**
	 * @return an Optional<ResourceInfo> summarising the slicing info, or Optional.empty() if no info present
	 */
	public Optional<ResourceInfo> toResourceInfo() {
		List<String> info = Lists.newArrayList();

		if (!Strings.isNullOrEmpty(description)) {
			info.add("Ordering: " + description); 
		}
		
		if (discriminatorPaths.size() == 1) {
			info.add("Discriminator: " + discriminatorPaths.get(0));
		} else if (discriminatorPaths.size() > 1) {
			info.add("Discriminators: [" + String.join(" ", discriminatorPaths) + "]");
		}
		
		if (ordered != null) {
			info.add("Ordering: " + ordered.toString());
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
