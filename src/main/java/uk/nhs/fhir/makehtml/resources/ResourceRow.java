package uk.nhs.fhir.makehtml.resources;

public class ResourceRow implements Comparable<ResourceRow> {
	
	private String name = null;
	private String description = null;
	private String url = null;
	private String type = null;
	private int publishOrder = 1000;
	
	public ResourceRow(String name, String description, String url, String type, int publishOrder) {
    	this.name = name;
    	this.description = description;
    	this.url = url;
    	this.type = type;
    	this.publishOrder = publishOrder;
    }
	
	public void writeResource(StringBuilder sb) {
		sb.append("<tr><td style='border-bottom: 1px solid #ddd;'><a href='").append(url).append("'>").append(name).append("</a></td>");
    	sb.append("<td style='border-bottom: 1px solid #ddd;'>").append(type).append("</td>");
    	sb.append("<td style='border-bottom: 1px solid #ddd;'>").append(description).append("</td>");
		sb.append("</tr>");
	}
	
	public String getUrl() {
		return url;
	}
	public String getName() {
		return name;
	}
	public int getPublishOrder() {
		return publishOrder;
	}

	@Override
	public int compareTo(ResourceRow other) {
		if (other.getName().equals(name) && other.getUrl().equals(url)) {
			return 0;
		} else {
			if (other.publishOrder > publishOrder)
				return -1;
			else
				return 1;
		}
	}
}
