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
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

/**
 * @author Tomas Muller
 */
public class RemoteSolverServer implements Runnable {
    private static ServerLogger sLogger = new ServerLogger(RemoteSolverServer.class);
	private static ServerThread sServerThread;
	private Socket iSocket;
	private static Class sRemoteSolverClass = null;
	
	protected RemoteSolverServer(Socket socket) {
		iSocket = socket;
	}
	
	public Socket getSocket() { return iSocket; }
	
	public static void startService(Properties properties) {
		if (sServerThread!=null)
			stopService();
		sServerThread = new ServerThread(properties);
		sServerThread.start();
	}
	
	public static void stopService() {
		try {
			if (sServerThread!=null) {
				sServerThread.close();
				sServerThread.join();
			}
		} catch (Exception e) {
			sLogger.warn("Unable to stop solver, reason: "+e.getMessage(),e);
		}
	}
	
	public static ServerThread getServerThread() {
		return sServerThread;
	}
	
	protected Object answer(Object cmd) {
		try {
			return sRemoteSolverClass.getMethod("answer",new Class[] {Object.class}).invoke(null, new Object[] {cmd});
		} catch (Exception e) {
			sLogger.warn("Exception '"+e.getMessage()+"' seen.",e);
			return e;
		}
	}
	
	public static Object query(Object cmd) throws Exception {
		return sServerThread.getControlThread().query(cmd);
	}
	
	public static class ShutdownHook extends Thread {
		public ShutdownHook() {
			setName("RemoteSolver.ShutdownHook");
		}
		public void run() {
			System.out.println("Server shutdown.");
			sLogger.info("shutdown");
			stopService();
			try {
				sRemoteSolverClass.getMethod("backupAll",new Class[]{}).invoke(null, new Object[]{});
			} catch (Exception e) {
				sLogger.error(e.getMessage(),e);
			}
			try {
				if (sRemoteSolverClass!=null)
					sRemoteSolverClass.getMethod("finish", new Class[]{}).invoke(null, new Object[]{});
			} catch (Exception e) {
				sLogger.error(e.getMessage(),e);
			}
		}
	}
	
	public static void initServer(ResourceProvider provider, String url, Properties webServerProperties) throws Exception {
		if (sServerThread==null) {
			throw new Exception("sServerThread is null!!!");
		}
		
		if (sRemoteSolverClass==null) {
			ClassLoader cl = RemoteSolverServer.class.getClassLoader();
			try {
				RemoteSolverServer.sRemoteSolverClass = cl.loadClass("org.unitime.timetable.solver.remote.RemoteSolver");
			} catch (ClassNotFoundException ex) {
				ServerClassLoader.getInstance().setResourceProvicer(provider);
				sRemoteSolverClass = ServerClassLoader.getInstance().loadClass("org.unitime.timetable.solver.remote.RemoteSolver");
			}
		}
		
		Properties properties = null;
		if (webServerProperties!=null) {
		    properties = webServerProperties;
		    properties.putAll(sServerThread.getProperties());
		} else {
		    properties = sServerThread.getProperties();
		}
		
		sRemoteSolverClass.getMethod("init", new Class[] {Properties.class, String.class}).invoke(null, new Object[]{properties, url});
		
		ServerLogger.setInitialized(true);
	}

	public void run() {
		try {
        	while (true) {
        		Object command = null;
        		try {
        			command = RemoteIo.readObject(iSocket);
        		} catch (java.io.EOFException ex) {};
        		if (command==null) break;
        		if ("disconnect".equals(command)) {
        			break;
        		}
        		if ("quit".equals(command)) {
        			RemoteIo.writeObject(iSocket,"ack");
        			stopService();
        			break;
        		}
                if ("kill".equals(command)) {
                    RemoteIo.writeObject(iSocket,"ack");
                    Runtime.getRuntime().halt(9);
                    break;
                }
        		Object ret = answer(command);
        		RemoteIo.writeObject(iSocket,ret);
        	}
        } catch (Exception e) {
            sLogger.error(e.getMessage(),e);
        }
        try {
            iSocket.close();
        } catch (Exception e) {}
	}
	
    public void destroy() {
        try {
            if (iSocket!=null)
                iSocket.close();
        }
        catch (Exception e) { }
        iSocket = null;
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

    public static void main(String[] args) {
		try {
			String solverHome = System.getProperty("tmtbl.solver.home");
			if (solverHome==null)
				solverHome = System.getProperty("user.home")+File.separator+"solver";
			
			if (System.getProperty("tmtbl.solver.backup.dir")==null)
				System.setProperty("tmtbl.solver.backup.dir",solverHome+File.separator+"backup");
			if (System.getProperty("tmtbl.solver.temp.dir")==null)
				System.setProperty("tmtbl.solver.temp.dir",solverHome+File.separator+"log");
			if (System.getProperty("tmtbl.solver.passivation.dir")==null)
				System.setProperty("tmtbl.solver.passivation.dir",solverHome+File.separator+"passivate");
			Properties properties = new Properties();
			
			ClassLoader classLoader = RemoteSolverServer.class.getClassLoader();
			URL propertiesUrl = classLoader.getResource("application.properties");
			if (propertiesUrl!=null) {
				System.out.println("Reading "+propertiesUrl+" ...");
				properties.load(propertiesUrl.openStream());
			}
            propertiesUrl = classLoader.getResource(properties.getProperty("tmtbl.custom.properties","custom.properties"));
            if (propertiesUrl!=null) {
                System.out.println("Reading "+propertiesUrl+" ...");
                properties.load(propertiesUrl.openStream());
            }
			
			if (System.getProperty("tmtbl.custom.properties")!=null) {
				FileInputStream in = null;
				try {
					System.out.println("Reading "+System.getProperty("tmtbl.custom.properties")+" ...");
					Properties x = new Properties();
					in = new FileInputStream(System.getProperty("tmtbl.custom.properties"));
					x.load(in);
					properties.putAll(x);
				} catch (Exception e) {
					System.out.println("Unable to read properties file "+System.getProperty("tmtbl.custom.properties")+", message: "+e.getMessage());
				} finally {
					if (in!=null) in.close();
				}
			}
			
			properties.putAll(System.getProperties());

			if (args.length>=1) {
				String host = args[0];
				if (host.indexOf(':')>=0) {
					properties.setProperty("tmtbl.solver.register.host",host.substring(0,host.indexOf(':')));
					properties.setProperty("tmtbl.solver.register.port",host.substring(host.indexOf(':')+1));
				} else {
					properties.setProperty("tmtbl.solver.register.host",host);
				}
			}
			if (args.length>=2) {
				properties.setProperty("tmtbl.solver.remote.port",args[1]);
			}
			if (args.length>=3) {
				properties.setProperty("General.Output", args[2]);
			} else if (properties.getProperty("tmtbl.solver.temp.dir")!=null) {
				properties.setProperty("General.Output", properties.getProperty("tmtbl.solver.temp.dir"));
			} else {
				properties.setProperty("General.Output", "."+File.separator+"server");
			}
			
			if (args.length>=4) {
				properties.setProperty("tmtbl.solver.backup.dir",args[3]);
			}

			startService(properties);
			
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
			
			if (!"false".equals(properties.getProperty("tmtbl.solver.remote.join")))
				getServerThread().join();
			
		} catch (Exception e) {
			sLogger.error(e.getMessage(),e);
			e.printStackTrace();
		}
    }
}
