package jifi;

import io.HexFile;
import io.HexReader;
import io.IfiComm;

import java.io.*;

public class Jifi
{
	public static int chunkSize = 16;
	public static String lastFile = "";
	public static boolean verifyWrite = false;
	public static String lastPort = "";
	
	/** version string, follows convention:
	 * first field - major version number
	 * second field - minor version, new features but not major version
	 * last field - bug fix version
	 * 
	 * idea: odd number in second field denotes a beta/alpha
	 */
	public static String version = "0.1.0";
	
	/**
	 * @param args (optional) arg[0] is the device arg[1] is the hex file
	 */
	public static void main(String[] args)
	{
		openConfig(".jifirc");
		
		//if we have arguments...don't use a gui
		//Usage: jifi <device> <hex file>
		if (args.length == 2 && true)
		{
			String dev = args[0];
			String file = args[1];
			
			IfiComm comm = new IfiComm(Jifi.lastPort);
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
				int dataSize = data.length;
				
				int firstUsedAddr = hex.getFirstUsedAddress();
				
				for (int i=0; i<data.length; ++i)
				{
					int perc = (i+1)*100/data.length;
					
					char[] progBar = new char[30*perc/100];
					java.util.Arrays.fill(progBar, '*');
					
					System.out.printf("Uploading File: |%-30s| %d%%\r", new String(progBar), perc);
					
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
				
				System.out.printf("\n");
				
				comm.returnToUserCode();
				
				System.exit(0);
			}
			catch (FileNotFoundException fnfe)
			{
				System.out.println("File not found ex: " + fnfe.getMessage());
			}
			catch (IOException ioe)
			{
				System.out.println(ioe.getMessage());
			}
		}
		else
		{
			JifiWin win = new JifiWin();
			win.open();
		}
		
		saveConfig(".jifirc");
	}
	
	private static void openConfig(String fileName)
	{
		try
		{
			BufferedReader config = new BufferedReader(new FileReader(fileName));
			lastFile = config.readLine();
			lastPort = config.readLine();
			verifyWrite = Boolean.parseBoolean(config.readLine());
			chunkSize = Integer.parseInt(config.readLine());
			config.close();
		}
		catch (Exception e) {} // No Config file yet
	}
	
	private static void saveConfig(String fileName)
	{
		try
		{
			BufferedWriter config = new BufferedWriter(new FileWriter(fileName, false));
			config.write(lastFile + "\n");
			config.write(lastPort + "\n");
			config.write(String.valueOf(verifyWrite) + "\n");
			config.write(chunkSize + "\n");
			config.close();
		}
		catch (IOException ioe) {} // don't really care that config saving failed
	}
}