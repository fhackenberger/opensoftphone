package org.acoveo.tools;

import java.io.Serializable;
import java.util.Map.Entry;

/** A simple class implementing the {@code Map.Entry} interface
 * 
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class SimpleEntry<K, V> implements Entry<K, V>, Serializable {
	private static final long serialVersionUID = -437528860062820394L;

	private K key;
	private V value;
	
	public SimpleEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

}
