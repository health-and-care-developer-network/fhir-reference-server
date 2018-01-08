package uk.nhs.fhir.render.tree;

import static uk.nhs.fhir.event.RendererEventType.IGNORABLE_MAPPING_ID;
import static uk.nhs.fhir.event.RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY;
import static uk.nhs.fhir.event.RendererEventType.MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.TestEventHandlerContext;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.event.EventHandler;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionDetails;

public class TestNodeMappingValidator {
	private static final String AN_IGNORABLE_VALUE = StructureDefinitionDetails.IGNORABLE_MAPPING_VALUES.iterator().next();
	
	private TestEventHandlerContext thisEventHandler;
	private EventHandler eventHandlerBefore = null;
	
	@Before
	public void substituteTestEventHandler() {
		this.eventHandlerBefore = EventHandlerContext.forThread();
		thisEventHandler = new TestEventHandlerContext();
		EventHandlerContext.setForThread(thisEventHandler);
	}
	
	@After
	public void reinstateOriginalEventHandler() {
		EventHandlerContext.setForThread(eventHandlerBefore);
		thisEventHandler = null;
	}
	
	public void assertEvents(RendererEventType... events) {
		Assert.assertEquals(Lists.newArrayList(events), thisEventHandler.getEvents());
	}
	
	@Test
	public void testSingleNonIgnorable() {
		HasMappings node = new TestHasMappings(new FhirElementMapping("id1", "value1", Optional.empty()));
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		Assert.assertTrue(thisEventHandler.getEvents().isEmpty());
	}
	
	@Test
	public void testSingleIgnorable() {
		HasMappings node = new TestHasMappings(new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty()));
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID);
	}
	
	@Test
	public void testIgnorableAndNonIgnorableNoOverlap() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty()),
			new FhirElementMapping("id2", "value1", Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID);
	}
	
	@Test
	public void testIgnorableAndNonIgnorableOverlap() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty()),
			new FhirElementMapping("id1", "value1", Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID, MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE);
	}
	
	@Test
	public void test2NonIgnorablesOverlap() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", "value1", Optional.empty()),
			new FhirElementMapping("id1", "value2", Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(MULTIPLE_MAPPINGS_SAME_KEY);
	}
	
	@Test
	public void test2IgnorablesOverlapDuplicateValue() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty()),
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID, IGNORABLE_MAPPING_ID, MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE);
	}
	
	@Test
	public void test2Ignorables1NonIgnorableOverlapIsIgnorable() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", "value1", Optional.empty()),
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty()),
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID, IGNORABLE_MAPPING_ID, MULTIPLE_MAPPINGS_SAME_KEY_IGNORABLE);
	}
	
	@Test
	public void test1Ignorable2NonIgnorablesOverlapIsNotIgnorable() {
		HasMappings node = new TestHasMappings(
			new FhirElementMapping("id1", "value1", Optional.empty()),
			new FhirElementMapping("id1", "value2", Optional.empty()),
			new FhirElementMapping("id1", AN_IGNORABLE_VALUE, Optional.empty())
		);
		NodeMappingValidator validator = new NodeMappingValidator(node);
		validator.validate();
		assertEvents(IGNORABLE_MAPPING_ID, MULTIPLE_MAPPINGS_SAME_KEY);
	}
}

class TestHasMappings implements HasMappings {

	private final List<FhirElementMapping> mappings;

	public TestHasMappings(FhirElementMapping... mappings) {
		this.mappings = Lists.newArrayList(mappings);
	}

	@Override
	public String getPath() {
		return "test.node";
	}
	
	@Override
	public List<FhirElementMapping> getMappings() {
		return mappings;
	}
	
}