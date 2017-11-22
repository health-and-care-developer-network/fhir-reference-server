package uk.nhs.fhir.metadata;

public interface Store<T> {
	public void populate(Iterable<T> supplier);
	public void clear();
}
