package org.acoveo.tools;

public interface ITuple<T,V> {
	public T getFirst();
	public V getSecond();
	public void setFirst(T first);
	public void setSecond(V second);
}
