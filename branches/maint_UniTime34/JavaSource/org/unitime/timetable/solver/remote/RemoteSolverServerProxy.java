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
package org.unitime.timetable.solver.remote;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxyFactory;
import org.unitime.timetable.solver.remote.SolverRegisterService.SolverConnection;
import org.unitime.timetable.solver.remote.core.ConnectionFactory;
import org.unitime.timetable.solver.remote.core.RemoteIo;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxyFactory;

import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public class RemoteSolverServerProxy {
	private static Log sLog = LogFactory.getLog(RemoteSolverServerProxy.class);
	public static final int sMaxPoolSize = 10; 
	private InetAddress iAddress;
	private int iPort;
	private Vector iSocketPool = new Vector();
	private Vector iLeasedSockets = new Vector();
	private SolverConnection iConnection;
	
	public RemoteSolverServerProxy(InetAddress address, int port, SolverConnection connection) {
		iPort = port;
		iAddress = address;
		iConnection = connection;
	}
	
	public InetAddress getAddress() {
		return iAddress;
	}
	public int getPort() {
		return iPort;
	}
	public String toString() {
		return iAddress.getHostName()+":"+iPort;
	}
	public int hashCode() {
		return toString().hashCode();
	}
	public boolean isActive() {
		return (iConnection!=null && iConnection.isActive());
	}
	public boolean equals(Object o) {
		if (o==null || !(o instanceof RemoteSolverServerProxy)) return false;
		return iAddress.equals(((RemoteSolverServerProxy)o).getAddress()) && iPort==((RemoteSolverServerProxy)o).getPort();
	}
	
	public Socket leaseConnection() throws Exception {
		synchronized (iSocketPool) {
			if (iSocketPool.isEmpty() && iSocketPool.size()+iLeasedSockets.size()<sMaxPoolSize) {
				Socket socket = ConnectionFactory.getSocketFactory().createSocket(iAddress.getHostName(),iPort);;
				sLog.debug("-- connection "+this+"@"+socket.getLocalPort()+" created");
				iLeasedSockets.addElement(socket);
				return socket;
			}
			while (true) {
				if (!iSocketPool.isEmpty()) {
					Socket socket = (Socket)iSocketPool.firstElement();
					iSocketPool.removeElement(socket);
					if (socket.isClosed()) {
						socket = ConnectionFactory.getSocketFactory().createSocket(iAddress.getHostName(),iPort);;
						sLog.debug("-- connection "+this+"@"+socket.getLocalPort()+" created (reconnect)");
					}
					iLeasedSockets.addElement(socket);
					return socket;
				}
				iSocketPool.wait();
			}
		}
	}
	
	public void releaseConnection(Socket socket) throws Exception {
		synchronized (iSocketPool) {
			iSocketPool.addElement(socket);
			iLeasedSockets.removeElement(socket);
			iSocketPool.notify();
		}
	}
	
	public void disconnectProxy() {
		try {
	        iConnection.unregister();
			iConnection.stopConnection();
		} catch (Exception e) {}
		try {
			iConnection.interrupt();
		} catch (Exception e) {}
		sLog.info("server "+this+" disconnected");
		disconnect();
	}
	
	public void disconnect() {
		synchronized (iSocketPool) {
			for (Enumeration e=iSocketPool.elements();e.hasMoreElements();) {
				Socket socket = (Socket)e.nextElement();
				try {
					sLog.debug("-- connection "+this+"@"+socket.getLocalPort()+" closed");
					socket.close(); 
				} catch (Exception x) {}
			}
			iSocketPool.clear();
			for (Enumeration e=iLeasedSockets.elements();e.hasMoreElements();) {
				Socket socket = (Socket)e.nextElement();
				try {
					sLog.debug("-- connection "+this+"@"+socket.getLocalPort()+" closed");
					socket.close(); 
				} catch (Exception x) {}
			}
			iLeasedSockets.clear();
		}
	}
	
	public Object query(Object command) throws Exception {
		Socket socket = null;
		try {
			socket = leaseConnection();
			sLog.debug("-- connection ("+iLeasedSockets.size()+") "+this+"@"+socket.getLocalPort()+" leased");
			Object answer = null;
			RemoteIo.writeObject(socket,command);
			//sLog.debug("Q:"+(command instanceof Object[]?((Object[])command)[0]:command));
			try {
				answer = RemoteIo.readObject(socket);
			} catch (java.io.EOFException ex) {};
			//sLog.debug("A:"+answer);
			//disconnect();
			if (answer!=null && answer instanceof Exception) throw (Exception)answer;
			return answer;
		} catch (SocketException e) {
			disconnectProxy();
			sLog.error("Unable to query, reason: "+e.getMessage());
			return null;
		} finally {
			if (socket!=null) {
				releaseConnection(socket);
				sLog.debug("-- connection ("+iLeasedSockets.size()+") "+this+"@"+socket.getLocalPort()+" released");
			}
		}
	}
	
	public RemoteSolverProxy createSolver(String puid, DataProperties properties) throws Exception {
		query(new Object[] {"createSolver", puid, properties});
        return RemoteSolverProxyFactory.create(this,puid);
	}
	
	public SolverProxy getSolver(String puid) throws Exception {
		RemoteSolverProxy solver = RemoteSolverProxyFactory.create(this,puid);
		return (solver.exists()?solver:null);
	}
	
	public void removeSolver(String puid) throws Exception {
		RemoteSolverProxy solver = RemoteSolverProxyFactory.create(this,puid);
		if (solver.exists()) solver.dispose();
	}

	public Hashtable<String,RemoteSolverProxy> getSolvers() throws Exception {
		Set puids = (Set)query(new Object[] {"getSolvers"});
		if (puids==null) return new Hashtable();
		Hashtable solvers = new Hashtable();
		for (Iterator i=puids.iterator();i.hasNext();) {
			String puid = (String)i.next();
			solvers.put(puid,RemoteSolverProxyFactory.create(this,puid));
		}
		return solvers;
	}
	
    public Hashtable<String,ExamSolverProxy> getExamSolvers() throws Exception {
        Set puids = (Set)query(new Object[] {"getExamSolvers"});
        if (puids==null) return new Hashtable();
        Hashtable<String,ExamSolverProxy> solvers = new Hashtable();
        for (Iterator i=puids.iterator();i.hasNext();) {
            String puid = (String)i.next();
            solvers.put(puid,ExamSolverProxyFactory.create(this,puid));
        }
        return solvers;
    }

    public ExamSolverProxy getExamSolver(String puid) throws Exception {
	    if (((Boolean)query(new Object[] {"hasExamSolver", puid})).booleanValue()) {
	        return ExamSolverProxyFactory.create(this,puid);
	    } else return null;
	}
	
    public ExamSolverProxy createExamSolver(String puid, DataProperties properties) throws Exception {
        query(new Object[] {"createExamSolver", puid, properties});
        return ExamSolverProxyFactory.create(this,puid);
    }

    public Hashtable<String,StudentSolverProxy> getStudentSolvers() throws Exception {
        Set puids = (Set)query(new Object[] {"getStudentSolvers"});
        if (puids==null) return new Hashtable();
        Hashtable<String,StudentSolverProxy> solvers = new Hashtable();
        for (Iterator i=puids.iterator();i.hasNext();) {
            String puid = (String)i.next();
            solvers.put(puid,StudentSolverProxyFactory.create(this,puid));
        }
        return solvers;
    }

    public StudentSolverProxy getStudentSolver(String puid) throws Exception {
        if (((Boolean)query(new Object[] {"hasStudentSolver", puid})).booleanValue()) {
            return StudentSolverProxyFactory.create(this,puid);
        } else return null;
    }
    
    public StudentSolverProxy createStudentSolver(String puid, DataProperties properties) throws Exception {
        query(new Object[] {"createStudentSolver", puid, properties});
        return StudentSolverProxyFactory.create(this,puid);
    }


    public String getVersion() throws Exception {
		return (String)query(new Object[] {"getVersion"});
	}
	
	public Date getStartTime() throws Exception {
		return (Date)query(new Object[] {"getStartTime"});
	}

	public long getAvailableMemory() throws Exception {
		Long mem = (Long)query(new Object[] {"getAvailableMemory"});
		return (mem==null?0:mem.longValue());
	}
	
	public long getUsage() throws Exception {
		Long usage = (Long)query(new Object[] {"getUsage"});
		return (usage==null?0:usage.longValue());
	}
	
    public void startUsing() throws Exception {
        query(new Object[] {"startUsing"});
    }

    public void stopUsing() throws Exception {
        query(new Object[] {"stopUsing"});
    }

    public void shutdown() throws Exception {
		query("quit");
		disconnectProxy();
        Set servers = SolverRegisterService.getInstance().getServers();
        synchronized (servers) {
            servers.remove(this);
        }
	}

    public void kill() throws Exception {
        query("kill");
        disconnectProxy();
        Set servers = SolverRegisterService.getInstance().getServers();
        synchronized (servers) {
            servers.remove(this);
        }
    }

    protected void finalize() throws Throwable {
    	disconnectProxy();
        super.finalize();
    }
	
}
