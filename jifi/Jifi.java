package jifi;

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
	 * @param args arg[0] is the device arg[1] is the hex file
	 */
	public static void main(String[] args)
	{
		openConfig(".jifirc");
		
		JifiWin win = new JifiWin();
		win.open();
		
		saveConfig(".jifirc");
	}
	
	private static void openConfig(String fileName)
	{
		/*Open config file */
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
		/* save config file */
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