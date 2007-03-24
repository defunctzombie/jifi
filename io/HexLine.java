package io;

import java.io.*;

public class HexLine
{
	private byte byteCount;
	
	private byte type;
	
	private byte[] addr;
	
	private byte[] data;
	
	private byte checksum;
	
	public HexLine(String line, byte addrOffset, int lineNum) throws IOException
	{
		char[] buff;
		
		//first byte is the number of bytes of data on that line
		//next two bytes are the address
		//next byte is the data type
		//then the bytes (number specified by first byte)
		//lastly the checksum (negative sum of all the bytes on the line, then truncated to one byte)
		
		StringReader s = new StringReader(line);
		
		if (s.read() != ':')
		{
			throw new IOException ("Invalid hex line format");
		}
		
		/* Read the byte count */
		buff = new char[2];
		s.read(buff);
		
		byteCount = parseByte(buff);
		
		/* Read the address bytes */
		s.read(buff);
		byte addr1 = parseByte(buff);
		s.read(buff);
		byte addr2 = parseByte(buff);
		
		addr = new byte[]{addrOffset, addr1, addr2};
		
		/* Read the data type byte */
		s.read(buff);
		
		type = parseByte(buff);
		
		/* Read the data bytes */
		data = new byte[byteCount];
		
		for (int i=0; i<byteCount; ++i)
		{
			s.read(buff);
			data[i] = parseByte(buff);
		}
		
		/* Get Checksum */
		s.read(buff);
		checksum = parseByte(buff);
		
		/* Validate checksum */
		byte total = 0;
		
		total -= byteCount + addr[1] + addr[2] + type;
		for (byte b : data)
		{
			total -= b;
		}
		
		if (checksum != total)
		{
			throw new IOException ("Invalid checksum on line: " + lineNum);
		}
	}
	
	public int getAddress()
	{
		int res = (0x00ff0000&addr[0]<<16) | (0x0000ff00&addr[1]<<8) | (0x000000ff&addr[2]);
		return res;
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public byte getType()
	{
		return type;
	}
	
	private byte parseByte(char[] c)
	{
		byte result = 0;
		
		switch (c[0])
		{
			case '1':	case '2':	case '3':	
			case '4':	case '5':	case '6':
			case '7':	case '8':	case '9':
				result |= (byte)((c[0] - 48)<<4);
				break;
			case 'A':	case 'B':	case 'C':
			case 'D':	case 'E':	case 'F':
				result |= (byte)((c[0] - 55)<<4);
				break;
		}
		
		switch (c[1])
		{
			case '1':	case '2':	case '3':	
			case '4':	case '5':	case '6':
			case '7':	case '8':	case '9':
				result |= (byte)(c[1] - 48);
				break;
			case 'A':	case 'B':	case 'C':
			case 'D':	case 'E':	case 'F':
				result |= (byte)(c[1] - 55);
				break;
		}
		
		return result;
	}
}
