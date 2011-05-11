package uk.ac.ed.inf.biopepa.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.ed.inf.biopepa.core.sba.SimpleTree;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class BioPEPACompRelationsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = 
		"uk.ac.ed.inf.biopepa.ui.views.BioPEPACompRelationsView";

	private TreeViewer viewer;
	// private DrillDownAdapter drillDownAdapter;

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private SimpleTree invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput instanceof SimpleTree){
				invisibleRoot = (SimpleTree) newInput;
			}
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof SimpleTree) {
				return ((SimpleTree)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof SimpleTree) {
				return ((SimpleTree)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof SimpleTree)
				return ((SimpleTree)parent).hasChildren();
			return false;
		}
		
		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		private void initialize() {
			SimpleTree root = new SimpleTree("No analysis done");
			invisibleRoot = new SimpleTree("");
			invisibleRoot.addChild(root);
		}
		
		
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if (obj instanceof SimpleTree){
				return ((SimpleTree) obj).getName();
			} 
			// It should never not be a BioPEPATree, but just in case.
			return obj.toString();
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BioPEPACompRelationsView() {
		invview = this;
	}
	
	// The shared instance
	private static BioPEPACompRelationsView invview;	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static BioPEPACompRelationsView getDefault() {
		return invview;
	}
	
	
	private SimpleTree relationsTree;
	public void setRelationsTree (SimpleTree relations){
		this.relationsTree = relations;
	}
	
	public void refreshTree (){
		if (viewer != null){
			updateTree();
			if (treeRoot != null){
				viewer.setInput(treeRoot);
			}
		}
	}
	private SimpleTree treeRoot;
	public void updateTree (){
		if (relationsTree == null) {
			SimpleTree root = 
				new SimpleTree("Haven't performed Component Relations Inference");
			treeRoot = new SimpleTree("");
			treeRoot.addChild(root);
		} else {
			treeRoot = relationsTree;
		}
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), 
				"uk.ac.ed.inf.biopepa.ui.viewer");
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}