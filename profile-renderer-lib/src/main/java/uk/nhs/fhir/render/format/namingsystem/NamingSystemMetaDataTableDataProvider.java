package uk.nhs.fhir.render.format.namingsystem;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedNamingSystem;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemMetaDataRowData;
import uk.nhs.fhir.render.html.table.TableTitle;
import uk.nhs.fhir.util.StringUtil;

public class NamingSystemMetaDataTableDataProvider {
	
	private final WrappedNamingSystem source;
	
	public NamingSystemMetaDataTableDataProvider(WrappedNamingSystem source) {
		this.source = source;
	}
	
	public List<TableTitle> getColumns() {
		// KGM 8/May/2017 Altered meta table column to % widths
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "40%"),
			new TableTitle("Value", "Additional information about the element", "60%")
		);
	}

	public List<NamingSystemMetaDataRowData> getRows() {
		return Lists.newArrayList(
			new NamingSystemMetaDataRowData("Name", StringUtil.capitaliseLowerCase(source.getName())),
			new NamingSystemMetaDataRowData("Kind",  StringUtil.capitaliseLowerCase(source.getKind())),
			new NamingSystemMetaDataRowData("Description", source.getDescription()),
			new NamingSystemMetaDataRowData("Usage", source.getUsage()),
			new NamingSystemMetaDataRowData("Responsible",  StringUtil.capitaliseLowerCase(source.getResponsible()))
		);
	}

}