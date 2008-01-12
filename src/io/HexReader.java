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
				HexLine hl = new HexLine(line, (byte)0x00, lineNum);
				if (hl.getType() == 0x04 && lineNum != 1)
				{
					break;
				}
				else if (hl.getType() != 0x04)
				{
					lines.add(hl);
				}
			}
			
			lineNum++;
		}
		
		return new HexFile(lines);
	}
}
