package org.acoveo.tools;

import java.io.Serializable;

public class Tuple<T,V> implements ITuple<T,V>, Serializable {
	private static final long serialVersionUID = -7744093210838948622L;

	protected T first;
	protected V second;
	
	public Tuple(T first, V second) {
		this.first = first;
		this.second = second;
	}
	
	public Tuple() {
	}

	@Override
	public T getFirst() {
		return first;
	}

	@Override
	public V getSecond() {
		return second;
	}

	@Override
	public void setFirst(T first) {
		this.first = first;
	}

	@Override
	public void setSecond(V second) {
		this.second = second;
	}

}
