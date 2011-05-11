/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractCSVExporter implements ICSVExporter {

	private ByteArrayOutputStream os = null;
	
	private String fHeader = "";
	
	protected static final byte[] SEP = ", ".getBytes();

	protected static final byte[] NEW_LINE = System.getProperty("line.separator")
			.getBytes();

	
	protected AbstractCSVExporter() {
		os = new ByteArrayOutputStream();
	}
	
	public final byte[] getCSV() throws IOException {
		handleHeader(os);
		handleChart(os);
		return os.toByteArray();
	}
	
	public void setHeader(String header) {
		if (header == null)
			fHeader = "";
		else {
			fHeader = header;
		}
	}
	
	/**
	 * Generates bytes for the actual chart
	 * @param outputStream
	 */
	protected abstract void handleChart(ByteArrayOutputStream outputStream) throws IOException;
	
	protected static String format(String original) {
		return "\"" + original + "\"";
	}

	private void handleHeader(ByteArrayOutputStream baos) throws IOException {
		if(fHeader == null || fHeader == "")
			return;
		String term = System.getProperty("line.separator");
		String header = fHeader;
		if(!fHeader.startsWith("#")) {
			header = "# " + header.replaceAll(term, term + "# ") + term;
		}
		baos.write(header.getBytes());
	}
	

}
