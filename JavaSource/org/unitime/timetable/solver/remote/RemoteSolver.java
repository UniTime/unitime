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
import org.unitime.timetable.solver.TimetableSolver.SolverDisposeListener;
import org.unitime.timetable.solver.exam.ExamSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ExamSolver.ExamSolverDisposeListener;
import org.unitime.timetable.solver.remote.core.RemoteSolverServer;
import org.unitime.timetable.solver.remote.core.SolverTray;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolver.StudentSolverDisposeListener;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class RemoteSolver {
	private static Log sLog = LogFactory.getLog(RemoteSolver.class);
	public static File sBackupDir = new File("."+File.separator+"restore");
	public static File sPassivationDir = new File("."+File.separator+"passivate");
	private static boolean sInitialized = false;
    private static int sUsageBase = 0;
	
	private static Date sStartTime = new Date();
	private static Hashtable<String,TimetableSolver> sSolvers = new Hashtable();
	private static SolverPassivationThread sSolverPassivationThread = null;
	private static Hashtable<String,ExamSolver> sExamSolvers = new Hashtable();
	private static Hashtable<String,StudentSolver> sStudentSolvers = new Hashtable();

	public static Object answer(Object cmd) throws Exception {
		try {
			if (cmd==null) return null;
			if (cmd instanceof Object[]) {
				Object[] arr = (Object[]) cmd;
                if ("getExamSolvers".equals(arr[0])) {
                    return new HashSet(sExamSolvers.keySet());
                }
                if ("hasExamSolver".equals(arr[0])) {
                    return new Boolean(sExamSolvers.get((String)arr[1])!=null);
                }
                if ("createExamSolver".equals(arr[0])) {
                    String puid = (String)arr[1];
                    ExamSolver solver = sExamSolvers.get(puid);
                    if (solver!=null) solver.dispose();
                    solver = new ExamSolver((DataProperties)arr[2], new ExamSolverOnDispose(puid));
                    sExamSolvers.put(puid, solver);
                    return Boolean.TRUE;
                }
                if (arr.length>=3 && "EXAM".equals(arr[1])) {
                    String puid = (String)arr[2];
                    ExamSolver solver = sExamSolvers.get(puid);
                    if ("exists".equals(arr[0])) {
                        return new Boolean(solver!=null);
                    }
                    if (solver!=null) 
                        return solver.exec(arr);
                    else
                        return null;
                }
                if ("getStudentSolvers".equals(arr[0])) {
                    return new HashSet(sStudentSolvers.keySet());
                }
                if ("hasStudentSolver".equals(arr[0])) {
                    return new Boolean(sStudentSolvers.get((String)arr[1])!=null);
                }
                if ("createStudentSolver".equals(arr[0])) {
                    String puid = (String)arr[1];
                    StudentSolver solver = sStudentSolvers.get(puid);
                    if (solver!=null) solver.dispose();
                    solver = new StudentSolver((DataProperties)arr[2], new StudentSolverOnDispose(puid));
                    sStudentSolvers.put(puid, solver);
                    return Boolean.TRUE;
                }
                if (arr.length>=3 && "SCT".equals(arr[1])) {
                    String puid = (String)arr[2];
                    StudentSolver solver = sStudentSolvers.get(puid);
                    if ("exists".equals(arr[0])) {
                        return new Boolean(solver!=null);
                    }
                    if (solver!=null) 
                        return solver.exec(arr);
                    else
                        return null;
                }
                TimetableSolver solver = null;
				String puid = null;
				if (arr.length>1) {
					puid = (String)arr[1];
					solver = sSolvers.get(puid);
				}
				if ("getSolvers".equals(arr[0]))
					return new HashSet(sSolvers.keySet());
				if ("createSolver".equals(arr[0])) {
					if (solver!=null) solver.dispose();
                    solver = new TimetableSolver((DataProperties)arr[2], new SolverOnDispose(puid));
                    solver.setFileProxy(new FileProxy());
                    sSolvers.put(puid, solver);
                    return Boolean.TRUE;
				}
				if ("exists".equals(arr[0])) {
					return new Boolean(solver!=null);
				}
				if ("getAvailableMemory".equals(arr[0])) {
					return new Long(getAvailableMemory());
				}
				if ("getVersion".equals(arr[0])) {
					return Constants.getVersion();
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
				TimetableSolver solver = (TimetableSolver)entry.getValue();
				solver.backup(folder, puid);
			}
			
			for (Iterator i=sExamSolvers.entrySet().iterator();i.hasNext();) {
			    Map.Entry entry = (Map.Entry)i.next();
                String puid = (String)entry.getKey();
                ExamSolver solver =(ExamSolver)entry.getValue();
                solver.backup(folder, puid);
			}

            for (Iterator i=sStudentSolvers.entrySet().iterator();i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                String puid = (String)entry.getKey();
                StudentSolver solver =(StudentSolver)entry.getValue();
                solver.backup(folder, puid);
            }
		}
	}
	
	public static void restore(File folder, File passivateFolder) {
		if (!folder.exists() || !folder.isDirectory()) return;
		synchronized (sSolvers) {
			for (Iterator i=sSolvers.values().iterator();i.hasNext();) {
				TimetableSolver solver = (TimetableSolver)i.next();
				solver.dispose();
			}
			sSolvers.clear();
			File[] files = folder.listFiles(new BackupFileFilter(true,false));
			for (int i=0;i<files.length;i++) {
				File file = files[i];
				String puid = file.getName().substring(0,file.getName().indexOf('.'));
				
				if (puid.startsWith("exam_")) {
				    String exPuid = puid.substring("exam_".length());
				    ExamSolver solver = new ExamSolver(new DataProperties(), new ExamSolverOnDispose(exPuid));
				    if (solver.restore(folder, exPuid)) {
	                    if (passivateFolder!=null)
	                        solver.passivate(passivateFolder,puid);
				        sExamSolvers.put(exPuid,solver);
				    }
				    continue;
				}
				
                if (puid.startsWith("sct_")) {
                    String exPuid = puid.substring("sct_".length());
                    StudentSolver solver = new StudentSolver(new DataProperties(), new StudentSolverOnDispose(exPuid));
                    if (solver.restore(folder, exPuid)) {
                        if (passivateFolder!=null)
                            solver.passivate(passivateFolder,puid);
                        sStudentSolvers.put(exPuid,solver);
                    }
                    continue;
                }
				
                TimetableSolver solver = new TimetableSolver(new DataProperties(), new SolverOnDispose(puid));
				if (solver.restore(folder,puid)) {
					if (passivateFolder!=null)
						solver.passivate(passivateFolder,puid);
					sSolvers.put(puid,solver);
				} 
			}
		}		
	}
	
    public static void refreshSolution(Long solutionId) throws Exception {
    	if (sInitialized)
    		RemoteSolverServer.query(new Object[]{"refreshSolution",solutionId});
    }
    
    public static void refreshExamSolution(Long sessionId, Long examTypeId) throws Exception {
    	if (sInitialized)
    		RemoteSolverServer.query(new Object[]{"refreshExamSolution", sessionId, examTypeId});
    }
    
	public static void init(Properties properties, String url) throws Exception {
		if (sInitialized) return;
		
		try {
			System.out.println("configure "+properties.getProperty("General.Output","."));
			// remove unitime logger
			for (Iterator<Map.Entry<Object, Object>> i = properties.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<Object, Object> entry = i.next();
				String name = entry.getKey().toString();
				if (name.startsWith("log4j.appender.unitime") ||
						(name.startsWith("log4j.logger.") && entry.getValue().toString().endsWith(", unitime")))
						i.remove();
			}
			String logFile = ToolBox.configureLogging(properties.getProperty("General.Output","."),properties);
			if (SolverTray.isInitialized())
				SolverTray.getInstance().setLogFile(logFile);

			if (properties.getProperty("tmtbl.solver.backup.dir")!=null) {
				sBackupDir = new File(properties.getProperty("tmtbl.solver.backup.dir"));
			}
			
			if (properties.getProperty("tmtbl.solver.passivation.dir")!=null) {
				sPassivationDir = new File(properties.getProperty("tmtbl.solver.passivation.dir"));
			}
            
            if (properties.getProperty("tmtbl.solver.connection.url")!=null) {
            	properties.setProperty("connection.url", properties.getProperty("tmtbl.solver.connection.url"));
            } else {
				properties.setProperty("connection.url", url);
            }

			HibernateUtil.configureHibernate(properties);
	        
	        restore(sBackupDir, sPassivationDir);
	        sLog.debug("  -- backuped solver instances restored");
	        
	        sSolverPassivationThread = new SolverPassivationThread(sPassivationDir, sSolvers, sExamSolvers, sStudentSolvers);
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
	        classLoader.loadClass("org.dom4j.io.XMLWriter");
	        classLoader.loadClass("org.hibernate.proxy.HibernateProxy"); 
	        
	        sLog.debug("  -- classes needed by shutdown hooks loaded");
	        
	        sLog.info("Solver ready.");
	        sInitialized = true;
		} catch (Exception e) {
			sLog.fatal("Solver initialization failed", e);
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
        for (Iterator i=sExamSolvers.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            ExamSolverProxy solver = (ExamSolverProxy)entry.getValue();
            ret++;
            if (!solver.isPassivated()) ret++;
            try {
                if (solver.isWorking()) ret++;
            } catch (Exception e) {};
        }
        for (Iterator i=sStudentSolvers.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            StudentSolverProxy solver = (StudentSolverProxy)entry.getValue();
            ret++;
            if (!solver.isPassivated()) ret++;
            try {
                if (solver.isWorking()) ret++;
            } catch (Exception e) {};
        }
		return ret;
	}
	
	private static class ExamSolverOnDispose implements ExamSolverDisposeListener {
	    String iOwnerId = null;
	    public ExamSolverOnDispose(String ownerId) {
	        iOwnerId = ownerId;
	    }
        public void onDispose() {
            sExamSolvers.remove(iOwnerId);
        }
	}
    private static class StudentSolverOnDispose implements StudentSolverDisposeListener {
        String iOwnerId = null;
        public StudentSolverOnDispose(String ownerId) {
            iOwnerId = ownerId;
        }
        public void onDispose() {
            sStudentSolvers.remove(iOwnerId);
        }
    }
    private static class SolverOnDispose implements SolverDisposeListener {
        String iOwnerId = null;
        public SolverOnDispose(String ownerId) {
            iOwnerId = ownerId;
        }
        @Override
        public void onDispose() {
            sSolvers.remove(iOwnerId);
        }
    }
    private static class FileProxy implements TimetableInfoFileProxy {
    	@Override
    	public void saveToFile(String name, TimetableInfo info) throws Exception {
    		RemoteSolverServer.query(new Object[]{"saveToFile",name,info});
    	}
    	@Override
    	public TimetableInfo loadFromFile(String name) throws Exception {
    		return (TimetableInfo)RemoteSolverServer.query(new Object[]{"loadFromFile",name});
    	}
    	@Override
        public void deleteFile(String name) throws Exception {
            RemoteSolverServer.query(new Object[]{"deleteFile",name});
        }
    }
}
