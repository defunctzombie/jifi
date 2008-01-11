package jifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.*;

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

import io.*;

public class TermWin extends TermWinUI
{
	private SerialPort port;
	private InputStream rx;
	
	private char inChar = 0;
	private Thread rxThread;
	
	/* when true, input is discarded and term is paused */
	private boolean pauseTerm = false;
	
	private boolean rxRun = true;
	
	public TermWin(Shell parent)
	{
		super (parent);
		
		port = null;
		
		self.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent de)
			{
				rxRun = false;
				
				if (port != null)
				{
					port.close();
				}
				
				self.getParent().setEnabled(true);
			}
		});
		
		/* close the terminal */
		close.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				self.close();
			}
		});
		
		/* pause the terminal */
		pause.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				pauseTerm = pause.getSelection();
			}
		});
		
		/* clear the terminal text */
		clear.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				terminal.setText("");
			}
		});
		
		/* define the thread for receiving data */
		rxThread = new Thread()
		{
			public void run()
			{
				while (!self.isDisposed() && rxRun)
				{
					try
					{
						int buff = rx.read();
						if (buff < 0)
						{
							break;
						}
						
						inChar = (char)buff;
					}
					catch (IOException ioe)
					{
						System.out.println(ioe.getMessage());
						port.close();
						
						self.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								MessageBox err = new MessageBox(self, SWT.ICON_ERROR);
								err.setMessage("Unable to read from port.\nCheck connection and port.");
								err.open();
								
								self.close();
							}
						});
					}
					
					/* if not paused, update the terminal */
					if (!pauseTerm)
					{
						self.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								/* make sure shell still exists */
								if (!self.isDisposed())
								{
									terminal.append(String.valueOf(inChar));
								}
							}
						});
					}
				}
			}
		};
	}
	
	public void open()
	{
		self.getParent().setEnabled(false);
		
		self.pack();
		self.open();
		
		try
		{
			CommPortIdentifier pid = CommPortIdentifier.getPortIdentifier(Jifi.lastPort);
			CommPort p = pid.open(this.getClass().getName(), 2000); //timeout of 2 milliseconds
			
			if (p instanceof SerialPort)
			{
				port = (SerialPort)p;
				
				//set up the port
				port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				port.enableReceiveTimeout(100);
				
				rx = port.getInputStream();
				
				rxThread.start();
			}
		}
		catch (Exception e)
		{
			MessageBox err = new MessageBox(this.self, SWT.ICON_ERROR);
			err.setMessage("Unable to open port.\nCheck that the port is available and exists.");
			err.open();
			
			this.self.close();
		}
	}
}
