package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.ed.inf.biopepa.core.Utilities;
import uk.ac.ed.inf.biopepa.core.analysis.IntegerMatrix;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.sba.InvariantInferer;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.views.BioPEPAInvariantsView;
import uk.ac.ed.inf.biopepa.ui.wizards.timeseries.ReactionKnockoutPage;

public class InferInvariantsWizard extends Wizard {

	BioPEPAModel model;
	
	private ExportPage exportPage;
	private ReactionKnockoutPage reactionKnockoutPage;
	
	public InferInvariantsWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Infer invariants for Bio-PEPA");
	}
	
	
	private class ExportPage extends WizardPage {

		protected ExportPage(String pageName) {
			super(pageName);
			this.setTitle("Infer Invariants");
			this.setDescription("Infer the invariants for the model");
		}

		private Button stateInvariantsCheck;
		private Button activityInvariantsCheck;
		// private Button uncoveredComponentsCheck;
		public void createControl(Composite parent) {
			// int textStyle = SWT.RIGHT | SWT.BORDER;
			int labelStyle = SWT.SINGLE | SWT.LEFT;
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			
			setControl(composite);

			/* Just a small label to say what to do */
			Label tmpLabel = new Label(composite, labelStyle);
			String labelText = "Inference of invariants, you should have\n" +
			                   " the 'Invariants' view open; if not go to:\n " +
			                   "Window -> Show View -> Other -> Analysis -> Invariants";
			tmpLabel.setText(labelText);
			tmpLabel.setLayoutData(createDefaultGridData());
			
			stateInvariantsCheck = new Button (composite, SWT.CHECK);
			stateInvariantsCheck.setText("Infer State Invariants");
			stateInvariantsCheck.setSelection(true);
			stateInvariantsCheck.addListener(SWT.Selection, checkBoxListener);
			
			activityInvariantsCheck = new Button (composite, SWT.CHECK);
			activityInvariantsCheck.setText("Infer Activity Invariants");
			activityInvariantsCheck.setSelection(true);
			activityInvariantsCheck.addListener(SWT.Selection, checkBoxListener);
			
			/*
			uncoveredComponentsCheck = new Button (composite, SWT.CHECK);
			uncoveredComponentsCheck.setText("Infer components not covered by any invariant");
			uncoveredComponentsCheck.setSelection (true);
			uncoveredComponentsCheck.addListener(SWT.Selection, checkBoxListener);*/

		}
		
		public boolean getStateInvariantSelection (){
			return stateInvariantsCheck.getSelection();
		}
		
		public boolean getActivityInvariantSelection (){
			return activityInvariantsCheck.getSelection();
		}
		
		
		private ModifyListener modifyListener = new ModifyListener (){
			public void modifyText(ModifyEvent arg0) {
				validate ();
			}
			
		};
		
		private Listener checkBoxListener = new Listener() {
			public void handleEvent(Event event) {
				validate();
			}
		};

		private void validate() {
			this.setPageComplete(true);
			this.setErrorMessage(null);
			
			if (!stateInvariantsCheck.getSelection() 
					&& !activityInvariantsCheck.getSelection()){
				this.setErrorMessage("Must infer at least one kind of invariant");
				this.setPageComplete(false);
			}
		}

		
		private GridData createDefaultGridData() {
			/* ...with grabbing horizontal space */
			return new GridData(SWT.FILL, SWT.CENTER, true, false);
		}
	}
	
	public void addPages (){
		exportPage = new ExportPage("Infer invariants with the model");
		addPage (exportPage);
		LineStringBuilder sb = new LineStringBuilder();
		sb.appendLine("De-select each reaction you wish to be ignored");
		sb.appendLine("for the purposes of invariate inference");
		
		reactionKnockoutPage = new ReactionKnockoutPage(model) ;
		reactionKnockoutPage.setHeaderHelp(sb.toString());
		reactionKnockoutPage.setDefaultSelection(true);
		addPage (reactionKnockoutPage) ;
	}
	
	
	
	
	

	@Override
	public boolean performFinish() {
		InvariantInferer inferer = new InvariantInferer(model.getSBAModel(),
				reactionKnockoutPage.getSelectedReactions());
		InvariantJob ijob = new InvariantJob("Invariants Inference");
		ijob.setDoStateInvariants(exportPage.getStateInvariantSelection());
		ijob.setDoActivityInvariants(exportPage.getActivityInvariantSelection());
		ijob.setInvariantInferer(inferer);
		ijob.schedule();
		return true;
	}

	private class InvariantJob extends Job {

		public InvariantJob(String name) {
			super(name);
		}

		private InvariantInferer inferer;
		public void setInvariantInferer(InvariantInferer inferer){
			this.inferer = inferer;
		}
		private boolean doStateInvariants = true;
		public void setDoStateInvariants (boolean b){
			this.doStateInvariants = b;
		}
		private boolean doActivityInvariants = true;
		public void setDoActivityInvariants (boolean b){
			this.doActivityInvariants = b;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			inferer.computeModelMatrix();
			final LinkedList<String> stateInvariantStrings = 
				doStateInvariants ? inferer.getStateInvariantStrings() : null;
			final LinkedList<String> uncoveredComponentStrings =
				doStateInvariants ? inferer.getUncoveredStateStrings() : null;
			final LinkedList<String> activityInvariantStrings = 
				doActivityInvariants ? inferer.getActivityInvariantStrings() : null;
			final LinkedList<String> uncoveredActivityStrings =
				doActivityInvariants ? inferer.getUncoveredActivityStrings() : null;
			
			
			Runnable runnable = new Runnable() {
				public void run() {
					BioPEPAInvariantsView invview = BioPEPAInvariantsView.getDefault();
					invview.clearInvariants();

					if (doStateInvariants) {
						for (String invariant : stateInvariantStrings) {
							invview.addInvariant(invariant);
							// System.out.println(invariant);
						}
						if (stateInvariantStrings.isEmpty()) {
							invview.addInvariant("No state invariants in this model");
						} else if (!uncoveredComponentStrings.isEmpty()) {
							String uncovered = "The following components are not " + "covered by any invariant: "
									+ Utilities.intercalateStrings(uncoveredComponentStrings, ", ");
							invview.addInvariant(uncovered);
							// System.out.println(uncovered);
						}

					}
					if (doActivityInvariants) {
						for (String invariant : activityInvariantStrings) {
							invview.addInvariant(invariant);
							// System.out.println(invariant);
						}
						if (activityInvariantStrings.isEmpty()) {
							invview.addInvariant("No activity invariants in this model");
						} else if (!uncoveredActivityStrings.isEmpty()) {
							String uncovered = "The following reactions are not " + "part of a loop: "
									+ Utilities.intercalateStrings(uncoveredActivityStrings, ", ");
							invview.addInvariant(uncovered);
							// System.out.println(uncovered);
						}
					}
					String viewId = invview.getViewSite().getId();
					// IWorkbenchPart wbPart = invview
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(invview);

						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			Display.getDefault().syncExec(runnable);
			
			return Status.OK_STATUS;
		}
		
	}
	
	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}
}
