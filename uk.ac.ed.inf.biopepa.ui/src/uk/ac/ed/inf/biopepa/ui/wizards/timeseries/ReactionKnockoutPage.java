package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.LinkedList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class ReactionKnockoutPage extends WizardPage {

	public final static String wizardPageName = "Reaction Knockout Page";
	
	private BioPEPAModel model ;
	private Button[] reactionCheckBoxes ;
	private String[] reactionNames ;
	private String headerHelp = "";
	private boolean defaultSelection = false ;
	public void setDefaultSelection (boolean b){
		this.defaultSelection = b;
	}
	
	public void setHeaderHelp(String help){
		this.headerHelp = help;
	}
	
	/* Note that this does not set the header help,
	 * you should do that yourself with
	 * page.setHeaderHelp.
	 */
	public ReactionKnockoutPage(BioPEPAModel model) {
		super(wizardPageName);
		this.model = model ;
		setTitle ("Reaction Knockout Page") ;
		setDescription("Reaction knockout page");
		setDescription("Select which reactions you wish to allow for this analysis. ");
	}
	
	public LinkedList<SBAReaction> getSelectedReactions(){
		LinkedList<SBAReaction> selectedReactions = new LinkedList<SBAReaction>();
		for (Button rCheckBox : reactionCheckBoxes){
			if(rCheckBox.getSelection()){
				selectedReactions.add((SBAReaction) rCheckBox.getData());
			}
		}
		return selectedReactions;
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		setControl(scrolledComposite) ;
		
		Composite composite = new Composite (scrolledComposite, SWT.NONE);
		int numColumns = 2;
		Layout compositeLayout= new GridLayout(numColumns, false);
		composite.setLayout(compositeLayout);
	
		
		// Layout
		GridLayout gl = new GridLayout();
		// gl.marginRight = gl.marginRight ; // + imageData.width;
		scrolledComposite.setLayout(gl);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		// gridData.horizontalIndent = 10;
		gridData.minimumWidth = SWT.DEFAULT;
		
		// Create the labelled help row which spans all seven columns:
		Label labelledHelp = new Label(composite, SWT.BORDER);
		labelledHelp.setText(headerHelp);
		GridData labelledHelpGridData = new GridData();
		labelledHelpGridData.grabExcessHorizontalSpace = true;
		// Make it span all the columns of the lines parent.
		labelledHelpGridData.horizontalSpan = numColumns;
		labelledHelp.setLayoutData(labelledHelpGridData);
		
		
		
		Button selectAll = new Button (composite, SWT.PUSH);
		selectAll.setText("Select All");
		selectAll.setEnabled(true);
		
		class SelectAll implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				setAllSelections(true);
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		selectAll.addSelectionListener(new SelectAll());
		
		Button deselectAll = new Button (composite, SWT.PUSH);
		deselectAll.setText("Deselect All");
		deselectAll.setEnabled(true);
		
		class DeselectAll implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				setAllSelections(false);
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		deselectAll.addSelectionListener(new DeselectAll());
		
		
		/* Set up the reaction check boxes */
		SBAReaction[] reactions = model.getSBAModel().getReactions();
		reactionCheckBoxes = new Button[reactions.length];
		reactionNames = new String[reactions.length];
		
		
		
		
		
		/*
		 * I did want to update the model but actually think it's
		 * more appropriate to do that at the end once all of the
		 * knockouts are finalised.
		 */
		Listener commonListener = new Listener() {
			public void handleEvent(Event event) {
			}
		};
		
		
		for (int i = 0; i < reactions.length; i++){
			SBAReaction reaction = reactions[i];
			String reactionName = reaction.getName();
			String reactionString = reaction.toString();
			Button checkBox = new Button (composite, SWT.CHECK);
			checkBox.setData(reaction);
			reactionCheckBoxes[i] = checkBox;		
			checkBox.setText(reactionString);
			reactionNames[i] = reactionName ;
			GridData checkGridData = new GridData();
			checkGridData.grabExcessHorizontalSpace = true;
			// Make it span all the columns of the lines parent.
			checkGridData.horizontalSpan = numColumns;
			checkBox.setLayoutData(checkGridData);
			checkBox.setSelection(this.defaultSelection);		
			checkBox.addListener(SWT.Selection, commonListener);
		}
		composite.pack();
		scrolledComposite.setContent(composite);
		
	}
	private void setAllSelections (boolean selection){
		for (Button rCheckBox : reactionCheckBoxes){
			rCheckBox.setSelection(selection);
		}
	}
}

