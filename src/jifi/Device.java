package jifi;

public enum Device
{
	PIC18F6527 (0x13, 0x40, 0x0000c000),
	PIC18F6622 (0x13, 0x80, 0x00010000),
	PIC18F6627 (0x13, 0xc0, 0x00018000),
	PIC18F6722 (0x14, 0x00, 0x00020000),
	PIC18F8527 (0x13, 0x60, 0x0000c000),
	PIC18F8622 (0x13, 0xa0, 0x00010000),
	PIC18F8627 (0x13, 0xe0, 0x00018000),
	PIC18F8722 (0x14, 0x20, 0x00020000), //2006+ FRC controller
	PIC18F8520 (0x0b, 0x01, 0x00008000); //Vex & 2005 controller
	
	public final byte dIDl;
	public final byte dIDh;
	
	public final int codeSize;
	
	private Device(int dIDl, int dIDh, int codeSize)
	{
		this.dIDl = (byte)dIDl;
		this.dIDh = (byte)dIDh;
		
		this.codeSize = codeSize;
	}
	
	public String toString()
	{	
		return this.name();
	}
}
