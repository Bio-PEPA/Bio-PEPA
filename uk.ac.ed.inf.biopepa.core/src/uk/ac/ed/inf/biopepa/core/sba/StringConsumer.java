package uk.ac.ed.inf.biopepa.core.sba;

import java.io.IOException;

public interface StringConsumer {

	public void openStringConsumer () throws IOException;
	public void closeStringConsumer () throws IOException;
	
	public void append(String s) throws IOException ;
	public void appendLine(String s) throws IOException; 
	public void endLine() throws IOException;
}
