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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;

/**
 * @author Tomas Muller
 */
public class ServerClassLoader extends ClassLoader {
	private ResourceProvider iResourceProvider;
	private Hashtable iCache = new Hashtable();
	private static ServerClassLoader sInstance = null;
	
	private ServerClassLoader() {
		super(ServerClassLoader.class.getClassLoader());
	}
	
	public static ServerClassLoader getInstance() {
		if (sInstance==null)
			sInstance = new ServerClassLoader();
		return sInstance;
	}

	public void setResourceProvicer(ResourceProvider resourceProvider) {
		iResourceProvider = resourceProvider;
	}
	public ResourceProvider getResourceProvicer() {
		return iResourceProvider;
	}
	
	public URL getResource(String name) {
		URL res = getParent().getResource(name);
		if (res!=null) return res;
		
		try {
			byte[] data = (byte[])getResourceBytes(name);
			if (data!=null)
				return new URL("file",null,-1,name, new MyURLStreamHandler(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private byte[] getResourceBytes(String name) throws Exception {
		if (iResourceProvider!=null)
			return iResourceProvider.getResource(name);
		
		URL res = super.getResource(name);
		
		if (res==null) return null;
		
		byte[] buffer = new byte[1024];
		int read = 0;
		InputStream in = res.openStream();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((read=in.read(buffer))>=0)
			out.write(buffer,0,read);

		out.flush(); out.close(); in.close();
		
		return out.toByteArray();
	}

	protected Class findClass(String className) throws ClassNotFoundException {
		try {
			Class clazz = super.findClass(className);
			if (clazz!=null) return clazz;
		} catch (ClassNotFoundException e) {}
			
		if (iResourceProvider==null)
			return getParent().loadClass(className);
		
		Class clazz = (Class)iCache.get(className);
		if (clazz!=null) return clazz;
		
		try {
			byte[] classBytes = getResourceBytes(formatClassName(className));
			if (classBytes!=null) {
				clazz = defineClass(className, classBytes, 0, classBytes.length);
				if (clazz!=null)
					iCache.put(className, clazz);
				return clazz;
			}
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return super.findClass(className);
	}
	
	protected String formatClassName(String className) {
	    return className.replace('.', '/') + ".class";
	}		


	public static class MyURLConnection extends URLConnection {
		private ByteArrayInputStream iInput = null;
		
		protected MyURLConnection(byte[] data, URL url) {
			super(url);
			iInput = new ByteArrayInputStream(data);
		}
		public void connect() throws IOException {
		}
		public InputStream getInputStream() throws IOException {
			return iInput;
		}
	}
	
	public static class MyURLStreamHandler extends URLStreamHandler {
		private byte[] iData;
		public MyURLStreamHandler(byte[] data) {
			iData = data;
		}
		
		protected URLConnection openConnection(URL u) throws IOException {
			return new MyURLConnection(iData, u);
		}
		
		protected String toExternalForm(URL u) {
			try {
				File f = File.createTempFile(u.getFile().replace('/','_'),".tmp");
				f.deleteOnExit();
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(f);
					out.write(iData);
					out.flush();out.close(); out=null;
				} finally {
	        		try {
	        			if (out!=null) out.close();
	        		} catch (IOException e) {}
				}
				String ret = f.toURI().toString();
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
