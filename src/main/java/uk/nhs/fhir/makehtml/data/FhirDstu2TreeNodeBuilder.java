package uk.nhs.fhir.makehtml.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.context.FhirDstu2DataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Binding;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Constraint;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Mapping;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Slicing;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.html.Dstu2Fix;
import uk.nhs.fhir.makehtml.html.RendererError;
import uk.nhs.fhir.makehtml.valid.NodeMappingValidator;
import uk.nhs.fhir.util.Dstu2FhirDocLinkFactory;
import uk.nhs.fhir.util.HAPIUtils;

public class FhirDstu2TreeNodeBuilder {
	private static final Dstu2FhirDocLinkFactory typeLinkFactory = new Dstu2FhirDocLinkFactory();
	
	public FhirTreeNode fromElementDefinition(ElementDefinitionDt elementDefinition) {
		
		Optional<String> name = Optional.ofNullable(elementDefinition.getName());

		List<LinkData> typeLinks = Lists.newArrayList();
		if (elementDefinition.getPath().split("\\.").length == 1) {
			typeLinks.add(new SimpleLinkData(FhirURL.buildOrThrow(FhirURLConstants.HTTP_HL7_DSTU2 + "/profiling.html"), "Profile"));
		} else {
			typeLinks.addAll(getTypeLinks(elementDefinition.getType()));
		}
		
		Set<FhirDataType> dataTypes = FhirDstu2DataTypes.getTypes(elementDefinition.getType());
		FhirDataType dataType;
		if (dataTypes.isEmpty()) {
			dataType = FhirDataType.DELEGATED_TYPE; 
		} else if (dataTypes.size() == 1) {
			dataType = dataTypes.iterator().next();
		} else if (dataTypes.size() > 1
		  && elementDefinition.getPath().endsWith("[x]")) {
			dataType = FhirDataType.CHOICE;
		} else {
			throw new IllegalStateException("Found " + dataTypes.size() + " data types for node " + elementDefinition.getPath());
		}

		ResourceFlags flags = ResourceFlags.forDefinition(elementDefinition);
		
		Integer min = elementDefinition.getMin();
		String max = elementDefinition.getMax();
		
		FhirDstu2Icon icon = FhirDstu2Icon.forElementDefinition(elementDefinition);
		
		String shortDescription = elementDefinition.getShort();
		if (shortDescription == null) {
			shortDescription = "";
		}
		
		List<IdDt> condition = elementDefinition.getCondition();
		Set<String> conditionIds = Sets.newHashSet();
		condition.forEach(idDt -> conditionIds.add(idDt.toString()));
		
		List<ConstraintInfo> constraints = Lists.newArrayList();
		for (Constraint constraint : elementDefinition.getConstraint()) {
			String key = constraint.getKey();
			if (!conditionIds.contains(key)) {
				RendererError.handle(RendererError.Key.CONSTRAINT_WITHOUT_CONDITION, "Constraint " + key + " doesn't have an associated condition pointing at it");
			}
			
			String description = constraint.getHuman();
			String severity = constraint.getSeverity();
			String requirementsString = constraint.getRequirements();
			Optional<String> requirements = Strings.isNullOrEmpty(requirementsString) ? Optional.empty() : Optional.of(requirementsString);
			String xpath = constraint.getXpath();
			
			constraints.add(new ConstraintInfo(key, description, severity, requirements, xpath));
		}

		//check for duplicate keys
		for (int i=0; i<constraints.size(); i++) {
			ConstraintInfo constraint1 = constraints.get(i);
			for (int j=i+1; j<constraints.size(); j++) {
				ConstraintInfo constraint2 = constraints.get(j);
				if (constraint1.getKey().equals(constraint2.getKey())) {
					RendererError.handle(RendererError.Key.DUPLICATE_CONSTRAINT_KEYS, "Node constraints with duplicate keys: '" + constraint1.getKey() + "'");
				}
			}
		}
		
		String path = elementDefinition.getPath();

		FhirTreeNode node = new FhirTreeNode(
			icon,
			name,
			flags,
			min,
			max,
			typeLinks, 
			shortDescription,
			constraints,
			path,
			dataType);

		String definition = elementDefinition.getDefinition();
		if (!Strings.isNullOrEmpty(definition)) {
			node.setDefinition(Optional.of(definition));
		}
		
		Slicing slicing = elementDefinition.getSlicing();
		if (!slicing.isEmpty()) {

			if (slicing.getDiscriminator().isEmpty()) {
				RendererError.handle(RendererError.Key.SLICING_WITHOUT_DISCRIMINATOR, "Element " + node.getPath() + " has slicing without any discriminator");
			}
			node.setSlicingInfo(new SlicingInfo(slicing));	
		}
		
		IDatatype fixed = elementDefinition.getFixed();
		if (fixed != null) {
			if (fixed instanceof BasePrimitive) {
				BasePrimitive<?> fixedPrimitive = (BasePrimitive<?>)fixed;
				String fixedValueAsString = fixedPrimitive.getValueAsString();
				
				if (fixedValueAsString.equals("https://hl7.org.uk/fhir/CareConnect-ConditionCategory-1")) {
					String correctedUrl = "https://fhir.hl7.org.uk/CareConnect-ConditionCategory-1";
					RendererError.handle(RendererError.Key.HL7_ORG_UK_HOST, "Fixing https://hl7.org.uk/fhir/CareConnect-ConditionCategory-1 to " + correctedUrl);
					fixedValueAsString = correctedUrl;
				}
				
				node.setFixedValue(fixedValueAsString);
			} else {
				throw new IllegalStateException("Unhandled type for fixed value: " + fixed.getClass().getName());
			}
		}
		
		IDatatype example = elementDefinition.getExample();
		if (example != null) {
			if (example instanceof BasePrimitive) {
				BasePrimitive<?> examplePrimitive = (BasePrimitive<?>)example;
				node.setExample(examplePrimitive.getValueAsString());
			} else if (example instanceof PeriodDt) {
				PeriodDt examplePeriod = (PeriodDt)example;
				node.setExample(HAPIUtils.periodToString(examplePeriod));
			} else {
				throw new IllegalStateException("Unhandled type for example value: " + example.getClass().getName());
			}
		}
		
		IDatatype defaultValue = elementDefinition.getDefaultValue();
		if (defaultValue != null) {
			if (defaultValue instanceof BasePrimitive) {
				BasePrimitive<?> defaultValuePrimitive = (BasePrimitive<?>)defaultValue;
				node.setDefaultValue(defaultValuePrimitive.getValueAsString());
			} else {
				throw new IllegalStateException("Unhandled type for default value: " + defaultValue.getClass().getName());
			}
		}
		
		Binding binding = elementDefinition.getBinding();
		if (!binding.isEmpty()) {
			
			IDatatype valueSet = binding.getValueSet();
			Optional<FhirURL> url = Optional.empty();
			if (valueSet != null) {
				String urlString = HAPIUtils.resolveDatatypeValue(valueSet);
				url = Optional.of(FhirURL.buildOrThrow(Dstu2Fix.fixValuesetLink(urlString)));
			}
			
			Optional<String> description = Optional.ofNullable(binding.getDescription());
			String strength = binding.getStrength();
			
			node.setBinding(new BindingInfo(description, url, strength));
		}
		
		String requirements = elementDefinition.getRequirements();
		if (!Strings.isNullOrEmpty(requirements)) {
			node.setRequirements(requirements);
		}
		
		String comments = elementDefinition.getComments();
		if (!Strings.isNullOrEmpty(comments)) {
			node.setComments(comments);
		}
		
		List<StringDt> alias = elementDefinition.getAlias();
		if (!alias.isEmpty()) {
			List<String> aliases = alias.stream().map(stringDt -> stringDt.getValue()).collect(Collectors.toList());
			node.setAliases(aliases);
		}
		
		for (Type type : elementDefinition.getType()) {
			if (type.getCode() != null 
			  && type.getCode().equals("Extension")) {
				node.setExtensionType(lookupExtensionType(type));
				break;
			}
		}
		
		String nameReference = elementDefinition.getNameReference();
		if (!Strings.isNullOrEmpty(nameReference)) {
			node.setLinkedNodeName(nameReference);
		}
			
		for (Mapping mapping : elementDefinition.getMapping()) {
			String identity = mapping.getIdentity();
			Optional<String> language = Optional.ofNullable(mapping.getLanguage());
			String map = mapping.getMap();
			
			node.addMapping(new FhirElementMapping(identity, map, language));
		}
		NodeMappingValidator.validate(node);
		
		return node;
	}
	
	/**
	 * Consider the extension complex iff it has an element with path "Extension.extension.url"
	 */
	private static ExtensionType lookupExtensionType(Type type)  {

		List<UriDt> profiles = type.getProfile();

		for (UriDt uriDt : profiles) {

			String filePath;
			try {
				URI uri = new URI(uriDt.getValue());
				filePath = uri.toURL().getFile() + ".xml";
			} catch (URISyntaxException | MalformedURLException e) {
				throw new IllegalStateException("URI/URL error for uri " + uriDt.getValue(), e);
			}
			
			String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
			// case insensitive search
			File extensionFile = null;
			for (File f : new File(FhirDstu2Icon.suppliedResourcesFolderPath).listFiles()) {
				if (f.getName().toLowerCase().equals(fileName.toLowerCase())) {
					extensionFile = f;
					break;
				}
			}

			if (extensionFile == null) {
				RendererError.handle(RendererError.Key.EXTENSION_FILE_NOT_FOUND, "Extension source expected at: " + fileName);
			}
			
			try (FileInputStream fis = new FileInputStream(extensionFile);
				Reader reader = new InputStreamReader(fis)) {
				
				IParser parser = HAPIUtils.dstu2XmlParser();
				StructureDefinition extension = parser.parseResource(StructureDefinition.class, reader);

				if (extension.getSnapshot().getElement().stream().anyMatch(element -> element.getPath().contains("Extension.extension.url"))) {
					return ExtensionType.COMPLEX;
				}
			} catch (IOException ie) {
				throw new IllegalStateException(ie);
			}
		}

		return ExtensionType.SIMPLE;
	}

	private List<LinkData> getTypeLinks(List<Type> snapshotElementTypes) {
		List<LinkData> typeLinks = Lists.newArrayList();
		
		List<Type> knownTypes = FhirDstu2DataTypes.knownTypes(snapshotElementTypes);
		if (!knownTypes.isEmpty()) {
			for (Type type : knownTypes) {
				String code = type.getCode();
				
				List<UriDt> profileUris = type.getProfile();
				if (profileUris.isEmpty()) {
					typeLinks.add(typeLinkFactory.forDataTypeName(code));
				} else {
					List<String> uris = Lists.newArrayList();
					profileUris.forEach((UriDt uri) -> uris.add(uri.getValue()));
					typeLinks.add(typeLinkFactory.withNestedLinks(code, uris));
				}
			}
		}
		
		return typeLinks;
	}

}
