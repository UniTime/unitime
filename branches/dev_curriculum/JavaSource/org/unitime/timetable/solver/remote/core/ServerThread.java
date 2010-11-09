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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 * @author Tomas Muller
 */
public class ServerThread extends Thread {
	private static ServerLogger sLogger = new ServerLogger(ServerThread.class); 
	private ControlThread iControlThread;
	private ServerSocket iServerSocket;
	private String iServerHost;
	private int iServerPort, iPort;
	private Properties iProperties;
	private Class iRemoteSolverClass = null;
	
	public String getServerHost() { return iServerHost; }
	public int getServerPort() { return iServerPort; }
	public int getPort() { return iPort; }
	public ControlThread getControlThread() { return iControlThread; }
	
	public ServerThread(Properties properties) {
		super();
		setDaemon(true);
		setName("RemoteSolver.ServerThread");
		iProperties = properties;
		iServerHost = properties.getProperty("tmtbl.solver.register.host","localhost");
		iServerPort = Integer.parseInt(properties.getProperty("tmtbl.solver.register.port","9998"));
		iPort = Integer.parseInt(properties.getProperty("tmtbl.solver.remote.port","1200"));
	}
	
	public Properties getProperties() { return iProperties; }
	
	public void close() {
		try {
			if (iServerSocket!=null)
				iServerSocket.close();
		} catch (Exception e){}
	}
	
	public void run() {
		iServerSocket = null;
		try {
			ConnectionFactory.init(iProperties);
			while (true) {
				try {
					iServerSocket = ConnectionFactory.getServerSocketFactory().createServerSocket(iPort);
					break;
				} catch (Exception e) {
					sLogger.debug("Unable to connect to port "+iPort);
					iPort++;
				}
			}
			sLogger.debug("Connected to port "+iServerSocket.getLocalPort());
			System.out.println("Connected to port "+iServerSocket.getLocalPort());
			iControlThread = new ControlThread(this);
			iControlThread.start();
			while (true) {
				Socket socket = iServerSocket.accept();
				sLogger.debug("Client "+socket.getInetAddress()+" connected.");
				Thread remoteServer = new Thread(new RemoteSolverServer(socket));
				remoteServer.setName("RemoteSolver.Server");
				remoteServer.setDaemon(true);
				remoteServer.start();
			}
		} catch (SocketException e) {
			sLogger.warn(e.getMessage());
		} catch (Exception e) {
			sLogger.error(e.getMessage(),e);
		} finally {
			if (iControlThread!=null)
				iControlThread.serverStopped();
			try {
				if (iServerSocket!=null && !iServerSocket.isClosed())
					iServerSocket.close();
			} catch (IOException e) {}
		}
		sLogger.debug("Server finished.");			
	}
	
	public void initServer(ResourceProvider provider, String url) throws Exception {
		if (iRemoteSolverClass==null) {
			ClassLoader cl = RemoteSolverServer.class.getClassLoader();
			try {
				iRemoteSolverClass = cl.loadClass("org.unitime.timetable.solver.remote.RemoteSolver");
			} catch (ClassNotFoundException ex) {
				ServerClassLoader.getInstance().setResourceProvicer(provider);
				iRemoteSolverClass = ServerClassLoader.getInstance().loadClass("org.unitime.timetable.solver.remote.RemoteSolver");
			}
		}
		
		iRemoteSolverClass.getMethod("init", new Class[] {Properties.class, String.class}).invoke(null, new Object[]{getProperties(), url});
		
		ServerLogger.setInitialized(true);
	}
    
    protected void finalize() throws Throwable {
        try {
            if (iServerSocket!=null)
                iServerSocket.close();
        }
        catch (Exception e) { }
        iServerSocket = null;
        super.finalize();
    }
	
}
