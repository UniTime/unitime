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
package org.unitime.timetable.solver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.solver.exam.ExamSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.RemoteExamSolverProxy;
import org.unitime.timetable.solver.exam.ExamSolver.ExamSolverDisposeListener;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.solver.remote.RemoteSolverProxy;
import org.unitime.timetable.solver.remote.RemoteSolverServerProxy;
import org.unitime.timetable.solver.remote.SolverRegisterService;
import org.unitime.timetable.tags.SolverWarnings;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressListener;

/**
 * @author Tomas Muller
 */
public class WebSolver extends TimetableSolver implements ProgressListener {
	protected static Log sLog = LogFactory.getLog(WebSolver.class);
	public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
	private JspWriter iJspWriter;
	private static Hashtable sSolvers = new Hashtable();
	private static ExamSolver sExamSolver = null;
	private static SolverPassivationThread sSolverPasivationThread = null;
	private static long sMemoryLimit = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.solver.mem_limit","200"))*1024*1024; //200 MB
	private static boolean sBackupWhenDone = false;
	
	public WebSolver(DataProperties properties) {
		super(properties);
	}
	
	public static ExamSolverProxy getExamSolver(Long sessionId) {
        try {
            if (sExamSolver!=null) {
                if (sessionId!=null && !sessionId.equals(sExamSolver.getProperties().getPropertyLong("General.SessionId",null))) 
                    return null;
                return sExamSolver;
            }
            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) continue;
                    ExamSolverProxy proxy = server.getExamSolver();
                    if (proxy!=null) {
                        if (sessionId!=null && !sessionId.equals(proxy.getProperties().getPropertyLong("General.SessionId",null))) 
                            return null;
                        return proxy;
                    }
                }
            }
        } catch (Exception e) {
            sLog.error("Unable to retrieve solver, reason:"+e.getMessage(),e);
        }
        return null;
    }
	
	public static ExamSolverProxy getExamSolver(javax.servlet.http.HttpSession session) {
	    ExamSolverProxy solver = (ExamSolverProxy)session.getAttribute("ExamSolverProxy");
        if (solver!=null) {
            try {
                if (solver instanceof RemoteExamSolverProxy && ((RemoteExamSolverProxy)solver).exists())
                    return solver;
                else
                    session.removeAttribute("ExamSolverProxy");
            } catch (Exception e) {
                session.removeAttribute("ExamSolverProxy");
            };
        }
        User user = Web.getUser(session);
        if (user==null) return null;
        Session acadSession = null;
        try {
            acadSession = Session.getCurrentAcadSession(user);
        } catch (Exception e) {}
        if (acadSession==null) return null;
        TimetableManager mgr = TimetableManager.getManager(user);
        if (!mgr.canTimetableExams(acadSession, user)) return null;
        solver = getExamSolver(acadSession.getUniqueId());
        if (solver==null) return null;
        session.setAttribute("ExamSolverProxy", solver);
        return solver;
    }
	
	public static ExamSolverProxy getExamSolverNoSessionCheck() {
        try {
            if (sExamSolver!=null) return sExamSolver;
            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) continue;
                    ExamSolverProxy proxy = server.getExamSolver();
                    if (proxy!=null) return proxy;
                }
            }
        } catch (Exception e) {
            sLog.error("Unable to retrieve solver, reason:"+e.getMessage(),e);
        }
        return null;
    }

	public static SolverProxy getSolver(String puid, Long sessionId) {
		try {
			SolverProxy proxy = (SolverProxy)sSolvers.get(puid);
			if (proxy!=null) {
				if (sessionId!=null && !sessionId.equals(proxy.getProperties().getPropertyLong("General.SessionId",null))) 
					return null;
				return proxy;
			}
            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) continue;
                    proxy = server.getSolver(puid);
                    if (proxy!=null) {
                        if (sessionId!=null && !sessionId.equals(proxy.getProperties().getPropertyLong("General.SessionId",null))) 
                            return null;
                        return proxy;
                    }
				}
			}
		} catch (Exception e) {
			sLog.error("Unable to retrieve solver, reason:"+e.getMessage(),e);
		}
		return null;
	}
	
	public static SolverProxy getSolver(javax.servlet.http.HttpSession session) {
		SolverProxy solver = (SolverProxy)session.getAttribute("SolverProxy");
		if (solver!=null) {
			try {
				if (solver instanceof RemoteSolverProxy && ((RemoteSolverProxy)solver).exists())
					return solver;
				else
					session.removeAttribute("SolverProxy");
			} catch (Exception e) {
				session.removeAttribute("SolverProxy");
			};
		}
		User user = Web.getUser(session);
		if (user==null) return null;
		Long sessionId = null;
		try {
			sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		} catch (Exception e) {}
		String puid = (String)session.getAttribute("ManageSolver.puid");
		if (puid!=null) {
			solver = getSolver(puid, sessionId);
			if (solver!=null) {
				session.setAttribute("SolverProxy", solver);
				return solver;
			}
		}
		solver = getSolver(user.getId(), sessionId);
		if (solver!=null)
			session.setAttribute("SolverProxy", solver);
		return solver;
	}
	
	public static SolverProxy getSolverNoSessionCheck(javax.servlet.http.HttpSession session) {
		User user = Web.getUser(session);
		if (user==null) return null;
		String puid = (String)session.getAttribute("ManageSolver.puid");
		if (puid!=null) {
			SolverProxy solver = getSolver(puid, null);
			if (solver!=null) return solver;
		}
		return getSolver(user.getId(), null);
	}	
	
	public static DataProperties createProperties(Long settingsId, Hashtable extraParams, int type) {
		DataProperties properties = new DataProperties();
		Transaction tx = null;
		try {
			SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
			org.hibernate.Session hibSession = dao.getSession();
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				tx = hibSession.beginTransaction();
			List defaultParams = hibSession.createCriteria(SolverParameterDef.class).list();
			for (Iterator i=defaultParams.iterator();i.hasNext();) {
				SolverParameterDef def = (SolverParameterDef)i.next();
				if (def.getGroup().getType()!=type) continue;
				if (def.getDefault()!=null)
					properties.put(def.getName(),def.getDefault());
				if (extraParams!=null && extraParams.containsKey(def.getUniqueId()))
					properties.put(def.getName(), (String)extraParams.get(def.getUniqueId()));
			}
			SolverPredefinedSetting settings = dao.get(settingsId);
			for (Iterator i=settings.getParameters().iterator();i.hasNext();) {
				SolverParameter param = (SolverParameter)i.next();
				if (!param.getDefinition().isVisible().booleanValue()) continue;
				if (param.getDefinition().getGroup().getType()!=type) continue;
				properties.put(param.getDefinition().getName(),param.getValue());
				if (extraParams!=null && extraParams.containsKey(param.getDefinition().getUniqueId()))
					properties.put(param.getDefinition().getName(), (String)extraParams.get(param.getDefinition().getUniqueId()));
			}
			properties.setProperty("General.SettingsId", settings.getUniqueId().toString());
			if (tx!=null) tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			sLog.error(e);
		}
		StringBuffer ext = new StringBuffer();
		if (properties.getPropertyBoolean("General.SearchIntensification",type==SolverParameterGroup.sTypeCourse)) {
        	if (ext.length()>0) ext.append(";");
        	ext.append("net.sf.cpsolver.ifs.extension.SearchIntensification");
        }
        if (properties.getPropertyBoolean("General.CBS",type==SolverParameterGroup.sTypeCourse)) {
        	if (ext.length()>0) ext.append(";");
        	ext.append("net.sf.cpsolver.ifs.extension.ConflictStatistics");
        }
        if (type==SolverParameterGroup.sTypeCourse) {
            String mode = properties.getProperty("Basic.Mode","Initial");
            if ("MPP".equals(mode)) {
                properties.setProperty("General.MPP","true");
                if (ext.length()>0) ext.append(";");
                ext.append("net.sf.cpsolver.ifs.extension.ViolatedInitials");
            }
        } else if (type==SolverParameterGroup.sTypeExam) {
            String mode = properties.getProperty("ExamBasic.Mode","Initial");
            if ("MPP".equals(mode)) {
                properties.setProperty("General.MPP","true");
                if (ext.length()>0) ext.append(";");
                ext.append("net.sf.cpsolver.ifs.extension.ViolatedInitials");
            }
        }
        properties.setProperty("Extensions.Classes",ext.toString());
        if (properties.getPropertyBoolean("Basic.DisobeyHard",false)) {
        	properties.setProperty("General.InteractiveMode", "true");
        }
        if ("No Action".equals(properties.getProperty("Basic.WhenFinished")) || "No Action".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","false");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save".equals(properties.getProperty("Basic.WhenFinished")) || "Save".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save as New".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","true");
            properties.setProperty("General.Unload","false");
        } else if ("Save and Unload".equals(properties.getProperty("Basic.WhenFinished")) || "Save and Unload".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","true");
        } else if ("Save as New and Unload".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","true");
            properties.setProperty("General.Unload","true");
        }
        properties.setProperty("Xml.ShowNames","true");
        if (type==SolverParameterGroup.sTypeCourse)
            properties.setProperty("Xml.ExportStudentSectioning", "true");
        if (type==SolverParameterGroup.sTypeExam) {
            properties.setProperty("Exam.GreatDeluge", ("Great Deluge".equals(properties.getProperty("Exam.Algorithm","Great Deluge"))?"true":"false"));
        }
        properties.expand();
        return properties;
	}
	
	public static SolverProxy createSolver(Long sessionId, javax.servlet.http.HttpSession session, Long[] ownerId, String solutionIds, Long settingsId, Hashtable extraParams, boolean startSolver, String host) throws Exception {
		try {
		    System.out.println("Memory limit is "+sMemoryLimit);
		    
		User user = Web.getUser(session);
		if (user==null) return null;
		
		removeSolver(session);
			
		DataProperties properties = createProperties(settingsId, extraParams, SolverParameterGroup.sTypeCourse);
		
        String warn = SolverWarnings.getSolverWarning(session, ownerId);
        if (warn!=null) 
        	properties.setProperty("General.SolverWarnings",warn);
        else
        	properties.remove("General.SolverWarnings");
		
		properties.setProperty("General.SessionId",sessionId.toString());
		properties.setProperty("General.SolverGroupId",ownerId);
		properties.setProperty("General.OwnerPuid", user.getId());
		if (solutionIds!=null)
			properties.setProperty("General.SolutionId",solutionIds);
		properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
	    properties.setProperty("General.StartSolver",Boolean.toString(startSolver));
	    String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
	    if (instructorFormat!=null)
	    	properties.setProperty("General.InstructorFormat",instructorFormat);
	    
	    if (host!=null) {
            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) continue;
                    if (host.equals(server.getAddress().getHostName()+":"+server.getPort())) {
                        SolverProxy solver = server.createSolver(user.getId(),properties);
                        solver.load(properties);
                        return solver;
                    }
                }
            }
	    }
	    
	    if (!"local".equals(host) && !SolverRegisterService.getInstance().getServers().isEmpty()) {
	    	RemoteSolverServerProxy bestServer = null;
            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) continue;
                    if (server.getAvailableMemory()<sMemoryLimit) continue;
                    if (bestServer==null) {
                        bestServer = server;
                    } else if (bestServer.getUsage()>server.getUsage()) {
                        bestServer = server;
                    }
                }
            }
			if (bestServer!=null) {
				SolverProxy solver = bestServer.createSolver(user.getId(),properties);
				solver.load(properties);
				return solver;
			}
	    }
	    
	    if (getAvailableMemory()<sMemoryLimit)
	    	throw new Exception("Not enough resources to create a solver instance, please try again later.");
    	WebSolver solver = new WebSolver(properties);
    	solver.load(properties);
    	Progress.getInstance(solver.currentSolution().getModel()).addProgressListener(solver);
    	sSolvers.put(user.getId(),solver);
    	return solver;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	   public static ExamSolverProxy createExamSolver(Long sessionId, javax.servlet.http.HttpSession session, Long settingsId, Hashtable extraParams, boolean startSolver, String host) throws Exception {
	        try {
	            
	        User user = Web.getUser(session);
	        if (user==null) return null;
	        
	        removeExamSolver(session);
	            
	        DataProperties properties = createProperties(settingsId, extraParams, SolverParameterGroup.sTypeExam);
	        
	        properties.setProperty("General.SessionId",sessionId.toString());
	        properties.setProperty("General.OwnerPuid", user.getId());
	        properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
	        properties.setProperty("General.StartSolver",Boolean.toString(startSolver));
	        String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
	        if (instructorFormat!=null)
	            properties.setProperty("General.InstructorFormat",instructorFormat);
	        
	        if (host!=null) {
	            Set servers = SolverRegisterService.getInstance().getServers();
	            synchronized (servers) {
	                for (Iterator i=servers.iterator();i.hasNext();) {
	                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
	                    if (!server.isActive()) continue;
	                    if (host.equals(server.getAddress().getHostName()+":"+server.getPort())) {
	                        ExamSolverProxy solver = server.createExamSolver(properties);
	                        solver.load(properties);
	                        return solver;
	                    }
	                }
	            }
	        }
	        
	        if (!"local".equals(host) && !SolverRegisterService.getInstance().getServers().isEmpty()) {
	            RemoteSolverServerProxy bestServer = null;
	            Set servers = SolverRegisterService.getInstance().getServers();
	            synchronized (servers) {
	                for (Iterator i=servers.iterator();i.hasNext();) {
	                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
	                    if (!server.isActive()) continue;
	                    if (server.getAvailableMemory()<sMemoryLimit) continue;
	                    if (bestServer==null) {
	                        bestServer = server;
	                    } else if (bestServer.getUsage()>server.getUsage()) {
	                        bestServer = server;
	                    }
	                }
	            }
	            if (bestServer!=null) {
	                ExamSolverProxy solver = bestServer.createExamSolver(properties);
	                solver.load(properties);
	                return solver;
	            }
	        }
	        
	        if (getAvailableMemory()<sMemoryLimit)
	            throw new Exception("Not enough resources to create a solver instance, please try again later.");
	        sExamSolver = new ExamSolver(properties, new ExamSolverDisposeListener() {
	            public void onDispose() {
	                sExamSolver = null;
	            }
	        });
	        sExamSolver.load(properties);
	        //Progress.getInstance(sExamSolver.currentSolution().getModel()).addProgressListener(sExamSolver);
	        return sExamSolver;
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw e;
	        }
	    }
	
	public static SolverProxy reload(javax.servlet.http.HttpSession session, Long settingsId, Hashtable extraParams) throws Exception {
		User user = Web.getUser(session);
		if (user==null) return null;
		
		SolverProxy solver = getSolver(session);
		if (solver==null) return null;
		DataProperties oldProperties = solver.getProperties();
		
		if (settingsId==null)
			settingsId = oldProperties.getPropertyLong("General.SettingsId", null);
		
		DataProperties properties = createProperties(settingsId, extraParams, SolverParameterGroup.sTypeCourse);
		
        String warn = SolverWarnings.getSolverWarning(session, oldProperties.getPropertyLongArry("General.SolverGroupId", null));
        if (warn!=null) properties.setProperty("General.SolverWarnings",warn);
		
		properties.setProperty("General.SessionId",oldProperties.getProperty("General.SessionId"));
		properties.setProperty("General.SolverGroupId",oldProperties.getProperty("General.SolverGroupId"));
		properties.setProperty("General.OwnerPuid", oldProperties.getProperty("General.OwnerPuid"));
		properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
	    String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
	    if (instructorFormat!=null)
	    	properties.setProperty("General.InstructorFormat",instructorFormat);
	    
    	solver.reload(properties);
    	
    	if (solver instanceof WebSolver) {
    		Progress p = Progress.getInstance(((WebSolver)solver).currentSolution().getModel());
    		p.clearProgressListeners();
    		p.addProgressListener((WebSolver)solver);
    		sSolvers.put(user.getId(),solver);
    	}
    	
    	return solver;
	}
	
    public static ExamSolverProxy reloadExamSolver(javax.servlet.http.HttpSession session, Long settingsId, Hashtable extraParams) throws Exception {
        User user = Web.getUser(session);
        if (user==null) return null;
        
        ExamSolverProxy solver = getExamSolver(session);
        if (solver==null) return null;
        DataProperties oldProperties = solver.getProperties();
        
        if (settingsId==null)
            settingsId = oldProperties.getPropertyLong("General.SettingsId", null);
        
        DataProperties properties = createProperties(settingsId, extraParams, SolverParameterGroup.sTypeExam);
        
        String warn = SolverWarnings.getSolverWarning(session, oldProperties.getPropertyLongArry("General.SolverGroupId", null));
        if (warn!=null) properties.setProperty("General.SolverWarnings",warn);
        
        properties.setProperty("General.SessionId",oldProperties.getProperty("General.SessionId"));
        properties.setProperty("General.SolverGroupId",oldProperties.getProperty("General.SolverGroupId"));
        properties.setProperty("General.OwnerPuid", oldProperties.getProperty("General.OwnerPuid"));
        properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
        String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
        if (instructorFormat!=null)
            properties.setProperty("General.InstructorFormat",instructorFormat);
        
        solver.reload(properties);
        
        if (solver instanceof ExamSolver) {
            Progress p = Progress.getInstance(((WebSolver)solver).currentSolution().getModel());
            p.clearProgressListeners();
            p.addProgressListener((WebSolver)solver);
            sSolvers.put(user.getId(),solver);
        }
        
        return solver;
    }
	
	public void dispose() {
		super.dispose();
		if (iJspWriter!=null) {
			try {
				iJspWriter.println("<I>Solver finished.</I>");
				iJspWriter.flush();
			} catch (Exception e) {}
			iJspWriter = null;
		}
		String puid = getProperties().getProperty("General.OwnerPuid");
		if (puid!=null)
			sSolvers.remove(puid);
	}
	
	public static void saveSolution(javax.servlet.http.HttpSession session, boolean createNewSolution, boolean commitSolution) throws Exception {
		SolverProxy solver = getSolver(session);
		if (solver==null) return;
		solver.save(createNewSolution, commitSolution);
	}
	
    public static void removeSolver(javax.servlet.http.HttpSession session) throws Exception {
		session.removeAttribute("SolverProxy");
		session.removeAttribute("Suggestions.model");
		session.removeAttribute("Timetable.table");
		SolverProxy solver = getSolverNoSessionCheck(session);
		if (solver!=null) {
			if (solver.isRunning()) solver.stopSolver();
			solver.dispose();
		}
		session.removeAttribute("ManageSolver.puid");
	}
	
    public static void removeExamSolver(javax.servlet.http.HttpSession session) throws Exception {
        session.removeAttribute("ExamSolverProxy");
        ExamSolverProxy solver = getExamSolverNoSessionCheck();
        if (solver!=null) {
            if (solver.isRunning()) solver.stopSolver();
            solver.dispose();
        }
    }

    public static Hashtable getSolvers() throws Exception {
		Hashtable solvers = new Hashtable(sSolvers);
        Set servers = SolverRegisterService.getInstance().getServers();
        synchronized (servers) {
            for (Iterator i=servers.iterator();i.hasNext();) {
                RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                if (!server.isActive()) continue;
                Hashtable serverSolvers = server.getSolvers();
                if (serverSolvers!=null)
                    solvers.putAll(serverSolvers);
            }
		}
		return solvers; 
	}
	
	public static Hashtable getLocalSolvers() throws Exception {
		return sSolvers;
	}

	public void setHtmlMessageWriter(JspWriter out) {
		if (iJspWriter!=null && !iJspWriter.equals(out)) {
			try {
				iJspWriter.println("<I>Thread ended.</I>");
			} catch (Exception e) {}
		}
		iJspWriter = out;
		while (out.equals(iJspWriter)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("STOP: "+e.getMessage());
				break;
			}
		}
	}
	
	//Progress listener
    public void statusChanged(String status) {}
    public void phaseChanged(String phase) {}
    public void progressChanged(long currentProgress, long maxProgress) {}
    public void progressSaved() {}
    public void progressRestored() {}
    public void progressMessagePrinted(Progress.Message message) {
    	try {
    		if (iJspWriter!=null) {
    			String m = message.toHtmlString(getDebugLevel());
    			if (m!=null)  {
    				iJspWriter.println(m+"<br>");
    				iJspWriter.flush();
    			}
    		}
    	} catch (IOException e) {
    		System.out.println("STOP: "+e.getMessage());
    		iJspWriter = null;
    	}
    }
    
    public String getHost() {
    	return "local";
    }
    
    public String getHostLabel() {
    	return getHost();
    }
    
    private void backup() {
    	if (!sBackupWhenDone) return;
    	String puid = getProperties().getProperty("General.OwnerPuid");
    	if (puid!=null)
    		backup(SolverRegisterService.sBackupDir,puid);
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
				WebSolver solver =(WebSolver)entry.getValue();
				solver.backup(folder, puid);
			}
		}
	}
	
	public static void restore(File folder, File passivateFolder) {
		if (!folder.exists() || !folder.isDirectory()) return;
		synchronized (sSolvers) {
			for (Iterator i=sSolvers.values().iterator();i.hasNext();) {
				WebSolver solver =(WebSolver)i.next();
				solver.dispose();
			}
			sSolvers.clear();
			File[] files = folder.listFiles(new BackupFileFilter(true,false));
			for (int i=0;i<files.length;i++) {
				File file = files[i];
				String puid = file.getName().substring(0,file.getName().indexOf('.'));
				
				WebSolver solver = new WebSolver(new DataProperties());
				if (solver.restore(folder,puid)) {
					if (passivateFolder!=null)
						solver.passivate(passivateFolder,puid);
					sSolvers.put(puid,solver);
				} 
			}
		}
	}
	
	public static void startSolverPasivationThread(File folder) {
		if (sSolverPasivationThread!=null && sSolverPasivationThread.isAlive()) return;
		sSolverPasivationThread = new SolverPassivationThread(folder, sSolvers);
		sSolverPasivationThread.start();
	}
	
    public static void stopSolverPasivationThread() {
        if (sSolverPasivationThread!=null && sSolverPasivationThread.isAlive()) {
            sSolverPasivationThread.interrupt();
        }
    }

    public static ClassAssignmentProxy getClassAssignmentProxy(HttpSession session) {
		SolverProxy solver = getSolver(session);
		if (solver!=null) return new CachedClassAssignmentProxy(solver);
		String solutionIdsStr = (String)session.getAttribute("Solver.selectedSolutionId");
		Set solutionIds = new HashSet();
		if (solutionIdsStr!=null) {
			for (StringTokenizer s = new StringTokenizer(solutionIdsStr, ",");s.hasMoreTokens();) {
                Long solutionId = Long.valueOf(s.nextToken());
				solutionIds.add(solutionId);
			}
		}
		SolutionClassAssignmentProxy cachedProxy = (SolutionClassAssignmentProxy)session.getAttribute("LastSolutionClassAssignmentProxy");
		if (cachedProxy!=null && cachedProxy.equals(solutionIds)) {
			return cachedProxy;
		}
		SolutionClassAssignmentProxy newProxy = new SolutionClassAssignmentProxy(solutionIds);
		session.setAttribute("LastSolutionClassAssignmentProxy",newProxy);
		return newProxy;
	}
	
    public static long getAvailableMemory() {
		System.gc();
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory(); 
	}

	public static long getUsage() {
		int ret = 0;
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