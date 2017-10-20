package uk.nhs.fhir.metadata;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.datalayer.PropertiesFhirFileLocator;
import uk.nhs.fhir.makehtml.FhirResourceCollector;
import uk.nhs.fhir.metadata.index.SimpleFhirResourceList;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;

public class FhirResourceImportSupervisor {
	private final FhirResourceImporter importer;
	private final FhirResourceMetadataStore resourceMetadataCache;
	
	private final Map<FhirVersion, FhirResourceCollector> importedResourceFinders;
	
	public FhirResourceImportSupervisor(Path fhirImportSource) {
		this(fhirImportSource, new PropertiesFhirFileLocator());
	}
	
	public FhirResourceImportSupervisor(Path fhirImportSource, AbstractFhirFileLocator fhirImportDestination) {
		VersionedFolderImportWriter versionedFolderImportWriter = new VersionedFolderImportWriter(fhirImportDestination);
		this.importer = new FhirResourceImporter(fhirImportSource, versionedFolderImportWriter);
		
		importedResourceFinders = Maps.newConcurrentMap();
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			importedResourceFinders.put(version, new FhirResourceCollector(fhirImportDestination.getSourceRoot(version)));
		}
		
		resourceMetadataCache = new FhirResourceMetadataStore();
		resourceMetadataCache.addIndex(new SimpleFhirResourceList());
	}
	
	public void cleanAndReimport() {
		clearCache();
		importAndCacheMetadata();
	}
	
	public void clearCache() {
		resourceMetadataCache.clear();
	}

	public void importAndCacheMetadata() {
		importer.doImport();
		
		for (FhirVersion version : FhirVersion.getSupportedVersions()) {
			
			List<ResourceMetadata> resourceMetadatas = 
				Streams.stream(importedResourceFinders.get(version).collect())
					.map(e -> e.getValue().getMetadata(e.getKey()))
					.collect(Collectors.toList());
			
			resourceMetadataCache.populate(resourceMetadatas);
		}
	}
}
