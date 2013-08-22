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

import java.net.Socket;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Tomas Muller
 */
public class ControlThread extends Thread implements ResourceProvider {
	private static ServerLogger sLogger = new ServerLogger(ControlThread.class);
	private ServerThread iServer;
	private Socket iSocket = null;
	private Hashtable iResourceCache = new Hashtable();
	
	public ControlThread(ServerThread server) {
		super();
		setDaemon(true);
		setName("RemoteSolver.ControlThread");
		iServer = server;
	}
	
	public boolean register() {
		Object answer = null;
		try {
			sLogger.debug("Trying to register at "+iServer.getServerHost()+":"+iServer.getServerPort());
			System.out.println("Trying to register at "+iServer.getServerHost()+":"+iServer.getServerPort());
			iSocket = ConnectionFactory.getSocketFactory().createSocket(iServer.getServerHost(),iServer.getServerPort());
			sLogger.debug("server connected");
			answer = query(new Object[]{"connect",new Integer(iServer.getPort())});
			sLogger.debug(" -- answer "+answer);
		} catch (Exception e) {
			sLogger.error("Unable to register server, reason: "+e.getMessage(),e);
		}
		if (!"ack".equals(answer)) {
			if (iSocket!=null) {
				try {
					iSocket.close();
				} catch (Exception e) {}
			}
			iSocket=null;
		}
		if (iSocket!=null) {
			sLogger.debug("server registered");
			System.out.println("Server registered.");
			return true;
		}
		return false;
	}
	
	public synchronized Object query(Object command) throws Exception {
		//System.out.println("q:"+(command instanceof Object[]?((Object[])command)[0]:command));
		Object answer = null;
		try {
			RemoteIo.writeObject(iSocket,command);
			try {
				answer = RemoteIo.readObject(iSocket);
				} catch (java.io.EOFException ex) {};
		} catch (Exception e) {
			sLogger.error("Unable to query, reason: "+e.getMessage(),e);
		}
		//System.out.println("a:"+(answer!=null && answer instanceof Object[]?((Object[])answer)[0]:answer));
		if (answer!=null && answer instanceof Exception)
			throw (Exception)answer;
		return answer;
	}
    
    public synchronized Object query(Object command, long timeout) throws Exception {
        if (timeout<=0) return query(command);
        ExecThread et = new ExecThread(command);
        et.start();
        et.join(timeout);
        if (et.isAlive()) et.interrupt();
        if (et.getAnswer()!=null && et.getAnswer() instanceof Exception)
            throw (Exception)et.getAnswer();
        return et.getAnswer();
    }
	
	public void ping() {
		sLogger.debug("ping"); long t0 = System.currentTimeMillis();
		Object answer = null;
		try {
			answer = query("ping", 10000);
		} catch (Exception e) {}
		sLogger.debug(" -- answer ("+answer+") received in "+(System.currentTimeMillis()-t0)+" ms");
		if (!"ack".equals(answer)) {
			sLogger.debug("   -- answer is not ack, disconnecting");
			try {
				iSocket.close();
			} catch (Exception e) {}
			iSocket=null;
			sLogger.debug("   -- disconnected");
		}
	}
	
	public byte[] getResource(String name) throws Exception {
		byte[] res = (byte[])iResourceCache.get(name);
		if (res==null) {
			res = (byte[])query(new Object[]{"resource",name});
			if (res!=null)
				iResourceCache.put(name, res);
		}
		return res; 
	}

	public void run() {
		try {
			while (true) {
				if (iSocket==null || iSocket.isClosed()) {
			        if (SolverTray.isInitialized()) {
			        	SolverTray.getInstance().setStatus("not connected",SolverTray.sSolverDiscIcon);
			        }
					if (register()) {
				        if (SolverTray.isInitialized()) {
				        	SolverTray.getInstance().setStatus("initializing",SolverTray.sSolverPauseIcon);
				        }
				        Properties webServerProperties = null;
				        try {
				            webServerProperties = (Properties)query("properties");
				        } catch (Exception e) {}
						RemoteSolverServer.initServer(this, (String)query("url"), webServerProperties);
				        if (SolverTray.isInitialized()) {
				        	SolverTray.getInstance().setStatus("running at "+iSocket.getLocalAddress().getHostName()+":"+iServer.getPort(),SolverTray.sSolverRunIcon);
				        }
					}
				}
				ping();
				try {
					sleep(30000);
				} catch (InterruptedException e) {
					sLogger.debug("Control thread interrupted.");
					break;
				}
			}
			if (iSocket!=null && !iSocket.isClosed()) {
				try {
					System.out.println("Server disconnected.");
					query("disconnect", 10000);
					iSocket.close();
			        if (SolverTray.isInitialized()) {
			        	SolverTray.getInstance().setStatus("disconnected",SolverTray.sSolverDiscIcon);
			        }
				} catch (Exception e) {
					sLogger.warn("Error during disconnect: "+e.getMessage(),e);
				}
			}
		} catch (Exception e) {
			sLogger.error("Unable to register and/or initialize: "+e.getMessage(),e);
	        if (SolverTray.isInitialized()) {
	        	SolverTray.getInstance().setStatus("initialization failed",SolverTray.sSolverStopIcon);
	        }
		}
	}
	
	public void serverStopped() {
		interrupt();
	}
	
	
    protected void finalize() throws Throwable {
        try {
            if (iSocket!=null)
                iSocket.close();
        }
        catch (Exception e) { }
        iSocket = null;
        super.finalize();
    }
    
    class ExecThread extends Thread {
        Object iCommand = null;
        Object iAnswer = null;
        ExecThread(Object command) {
            iCommand = command;
        }
        public void run() {
            try {
                try {
                    RemoteIo.writeObject(iSocket,iCommand);
                    try {
                        iAnswer = RemoteIo.readObject(iSocket);
                    } catch (java.io.EOFException ex) {};
                } catch (Exception e) {
                    sLogger.error("Unable to query, reason: "+e.getMessage(),e);
                    iAnswer = e;
                }
            } catch (Exception e) {
                iAnswer = e;
            }
        }
        public Object getAnswer() {
            return iAnswer;
        }
    }
}
