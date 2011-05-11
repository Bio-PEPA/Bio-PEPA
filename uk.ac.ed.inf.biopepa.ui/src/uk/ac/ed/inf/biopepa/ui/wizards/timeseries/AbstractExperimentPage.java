package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.LinkedList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;

public abstract class AbstractExperimentPage extends WizardPage {
	protected AbstractExperimentPage(String pageName) {
		super(pageName);
	}
	
	private String headerHelp = "";
	public void setHeader(String s){
		headerHelp = s;
	}

	/* These protected fields should be set during the constructor */
	protected NameHintPair[] experimentObjectNameHints;

	/*
	 * private BioPEPAModel model;
	 * 
	 * public Constructor (BioPEPAModel model) { super(wizardPageName);
	 * this.model = model;
	 * setTitle("Rate Variables Setup and Experimentation Page");
	 * setDescription("Set up the rate variables"); experimentObjectNames = ...
	 * ; }
	 */

	protected LinkedList<ArrayInput> arrayInputs = new LinkedList<ArrayInput>();
	public LinkedList<ArrayInput> getArrayInputs () {
		return arrayInputs;
	}
	
	/*
	 * public void addExperimentArrays (ExperimentSet experimentation){ for
	 * (ArrayInput arrayInput : arrayInputs){
	 * arrayInput.addArrayInput(experimentation); } }
	 */
	public abstract void addExperimentArrays(ExperimentSet experiment);
	
	

	public void createControl(Composite parent) {
		ScrolledComposite composite = new ScrolledComposite(parent,
				SWT.V_SCROLL | SWT.H_SCROLL);
		setControl(composite);
		// Layout

		Composite linesParent = new Composite(composite, SWT.FILL);
		GridLayout linesLayout = new GridLayout();
		linesLayout.numColumns = 7;
		linesLayout.makeColumnsEqualWidth = false;

		GridData linesGridData = new GridData();
		linesGridData.grabExcessHorizontalSpace = true;
		linesGridData.horizontalAlignment = GridData.FILL;
		linesParent.setLayoutData(linesGridData);

		linesParent.setLayout(linesLayout);

		// Create the labelled help row which spans all seven columns:
		Label labelledHelp = new Label(linesParent, SWT.BORDER);
		labelledHelp.setText(headerHelp);
		GridData labelledHelpGridData = new GridData();
		labelledHelpGridData.grabExcessHorizontalSpace = true;
		// Make it span all the columns of the lines parent.
		labelledHelpGridData.horizontalSpan = linesLayout.numColumns;
		labelledHelp.setLayoutData(labelledHelpGridData);
		
		
		// Create the first row with labels tell people what to do
		int labelStyle = SWT.SINGLE | SWT.CENTER;

		Label nameLabel = new Label(linesParent, labelStyle);
		nameLabel.setText("Name");
		Label checkBoxDummy1 = new Label(linesParent, labelStyle);
		checkBoxDummy1.setText("");
		Label commaSep = new Label(linesParent, labelStyle);
		commaSep.setText("comma separated values");
		Label checkBoxDummy2 = new Label(linesParent, labelStyle);
		checkBoxDummy2.setText("");
		Label startValue = new Label(linesParent, labelStyle);
		startValue.setText("start value");
		Label stopValue = new Label(linesParent, labelStyle);
		stopValue.setText("stop value");
		Label stepValue = new Label(linesParent, labelStyle);
		stepValue.setText("step");

		for (NameHintPair nameHint : experimentObjectNameHints) {
			ArrayInput arrayInput = new ArrayInput(linesParent, nameHint);
			arrayInputs.add(arrayInput);
		}
		linesParent.pack(true);
		composite.setContent(linesParent);
	}

	/*
	 * A simple method to create a grid data object for text object, since I
	 * think it is better to create a new one for each text object such that any
	 * changes are not then global.
	 */
	private GridData newTextGridData() {
		GridData textGridData = new GridData();
		// textGridData.widthHint = 80;
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.grabExcessHorizontalSpace = true;
		return textGridData;
	}

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent event) {
			validatePage();
		}
	};

	public void validatePage() {
		for (ArrayInput arrayInput : arrayInputs) {
			String message = arrayInput.validString();
			if (!message.equals("")) {
				this.setErrorMessage("array input for: " + arrayInput.getName()
						+ " is invalid: " + message);
				this.setPageComplete(false);
				return;
			}
		}
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

	protected class NameHintPair {
		public String name;
		public String hint;
		
		public NameHintPair (String name, String hint){
			this.name = name;
			this.hint = hint;
		}
	}
	
	public class ArrayInput {
		Label nameLabel;
		Composite parent;
		
		String name;
		String hint;

		Button listCheckBox;
		Text listText;
		Button rangeCheckBox;
		Text startText;
		Text stopText;
		Text stepText;

		public ArrayInput(Composite parent, NameHintPair nameHint) {
			int labelStyle = SWT.SINGLE | SWT.LEFT;
			int textStyle = SWT.RIGHT | SWT.BORDER;

			this.parent = parent;
			this.name   = nameHint.name;
			this.hint   = nameHint.hint;

			nameLabel = new Label(parent, labelStyle);
			nameLabel.setText(name + " (" + hint + ")");
			nameLabel.setLayoutData(new GridData());

			listCheckBox = new Button(parent, SWT.CHECK);
			listCheckBox.setEnabled(true);
			listCheckBox.setSelection(false);
			listCheckBox.addListener(SWT.Selection, listCheckBoxListener);

			listText = new Text(parent, textStyle);
			listText.setLayoutData(newTextGridData());
			listText.setText("");
			listText.addModifyListener(modifyListener);

			rangeCheckBox = new Button(parent, SWT.CHECK);
			rangeCheckBox.setEnabled(true);
			rangeCheckBox.setSelection(false);
			rangeCheckBox.addListener(SWT.Selection, rangeCheckBoxListener);
			startText = new Text(parent, textStyle);
			startText.addModifyListener(modifyListener);
			stopText = new Text(parent, textStyle);
			stopText.addModifyListener(modifyListener);
			stepText = new Text(parent, textStyle);
			stepText.addModifyListener(modifyListener);
			enableWidgets();
		}

		private Listener listCheckBoxListener = new Listener() {
			public void handleEvent(Event event) {
				if (listCheckBox.getSelection()) {
					listText.forceFocus();
					rangeCheckBox.setSelection(false);
				}
				enableWidgets();
				validatePage();
			}
		};

		private Listener rangeCheckBoxListener = new Listener() {
			public void handleEvent(Event event) {
				if (rangeCheckBox.getSelection()) {
					startText.forceFocus();
					listCheckBox.setSelection(false);
				}
				enableWidgets();
				validatePage();
			}
		};

		private void enableWidgets() {
			boolean listEnabled = listCheckBox.getSelection();
			listText.setEnabled(listEnabled);

			boolean rangeEnabled = rangeCheckBox.getSelection();
			startText.setEnabled(rangeEnabled);
			stopText.setEnabled(rangeEnabled);
			stepText.setEnabled(rangeEnabled);
		}

		public String getName() {
			return name;
		}

		private Number[] getCommaSeparatedDoubleValues(String text)
				throws Exception {
			String[] values = text.split("(\\s)*,(\\s)*");
			if (values.length == 0) {
				throw (new Exception("No comma separated values"));
			}
			Number[] list = new Number[values.length];
			for (int i = 0; i < values.length; i++) {
				double current = Double.parseDouble(values[i]);
				if (current < 0) {
					throw new Exception("zero number");
				}
				list[i] = current;
			}
			return list;
		}

		/*
		 * Allows the callers to obtain the double values without worrying about
		 * any errors. All errors should be caught before the user presses
		 * finish and not allow the user to press finish when there exists some
		 * errors.
		 */
		public Number[] getDoubleValues() {
			try {
				return obtainDoubleValues();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new Number[0];
			}
		}

		private Number[] obtainDoubleValues() throws Exception {
			if (listCheckBox.getSelection()) {
				String text = listText.getText().trim();
				Number[] list = getCommaSeparatedDoubleValues(text);
				// Could check here if list has zero length but that
				// should already be checked by getCommaSep.. 
				return list;
			}
			if (rangeCheckBox.getSelection()) {
				String startS = startText.getText().trim();
				String stopS = stopText.getText().trim();
				String stepS = stepText.getText().trim();
				double start = Double.parseDouble(startS);
				double stop = Double.parseDouble(stopS);
				double step = Double.parseDouble(stepS);

				if (start > stop) {
					throw new Exception("start value higher than stop value");
				}
				if (step > (stop - start)) {
					throw new Exception(
							"step value too high (step > (stop - start)");
				}
				if (step == 0){
					throw new Exception ("step value zero");
				}
				if (step < 0){
					throw new Exception ("step value negative");
				}

				LinkedList<Number> numbers = new LinkedList<Number>();
				for (; start <= stop; start += step) {
					numbers.add(start);
				}
				
				// I guess this should never really happen since
				// it would only do so if either of the above to
				// error conditions were met, in which case we wouldn't
				// get here.
				if (numbers.size() == 0){
					throw new Exception ("No values specified");
				}
				
				Number[] list = new Number[numbers.size()];
				for (int i = 0; i < numbers.size(); i++) {
					list[i] = numbers.get(i);
				}
				return list;
			}
			// If neither checkbox is checked then we shouldn't
			// really be here but we should return something
			// which is just the empty result.
			return new Number[0];
		}

		/*
		 * Returns the empty string in the case that the array input is valid
		 * and some error message otherwise
		 */
		public String validString() {
			try {
				obtainDoubleValues();
			} catch (Exception e) {
				return e.getMessage();
			}
			// If we haven't returned by now then we must
			// have a valid array input.
			return "";
		}

	}

}
