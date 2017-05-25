package uk.nhs.fhir.makehtml.data;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class ResourceFlags {

	public static ResourceFlags forDefinition(ElementDefinitionDt elementDefinition) {
		
		boolean isSummary = Boolean.TRUE.equals(elementDefinition.getIsSummary());
		boolean isModifier = Boolean.TRUE.equals(elementDefinition.getIsModifier());
		boolean isConstrained = !elementDefinition.getConstraint().isEmpty();
		boolean isMustSupport = Boolean.TRUE.equals(elementDefinition.getMustSupport());
		
		ResourceFlags flags = new ResourceFlags();
		if (isSummary) {
			flags.addSummaryFlag();
		}
		if (isModifier) {
			flags.addModifierFlag();
		}
		if (isConstrained) {
			flags.addConstrainedFlag();
		}
		if (isMustSupport) {
			flags.addMustSupportFlag();
		}
		
		return flags;
	}
	
	private Set<ResourceFlag> flags = Sets.newHashSet();

	public void addSummaryFlag() {
		flags.add(ResourceFlag.SUMMARY);
	}
	public boolean isSummary() {
		return flags.contains(ResourceFlag.SUMMARY);
	}
	public void addModifierFlag() {
		flags.add(ResourceFlag.MODIFIER);
	}
	public boolean isModifier() {
		return flags.contains(ResourceFlag.MODIFIER);
	}
	public void addMustSupportFlag() {
		flags.add(ResourceFlag.MUSTSUPPORT);
	}
	public boolean isMustSupport() {
		return flags.contains(ResourceFlag.MUSTSUPPORT);
	}
	public void addConstrainedFlag() {
		flags.add(ResourceFlag.CONSTRAINED);
	}
	public boolean isConstrained() {
		return flags.contains(ResourceFlag.CONSTRAINED);
	}
	
	@Override
	public String toString() {
		List<String> flagStrings = Lists.newArrayList();
		for (ResourceFlag flag : flags) {
			flagStrings.add(flag.name());
		}
		
		StringBuilder flagsString = new StringBuilder();
		flagsString.append("[");
		flagsString.append(String.join(" ", flagStrings));
		flagsString.append("]");
		
		return flagsString.toString();
	}
	
	public Set<ResourceFlag> getFlags() {
		return flags;
	}
	
	public int hashCode() {
		return (isSummary() ? 0 : 1 << 0)
			| (isModifier() ? 0 : 1 << 1)
			| (isConstrained() ? 0 : 1 << 2)
			| (isMustSupport() ? 0 : 1 << 3);
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof ResourceFlags)) {
			return false;
		}
		
		ResourceFlags otherResourceFlags = (ResourceFlags)other;
		
		return isSummary() == otherResourceFlags.isSummary()
			&& isModifier() == otherResourceFlags.isModifier()
			&& isConstrained() == otherResourceFlags.isConstrained()
			&& isMustSupport() == otherResourceFlags.isMustSupport();
	}
}
