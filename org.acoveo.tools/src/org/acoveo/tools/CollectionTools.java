package org.acoveo.tools;

import java.util.Collection;

public class CollectionTools {
	public static <T extends Number> double sum(Collection<T> values) {
		double result = 0.0f;
		for(Number value : values) {
			result += value.doubleValue();
		}
		return result;
	}
}
