package uk.nhs.fhir.render.tree;

import java.util.List;

public interface TreeContentI<T> {

	public String getPath();
	public List<? extends T> getChildren();
	public void setParent(T parent);
	public T getParent();
	public void addChild(T child);

}
