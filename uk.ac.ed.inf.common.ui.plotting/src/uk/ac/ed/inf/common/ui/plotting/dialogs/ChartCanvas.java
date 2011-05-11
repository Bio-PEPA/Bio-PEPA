/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.dialogs;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.internal.CommonChart;

/**
 * The canvas to show chart.
 * In alternative, use
 * package org.eclipse.birt.chart.examples.view.util.ChartExamples
 * and ChartPreview.
 * 
 * @author Qi Liang
 */
public class ChartCanvas extends Canvas {

    /**
     * The device render for rendering chart.
     */
    protected IDeviceRenderer render = null;

    /**
     * The chart instantce.
     */
    protected Chart chart = null;

    /**
     * The chart state.
     */
    protected GeneratedChartState state = null;

    /**
     * The image which caches the chart image to improve drawing performance.
     */
    private Image cachedImage = null;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public ChartCanvas(IChart chart, Composite parent, int style) {
        super(parent, style);

        // initialize the SWT rendering device
        try {
            PluginSettings ps = PluginSettings.instance();
            render = ps.getDevice("dv.SWT");
        } catch (ChartException ex) {
            ex.printStackTrace();
        }

        addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {

                Composite co = (Composite) e.getSource();
                final Rectangle rect = co.getClientArea();

                if (cachedImage == null) {
                    buildChart(rect);
                    drawToCachedImage(rect);
                }
                e.gc.drawImage(cachedImage,
                        0,
                        0,
                        cachedImage.getBounds().width,
                        cachedImage.getBounds().height,
                        0,
                        0,
                        rect.width,
                        rect.height);

            }
        });

        addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {
            	Composite co = (Composite) e.getSource();
                final Rectangle rect = co.getClientArea();
                buildChart(rect);
                cachedImage = null;
            }
        });
        setChart(((CommonChart) chart).getBirtChart());
    }

    /**
     * Builds the chart state. This method should be call when data is changed.
     */
    private void buildChart(Rectangle rect) {
        Point size = getSize();
        Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
        int resolution = render.getDisplayServer().getDpiResolution();
        bo.scale(72d / resolution);
        try {
            Generator gr = Generator.instance();
            if (cachedImage != null)
                cachedImage.dispose();
            cachedImage = new Image(Display.getCurrent(), rect.width,
                    rect.height);
            GC gc = new GC(cachedImage);
            render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
            state = gr.build(render.getDisplayServer(),
                    chart,
                    bo,
                    null,
                    null,
                    null);
        } catch (ChartException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Draws the chart onto the cached image in the area of the given
     * <code>Rectangle</code>.
     * 
     * @param size
     *            the area to draw
     */
    public void drawToCachedImage(Rectangle size) {
        GC gc = null;
        try {
            if (cachedImage != null)
                cachedImage.dispose();
            cachedImage = new Image(Display.getCurrent(), size.width,
                    size.height);

            gc = new GC(cachedImage);
            render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
            Generator gr = Generator.instance();

            gr.render(render, state);
        } catch (ChartException ex) {
            ex.printStackTrace();
        } finally {
            if (gc != null)
                gc.dispose();
        }
    }

    /**
     * Returns the chart which is contained in this canvas.
     * 
     * @return the chart contained in this canvas.
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Sets the chart into this canvas. Note: When the chart is set, the cached
     * image will be dopped, but this method doesn't reset the flag
     * <code>cachedImage</code>.
     * 
     * @param chart
     *            the chart to set
     */
    public void setChart(Chart chart) {
        if (cachedImage != null)
            cachedImage.dispose();

        cachedImage = null;
        this.chart = chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (cachedImage != null)
            cachedImage.dispose();
        super.dispose();
    }

}
