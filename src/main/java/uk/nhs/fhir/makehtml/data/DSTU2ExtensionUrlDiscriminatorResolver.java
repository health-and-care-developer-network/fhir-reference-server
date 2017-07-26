package uk.nhs.fhir.makehtml.data;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class DSTU2ExtensionUrlDiscriminatorResolver implements ExtensionUrlDiscriminatorResolver {

	@Override
	public Set<FhirURL> getExtensionUrlDiscriminators(FhirTreeNode node) { {
		return node
			.getTypeLinks()
			.links()
			.stream()
			.filter(typeLink -> typeLink.getKey().getText().equals("Extension"))
			.flatMap(typeLink -> typeLink.getValue().isEmpty() ? Lists.newArrayList(typeLink.getKey()).stream() : typeLink.getValue().stream())
			.map(link -> link.getURL())
			.collect(Collectors.toSet());
		}
	}

}
