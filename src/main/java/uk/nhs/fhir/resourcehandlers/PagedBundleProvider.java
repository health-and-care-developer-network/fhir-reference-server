package uk.nhs.fhir.resourcehandlers;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FhirVersion;

public class PagedBundleProvider implements IBundleProvider {
	
	private static final Logger LOG = Logger.getLogger(PagedBundleProvider.class.getName());

	public static final int SEARCH_BY_TYPE = 1;
	public static final int SEARCH_BY_NAME = 2;
	public static final int SEARCH_BY_URL = 3;
	
	private int searchType = 0;
	private FilesystemIF dataSource = null;
	private FhirVersion fhirVersion = null;
	private ResourceType resourceType = null;
	private String searchParam = null;
	
	public PagedBundleProvider(int searchType, FilesystemIF dataSource,
									FhirVersion fhirVersion, ResourceType resourceType,
									String searchParam) {
		this.searchType = searchType;
		this.dataSource = dataSource;
		this.fhirVersion = fhirVersion;
		this.resourceType = resourceType;
		this.searchParam = searchParam;
		if (this.searchParam == null) {
			this.searchParam = "";
		}
	}
	
	public PagedBundleProvider(int searchType, FilesystemIF dataSource,
									FhirVersion fhirVersion, ResourceType resourceType) {
		this.searchType = searchType;
		this.dataSource = dataSource;
		this.fhirVersion = fhirVersion;
		this.resourceType = resourceType;
		this.searchParam = null;
	}
	
	@Override
	public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
		
		LOG.fine("Paging results provider - getResources method called for index " + theFromIndex + " to " + theToIndex);
		
		switch(searchType) {
		case SEARCH_BY_TYPE:
			return dataSource.getAllResourcesOfType(this.fhirVersion, this.resourceType,
															theFromIndex, theToIndex);
		case SEARCH_BY_NAME:
			return dataSource.getResourceMatchByName(this.fhirVersion, this.resourceType, this.searchParam,
															theFromIndex, theToIndex);
		case SEARCH_BY_URL:
			return dataSource.getResourceMatchByURL(this.fhirVersion, this.resourceType, this.searchParam,
															theFromIndex, theToIndex);
		}
		return null;
	}

	@Override
	public Integer preferredPageSize() {
		return null;
	}

	@Override
	public Integer size() {
		
		LOG.fine("Paging results provider - size() method called");
		
		switch(searchType) {
		case SEARCH_BY_TYPE:
			return new Integer(dataSource.getResourceCount(this.fhirVersion, this.resourceType));
		case SEARCH_BY_NAME:
			return new Integer(dataSource.getResourceCountByName(this.fhirVersion, this.resourceType, this.searchParam));
		case SEARCH_BY_URL:
			return dataSource.getResourceCountByURL(this.fhirVersion, this.resourceType, this.searchParam);
		}
		return new Integer(0);
	}

	@Override
	public IPrimitiveType<Date> getPublished() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}
}
