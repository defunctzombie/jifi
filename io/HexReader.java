package io;

import java.io.*;
import java.util.*;

public abstract class HexReader
{
	public static HexFile parse(String fileName) throws IOException, FileNotFoundException
	{
		Vector<HexLine> lines;
		
		BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
	
		lines = new Vector<HexLine>();
		
		/** Gather all of the lines */
		int lineNum = 1;
		while (true)
		{
			String line = fileReader.readLine();
			
			if (line == null)
			{
				break;
			}
			else
			{
				lines.add(new HexLine(line, (byte)0x00, lineNum));
			}
			
			lineNum++;
		}
		
		return new HexFile(lines);
	}
}
