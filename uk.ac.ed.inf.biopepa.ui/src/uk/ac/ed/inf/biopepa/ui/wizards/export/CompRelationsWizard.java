package uk.ac.ed.inf.biopepa.ui.wizards.export;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uk.ac.ed.inf.biopepa.core.sba.ComponentRelationsInferer;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.views.BioPEPACompRelationsView;
import uk.ac.ed.inf.biopepa.ui.wizards.timeseries.ReactionKnockoutPage;

public class CompRelationsWizard extends Wizard {

	BioPEPAModel model;
	
	private ExportPage exportPage;
	private ReactionKnockoutPage reactionKnockoutPage;
	
	public CompRelationsWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Infer invariants for Bio-PEPA");
	}
	
	
	private class ExportPage extends WizardPage {

		protected ExportPage(String pageName) {
			super(pageName);
			this.setTitle("Infer Component Relations");
			this.setDescription("Infer the relations between components in the model");
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

		}
		
		/*
		private ModifyListener modifyListener = new ModifyListener (){
			public void modifyText(ModifyEvent arg0) {
				validate ();
			}
			
		};
		*/
		
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
		exportPage = new ExportPage("Infer component relations with the model");
		addPage (exportPage);
		LineStringBuilder sb = new LineStringBuilder();
		sb.appendLine("De-select each reaction you wish to be ignored");
		sb.appendLine("for the purposes of component relation inference");
		
		reactionKnockoutPage = new ReactionKnockoutPage(model) ;
		reactionKnockoutPage.setHeaderHelp(sb.toString());
		reactionKnockoutPage.setDefaultSelection(true);
		addPage (reactionKnockoutPage) ;
	}
	
	
			
//		
//			helpersMap = new CompRelMap ();
//			hindersMap = new CompRelMap ();
//			
//			// Set up the initial maps
//			for (SBAReaction reaction : reactions){
//				String reactionName = reaction.getName();
//				HashSet<String> helpeeSet = new HashSet<String>();
//				HashSet<String> hinderedSet = new HashSet<String>();
//				for (SBAReaction helpee : reactions){
//					if (!helpee.getName().equals(reaction.getName())){
//						if (reactionHelps(reaction, helpee)){
//							helpeeSet.add(helpee.getName());
//						}
//						if (reactionHinders(reaction, helpee)){
//							hinderedSet.add(helpee.getName());
//						}
//					}
//				}
//				helpersMap.put(reactionName, helpeeSet);
//				hindersMap.put(reactionName, hinderedSet);
//			}
//			
//			// Now we iterate through the maps for a limited number
//			// of iterations
//			int limit = 100;
//			boolean changed = true;
//			while (changed && limit-- > 0){
//				changed = false;
//				for (SBAReaction reaction : reactions){
//					String reactionName = reaction.getName();
//					HashSet<String> reactionHelps = helpersMap.get(reactionName);
//					HashSet<String> reactionHinders = hindersMap.get(reactionName);
//
//					// We cannot add directly to the 'newHelp' as we are iterating
//            		// through it as we will get a concurent modification exception.
//            		HashSet<String> newHelps = new HashSet<String>();
//            		HashSet<String> newHinders = new HashSet<String>();
//            		
//            		// For each helped reaction we work out the new set of
//            		// indirectly helped/hindered reactions
//            		for (String helped : reactionHelps){
//            			// If 'reaction' helps 'helped' and 'helped' helps
//            			// some reaction 'helpedByHelped' then 'reaction' 
//            			// indirectly helps 'helpedByHelped'
//            			for (String helpedByHelped : helpersMap.get(helped)){
//            				if (/*!helpedByHelped.equals(reactionName) && */
//            						!reactionHelps.contains(helpedByHelped)){
//            					changed = true;
//            					newHelps.add(helpedByHelped);
//            				}
//            			}
//            			// Similarly if 'reaction' helps 'helped' and
//            			// 'helped' hinders 'hinderedByHelped' then indirectly
//            			// 'reaction' hinders 'hinderedByHelped'
//            			for (String hinderedByHelped : hindersMap.get(helped)){
//            				if (/* !hinderedByHelped.equals(reactionName) && */
//            						!reactionHinders.contains(hinderedByHelped)){
//            					changed = true;
//            					newHinders.add(hinderedByHelped);
//            				}
//            			}
//            		}
//            		
//            		// Similarly for each hindered reaction we work out the
//            		// new set of reactions which are hindered/helped as indirectly
//                    for (String hindered : reactionHinders){
//                    	// If 'reaction' hinders 'hindered' and 'hindered'
//                    	// helps 'helpedByHindered' then 'reaction' indirectly
//                    	// hinders 'helpedByHindered'
//                    	for (String helpedByHindered : helpersMap.get(hindered)){
//                    		if (!helpedByHindered.equals(reactionName) &&
//                    				!reactionHinders.contains(helpedByHindered)){
//                    			changed = true;
//                    			newHinders.add(helpedByHindered);
//                    		}
//                    	}
//                    	// Similarly if 'reaction' hinders 'hindered' and 
//                    	// 'hindered' hinders 'hinderedByHindered' then indirectly
//                    	// 'reaction' helps 'hinderedByHindered'
//                    	// sort of double negative making a positive
//                    	for (String hinderedByHindered : hindersMap.get(hindered)){
//                    		if (!hinderedByHindered.equals(reactionName) &&
//                    				!reactionHelps.contains(hinderedByHindered)){
//                    			changed = true;
//                    			newHelps.add(hinderedByHindered);
//                    		}
//                    	}
//                    }
//            		
//            		// Finally for this reaction add in all the new helps and
//            		// the new hinders
//            		reactionHelps.addAll(newHelps);
//            		reactionHinders.addAll(newHinders);
//				}
//			}
//			if (limit <= 0) { 
//            	System.out.println ("Limit iterations reached");
//            }
//			
//			/*
//			 * Having worked out all the helps and hinders for each reaction
//			 * we now turn our attention to components we now calculate for
//			 * each reaction the set of components which it either helps or hinders.
//			 */
//			for (SBAReaction reaction : reactions){
//				String reactionName = reaction.getName();
//				HashSet<String> reactionHelps = helpersMap.get(reactionName);
//				HashSet<String> reactionHinders = hindersMap.get(reactionName);
//				
//				// For each reaction if it is helped by the current reaction 'reaction'
//				// then all of the products (which are actually produced) are helped
//				// by the current reaction 'reaction'
//				// (Of course 'reaction' also helps all its own products)
//				for (SBAReaction helpedReaction : reactions){
//					String helpedName = helpedReaction.getName();
//					if (reactionHelps.contains(helpedName) ||
//							reactionName.equals(helpedName)){
//						for (SBAComponentBehaviour product : helpedReaction.getProducts()){
//							if (reactionProduces(helpedReaction, product)){
//								reactionHelps.add(product.getName());
//							}
//						}
//					} // end of if
//				} // end of for 
//				// Similarly for the reaction's hindered, if a reaction is hindered
//				// by the current reaction 'reaction' and it produces a given component
//				// then 'reaction' hinders that component.
//				// (Question: should 'reaction' hinder all its own reactants 
//				// (if they are consumed)?)
//				for (SBAReaction hinderedReaction : reactions){
//					String hinderedName = hinderedReaction.getName();
//					if (reactionHinders.contains(hinderedName)){
//						for (SBAComponentBehaviour product : hinderedReaction.getProducts()){
//							if (reactionProduces(hinderedReaction, product)){
//								reactionHinders.add(product.getName());
//							}
//						}
//					} // end of if
//				} // end for
//			} // end of for (SBAReaction ..)
//			
//			/*
//			 * This has given us a mapping from reactions to components but what
//			 * we seek is a mapping from components to components.
//			 */
//			// Unfortunately this is unusable, because of similar problems to
//			// the initial component-component analysis. For now we are happy
//			// with reaction-reaction and reaction-component mappings.
//			/*
//			for (ComponentNode comp : species) {
//				String compName = comp.getName();
//				HashSet<String> compHelps = new HashSet<String>();
//				HashSet<String> compHinders = new HashSet<String>();
//				
//				for (SBAReaction reaction : reactions){
//					if (rateAffected(reaction, compName)){
//						String reactionName = reaction.getName();
//						HashSet<String> reactionHelps = helpersMap.get(reactionName);
//						HashSet<String> reactionHinders = hindersMap.get(reactionName);
//						
//						compHelps.add(reactionName);
//						compHelps.addAll(reactionHelps);
//						compHinders.addAll(reactionHinders);
//					}
//				} // end of for reaction : reactions
//				helpersMap.put(compName, compHelps);
//				hindersMap.put(compName, compHinders);
//			} // end for comp : species
//			*/
//		}
// 	}
	
	
	@Override
	public boolean performFinish() {
		ComponentRelationsInferer inferer = new ComponentRelationsInferer(model.getSBAModel(),
				reactionKnockoutPage.getSelectedReactions());
		CompRelationsJob crjob = new CompRelationsJob("Component Relations Inference");
		crjob.setCompRelationsInferer(inferer);
		crjob.schedule();
		return true;
	}

	private class CompRelationsJob extends Job {

		public CompRelationsJob(String name) {
			super(name);
		}

		private ComponentRelationsInferer inferer;
		public void setCompRelationsInferer(ComponentRelationsInferer inferer){
			this.inferer = inferer;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			inferer.computeComponentRelations();
			
			
			Runnable runnable = new Runnable() {
				public void run() {
					// String outputString = inferer.getOutputString();
					// System.out.println(outputString);
					
					BioPEPACompRelationsView invview = 
						BioPEPACompRelationsView.getDefault();
					invview.setRelationsTree(inferer.getRelationsTree());
					invview.refreshTree();
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
