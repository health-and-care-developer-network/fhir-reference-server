package uk.nhs.fhir.metadata;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.datalayer.DefaultFhirFileLocator;
import uk.nhs.fhir.datalayer.AbstractFhirFileLocator;
import uk.nhs.fhir.makehtml.FhirResourceCollector;
import uk.nhs.fhir.metadata.index.SimpleFhirResourceList;
import uk.nhs.fhir.util.FhirVersion;

public class FhirResourceImportSupervisor {
	private final FhirResourceImporter importer;
	private final FhirResourceMetadataStore resourceMetadataCache;
	
	private static final FhirVersion[] supportedVersions = new FhirVersion[]{FhirVersion.DSTU2, FhirVersion.STU3};
	private final Map<FhirVersion, FhirResourceCollector> importedResourceFinders;
	
	public FhirResourceImportSupervisor(Path fhirImportSource) {
		this(fhirImportSource, new DefaultFhirFileLocator());
	}
	
	public FhirResourceImportSupervisor(Path fhirImportSource, AbstractFhirFileLocator fhirImportDestination) {
		VersionedFolderImportWriter versionedFolderImportWriter = new VersionedFolderImportWriter(fhirImportDestination);
		this.importer = new FhirResourceImporter(fhirImportSource, versionedFolderImportWriter);
		
		importedResourceFinders = Maps.newConcurrentMap();
		for (FhirVersion version : supportedVersions) {
			importedResourceFinders.put(version, new FhirResourceCollector(fhirImportDestination.getRoot(version)));
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
		
		for (FhirVersion version : supportedVersions) {
			
			List<ResourceMetadata> resourceMetadatas = 
				Streams.stream(importedResourceFinders.get(version).collect())
					.map(e -> e.getValue().getMetadata(e.getKey()))
					.collect(Collectors.toList());
			
			resourceMetadataCache.populate(resourceMetadatas);
		}
	}
}
