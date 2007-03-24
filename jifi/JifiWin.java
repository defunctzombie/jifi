package jifi;

import gnu.io.*;
import java.util.*;
import java.io.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import io.*;

public class JifiWin extends JifiWinUI
{
	public JifiWin()
	{
		super();
		
		this.hexUpload.addMouseListener(new Upload());
		
		/* Disable uploading when nothin in the hexFileLoc field */
		this.hexFileLoc.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				if (JifiWin.this.hexFileLoc.getText().length() == 0)
				{
					JifiWin.this.hexUpload.setEnabled(false);
				}
				else
				{
					JifiWin.this.hexUpload.setEnabled(true);
				}
				
				Jifi.lastFile = JifiWin.this.hexFileLoc.getText();
			}
		});
		
		/* Terminal window button */
		this.term.addMouseListener(new MouseAdapter()
		{	
			public void mouseUp(MouseEvent e)
			{
				TermWin term = new TermWin(JifiWin.this.win);
				term.open();
			}
		});
		
		/* Verify Write checkbox behavior */
		this.verifyWrite.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Jifi.verifyWrite = JifiWin.this.verifyWrite.getSelection();
			}
		});
		
		/* Browse for a hex file */
		this.hexBrowse.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				FileDialog fd = new FileDialog(JifiWin.this.win, SWT.OPEN);
				
				fd.setFilterNames(new String[] {"User Code (.hex)", "Master Code (.bin)"});
				fd.setFilterExtensions(new String[] {"*.hex", "*.bin"});
				
				String file = "";
				if ((file = fd.open()) != null)
				{
					JifiWin.this.hexFileLoc.setText(file);
				}
			}
		});
		
		/* Port Rescan Button */
		this.rescanPorts.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				JifiWin.this.portSelector.removeAll();
				JifiWin.this.findAvailPorts();
			}
		});
		
		/* change port combo box */
		this.portSelector.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				Jifi.lastPort = JifiWin.this.portSelector.getText();
			}
		});
		
		findAvailPorts();
		setupAvailChunkSizes();
		
		this.hexFileLoc.setText(Jifi.lastFile);
		this.verifyWrite.setSelection(Jifi.verifyWrite);
	}
	
	/**
	 * Finds available ports on the system and 
	 * adds them to the port selection list.
	 */
	private void findAvailPorts()
	{
		/* List available comm ports */
		Enumeration comms = CommPortIdentifier.getPortIdentifiers();
		
		int i=0, sel = 0;
		while (comms.hasMoreElements())
		{
			CommPortIdentifier cpi = (CommPortIdentifier) comms.nextElement();
			
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL)
			{
				if (cpi.getName().equals(Jifi.lastPort))
				{
					sel = i;
				}
				
				this.portSelector.add(cpi.getName());
				
				i++;
			}
		}
		
		if (this.portSelector.getItemCount() == 0)
		{
			this.portSelector.add("no ports detected");
		}
		
		this.portSelector.select(sel);
	}
	
	private void setupAvailChunkSizes()
	{
		this.chunkSizeSelector.setMinimum(16);
		this.chunkSizeSelector.setMaximum(248);
		
		this.chunkSizeSelector.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				Jifi.chunkSize = JifiWin.this.chunkSizeSelector.getSelection();
			}
		});
		
		this.chunkSizeSelector.setSelection(Jifi.chunkSize);
	}
	
	
	private class Upload extends MouseAdapter
	{
		public void mouseUp(MouseEvent e)
		{
			/* set chunksize because of way ModifyListener works */
			Jifi.chunkSize = JifiWin.this.chunkSizeSelector.getSelection();
			
			/* upload thread/task */
			Thread upload = new Thread()
			{
				private int i;
				private int dataSize;
				private IfiComm comm;
				
				public void run()
				{
					comm = new IfiComm(Jifi.lastPort);
					
					try
					{
						comm.open();
						
						/* TODO: recognize what controller is being used and act accordingly */
						if (comm.getDevice() != Device.PIC18F8722 && 
								comm.getDevice() != Device.PIC18F8520)
						{
							throw new IOException ("Controller not of the right type");
						}
						
						/* write the hex file to the device */
						HexFile hex = HexReader.parse(Jifi.lastFile);
						
						comm.eraseMem(0x000800, hex.getDataSize());
						
						byte[][] data = hex.getChunkedData(Jifi.chunkSize);
						dataSize = data.length;
						
						win.getDisplay().asyncExec(new Runnable()
						{
							public void run()
							{
								/* Set the progress bar params */
								JifiWin.this.uploadProgress.setMaximum(dataSize);
								JifiWin.this.uploadProgress.setVisible(true);
							}
						});
						
						int firstUsedAddr = hex.getFirstUsedAddress();
						
						for (i=0; i<data.length; ++i)
						{
							win.getDisplay().syncExec(new Runnable()
							{
								public void run()
								{
									JifiWin.this.uploadProgress.setSelection(i+1);
								}
							});
							
							try
							{
								comm.writeMem(firstUsedAddr + Jifi.chunkSize*i, data[i]);
							}
							catch (IOException ioe)
							{
								//TODO redo
								System.out.println("Failed at chunk: "+ i+1 + "[" + (firstUsedAddr+251*i) + "]");
								throw new IOException ("Connection to RC was lost.\nMake Sure the cable is connected and try again.");
							}
						}
						
						comm.returnToUserCode();
					}
					catch (FileNotFoundException fnfe)
					{
						win.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								MessageBox err = new MessageBox(JifiWin.this.win, SWT.ICON_ERROR);
								err.setMessage("File not found.\nCheck that the selected file exists.");
								err.open();
							}
						});
					}
					catch (IOException ioe)
					{
						System.out.println("Internal error during upload: " + ioe.getMessage());
						
						win.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								MessageBox err = new MessageBox(JifiWin.this.win, SWT.ICON_ERROR);
								err.setMessage("Upload Failed!\nCheck RC and try again.");
								//err.setMessage(ioe.getMessage());
								err.open();
							}
						});
					}
					
					comm.close();
					
					win.getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							JifiWin.this.uploadProgress.setVisible(false);
						}
					});
				}
			};
			
			IfiComm comm = new IfiComm(JifiWin.this.portSelector.getText());
			try
			{
				comm.open();
				
				/* identify the device being used */
				Device dev = comm.getDevice();
				
				if (dev != null)
				{
					JifiWin.this.device.setText("Device: " + dev);
				}
				else
				{
					JifiWin.this.device.setText("Unknown Device");
				}
				
				device.getParent().layout();
				
				comm.close();
				
				/* perform the upload */
				upload.start();
			}
			catch (IOException ioe)
			{
				comm.close();
				
				MessageBox err = new MessageBox(JifiWin.this.win, SWT.ICON_ERROR);
				err.setMessage("RC not found.\nCheck that the RC is in program mode and connected to the right port");
				//err.setMessage(ioe.getMessage());
				err.open();
			}
		}
			
	}
}
