/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.remote;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.base._BaseRootDAO;
import org.unitime.timetable.solver.SolverPassivationThread;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.exam.ExamSolver;
import org.unitime.timetable.solver.exam.ExamSolver.ExamSolverDisposeListener;
import org.unitime.timetable.solver.remote.core.RemoteSolverServer;
import org.unitime.timetable.solver.remote.core.SolverTray;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class RemoteSolver extends TimetableSolver implements TimetableInfoFileProxy {
	private static Log sLog = LogFactory.getLog(RemoteSolver.class);
	public static File sBackupDir = new File("."+File.separator+"restore");
	public static File sPassivationDir = new File("."+File.separator+"passivate");
	private static boolean sInitialized = false;
	private static boolean sBackupWhenDone = false;
    private static int sUsageBase = 0;
	
	private static Date sStartTime = new Date();
	private static Hashtable sSolvers = new Hashtable();
	private static SolverPassivationThread sSolverPassivationThread = null;
	private static ExamSolver sExamSolver = null;
	
	public RemoteSolver(DataProperties properties) {
		super(properties);
	}
	
    public String getHost() {
    	return "remote";
    }
    public String getHostLabel() {
    	return getHost();
    }
    
	public void dispose() {
		dispose(getProperties().getProperty("General.OwnerPuid"));
	}
	
	public void dispose(String puid) {
		super.dispose();
		if (puid!=null)
			sSolvers.remove(puid);
	}
    
	
	public Object exec(Object[] cmd) throws Exception {
		Class[] types = new Class[(cmd.length-2)/2];
		Object[] args = new Object[(cmd.length-2)/2];
		for (int i=0;i<types.length;i++) {
			types[i]=(Class)cmd[2*i+2];
			args[i]=cmd[2*i+3];
		}
		
		return getClass().getMethod((String)cmd[0],types).invoke(this, args);
	}
	
	public static Object answer(Object cmd) throws Exception {
		try {
			if (cmd==null) return null;
			if (cmd instanceof Object[]) {
				Object[] arr = (Object[]) cmd;
                if ("hasExamSolver".equals(arr[0])) {
                    return new Boolean(sExamSolver!=null);
                }
                if ("createExamSolver".equals(arr[0])) {
                    if (sExamSolver!=null) sExamSolver.dispose();
                    sExamSolver = new ExamSolver((DataProperties)arr[1], new ExamSolverDisposeListener() {
                        public void onDispose() {
                            sExamSolver = null;
                        }
                    });
                    return null;
                }
                if (arr.length>=2 && "EXAM".equals(arr[1])) {
                    if ("exists".equals(arr[0])) {
                        return new Boolean(sExamSolver!=null);
                    }
                    if (sExamSolver!=null) 
                        return sExamSolver.exec(arr);
                    else
                        return null;
                }
				RemoteSolver solver = null;
				String puid = null;
				if (arr.length>1) {
					puid = (String)arr[1];
					solver = (RemoteSolver)sSolvers.get(puid);
				}
				if ("getSolvers".equals(arr[0]))
					return new HashSet(sSolvers.keySet());
				if ("create".equals(arr[0])) {
					if (solver!=null) solver.dispose();
					solver = new RemoteSolver((DataProperties)arr[3]);
					sSolvers.put(puid,solver);
					return Boolean.TRUE;
				}
				if ("dispose".equals(arr[0])) {
					if (solver!=null) {
						solver.dispose(puid);
						return Boolean.TRUE;
					} else
						return Boolean.FALSE;
				}
				if ("exists".equals(arr[0])) {
					return new Boolean(solver!=null);
				}
				if ("getAvailableMemory".equals(arr[0])) {
					return new Long(getAvailableMemory());
				}
				if ("getVersion".equals(arr[0])) {
					return Constants.VERSION+"."+Constants.BLD_NUMBER;
				}
				if ("getStartTime".equals(arr[0])) {
					return sStartTime;
				}
				if ("getUsage".equals(arr[0])) {
					return new Long(getUsage());
				}
                if ("stopUsing".equals(arr[0])) {
                    sUsageBase = 1000;
                    return null;
                }
                if ("startUsing".equals(arr[0])) {
                    sUsageBase = 0;
                    return null;
                }
				if (solver!=null)
					return solver.exec(arr);
			}
			sLog.warn("Unknown command:"+cmd);
			return null;
		} finally {
			_BaseRootDAO.closeCurrentThreadSessions();
		}
	}
	
	public static void backupAll() {
		backup(sBackupDir);
	}
	
	public static void backup(File folder) {
		synchronized (sSolvers) {
			if (folder.exists() && !folder.isDirectory()) return;
			folder.mkdirs();
			File[] old = folder.listFiles(new BackupFileFilter(true,true));
			for (int i=0;i<old.length;i++)
				old[i].delete();

			for (Iterator i=sSolvers.entrySet().iterator();i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				String puid = (String)entry.getKey();
				RemoteSolver solver =(RemoteSolver)entry.getValue();
				solver.backup(folder, puid);
			}
		}
	}
	
	public static void restore(File folder, File passivateFolder) {
		if (!folder.exists() || !folder.isDirectory()) return;
		synchronized (sSolvers) {
			for (Iterator i=sSolvers.values().iterator();i.hasNext();) {
				RemoteSolver solver =(RemoteSolver)i.next();
				solver.dispose();
			}
			sSolvers.clear();
			File[] files = folder.listFiles(new BackupFileFilter(true,false));
			for (int i=0;i<files.length;i++) {
				File file = files[i];
				String puid = file.getName().substring(0,file.getName().indexOf('.'));
				
				RemoteSolver solver = new RemoteSolver(new DataProperties());
				if (solver.restore(folder,puid)) {
					if (passivateFolder!=null)
						solver.passivate(passivateFolder,puid);
					sSolvers.put(puid,solver);
				} 
			}
		}		
	}
	
    private void backup() {
    	if (!sBackupWhenDone) return;
    	String puid = getProperties().getProperty("General.OwnerPuid");
    	if (puid!=null)
    		backup(sBackupDir,puid);
    }
	
    protected void onFinish() {
    	super.onFinish();
    	backup();
    }
    
    protected void onStop() {
    	super.onStop();
    	backup();
    }

    protected void afterLoad() {
    	super.afterLoad();
    	backup();
    }
    
    protected void afterFinalSectioning() {
    	super.afterFinalSectioning();
    	backup();
    }
    
    public void restoreBest() {
    	super.restoreBest();
    	backup();
    }

    public void saveBest() {
    	super.saveBest();
    	backup();
    }
    
	public void saveToFile(String name, TimetableInfo info) throws Exception {
		RemoteSolverServer.query(new Object[]{"saveToFile",name,info});
	}
	public TimetableInfo loadFromFile(String name) throws Exception {
		return (TimetableInfo)RemoteSolverServer.query(new Object[]{"loadFromFile",name});
	}
    public void deleteFile(String name) throws Exception {
        RemoteSolverServer.query(new Object[]{"deleteFile",name});
    }
    public void refreshSolution(Long solutionId) throws Exception {
        RemoteSolverServer.query(new Object[]{"refreshSolution",solutionId});
    }
    
	public static void init(Properties properties, String url) throws Exception {
		if (sInitialized) return;
		
		try {
			System.out.println("configure "+properties.getProperty("General.Output","."));
			String logFile = ToolBox.configureLogging(properties.getProperty("General.Output","."),properties);
			if (SolverTray.isInitialized())
				SolverTray.getInstance().setLogFile(logFile);

			if (properties.getProperty("tmtbl.solver.backup.dir")!=null) {
				sBackupDir = new File(properties.getProperty("tmtbl.solver.backup.dir"));
			}
			
			if (properties.getProperty("tmtbl.solver.passivation.dir")!=null) {
				sPassivationDir = new File(properties.getProperty("tmtbl.solver.passivation.dir"));
			}
            
            properties.setProperty("connection.url", url);

			HibernateUtil.configureHibernate(properties);
	        
	        restore(sBackupDir, sPassivationDir);
	        sLog.debug("  -- backuped solver instances restored");
	        
	        sSolverPassivationThread = new SolverPassivationThread(sPassivationDir, sSolvers);
	        sSolverPassivationThread.start();
	        sLog.debug("  -- solver passivation thread started");
	        
	        //ensure that all classes needed for correct shutdown (backup) are loaded
	        ClassLoader classLoader = RemoteSolver.class.getClassLoader();
	        classLoader.loadClass("org.apache.log4j.spi.ThrowableInformation");
	        classLoader.loadClass("org.apache.log4j.spi.VectorWriter");
	        classLoader.loadClass("org.apache.log4j.spi.NullWriter");
	        classLoader.loadClass("net.sf.cpsolver.coursett.TimetableXMLSaver");
	        classLoader.loadClass("org.dom4j.DocumentHelper");
	        classLoader.loadClass("org.unitime.commons.ToolBox");
	        classLoader.loadClass("org.unitime.commons.ToolBox$LineOutputStream");
	        classLoader.loadClass("org.dom4j.io.XMLWriter");
	        classLoader.loadClass("org.hibernate.proxy.HibernateProxy"); 
	        
	        sLog.debug("  -- classes needed by shutdown hooks loaded");
	        
	        sLog.info("Solver ready.");
	        sInitialized = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void finish() throws Exception {
		HibernateUtil.closeHibernate();
		sLog.info("Solver finished.");
	}
	
	public static long getAvailableMemory() {
		System.gc();
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
	}
	
	public static long getUsage() {
		int ret = sUsageBase;
		for (Iterator i=sSolvers.entrySet().iterator();i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			SolverProxy solver = (SolverProxy)entry.getValue();
			ret++;
			if (!solver.isPassivated()) ret++;
			try {
				if (solver.isWorking()) ret++;
			} catch (Exception e) {};
		}
		return ret;
	}
}
