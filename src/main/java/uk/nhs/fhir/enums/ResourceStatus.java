package uk.nhs.fhir.enums;

public enum ResourceStatus {

	draft("<span class='status-draft'>Draft</span>"),
	active("<span class='status-active'>Active</span>"),
	retired("<span class='status-retired'>Retired</span>"),
	unknown("<span class='status-unknown'>Status unknown</span>"),
	;
	
	private String statusLabel = null;
	
	ResourceStatus(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public String getStatusLabel() {
		return statusLabel;
	}
	
	public static ResourceStatus getStatus(String status) {
		status = status.toLowerCase();
		for (ResourceStatus resourceStatus : ResourceStatus.values()) {
			if (resourceStatus.name().equals(status)) {
				return resourceStatus;
			}
		}
		return unknown;
	}
}
