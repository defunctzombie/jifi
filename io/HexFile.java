package io;

import java.util.*;

public class HexFile
{
	private Vector<HexLine> data;
	
	public HexFile(Vector<HexLine> data)
	{
		this.data = data;
	}
	
	public byte[] getData()
	{
		int dataSize = getDataSize();
		
		byte[] allData = new byte[dataSize];
		int firstAddr = this.getFirstUsedAddress();
		
		Hashtable<Integer, Byte> data = new Hashtable<Integer, Byte>();
		
		for (HexLine l : this.data)
		{
			int i = l.getAddress();
			for (byte b : l.getData())
			{
				data.put(i, b);
				i++;
			}
		}
		
		for (int i=0; i<dataSize; ++i)
		{
			Object val = data.get(i + firstAddr);
			
			if (val == null)
			{
				//no data for that given address
				allData[i] = 0;
			}
			else
			{
				//add the data to our array
				allData[i] = ((Byte)val).byteValue();
			}
		}
		
		return allData;
	}
	
	public byte[][] getChunkedData(int size)
	{
		byte[] data = this.getData();
		
		int chunkSize = size;
		int chunksCount = data.length/chunkSize  + (data.length%chunkSize==0?0:1) ;
		
		byte[][] chunks = new byte[chunksCount][chunkSize];
		
		for (int i=0; i<chunksCount; ++i)
		{
			for (int j=0; j<chunkSize; ++j)
			{
				if (i*chunkSize + j > data.length-1)
				{
					chunks[i][j] = (byte)0xff;
					continue;
				}
				
				chunks[i][j] = data[i*chunkSize + j];
			}
		}
		
		return chunks;
	}
	
	public int getDataSize()
	{
		return this.getLastUsedAddress() - this.getFirstUsedAddress() + 1; //first and last are inclusive
	}
	
	public int getFirstUsedAddress()
	{
		int addr = -1;
		for (HexLine l : data)
		{
			if ((addr == -1 && l.getType() == 0x00) || (l.getType() == 0x00 && l.getAddress() < addr))
			{
				addr = l.getAddress();
			}
		}
		
		return addr;
	}
	
	public int getLastUsedAddress()
	{
		int addr = 0;
		for (HexLine l : data)
		{
			if ((l.getAddress() > addr) && (l.getType() == 0x00))
			{
				addr = l.getAddress() + l.getData().length - 1;
			}
		}
		
		return addr;
	}
}
