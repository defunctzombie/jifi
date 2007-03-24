package io;

import java.io.*;
import java.util.*;

import jifi.*;
import gnu.io.*;

/**
 * IFI Communicator class that uses a serial port to talk to 
 * the Robot Controller with the IFI protocol. <br>
 * see: http://shtylman.com/index.php?p=18
 * @author shtylman
 */
public class IfiComm
{	
	/** output stream to write to the controller */
	private OutputStream tx;
	/* input stream to read from the controller */
	private InputStream rx;
	
	/* the serial port for the connection */
	private SerialPort port;
	
	private String comm;
	
	/**
	 * Create a new IfiComm object for IFI communication and data sending
	 * @param comm the serial port to use for the connection
	 */
	public IfiComm(String comm)
	{
		this.comm = comm;
		
		this.port = null;
		this.tx = null;
		this.rx = null;
	}
	
	/**
	 * Opens the connection for use.
	 * Must be called before performing any operation.
	 * @throws IOException if there was a failure opening the connection
	 */
	public void open() throws IOException
	{
		try
		{
			CommPortIdentifier pid = CommPortIdentifier.getPortIdentifier(comm);
			CommPort p = pid.open(this.getClass().getName(), 2000); //timeout of 2 milliseconds
			
			if (p instanceof SerialPort)
			{
				port = (SerialPort)p;
				
				//set up the port
				port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				port.enableReceiveTimeout(100);
				
				tx = port.getOutputStream();
				rx = port.getInputStream();
				
				byte[] firmware = this.getFirmwareVersion();
					
				if (firmware[0] != 0x01 && firmware[1] != 0x01)
				{
					throw new IOException("Firmware version is not v1.1");
				}
			}
			else
			{
				throw new IOException("Port is not a serial port: " + comm);
			}
		} 
		catch (PortInUseException piue)
		{
			throw new IOException("The port is in use.");
		}
		catch (NoSuchPortException nspe)
		{
			throw new IOException("Port not found:  "+ comm);
		}
		catch (UnsupportedCommOperationException ucoe)
		{
			throw new IOException("Serial Port cannot be set to the proper mode for RC communication.");
		}
	}
	
	/**
	 * Closes the connection.
	 * Should be called when the connection is no longer needed for a task
	 */
	public void close()
	{
		if (port != null)
		{
			port.close();
			port = null;
		}
	}
	
	/**
	 * Checks if the connection is open.
	 * Used internally before any operation on the port.
	 * @throws IOException if the connection is not open
	 */
	private void checkOpen() throws IOException
	{
		if (port == null)
		{
			throw new IOException("Port is not open.");
		}
	}
	
	/**
	 * @return the device being talked to, or null if not recognized
	 */
	public Device getDevice() throws IOException
	{
		checkOpen();
		
		try
		{
			/* processor ID check */
			byte[] pidRes = new byte[2];
			this.readMem(0x3ffffe, pidRes);
			
			for (Device d : Device.values())
			{
				if (d.dIDh == pidRes[0] && d.dIDl == pidRes[1])
				{
					return d;
				}
			}
		}
		catch (IOException ioe)
		{
			System.out.println("Internal error during device ID: " + ioe.getMessage());
			throw new IOException("Unable to get Device ID");
		}
		
		return null;
	}
	
	/**
	 * Tells the controller to run user code. <br>
	 * @throws IOException
	 */
	public void returnToUserCode() throws IOException
	{
		checkOpen();
		
		 /* -> 0x08 0x04
		 * <- 0xAA 0x55 0xFF 0x01 0x01 0x40 */
		
		try
		{
			this.send(new byte[]{0x08,0x40});
			
			/*TODO check for valid return */
			for (int i=0 ; i<6 ; ++i)
			{
				rx.read();
			}
		}
		catch (IOException ioe)
		{
			System.out.println("Internal Error returning to user code: " + ioe.getMessage());
			throw new IOException("Unable to leave program mode.");
		}
	}
	
	/**
	 * @return a byte array of length 2 with the firmware version
	 */
	public byte[] getFirmwareVersion() throws IOException
	{
		checkOpen();
		
		 /* -> 0x00 0x02 
		 * <- 0x00 0x02 0x01 0x01 (v1.1) */
		
		byte[] res = new byte[2];
		res[0] = res[1] = 0;
		
		try
		{
			this.send(new byte[] {0x00, 0x02});
			byte[] vres = this.receive();
			
			if (vres.length != 4 && vres[0] != 0x00 && vres[1] != 0x02)
			{
				throw new IOException ("Response is not of the expected type.");
			}
			
			res[0] = vres[2];
			res[1] = vres[3];
		}
		catch (IOException ioe)
		{
			System.out.println("Internal Error getting firmware version: " + ioe.getMessage());
			throw new IOException("Failed to get the firmware version.");
		}
		
		return res;
	}
	
	/**
	 * Erases a section of Program memory.
	 * @param length the number of bytes to erase
	 * @throws IOException
	 */
	public void eraseMem(int addr, int length) throws IOException
	{
		checkOpen();
		
		/* -> 0x09 [len low] [address] [len high]
		 * <- 0x09
		 * [len] is number of 64 byte blocks */
		
		byte[] payload = new byte[6];
		char blocks = (char)(length/64 + ((length%64 > 0) ? 1 : 0));
		
		payload[0] = 0x09;
		payload[1] = (byte)blocks;
		payload[2] = (byte)(addr & 0xff);
		payload[3] = (byte)(addr>>8 & 0xff);
		payload[4] = (byte)(addr>>16 & 0xff);
		payload[5] = (byte)(blocks>>8);
		
		try
		{
			this.send(payload);
			
			byte[] res = this.receive();
			
			if (res.length != 1 && res[0] != 0x09)
			{
				throw new IOException("Respose from erase not of expected type.");
			}
		}
		catch (IOException ioe)
		{
			System.out.println("Internal error during RC byte erase:" + ioe.getMessage());
			throw new IOException("Program Memory Erase failed.");
		}
	}
	
	/**
	 * Writes data to the controller at a specified address.
	 * @param addr
	 * @param chunk
	 * @return
	 * @throws IOException
	 */
	public boolean writeMem(int addr, byte[] data) throws IOException
	{	
		checkOpen();
		
		 /* -> 0x02 [len] [addr] [data]
		 * <- 0x02
		 * [len] is number of 8 byte blocks in data */
		
		byte[] contents = new byte[5 + data.length];
		
		contents[0] = 0x02;
		contents[1] = (byte)(data.length/8);
		contents[2] = (byte)(addr & 0xff);
		contents[3] = (byte)(addr>>8 & 0xff);
		contents[4] = (byte)(addr>>16 & 0xff);
		
		for (int i=0; i<data.length; ++i)
		{
			contents[i+5] = data[i];
		}
		
		this.send(contents);
		
		//looking for a 0f 0f 02 fe 04 return from RC
		byte[] res = this.receive();
		
		return true;
	}
	
	/**
	 * Reads a certain number of bytes from RC memory. Will try to fill
	 * the buffer. <br><br>
	 * Note: If the data goes out of range the reading will stop.
	 * @param addr the starting address to read from
	 * @param buffer the byte array to fill up with data
	 */
	public void readMem(int addr, byte[] buffer) throws IOException
	{
		checkOpen();
		
		byte[] contents = new byte[5];
		
		contents[0] = 0x01;
		contents[1] = (byte)buffer.length; //number of bytes to read
		contents[2] = (byte)(addr & 0xff);
		contents[3] = (byte)((addr>>8) & 0xff);
		contents[4] = (byte)((addr>>16) & 0xff);
		
		send(contents);
		
		/* read back the response */
		byte[] buff = this.receive();
		
		for (int i=0; i<buffer.length && (i+5)<buff.length; ++i)
		{
			buffer[i] = buff[i+5];
		}
	}
	
	/**
	 * Receives a packet from the controller.
	 * @return the payload portion of the packet, minus the checksum and any escape characters.
	 * @throws IOException
	 */
	private byte[] receive() throws IOException
	{
		checkOpen();
		
		byte packetAttemps = 0;
		boolean inPacket = false;
		
		byte b;
		
		Vector<Byte> bytes = new Vector<Byte>();
		
		while (true)
		{
			/* check for exceeded retry */
			if (packetAttemps > 5)
			{
				throw new IOException ("No packets to receive.");
			}
			
			int res = rx.read();
			
			/* if some error in reading, try again */
			if (res < 0)
			{
				packetAttemps++;
				continue;
			}
			
			b = (byte)res;
			
			if (b != 0x0f && !inPacket)
			{
				packetAttemps++;
				continue;
			}
			else if (b == 0x0f)
			{
				packetAttemps = 0;
				inPacket = true;
				continue;
			}
			
			if (b == 0x05)
			{
				b = (byte)rx.read();
			}
			else if (b == 0x04)
			{
				break;
			}
			
			bytes.add(b);
		}
		
		byte checksum = bytes.get(bytes.size() - 1);
		
		/* no checksum in result */
		byte[] result = new byte[bytes.size() - 1];
		
		for (int i=0; i<result.length; ++i)
		{
			result[i] = bytes.get(i);
		}
		
		if (checksum(result) != checksum)
		{
			throw new IOException ("Data corrupt. Invalid Checksum.");
		}
		
		return result;
	}
	
	/** Used to write a byte array to the RC. <br>
	 * @param contents the payload portion of the protocol.
	 */
	private void send(byte[] contents) throws IOException
	{
		checkOpen();
		
		try
		{
			byte cs = checksum(contents);
			
			/* SOP */
			tx.write((byte)0x0f);
			tx.write((byte)0x0f);
			
			/* send bytes, escape if needed */
			for (byte b : contents)
			{
				if (b == 0x0f || b == 0x05 || b == 0x04)
				{
					tx.write((byte)0x05);
				} 
				
				tx.write(b);
			}
			
			/* escape the checksum if needed */
			if (cs == 0x0f || cs == 0x05 || cs == 0x04)
			{
				tx.write((byte)0x05);
			}
			
			tx.write(cs); //checksum for contents
			tx.write((byte)0x04); //EOP
		}
		catch (IOException ioe)
		{
			System.out.println("Internal error during send: " + ioe.getMessage());
			throw new IOException("Unable to send packet.");
		}
	}
	
	/**
	 * Computes the checksum of a byte[] based on the IFI checksum. <br><br>
	 * This is a one byte negative sum of all the packet contents (excluding escapes, and mod 256)
	 * @param toChecksum the bytes to checksum (this needs to be free of escape characters)
	 * @return the checksum of the byte[]
	 */
	private static byte checksum(byte[] toChecksum)
	{
		byte result = 0;
		
		for(byte b : toChecksum)
		{
			result -= b;
		}
		
		return result;
	}
}
