/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.internal.CommonChart;

/**
 * A simple dialog containing a chart
 * @author mtribast
 * 
 */
public class ChartDialog extends Dialog {

	protected IChart chart;
	
	private Point initialSize;

	private Canvas paintCanvas;

	private uk.ac.ed.inf.common.ui.plotting.dialogs.ChartPreview preview;

	public ChartDialog(Shell parentShell, IChart chart) {
		this(parentShell, chart, null);
	}
	
	public ChartDialog(Shell parentShell, IChart chart, Point initialSize) {
		super(parentShell);
		Assert.isTrue(chart instanceof CommonChart);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.chart = chart;
		this.initialSize = initialSize;
	}
	
	public IChart getChart() {
		return chart;
	}
	
	@Override
	protected Point getInitialSize() {
		if (initialSize == null)
			return new Point(800,600);
		else
			return initialSize;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		paintCanvas = new Canvas( composite, SWT.BORDER );
		paintCanvas.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		paintCanvas.setBackground( Display.getDefault( )
				.getSystemColor( SWT.COLOR_WHITE ) );
		preview = new ChartPreview( chart );
		paintCanvas.addPaintListener( preview );
		paintCanvas.addControlListener( preview );
		preview.setPreview( paintCanvas );
		preview.renderModel();
		paintCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		//ChartCanvas canvas = null;
		//canvas = new ChartCanvas(chart, composite, SWT.NONE);
		//canvas.setSize(800, 600);
		//GridData data = new GridData(GridData.FILL_BOTH);
		//canvas.setLayoutData(data);
		return composite;
	}

}
