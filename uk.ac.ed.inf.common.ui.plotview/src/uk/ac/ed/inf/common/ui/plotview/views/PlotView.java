/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotview.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.dialogs.ChartPreview;
import uk.ac.ed.inf.common.ui.plotview.views.actions.CloseTabAction;
import uk.ac.ed.inf.common.ui.plotview.views.actions.PlotViewAction;

public class PlotView extends ViewPart implements ISelectionProvider {

	public static final String ID = "uk.ac.ed.inf.common.ui.plotview.views.PlotView";

	private TabFolder folder;

	private PlotViewAction renameTabAction;

	private PlotViewAction closeTabAction;

	private PlotViewAction saveToPNGAction;

	private PlotViewAction detachAction;

	private PlotViewAction saveToCSVAction;

	private HashMap<TabItem, IChart> charts = new HashMap<TabItem, IChart>();

	private IWorkbenchPage page;

	private int figureCounter = 1;

	// /private EditAction editAction;

	/**
	 * The constructor.
	 */
	public PlotView() {
	}

	public synchronized void reveal(IChart chart) {
		this.reveal(chart, "Figure " + figureCounter++);
	}

	public synchronized void reveal(IChart chart, String name) {
		if (name == null || chart == null)
			throw new NullPointerException();
		TabItem item = new TabItem(folder, SWT.NULL);
		item.setText(name);
		Composite composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new FillLayout());

		Canvas paintCanvas = new Canvas(composite, SWT.BORDER);
		// paintCanvas.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		paintCanvas.setBackground(Display.getDefault().getSystemColor(
				SWT.COLOR_WHITE));
		ChartPreview preview = new ChartPreview(chart);
		paintCanvas.addPaintListener(preview);
		paintCanvas.addControlListener(preview);
		preview.setPreview(paintCanvas);
		preview.renderModel();
		// new ChartCanvas(chart, composite, SWT.NULL);
		item.setControl(composite);
		charts.put(item, chart);
		folder.setSelection(item);
		notify(new StructuredSelection(chart));
		// validateActions();

	}

	public synchronized void close(IChart chart) {
		TabItem itemToDispose = getTab(chart);
		if (itemToDispose != null) {
			charts.remove(itemToDispose);
			if (!itemToDispose.isDisposed())
				itemToDispose.dispose();
		}
		notify((IStructuredSelection) getSelection());
	}

	synchronized TabItem getTab(IChart chart) {
		TabItem foundItem = null;
		for (Map.Entry<TabItem, IChart> entry : charts.entrySet()) {
			if (entry.getValue() == chart) {
				foundItem = entry.getKey();
				break;

			}
		}
		return foundItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().setSelectionProvider(this);
		page = getSite().getPage();
	}

	IWorkbenchPage getPage() {
		return page;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		SelectionListener listener = new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				PlotView.this.notify((IStructuredSelection) getSelection());
			}
		};
		folder = new TabFolder(parent, SWT.NONE);
		folder.addSelectionListener(listener);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PlotView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(folder);
		folder.setMenu(menu);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(renameTabAction);
		manager.add(closeTabAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(renameTabAction);
		manager.add(detachAction);
		manager.add(closeTabAction);
		manager.add(new Separator());
		// manager.add(editAction);
		manager.add(saveToPNGAction);
		manager.add(saveToCSVAction);
	}

	private void makeActions() {
		renameTabAction = new RenameTabAction(this);
		saveToPNGAction = new SaveToPNGAction(this);
		closeTabAction = new CloseTabAction(this);
		detachAction = new DetachAction(this);
		saveToCSVAction = new SaveToCSVAction(this);
		// editAction = new EditAction(this);
		// serialiseAction = new SerialiseAction(this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		folder.setFocus();
	}

	private ArrayList<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null || !listeners.contains(listener))
			listeners.add(listener);

	}

	public ISelection getSelection() {
		int selectionIndex = this.folder.getSelectionIndex();
		if (selectionIndex == -1)
			return StructuredSelection.EMPTY;
		IChart chart = charts.get(folder.getItem(selectionIndex));
		if (chart == null)
			return StructuredSelection.EMPTY;
		return new StructuredSelection(chart);
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		listeners.remove(listener);

	}

	public void setSelection(ISelection selection) {
		if (selection == null || !(selection instanceof IStructuredSelection)
				|| selection.isEmpty())
			return;

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object chart = structuredSelection.getFirstElement();
		if (!(chart instanceof IChart))
			return;
		IChart newSelection = (IChart) chart;
		TabItem item = getTab(newSelection);
		if (item != null) {
			folder.setSelection(item);
			notify(structuredSelection);
		}
	}

	private void notify(IStructuredSelection selection) {
		for (ISelectionChangedListener l : listeners) {
			l.selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}
}