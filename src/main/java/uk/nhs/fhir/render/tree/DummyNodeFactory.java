package uk.nhs.fhir.render.tree;

public interface DummyNodeFactory<T> {
	public T create(T parent, NodePath path);
}
