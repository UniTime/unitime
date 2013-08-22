/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.StudentConflictStatistics;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.weights.StudentWeights;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.solver.remote.RemoteSolverProxy;
import org.unitime.timetable.solver.remote.RemoteSolverServerProxy;
import org.unitime.timetable.solver.remote.SolverRegisterService;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolver.StudentSolverDisposeListener;

@Service("studentSectioningSolverService")
public class StudentSectioningSolverService implements SolverService<StudentSolverProxy>, InitializingBean, DisposableBean {
	protected static Log sLog = LogFactory.getLog(StudentSectioningSolverService.class);
	private Map<String, StudentSolverProxy> iSolvers = new Hashtable<String, StudentSolverProxy>();
	private PassivationThread iPassivation = null;
	
	@Autowired SessionContext sessionContext;

	@Override
	public DataProperties createConfig(Long settingsId, Map<Long, String> options) {
		DataProperties properties = new DataProperties();
		
		// Load properties
		for (SolverParameterDef def: (List<SolverParameterDef>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.type = :type").setInteger("type", SolverParameterGroup.sTypeStudent).list()) {
			if (def.getDefault() != null) properties.put(def.getName(), def.getDefault());
			if (options != null && options.containsKey(def.getUniqueId()))
				properties.put(def.getName(), options.get(def.getUniqueId()));
		}
		
		SolverPredefinedSetting settings = SolverPredefinedSettingDAO.getInstance().get(settingsId);
		for (SolverParameter param: settings.getParameters()) {
			if (!param.getDefinition().isVisible() || param.getDefinition().getGroup().getType() != SolverParameterGroup.sTypeStudent) continue;
			properties.put(param.getDefinition().getName(),param.getValue());
			if (options != null && options.containsKey(param.getDefinition().getUniqueId()))
				properties.put(param.getDefinition().getName(), options.get(param.getDefinition().getUniqueId()));
		}
		properties.setProperty("General.SettingsId", settings.getUniqueId().toString());
		
		// Generate extensions
		String ext = properties.getProperty("Extensions.Classes", "");
		        
        if (properties.getPropertyBoolean("StudentSct.CBS",true)) {
        	if (!ext.isEmpty()) ext += ";";
			ext += StudentConflictStatistics.class.getName();
            properties.setProperty("ConflictStatistics.Print","true");
        }
        if (properties.getPropertyBoolean("StudentSct.StudentDist",true)) {
        	if (!ext.isEmpty()) ext += ";";
			ext += DistanceConflict.class.getName();
        }
        if (properties.getPropertyBoolean("StudentSct.TimeOverlaps",true)) {
        	if (!ext.isEmpty()) ext += ";";
			ext += TimeOverlapsCounter.class.getName();
        }
        
        if (!properties.getProperty("StudentWeights.Mode","").isEmpty()) {
            StudentWeights.Implementation studentWeights = StudentWeights.Implementation.valueOf(properties.getProperty("StudentWeights.Mode"));
            if (studentWeights != null) {
            	properties.setProperty("StudentWeights.Class", studentWeights.getImplementation().getName());
            	properties.setProperty("Comparator.Class", studentWeights.getImplementation().getName());
            }
        }
        
        String mode = properties.getProperty("StudentSctBasic.Mode","Initial");
        if ("MPP".equals(mode))
            properties.setProperty("General.MPP","true");

        properties.setProperty("Extensions.Classes", ext);
        
        // Interactive mode?
        if (properties.getPropertyBoolean("Basic.DisobeyHard",false))
        	properties.setProperty("General.InteractiveMode", "true");
        
        // When finished?
        if ("No Action".equals(properties.getProperty("StudentSctBasic.WhenFinished"))) {
            properties.setProperty("General.Save","false");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save".equals(properties.getProperty("StudentSctBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save and Unload".equals(properties.getProperty("StudentSctBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","true");
        }
        
        // XML save/load properties
        properties.setProperty("Xml.ShowNames", "true");
        
        // Distances Matrics
        if (properties.getProperty("Distances.Ellipsoid") == null || properties.getProperty("Distances.Ellipsoid").equals("DEFAULT"))
            properties.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty(ApplicationProperty.DistanceEllipsoid));
        
        properties.expand();
        
        return properties;
	}

	@Override
	public StudentSolverProxy createSolver(DataProperties properties) {
		try {
			if (!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAcademicSessionId() == null) return null;
			
			removeSolver();
				
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			properties.setProperty("General.OwnerPuid", sessionContext.getUser().getExternalUserId());
			properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
			
			String host = properties.getProperty("General.Host");
		    
		    String instructorFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
		    if (instructorFormat != null)
		    	properties.setProperty("General.InstructorFormat",instructorFormat);
		    
		    if (host!=null) {
	            Set servers = SolverRegisterService.getInstance().getServers();
	            synchronized (servers) {
	                for (Iterator i=servers.iterator();i.hasNext();) {
	                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
	                    if (!server.isActive()) continue;
	                    if (host.equals(server.getAddress().getHostName()+":"+server.getPort())) {
	                        StudentSolverProxy solver = server.createStudentSolver(sessionContext.getUser().getExternalUserId(), properties);
	                        solver.load(properties);
	                        return solver;
	                    }
	                }
	            }
		    }
		    
		    int memoryLimit = Integer.parseInt(ApplicationProperties.getProperty(ApplicationProperty.SolverMemoryLimit));
		    
		    if (!"local".equals(host) && !SolverRegisterService.getInstance().getServers().isEmpty()) {
		    	RemoteSolverServerProxy bestServer = null;
	            Set servers = SolverRegisterService.getInstance().getServers();
	            synchronized (servers) {
	                for (Iterator i=servers.iterator();i.hasNext();) {
	                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
	                    if (!server.isActive()) continue;
	                    if (server.getAvailableMemory() < memoryLimit) continue;
	                    if (bestServer == null) {
	                        bestServer = server;
	                    } else if (bestServer.getUsage() > server.getUsage()) {
	                        bestServer = server;
	                    }
	                }
	            }
				if (bestServer != null) {
					StudentSolverProxy solver = bestServer.createStudentSolver(sessionContext.getUser().getExternalUserId(), properties);
					solver.load(properties);
					return solver;
				}
		    }
		    
		    if (getAvailableMemory() < memoryLimit)
		    	throw new RuntimeException("Not enough resources to create a solver instance, please try again later.");
		    
	    	StudentSolverProxy solver = new StudentSolver(properties, new SolverOnDispose(sessionContext.getUser().getExternalUserId()));
	    	solver.load(properties);
	    	iSolvers.put(sessionContext.getUser().getExternalUserId(), solver);
	    	return solver;
		} catch (Exception e) {
			sLog.error("Failed to start the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}

	private long getAvailableMemory() {
		System.gc();
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory(); 
	}

	public StudentSolverProxy getSolver(String puid, Long sessionId) {
		try {
			StudentSolverProxy proxy = iSolvers.get(puid);
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
                    proxy = server.getStudentSolver(puid);
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

	@Override
	public StudentSolverProxy getSolver() {
		StudentSolverProxy solver = (StudentSolverProxy)sessionContext.getAttribute(SessionAttribute.StudentSectioningSolver);
		if (solver!=null) {
			try {
				if (solver instanceof RemoteSolverProxy && ((RemoteSolverProxy)solver).exists())
					return (StudentSolverProxy)solver;
				else
					sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
			} catch (Exception e) {
				sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
			};
		}
		if (!sessionContext.isAuthenticated()) return null;
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		if (sessionId == null) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.StudentSectioningUser);
		if (puid != null) {
			solver = getSolver(puid, sessionId);
			if (solver!=null) {
				sessionContext.setAttribute(SessionAttribute.StudentSectioningSolver, solver);
				return solver;
			}
		}
		solver = getSolver(sessionContext.getUser().getExternalUserId(), sessionId);
		if (solver!=null)
			sessionContext.setAttribute(SessionAttribute.StudentSectioningSolver, solver);
		return solver;
	}
	
	@Override
	public StudentSolverProxy getSolverNoSessionCheck() {
		if (!sessionContext.isAuthenticated()) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.StudentSectioningUser);
		if (puid!=null) {
			StudentSolverProxy solver = getSolver(puid, null);
			if (solver!=null) return solver;
		}
		return getSolver(sessionContext.getUser().getExternalUserId(), null);
	}

	@Override
	public void removeSolver() {
		try {
			sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
			StudentSolverProxy solver = getSolverNoSessionCheck();
			if (solver != null) {
				solver.interrupt();
				solver.dispose();
			}
			sessionContext.removeAttribute(SessionAttribute.StudentSectioningUser);
		} catch (Exception e) {
			sLog.warn("Failed to remove a solver: " + e.getMessage(), e);
		}
	}

	@Override
	public StudentSolverProxy reload(DataProperties properties) {
		try {
			StudentSolverProxy solver = getSolver();
			if (solver == null) return createSolver(properties);

			DataProperties oldProperties = solver.getProperties();
			
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			properties.setProperty("General.OwnerPuid", oldProperties.getProperty("General.OwnerPuid"));
			properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));

			String instructorFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
		    if (instructorFormat != null)
		    	properties.setProperty("General.InstructorFormat",instructorFormat);

		    solver.reload(properties);
		    
		    return solver;
		} catch (Exception e) {
			sLog.error("Failed to reload the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}

	@Override
	public Map<String, StudentSolverProxy> getSolvers() {
		Map<String, StudentSolverProxy> solvers = new HashMap<String, StudentSolverProxy>(iSolvers);
        Set servers = SolverRegisterService.getInstance().getServers();
        synchronized (servers) {
            for (Iterator i=servers.iterator();i.hasNext();) {
                RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                if (!server.isActive()) continue;
                try {
                	Map<String, StudentSolverProxy> serverSolvers = server.getStudentSolvers();
                	if (serverSolvers != null)
                		solvers.putAll(serverSolvers);
                } catch (Exception e) {
                	sLog.error("Failed to retrieve solvers from " + server + ": " + e.getMessage(), e);
                }
            }
		}
		return solvers; 
	}
	
	@Override
	public Map<String, StudentSolverProxy> getLocalSolvers() {
		return iSolvers;
	}
	
	private class SolverOnDispose implements StudentSolverDisposeListener {
        String iOwnerId = null;
        public SolverOnDispose(String ownerId) {
            iOwnerId = ownerId;
        }
        public void onDispose() {
            iSolvers.remove(iOwnerId);
        }
    }
	
	public void backup(File folder) {
        if (folder.exists() && !folder.isDirectory()) return;
        folder.mkdirs();
        
        File[] old = folder.listFiles(new BackupFileFilter(true, true, SolverParameterGroup.sTypeStudent));
        for (int i=0;i<old.length;i++)
            old[i].delete();
		synchronized (iSolvers) {
			for (Map.Entry<String, StudentSolverProxy> entry: iSolvers.entrySet())
				entry.getValue().backup(folder, entry.getKey());
		}
	}
    
    public void restore(File folder, File passivateFolder) {
		if (!folder.exists() || !folder.isDirectory()) return;
		
		synchronized (iSolvers) {
			for (StudentSolverProxy solver: new ArrayList<StudentSolverProxy>(iSolvers.values()))
				solver.dispose();
			iSolvers.clear();
			
			File[] files = folder.listFiles(new BackupFileFilter(true, false, SolverParameterGroup.sTypeStudent));
			for (int i=0;i<files.length;i++) {
				File file = files[i];
				String puid = file.getName().substring("sct_".length(), file.getName().indexOf('.'));
                
				StudentSolverProxy solver = new StudentSolver(new DataProperties(), new SolverOnDispose(puid));
				if (solver.restore(folder,puid)) {
					if (passivateFolder!=null)
						solver.passivate(passivateFolder,puid);
					iSolvers.put(puid,solver);
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		iPassivation = new PassivationThread(ApplicationProperties.getPassivationFolder());
		iPassivation.start();
		restore(ApplicationProperties.getRestoreFolder(), ApplicationProperties.getPassivationFolder());
	}

	@Override
	public void destroy() throws Exception {
		backup(ApplicationProperties.getRestoreFolder());
		iPassivation.destroy();
	}
	
	private class PassivationThread extends Thread {
		private File iFolder = null;
		public long iDelay = 30000;
		public boolean iContinue = true;
		
		public PassivationThread(File folder) {
			iFolder = folder;
			setName("Passivation[StudentSectioning]");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}
		
		public void run() {
			try {
				sLog.info("Solver passivation thread started.");
				while (iContinue) {
					for (Map.Entry<String, StudentSolverProxy> entry: iSolvers.entrySet())
						entry.getValue().passivateIfNeeded(iFolder, entry.getKey());
					try {
						sleep(iDelay);
					} catch (InterruptedException e) {
					    break;
	                }
				}
				sLog.info("Solver passivation thread finished.");
			} catch (Exception e) {
				sLog.warn("Solver passivation thread failed, reason: " + e.getMessage(), e);
			}
		}
		
		public void destroy() {
			iContinue = false;
			if (isAlive()) interrupt();
		}
	}
}
