package uk.nhs.fhir.page.namingsystem;



import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.page.VelocityTemplate;
public class NamingSystemListPageTemplate extends VelocityTemplate {

private final List<ResourceMetadata> namingsystem;
	
	public NamingSystemListPageTemplate(String baseUrl, List<ResourceMetadata> namingsystem) {
		
		super("namingsystem.vm", Optional.of(ResourceType.NAMINGSYSTEM), Optional.empty());
		this.namingsystem = namingsystem;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "namingsystems", namingsystem );
	}

}

