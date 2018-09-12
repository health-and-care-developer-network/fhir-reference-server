package uk.nhs.fhir.render.format.namingsystem;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.namingsystem.FhirNamingSystemUniqueId;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemIdentifierTableData;
import uk.nhs.fhir.render.html.table.TableTitle;

public class NamingSystemIdentifierTableDataProvider {
	


	private final List<FhirNamingSystemUniqueId> parameters;
	
	public NamingSystemIdentifierTableDataProvider(List<FhirNamingSystemUniqueId> parameters) {
		this.parameters = parameters;
	}

	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Value", "The logical name of the element", "30%"),
			new TableTitle("Type", "Type of the Element", "40%"),
			new TableTitle("Preferred", "Preferrence for the element", "30%")
		);
	}

	public List<NamingSystemIdentifierTableData> getRows() {
		List<NamingSystemIdentifierTableData> data = Lists.newArrayList();
		for (FhirNamingSystemUniqueId parameter : parameters) {
			addParameter(parameter, data);
		}
		return data;
	}

	private void addParameter(FhirNamingSystemUniqueId parameter, List<NamingSystemIdentifierTableData> data) {
		String rowValue = parameter.getValue();
		String type = parameter.getType(); 
		Boolean preferred = parameter.getPreferred();
		data.add(new NamingSystemIdentifierTableData(rowValue, type, preferred));
		
	}

}