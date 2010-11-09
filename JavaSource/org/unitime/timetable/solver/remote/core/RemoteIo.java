/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.remote.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Tomas Muller
 */
public class RemoteIo {
	public static boolean sZip = true;
	public static boolean sDebug = false;
	public static long sTotalSent = 0;
	public static long sTotalRead = 0;

	public static void writeObject(Socket socket, Object obj) throws IOException {
		if (socket==null) return;
		OutputStream out = socket.getOutputStream();
		MyByteArrayOutputStream bytes = null;
		int size = 0;
		if (obj!=null) {
			bytes = new MyByteArrayOutputStream();
			ObjectOutputStream objOut = (sZip?
					new ObjectOutputStream(new GZIPOutputStream(bytes)):
						new ObjectOutputStream(bytes));
			objOut.writeObject(obj);
			objOut.flush(); objOut.close();
			size = bytes.size();
			bytes.writeTo(out);
		} else {
			out.write(new byte[]{0,0,0,0});
		}
		out.flush();
		if (sDebug) {
			sTotalSent+=size+4;
			System.out.println("  -- "+(size+4)+" bytes sent (total "+(sTotalSent/1024)+" kB)");
		}
	}
	
	public static Object readObject(Socket socket) throws IOException, ClassNotFoundException {
		if (socket==null) return null;
		InputStream in = socket.getInputStream();
		byte[] ch = new byte[4];
		int read = 0;
		while ((read += in.read(ch,read,4))<4);
		int size = (ch[0] & 0xFF)<<24 | (ch[1] & 0xFF)<<16 | (ch[2] & 0xFF)<<8 | (ch[3] & 0xFF);
		if (size<=0) return null;
		byte[] buffer = new byte[size];
		read = 0;
		while ((read += in.read(buffer,read,size-read))<size);
        ObjectInputStream objIn = (sZip?
        		new MyObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(buffer))):
        			new MyObjectInputStream(new ByteArrayInputStream(buffer)));
        Object ret = objIn.readObject();
        objIn.close();
        if (sDebug) {
        	sTotalRead+=size+4;
        	System.out.println("  -- "+(size+4)+" bytes read (total "+(sTotalRead/1024)+" kB)");
        }
        return ret;
	}
	
	public static class MyObjectInputStream extends ObjectInputStream {
		public MyObjectInputStream(InputStream is) throws IOException {
			super(is);
		}
		
		protected Class resolveClass(ObjectStreamClass clazz) throws ClassNotFoundException, IOException {
			try {
				return ServerClassLoader.getInstance().loadClass(clazz.getName());
			} catch (ClassNotFoundException e) {
				return super.resolveClass(clazz);
			}
		}
	}
	
	public static class MyByteArrayOutputStream extends ByteArrayOutputStream {
		public MyByteArrayOutputStream() {
			super();
			buf[0]=buf[1]=buf[2]=buf[3]=0;
			count=4;
		}
		
	    public void writeTo(OutputStream out) throws IOException {
	    	int size = count - 4;
			buf[0] = (byte)((size>>>24)&0xFF);
			buf[1] = (byte)((size>>>16)&0xFF);
			buf[2] = (byte)((size>>>8)&0xFF);
			buf[3] = (byte)(size&0xFF);
	    	out.write(buf, 0, count);
	    }
	    
	    public int size() {
	    	return count - 4;
	    }
	}
	
}
