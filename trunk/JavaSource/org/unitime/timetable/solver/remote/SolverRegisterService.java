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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.base._BaseRootDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.remote.core.ConnectionFactory;
import org.unitime.timetable.solver.remote.core.RemoteIo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoUtil;
import org.unitime.timetable.util.RoomAvailability;

import net.sf.cpsolver.ifs.util.Callback;

/**
 * @author Tomas Muller
 */
public class SolverRegisterService extends Thread {
	static Log sLog = LogFactory.getLog(SolverRegisterService.class);
	private static SolverRegisterService sInstance = null;
	private ServerSocket iServerSocket = null;
	private Set iServers = new HashSet();
	public static File sBackupDir = ApplicationProperties.getRestoreFolder();
	public static File sPassivationDir = ApplicationProperties.getPassivationFolder();
	private static ShutdownHook sHook = null;
	private static int sTimeout = 300000; //5 minutes
	private static boolean sLocalSolverInitialized = false;
	private Date iStartTime = null;
	
	private SolverRegisterService() {
	    setDaemon(true);
		setName("SolverRegister.Service");
	}
	
	public static int getPort() {
		if (sInstance==null || sInstance.iServerSocket==null) return -1;
		return sInstance.iServerSocket.getLocalPort();
	}
	
	public static synchronized void startService() {
		if (sInstance!=null) stopService();
		
		sInstance = new SolverRegisterService();
		sInstance.start();
		sLog.info("service started");
	}
	
	public static synchronized void stopService() {
		if (sInstance==null) return;
		try {
            synchronized (sInstance.iServers) {
                for (Iterator i=sInstance.iServers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    execute(new DisconnectProxyCallback(server),10000);
                }
                sInstance.iServers.clear();
			}
			if (sInstance.iServerSocket!=null)
				sInstance.iServerSocket.close();
			sInstance.join(30000);
		} catch (Exception e) {
			sLog.warn("Unable to stop service, reason: "+e.getMessage(),e);
		}
		sInstance=null;
		sLog.info("service stopped");
	}
	
	public static void setupLocalSolver(String codeBase, String host, int port) {
		if (sInstance==null || sLocalSolverInitialized) return;
		synchronized (sInstance) {
			try {
				File webInfDir = new File(ApplicationProperties.getBasePath());
				File timetablingDir = webInfDir.getParentFile();
				File solverDir = new File(timetablingDir, "solver");
				File solverJnlp = new File(solverDir,"solver.jnlp");
				
		        Document document = (new SAXReader()).read(solverJnlp);
		        Element root = document.getRootElement();
		        root.attribute("codebase").setValue(codeBase+(codeBase.endsWith("/")?"":"/")+"solver");
		        
		        boolean hostSet = false, portSet = false;
		        Element resources = root.element("resources");
		        for (Iterator i=resources.elementIterator("property");i.hasNext();) {
		        	Element property = (Element)i.next();
		        	if ("tmtbl.solver.register.host".equals(property.attributeValue("name"))) {
		        		property.attribute("value").setValue(host);
		        		hostSet = true;
		        	}
		        	if ("tmtbl.solver.register.port".equals(property.attributeValue("name"))) {
		        		property.attribute("value").setValue(String.valueOf(port));
		        		portSet = true;
		        	}
		        }
		        
		        if (!hostSet) {
		        	resources.addElement("property").addAttribute("name","tmtbl.solver.register.host").addAttribute("value",host);
		        }
		        if (!portSet) {
		        	resources.addElement("property").addAttribute("name","tmtbl.solver.register.port").addAttribute("value",String.valueOf(port));
		        }

		        FileOutputStream fos = null;
		        try {
		        	fos = new FileOutputStream(solverJnlp);
		        	(new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
		        	fos.flush();fos.close();fos=null;
		        } finally {
	        		try {
	        			if (fos!=null) fos.close();
	        		} catch (IOException e) {}
		        }
			} catch (Exception e) {
				sLog.debug("Unable to alter solver.jnlp, reason: "+e.getMessage(), e);
			}
			sLocalSolverInitialized = true;
		}
	}

	public void run() {
	    iStartTime = new Date();
		try {
			ConnectionFactory.init(ApplicationProperties.getProperties(), ApplicationProperties.getDataFolder());
			try {
				while (true) {
					try {
						iServerSocket = ConnectionFactory.getServerSocketFactory().createServerSocket(
                                Integer.parseInt(ApplicationProperties.getProperty("tmtbl.solver.register.port","9998")));
						sLog.info("Service is running at "+iServerSocket.getInetAddress().getHostName()+":"+iServerSocket.getLocalPort());
						break;
					} catch (BindException e) {
						try {
							sLog.warn("Prior instance of service still running");
							Socket socket = ConnectionFactory.getSocketFactory().createSocket("localhost", 
                                    Integer.parseInt(ApplicationProperties.getProperty("tmtbl.solver.register.port","9998")));
							RemoteIo.writeObject(socket, "quit");
							Object answer = RemoteIo.readObject(socket);
							sLog.warn("quit command sent, answer: "+answer);
							socket.close();
						} catch (Exception f) {
							sLog.warn("Unable to connect to prior instance", f);
						}
						sleep(1000);
					}
				}
			} catch (IOException io) {
				sLog.error("Unable to start service, reason: "+io.getMessage(),io);
				return;
			}
			while (!iServerSocket.isClosed()) {
				try {
					Socket socket = iServerSocket.accept();
					socket.setKeepAlive(true);
					sLog.debug("Client "+socket.getInetAddress()+" connected.");
					(new Thread(new SolverConnection(socket))).start();
				} catch (Exception e) {
					if (!iServerSocket.isClosed())
						sLog.warn("Unable to accept new connection, reason:"+e.getMessage(),e);
				}
			}
		} catch (Exception e) {
			sLog.error("Unexpected exception occured, reason: "+e.getMessage(),e);
		} finally {
			try {
				if (iServerSocket!=null && !iServerSocket.isClosed())
					iServerSocket.close();
			} catch (Exception e) {
				sLog.warn("Unable to close socket, reason: "+e.getMessage(),e);
			}
		}
	}
	
	public Set getServers() {
		return iServers;
	}
	
	public static SolverRegisterService getInstance() {
		return sInstance;
	}
	
	public static class ShutdownHook extends Thread {
		public ShutdownHook() {
			setName("SolverRegister.ShutdownHook");
		}
		public void run() {
			sLog.info("shutdown");
			stopService();
		}
	}
	
	public class SolverConnection extends Thread {
		private Socket iSocket;
		private boolean iFinish = false;
		private RemoteSolverServerProxy iProxy = null;
		private long iLastPing = -1;
		 
		protected SolverConnection(Socket socket) {
			iSocket = socket;
			setName("SolverRegister.SolverConnection");
		}
		public Socket getSocket() {
			return iSocket;
		}
		public void stopConnection() {
			iFinish = true;
		}
		public void unregister() {
		    if (iProxy!=null) {
                synchronized (iServers) {
                    if (iServers.remove(iProxy))
                    sLog.info("Sever "+iProxy+" disconnected.");
                }
            }
		    iProxy = null;
		}
		public boolean isConnected() {
			return (iSocket!=null && !iSocket.isClosed());
		}
		public void run() {
			try {
				while (!iFinish) {
					Object command = null;
					try {
						command = RemoteIo.readObject(iSocket);
					} catch (java.io.EOFException ex) {};
					if (command==null) continue;
					Object ret = null;
					try {
						ret = answer(command);
					} catch (Exception e) {
						ret = e;
					}
					RemoteIo.writeObject(iSocket,ret);
				}
			} catch (Exception e) {
				sLog.error(e.getMessage(),e);
			}
			unregister();
			try {
				iSocket.close();
			} catch (Exception e) {}
		}
		
		public long lastActive() {
			return (System.currentTimeMillis()-iLastPing);
		}
		
		public boolean isActive() {
			return (lastActive()<sTimeout);
		}
		
		private Object answer(Object command) throws Exception {
			if ("quit".equals(command)) {
				stopConnection();
				stopService();
				return "ack";
			}
			if ("ping".equals(command)) {
				iLastPing = System.currentTimeMillis();
				if (iProxy!=null && !iServers.contains(iProxy)) {
				    sLog.warn("Server "+iProxy+" is alive, but it is not registered.");
				    iServers.add(iProxy);
				}
				return "ack";
			}
			if ("url".equals(command)) {
				return HibernateUtil.getConnectionUrl();
			}
			if ("properties".equals(command)) {
			    return ApplicationProperties.getProperties();
			}
			if ("disconnect".equals(command)) {
			    unregister();
				stopConnection();
				return "ack";
			}
			if (command instanceof Object[]) {
				Object cmd[] = (Object[])command;
				if ("connect".equals(cmd[0])) {
					int port = ((Integer)cmd[1]).intValue();
					iProxy = new RemoteSolverServerProxy(iSocket.getInetAddress(),port, this);
					sLog.debug("Sever "+iProxy+" connected.");
                    synchronized (iServers) {
                        if (iServers.contains(iProxy)) {
                            sLog.warn("Previous run of the server "+iProxy+" was not properly disconnected.");
                            for (Iterator i=iServers.iterator();i.hasNext();) {
                                RemoteSolverServerProxy oldProxy = (RemoteSolverServerProxy)i.next();
                                if (oldProxy.equals(iProxy)) {
                                    try {
                                        execute(new DisconnectProxyCallback(oldProxy),10000);
                                    } catch (Exception e) {}
                                }
                            }
                            iServers.remove(iProxy);
                        }
                        iServers.add(iProxy);
                    }
					return "ack";
					
				}
				if ("saveToFile".equals(cmd[0])) {
					TimetableInfoUtil.getInstance().saveToFile((String)cmd[1],(TimetableInfo)cmd[2]);
					return "ack";
				}
				if ("loadFromFile".equals(cmd[0])) {
					return TimetableInfoUtil.getInstance().loadFromFile((String)cmd[1]);
				}
                if ("deleteFile".equals(cmd[0])) {
                    TimetableInfoUtil.getInstance().deleteFile((String)cmd[1]);
                    return "ack";
                }
				if ("resource".equals(cmd[0])) {
					URL resource = SolverRegisterService.class.getClassLoader().getResource((String)cmd[1]);
					if (resource==null) return null;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = resource.openStream();
					byte[] buffer = new byte[1024];
					int read = 0;
					while ((read=in.read(buffer))>=0)
						out.write(buffer,0,read);
					out.flush();out.close();in.close();
					return out.toByteArray();
				}
                if ("refreshSolution".equals(cmd[0])) {
                    try {
                        Solution.refreshSolution((Long)cmd[1]);
                    } finally {
                        _BaseRootDAO.closeCurrentThreadSessions();
                    }
                    return null;
                }
                if ("refreshExamSolution".equals(cmd[0])) {
                    try {
                        ExamType.refreshSolution((Long)cmd[1], (Long)cmd[2]);
                    } finally {
                        _BaseRootDAO.closeCurrentThreadSessions();
                    }
                    return null;
                }
                if ("hasRoomAvailability".equals(cmd[0])) {
                    return new Boolean(RoomAvailability.getInstance()!=null);
                }
                if ("activateRoomAvailability".equals(cmd[0])) {
                    if (RoomAvailability.getInstance()!=null) {
                        RoomAvailability.getInstance().activate(
                                new SessionDAO().get((Long)cmd[1]),
                                (Date)cmd[2],
                                (Date)cmd[3],
                                (String)cmd[4],
                                "true".equals(ApplicationProperties.getProperty("tmtbl.room.availability.solver.waitForSync","true")));
                        return "ack";
                    }
                    return null;
                }
                if ("getRoomAvailability".equals(cmd[0])) {
                    if (RoomAvailability.getInstance()!=null) {
                        return RoomAvailability.getInstance().getRoomAvailability(
                                new LocationDAO().get((Long)cmd[1]),
                                (Date)cmd[2],
                                (Date)cmd[3],
                                (String)cmd[4]);
                    }
                    return null;
                }
                if ("getRoomAvailabilityTimeStamp".equals(cmd[0])) {
                    if (RoomAvailability.getInstance()!=null) {
                        return RoomAvailability.getInstance().getTimeStamp(
                                (Date)cmd[1],
                                (Date)cmd[2],
                                (String)cmd[3]);
                    }
                    return null;
                }
			}
			sLog.warn("Unknown command "+command);
			return null;
		}
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

    public static void addShutdownHook() {
		if (sHook!=null) removeShutdownHook();
		sHook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(sHook);
	}
	
	public static void removeShutdownHook() {
		if (sHook==null) return;
		Runtime.getRuntime().removeShutdownHook(sHook);
		sHook = null;
	}
	
    public static Object execute(Callback callback, long timeout) throws Exception {
        Exec ex = new Exec(callback);
        if (timeout<=0) {
            ex.run();
        } else {
            Thread et = new Thread(ex);
            et.start();
            et.join(timeout);
            if (et.isAlive()) et.interrupt();
        }
        if (ex.getAnswer()!=null && ex.getAnswer() instanceof Exception)
            throw (Exception)ex.getAnswer();
        return ex.getAnswer();
    }
    
    static class Exec implements Runnable {
        Callback iCallback = null;
        Object iAnswer = null;
        Exec(Callback callback) {
            iCallback = callback;
        }
        public void run() {
            try {
                iCallback.execute();
                iAnswer = null;
            } catch (Exception e) {
                sLog.error("Unable to execute a callback, reason: "+e.getMessage(),e);
                iAnswer = e;
            }
        }
        public Object getAnswer() {
            return iAnswer;
        }
    }
    
    static class DisconnectProxyCallback implements Callback {
        RemoteSolverServerProxy iProxy = null;
        DisconnectProxyCallback(RemoteSolverServerProxy proxy) {
            iProxy = proxy;
        }
        public void execute() {
            iProxy.disconnectProxy();
        }
    }
    
    public Date getStartTime() { return iStartTime; }
}
