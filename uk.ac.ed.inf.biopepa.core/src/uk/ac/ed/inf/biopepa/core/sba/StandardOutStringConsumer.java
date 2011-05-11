package uk.ac.ed.inf.biopepa.core.sba;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class StandardOutStringConsumer implements StringConsumer {
    private Writer outWriter;
    private static String lineTerminator = System.getProperty("line.separator");

	
	public void openStringConsumer() throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		this.outWriter = new BufferedWriter(osw);

	}

	public void closeStringConsumer() throws IOException {
		outWriter.flush();
	}

	public void append(String s) throws IOException {
		outWriter.write(s);
	}

	public void appendLine(String s) throws IOException {
		outWriter.write(s);
		outWriter.write(lineTerminator);
	}

	public void endLine() throws IOException {
		outWriter.write(lineTerminator);
	}

}
