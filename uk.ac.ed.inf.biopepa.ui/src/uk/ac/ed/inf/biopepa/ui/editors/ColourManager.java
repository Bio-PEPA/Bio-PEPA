/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColourManager {
	
	public static final RGB COMMENT = new RGB(100, 100, 100);
	
	// RGB QUOTED_NAME = new RGB(0,0,255);
	
	public static final RGB KEYWORD = new RGB(134, 17, 97);
	
	public static final RGB KINETIC_1 = new RGB(200, 0, 0);
	
	public static final RGB KINETIC_2 = new RGB(255, 64, 64);
	
	public static final RGB LOCATION_1 = new RGB(0, 200, 0);
	
	public static final RGB LOCATION_2 = new RGB(64, 255, 64);
	
	public static final RGB LOCATED = new RGB(255, 160, 64);
	
	public static final RGB OP = new RGB(255, 0, 255);
	
	public static final RGB PROCESS_NAME = new RGB(20, 90 , 15);
	
	public static final RGB LOWER_NAME = new RGB(160, 5, 5);
	
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	
	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext())
			 e.next().dispose();
	}
	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
