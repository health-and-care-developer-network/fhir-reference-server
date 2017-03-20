package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.LinkData;

public class FhirTreeNodeBuilder {
	private final FhirDocLinkFactory typeLinks = new FhirDocLinkFactory();
	
	public FhirTreeNode fromElementDefinition(ElementDefinitionDt elementDefinition) {
		
		String displayName = getDisplayName(elementDefinition);
		String path = elementDefinition.getPath();
		
		List<Type> snapshotElementTypes = elementDefinition.getType();
		String snapshotTypeDesc = "";
		LinkData typeLink = null;
		if (!snapshotElementTypes.isEmpty()) {
			for (Type snapshotElementType : snapshotElementTypes) {
				String code = snapshotElementType.getCode();
				if (FhirDataTypes.forType(code) != FhirDataType.UNKNOWN) {
					if (typeLink != null) {
						System.out.println("Multiple type codes matched resources or element types."
								+ " Don't know which type URL to use");
					}
					typeLink = typeLinks.forDataTypeName(code);
				}
				
				if (snapshotTypeDesc.length() > 0) {
					snapshotTypeDesc += " ";
				}
				snapshotTypeDesc += code;
			}
		}
		
		if (typeLink == null) {
			typeLink = new LinkData("","");
		}

		ResourceFlags flags = ResourceFlags.forDefinition(elementDefinition);
		String flagsString = flags.toString();
		
		String max = elementDefinition.getMax();
		FhirCardinality cardinality = new FhirCardinality(elementDefinition.getMin(), max);
		
		boolean removedByProfile = max.equals("0"); 
		if (removedByProfile) {
			System.out.println("***REMOVED***: Snapshot element: " + displayName + " " + path);
		} else {
			System.out.println("Snapshot element: " + snapshotTypeDesc + " " + displayName + " " + path + " [" + cardinality + "] " + flagsString);
		}
		
		Slicing slicing = elementDefinition.getSlicing();
		if (!slicing.isEmpty()) {
			System.out.println("***SLICING***");
		}
		
		URL nameUrl;
		try {
			nameUrl = new URL("http://www.hl7.org");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		FhirIcon icon = FhirIcon.forElementDefinition(elementDefinition);
		
		return new FhirTreeNode(
			new FhirTreeNodeId(displayName, nameUrl, icon),
			flags,
			cardinality,
			typeLink, 
			"test",
			Lists.newArrayList());
	}

	String getDisplayName(ElementDefinitionDt elementDefinition) {
		String name = elementDefinition.getName();
		boolean hasName = !Strings.isNullOrEmpty(name);
		
		String path = elementDefinition.getPath();
		boolean hasPath = !Strings.isNullOrEmpty(path);
		
		String pathName = null;
		if (hasPath) {
			String[] pathTokens = path.split("\\.");
			pathName = pathTokens[pathTokens.length - 1]; 
		}
		
		String displayName;
		
		if (hasName && hasPath) {
			displayName = pathName + " (" + name + ")";
		} else if (hasPath) {
			displayName = pathName;
		} else if (hasName) {
			displayName = name;
		} else {
			throw new IllegalStateException("No name or path information");
		}
		
		return displayName;
	}

}
