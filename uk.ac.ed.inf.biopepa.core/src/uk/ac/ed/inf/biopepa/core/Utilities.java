package uk.ac.ed.inf.biopepa.core;

import java.util.LinkedList;

public class Utilities {
	
	public static String intercalateStrings(LinkedList<String> strings, String separator){
		StringBuilder sb = new StringBuilder();
		// Note -1 so that we do not take the final term
		// Also if strings.size() is 0 or 1 the for loop is not
		// entered at all since 0 is not less than -1 or 0.
		for (int termIndex = 0; termIndex < strings.size() -1; termIndex++){
			sb.append(strings.get(termIndex) + separator);
		}
		sb.append(strings.getLast());
		return sb.toString();
	}
	
	/*
	 * Returns true if the given object array contains the given element
	 * or false if the given array is null or doesn't contain the element.
	 */
	public static boolean arrayContains(Object[] array, Object element){
		if (array == null){
			return false;
		}
		for (Object e : array){
			if (e.equals(element)){
				return true;
			}
		}
		return false;
	}
}
