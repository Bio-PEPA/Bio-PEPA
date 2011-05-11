/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.*;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.ui.ImageManager;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

/**
 * 
 * @author ajduguid
 * 
 */
public class SpeciesSelectionWizardPage extends WizardPage {
	
	private class ComponentTree {
		String name;
		ComponentTree[] children = null;
		ComponentTree parent = null;
	}

	private class ComponentTreeContentProvider extends ArrayContentProvider
			implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((ComponentTree) parentElement).children;
		}

		public Object getParent(Object element) {
			return ((ComponentTree) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((ComponentTree) element).children != null;
		}
	}
	
	private class ComponentLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((ComponentTree) element).name;
		}
	}
	
	private class SpeciesFilter extends ViewerFilter {
		
		String filter = "";
		Set<ComponentTree> nonVisibleSelected = new HashSet<ComponentTree>();
		Set<ComponentTree> toCheck = new HashSet<ComponentTree>();

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			ComponentTree ct = (ComponentTree) element;
			if(ct.name.toLowerCase().indexOf(filter.toLowerCase()) != -1) {
				if(nonVisibleSelected.contains(ct)) {
					toCheck.add(ct);
					nonVisibleSelected.remove(ct);
				}
				return true;
			}
			if(ct.children != null)
				return true;
			if(checkboxTreeViewer.getChecked(ct))
				nonVisibleSelected.add(ct);
			return false;
		}
		
		void updateFilter(String name) {
			filter = name;
		}
		
		void updateSelected() {
			for(ComponentTree ct : toCheck)
				checkboxTreeViewer.setChecked(ct, true);
			toCheck.clear();
		}
		
	}

	public final static String name = "SpeciesSelection";

	CheckboxTreeViewer checkboxTreeViewer;
	private int selectionCount = 0;
	Composite composite;

	BioPEPAModel model;
	
	Map<Parameter, Object> parameters;

	Button selectAllButton;
	
	SpeciesFilter speciesFilter;

	boolean listenersUpdated = false;

	protected SpeciesSelectionWizardPage(BioPEPAModel model, Map<Parameter, Object> parameters) {
		super(name);
		this.model = model;
		this.parameters = parameters;
		setTitle("Component Selection");
		setDescription("Please select the components you wish to record during this analysis");
	}
	
	private void checkPage() {
		setPageComplete(false);
		
		boolean all = true;
		
		Stack<TreeItem> items = new Stack<TreeItem>();
		for(TreeItem ti : checkboxTreeViewer.getTree().getItems())
			items.add(ti);
		TreeItem ti;
		while(!items.empty()) {
			ti = items.pop();
			if (ti.getChecked() == false){
				all = false ;
			}
			for(TreeItem child : ti.getItems())
				items.add(child);
		}
		
		Object[] oArray = checkboxTreeViewer.getCheckedElements();
		// int items = checkboxTreeViewer.getTree().getItemCount();
		// boolean all = oArray.length == items;
		// System.out.println("---------");
		// System.out.println("items = " + items);
		// System.out.println("checked = " + oArray.length);
		selectAllButton.setText((all ? "Deselect all" : "Select all"));
		composite.layout();
		LinkedList<String> sList = new LinkedList<String>();
		ComponentTree ct;
		for(Object o : oArray) {
			ct = (ComponentTree) o;
			if(ct.parent != null)
				sList.add(ct.name);
		}
		if(sList.size() > 0) {
			setPageComplete(true);
			parameters.put(Parameter.Components, sList.toArray(new String[] {}));
		}
		// No longer required as graphing in common has been fixed
		//		if(sList.size() > 12) {
		//			setErrorMessage("The results can no longer be plotted correctly on a single graph. To view the results you will need to export once analysis is complete.");
		//		} else
		//			setErrorMessage(null);
	}

	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		// Widget generation
		final Text filterInput = new Text(composite, SWT.SINGLE | SWT.SEARCH);
		checkboxTreeViewer = new CheckboxTreeViewer(composite);
		checkboxTreeViewer.setContentProvider(new ComponentTreeContentProvider());
		checkboxTreeViewer.setLabelProvider(new ComponentLabelProvider());
		String[] names;
		int i = 0;
		ComponentTree[] components;
		ComponentTree ct;
		names = model.getDynamicVariableNames();
		if(names.length > 0) {
			selectionCount = names.length + 1;
			components = new ComponentTree[2];
			components[1] = new ComponentTree();
			components[1].name = "Variables";
			components[1].children = new ComponentTree[names.length];
			for(String s : names) {
				ct = new ComponentTree();
				ct.name = s;
				ct.parent = components[1];
				components[1].children[i++] = ct;
			}
		} else
			components = new ComponentTree[1];
		names = model.getComponentNames();
		selectionCount += names.length + 1;		
		components[0] = new ComponentTree();
		components[0].name = "Species";
		components[0].children = new ComponentTree[names.length];
		i = 0;
		for(String s : names) {
			ct = new ComponentTree();
			ct.name = s;
			ct.parent = components[0];
			components[0].children[i++] = ct;
		}
		checkboxTreeViewer.setInput(components);		
		checkboxTreeViewer.setExpandedElements(components);
		
		
		if(parameters.containsKey(Parameter.Components) ) {
			Set<String> sSet = new HashSet<String>();
			for(String s : (String[]) parameters.get(Parameter.Components))
				sSet.add(s);
			for(ComponentTree ct2 : components)
				for(ComponentTree ct3 : ct2.children)
					if(sSet.contains(ct3.name))
						checkboxTreeViewer.setChecked(ct3, true);
		} else {
			/* If we haven't save information about this then
			 * the default is to select all components.
			 */
			selectAllSpecies(true);
		}
		
		speciesFilter = new SpeciesFilter();
		checkboxTreeViewer.addFilter(speciesFilter);
		filterInput.setMessage("Filter species/variables list");
		filterInput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				speciesFilter.updateFilter(filterInput.getText());
				checkboxTreeViewer.refresh();
				speciesFilter.updateSelected();
				checkPage();
			}
		});
		Image image = ImageManager.getInstance().getImage(ImageManager.ICONS.CLEAR);
		ImageData imageData = image.getImageData();
		imageData = imageData.scaledTo(10, 10);
		image = new Image(image.getDevice(), imageData);
		ControlDecoration controlDecoration = new ControlDecoration(
				filterInput, SWT.RIGHT | SWT.TOP, composite);
		controlDecoration.setImage(image);
		controlDecoration.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				filterInput.setText("");
				speciesFilter.updateFilter("");
				checkboxTreeViewer.refresh();
				speciesFilter.updateSelected();
				checkPage();
			}
		});
		controlDecoration.setDescriptionText("Clear the filter");
		selectAllButton = new Button(composite, SWT.PUSH);
		selectAllButton.setText("Select all");
		// Listener generation
		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				ComponentTree ct = (ComponentTree) event.getElement();
				if(ct.children != null) {
					boolean state = event.getChecked();
					for(ComponentTree ct2 : ct.children)
						checkboxTreeViewer.setChecked(ct2, state);
				}
				checkPage();
			}
		});
		selectAllButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				boolean select = !selectAllButton.getText().startsWith("De");
				selectAllSpecies(select);
				checkPage();
			}
		});
		// Layout
		GridLayout gl = new GridLayout();
		gl.marginRight = gl.marginRight + imageData.width;
		composite.setLayout(gl);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		// gridData.horizontalIndent = 10;
		gridData.minimumWidth = SWT.DEFAULT;
		// Hack to force a given size of text input
		filterInput.setText("          ");
		gridData.widthHint = filterInput.getSize().x;
		filterInput.setText("");
		filterInput.setLayoutData(gridData);
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		checkboxTreeViewer.getTree().setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		selectAllButton.setLayoutData(gridData);
		setControl(composite);	
		
		// selectAllSpecies(true);
		checkPage();
	}
	
	private void selectAllSpecies(boolean select){
		// checkboxTreeViewer.setAllChecked(select); (deprecated method)
		Stack<TreeItem> items = new Stack<TreeItem>();
		for(TreeItem ti : checkboxTreeViewer.getTree().getItems())
			items.add(ti);
		TreeItem ti;
		while(!items.empty()) {
			ti = items.pop();
			ti.setChecked(select);
			for(TreeItem child : ti.getItems()){
				items.add(child);
			}
		}
		// for(TreeItem ti : checkboxTreeViewer.getTree().getItems())
			// ti.setChecked(select);
		// checkPage();
	}
}
