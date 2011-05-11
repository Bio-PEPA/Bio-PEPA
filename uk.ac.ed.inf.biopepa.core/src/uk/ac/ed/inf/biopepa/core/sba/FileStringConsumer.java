package uk.ac.ed.inf.biopepa.core.sba;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileStringConsumer implements StringConsumer {
    private BufferedWriter outWriter;
    private String filename;
    private static String lineTerminator = System.getProperty("line.separator");
    
	public FileStringConsumer (String filename){
    	this.filename = filename;
	}
	
	public void append(String s) throws IOException {
		this.outWriter.write(s);
	}

	public void appendLine(String s) throws IOException {
		this.outWriter.write(s);
		this.outWriter.write(lineTerminator);
	}
	
	public void endLine() throws IOException {
		this.outWriter.write(lineTerminator);
	}

	public void closeStringConsumer() throws IOException {
		this.outWriter.close();
	}

	public void openStringConsumer() throws IOException {
		// Create file 
	    FileWriter fstream = new FileWriter(this.filename);
	    this.outWriter = new BufferedWriter(fstream);
	}

}
