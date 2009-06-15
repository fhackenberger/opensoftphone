package org.acoveo.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** An unmodifiable view of a float array
 * 
 * @author fhackenberger
 *
 */ 
public class FloatCollection implements Collection<Float> {
	float[] array;
	
	/** Constructs a new Collection view of the given float array
	 * 
	 * @param array The array to use as a backing store
	 */
	public FloatCollection(float[] array) {
		this.array = array;
	}

	@Override
	public boolean add(Float e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Float> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		if(o == null) {
			return false;
		}
		for(float member : array) {
			if(o.equals(member)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o : c) {
			if(!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return array.length == 0;
	}

	@Override
	public Iterator<Float> iterator() {
		return new FloatIterator(array);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return array.length;
	}

	@Override
	public Object[] toArray() {
		Float[] copy = new Float[array.length];
		for(int index = 0; index < array.length; index++) {
			copy[index] = array[index];
		}
		return copy;
	}

	@Override
	public <T> T[] toArray(T[] a) {
        if (a.length < array.length) {
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(toArray(), array.length, a.getClass());
        }
        System.arraycopy(toArray(), 0, a, 0, array.length);
        if (a.length > array.length)
            a[array.length] = null;
        return a;
	}

	public class FloatIterator implements Iterator<Float> {
		float[] array;
		int pos = -1;
		
		protected FloatIterator(float[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return pos < array.length - 1;
		}

		@Override
		public Float next() {
			if(pos < array.length - 1) {
				return array[++pos];
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
