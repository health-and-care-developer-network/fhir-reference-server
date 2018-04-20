package uk.nhs.fhir.data.structdef;

public enum ResourceFlag {
	SUMMARY(ResourceFlag.SIGMA, "This element is included in summaries"),
	MODIFIER("?!", "This element is a modifier element"),
	CONSTRAINED("I", "This element has or is affected by some invariants"),
	MUSTSUPPORT("S", "This element must be supported"),
	NOEXTEND("NE", "This element cannot have extensions");
	
	private static final String SIGMA = "&#931;"; //"Σ"
	
	private final String flag;
	private final String desc;
	
	ResourceFlag(String flag, String desc) {
		this.flag = flag;
		this.desc = desc;
	}
	
	public String getFlag() {
		return flag;
	}
	
	public String getDesc() {
		return desc;
	}
}