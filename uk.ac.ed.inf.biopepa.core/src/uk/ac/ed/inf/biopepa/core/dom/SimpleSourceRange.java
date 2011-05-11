package uk.ac.ed.inf.biopepa.core.dom;

public class SimpleSourceRange implements ISourceRange {
	private int position;
	private int length;
	private int line;
	private int column;
	public SimpleSourceRange(final int position, 
			                 final int length, 
			                 final int line, 
			                 final int column) {
		this.position = position;
		this.length = length;
		this.line = line;
		this.column = column;
	}
	
	public int getLength() {
		return length;
	}

	public String toString() {
		return "position:" + position + ",length:" + length;
	}

	public int getColumn() {
		return column;
	}

	public int getChar() {
		return position;
	}

	public int getLine() {
		return line;
	}
}
