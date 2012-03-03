/**
 * 
 */
package nl.thanod.evade.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author nilsdijk
 */
public class CountingOutputStream extends OutputStream
{

	private final OutputStream stream;
	private int count = 0;

	public CountingOutputStream(OutputStream stream)
	{
		this.stream = stream;
	}
	
	public int getCount(){
		return this.count;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException
	{
		this.stream.write(b);
		++this.count;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		this.stream.write(b, off, len);
		this.count += len;
	}

	@Override
	public void close() throws IOException
	{
		this.stream.close();
	}

	@Override
	public void flush() throws IOException
	{
		this.stream.flush();
	}
}
