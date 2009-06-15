package org.acoveo.tools;

public class AttributeTools {
	/** This method is a helper for deciding whether to update a property
	 * 
	 * This helper method can be used to decide whether to
	 * update a property or not in order to prevent unnecessary calls to
	 * expensive methods.
	 * The method checks if the new property object has a different address
	 * and if the new value is unequal {@code Object#equals(Object)) to the
	 * old property.
	 * @param currValue The current value of the property (can be null)
	 * @param newValue The new value of the property (can be null)
	 * @return Whether the property has actually changed an should be updated
	 */
	public static boolean shouldUpdate(Object currValue, Object newValue) {
		if(currValue == newValue || currValue != null && currValue.equals(newValue)) {
			return false;
		}
		return true;
	}
}
