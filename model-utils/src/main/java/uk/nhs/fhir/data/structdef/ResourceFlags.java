package uk.nhs.fhir.data.structdef;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ResourceFlags {
	
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
