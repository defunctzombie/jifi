package jifi;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

public abstract class TermWinUI
{
	/** reference  */
	protected Shell self;
	
	private Composite mainPane;
	
	/** pause the terminal */
	protected Button pause;
	/** clear the terminal */
	protected Button clear;
	
	/** close the window */
	protected Button close;
	
	/** textbox for the terminal (rx) */
	protected Text terminal;
	
	public TermWinUI(Shell parent)
	{
		self = new Shell(parent, SWT.SHELL_TRIM);
		self.setLayout(new GridLayout());
		
		mainPane = new Composite(self, SWT.NONE);
		mainPane.setLayout(new GridLayout(3, false));
		mainPane.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createContent();
	}
	
	private void createContent()
	{
		GridData termGD = new GridData(GridData.FILL_BOTH);
		termGD.heightHint = 400;
		termGD.widthHint = 400;
		termGD.horizontalSpan = 3;
		
		terminal = new Text(mainPane, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		terminal.setLayoutData(termGD);
		
		pause = new Button(mainPane, SWT.TOGGLE);
		pause.setText("Pause");
		pause.setToolTipText("Pause Terminal");
		
		clear = new Button(mainPane, SWT.PUSH);
		clear.setText("Clear");
		clear.setToolTipText("Clear the Terminal");
		
		close = new Button(mainPane, SWT.PUSH);
		close.setText("Close");
	}
}
