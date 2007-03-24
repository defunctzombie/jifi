package jifi;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.TabFolder;

/** Abstract class for the JifiWin gui
 * @author shtylman
 */
public abstract class JifiWinUI
{
	protected Display disp;
	protected TabFolder content = null;
	protected Shell win = null;
	
	/** upload panel */
	private Composite uploadComposite = null;
	/** config panel */
	private Composite configComposite = null;
	
	/** hex file location */
	protected Text hexFileLoc = null;
	/** hex file browse */
	protected Button hexBrowse = null;
	/** hex upload button to upload file */
	protected Button hexUpload = null;
	
	/** upload progress bar */
	protected ProgressBar uploadProgress = null;
	
	/** terminal open */
	protected Button term = null;
	
	protected Button verifyWrite = null;
	protected Combo portSelector = null;
	protected Spinner chunkSizeSelector = null;
	protected Label chunkSizeHelp = null;
	protected Button rescanPorts = null;
	
	protected Label device = null;
	protected Label firmware = null;
	
	public JifiWinUI()
	{
		disp = new Display();
		
		win = new Shell(SWT.DIALOG_TRIM);
		win.setText("Jifi");
		win.setLayout(new GridLayout());
		
		createContent();
		
		win.pack();
	}
	
	public final void open()
	{
		win.open ();
		
		while (!win.isDisposed ())
		{
			if (!disp.readAndDispatch ())
			{
				disp.sleep();
			}
		}
		disp.dispose ();
	}

	/**
	 * Creates the tabs and the content that will go in them.
	 */
	private void createContent()
	{
		/* Content tab layout data */
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.widthHint = 450;
		
		content = new TabFolder(win, SWT.NONE);
		content.setLayoutData(gd);
		
		createUploadComposite();
		createConfigComposite();
		
		TabItem uploadTab = new TabItem(content, SWT.NONE);
		uploadTab.setText("Upload");
		uploadTab.setControl(uploadComposite);
		
		TabItem configTab = new TabItem(content, SWT.NONE);
		configTab.setText("Config");
		configTab.setControl(configComposite);
		
		GridData statusGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		statusGD.heightHint = 30;
		Composite status = new Composite(win, SWT.BORDER);
		status.setLayout(new GridLayout(2, false));
		status.setLayoutData(statusGD);
		
		device = new Label(status, SWT.NONE);
		device.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		firmware = new Label(status, SWT.NONE);
		firmware.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	}
	
	private void createUploadComposite()
	{
		GridLayout uploadLayout = new GridLayout();
		uploadLayout.numColumns = 4;
		uploadLayout.horizontalSpacing = 5;
		
		uploadComposite = new Composite(content, SWT.NONE);
		uploadComposite.setLayout(uploadLayout);
		
		GridData hexFileLocGD = new GridData();
		hexFileLocGD.grabExcessHorizontalSpace = true;
		hexFileLocGD.horizontalAlignment = GridData.FILL;
		
		hexFileLoc = new Text(uploadComposite, SWT.BORDER);
		hexFileLoc.setLayoutData(hexFileLocGD);
		
		hexBrowse = new Button(uploadComposite, SWT.NONE);
		hexBrowse.setText("...");
		
		hexUpload = new Button(uploadComposite, SWT.NONE);
		hexUpload.setText("Upload");
		hexUpload.setEnabled(false);
		
		term = new Button(uploadComposite, SWT.NONE);
		term.setText("Terminal");
		
		GridData progressGD = new GridData();
		progressGD.grabExcessHorizontalSpace = true;
		progressGD.horizontalAlignment = GridData.FILL;
		progressGD.horizontalSpan = uploadLayout.numColumns;
		
		uploadProgress = new ProgressBar(uploadComposite, SWT.NONE);
		uploadProgress.setLayoutData(progressGD);
		uploadProgress.setVisible(false);
	}
	
	private void createConfigComposite()
	{		
		GridLayout configLayout = new GridLayout();
		configLayout.numColumns = 2;
		configLayout.makeColumnsEqualWidth = true;
		
		configComposite = new Composite(content, SWT.NONE);
		configComposite.setLayout(configLayout);
		
		createConfigOptions();
	}
	
	private void createConfigOptions()
	{
		createOptions1();
		createOptions2();
	}
	
	private void createOptions1()
	{	
		Composite options = new Composite(configComposite, SWT.NONE);
		options.setLayout(new GridLayout(3, false));
		options.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		rescanPorts = new Button(options, SWT.PUSH);
		rescanPorts.setText("Rescan Ports");
		
		GridData span = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		span.horizontalSpan = 2;
		
		rescanPorts.setLayoutData(span);
		
		verifyWrite = new Button(options, SWT.CHECK);
		verifyWrite.setText("Verify Write");
		verifyWrite.setEnabled(false);
		verifyWrite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		Label portLabel = new Label(options, SWT.NONE);
		portLabel.setText("Port:");
		
		portSelector = new Combo(options, SWT.READ_ONLY);
		portSelector.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		portSelector.setLayoutData(span);
	}
	
	private void createOptions2()
	{		
		Composite options = new Composite(configComposite, SWT.NONE);
		options.setLayout(new GridLayout(3, false));
		options.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
				GridData.GRAB_HORIZONTAL | 
				GridData.VERTICAL_ALIGN_BEGINNING));
		
		Label chunkSizeLabel = new Label(options, SWT.NONE);
		chunkSizeLabel.setText("Chunk Size: ");
		
		chunkSizeSelector = new Spinner(options, SWT.BORDER);
		chunkSizeSelector.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | 
				GridData.HORIZONTAL_ALIGN_FILL | 
				GridData.VERTICAL_ALIGN_BEGINNING));
		
		chunkSizeHelp = new Label(options, SWT.NONE);
		chunkSizeHelp.setText("?");
		chunkSizeHelp.setToolTipText("Amount of bytes to send at once.\nIFI Loader sends 16, but more is faster.");
	}
}
