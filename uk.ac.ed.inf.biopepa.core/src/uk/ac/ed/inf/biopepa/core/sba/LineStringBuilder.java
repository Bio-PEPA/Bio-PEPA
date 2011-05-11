package uk.ac.ed.inf.biopepa.core.sba;

public class LineStringBuilder implements StringConsumer {
	private StringBuilder stringbuilder;

	public LineStringBuilder() {
		stringbuilder = new StringBuilder();
	}

	/* We do not need to do anything to close or open a
	 * string builder.
	 */
	public void closeStringConsumer() {
		return ;
	}

	public void openStringConsumer() {
		return ;
	}

	
	public void append(String s) {
		stringbuilder.append(s);
	}

	private static String lineTerminator = System.getProperty("line.separator");

	public void appendLine(String s) {
		stringbuilder.append(s).append(lineTerminator);
	}

	/*
	 * Currently just the same as 'endLine()' however makes your code
	 * a bit clearer. We could also check to see if the current line is
	 * empty and if so end it and add a further new line.
	 */
	public void emptyLine(){
		this.endLine();
	}
	
	public void endLine() {
		stringbuilder.append(lineTerminator);
	}

	public String toString() {
		return stringbuilder.toString();
	}

	public StringBuilder underlyingStringBuilder() {
		return stringbuilder;
	}

	
}
