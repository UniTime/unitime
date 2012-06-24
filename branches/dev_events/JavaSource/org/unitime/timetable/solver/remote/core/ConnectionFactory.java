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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Tomas Muller
 */
public class ConnectionFactory {
	private static boolean sInitialized = false;
	private static ServerSocketFactory sServerSocketFactory = null;
	private static SocketFactory sSocketFactory = null;
	
	public static void init(Properties properties) throws Exception {
		init(properties, null);
	}
	
	public static void init(Properties properties, File defaultKeyFolder) throws Exception {
		if (sInitialized) return;
		RemoteIo.sDebug = "true".equals(properties.getProperty("tmtbl.solver.register.debug", "false"));
		RemoteIo.sZip = "true".equals(properties.getProperty("tmtbl.solver.register.zip", "true"));
		if ("false".equalsIgnoreCase(properties.getProperty("tmtbl.solver.register.ssl", "true"))) {
			sServerSocketFactory = ServerSocketFactory.getDefault();
			sSocketFactory = SocketFactory.getDefault();
			sInitialized = true;
			return;
		}
		String keyStore = properties.getProperty("tmtbl.solver.register.keystore");
		String keyStorePasswd = properties.getProperty("tmtbl.solver.register.keystore.passwd","Fh3g1H03e95kf54xZ");
		InputStream keyStoreIs = null;

		File keyFile = (keyStore!=null?new File(keyStore):defaultKeyFolder!=null?new File(defaultKeyFolder,"solver.key"):new File("solver.key"));
		if (keyFile.exists()) {
			keyStoreIs = new FileInputStream(keyFile);
		} else {
			keyStoreIs = ServerThread.class.getClassLoader().getResourceAsStream("solver.key");
		}
		
		SSLContext ctx = SSLContext.getInstance("SSL");
		
		KeyStore ks = KeyStore.getInstance("JKS");
		if (keyStoreIs!=null) {
			ks.load(keyStoreIs, keyStorePasswd.toCharArray());
		}
		
		try {
			if (keyStoreIs!=null) keyStoreIs.close();
		} catch (IOException e) {}
		
		/*
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);
		*/
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
					
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
						}
				}
		};
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, keyStorePasswd.toCharArray());
		
		ctx.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
		
		sServerSocketFactory = ctx.getServerSocketFactory();
		sSocketFactory = ctx.getSocketFactory();
		sInitialized = true;
	}
	
	public static SocketFactory getSocketFactory() throws Exception {
		if (!sInitialized) throw new Exception("Connection factory not initialized.");
		return sSocketFactory; 
	}
	public static ServerSocketFactory getServerSocketFactory() throws Exception {
		if (!sInitialized) throw new Exception("Connection factory not initialized.");
		return sServerSocketFactory;
	}
	
	public static boolean isInitialized() { return sInitialized; }
}
