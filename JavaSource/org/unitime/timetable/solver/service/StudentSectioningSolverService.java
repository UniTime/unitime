/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.solver.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.StudentConflictStatistics;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.weights.StudentWeights;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.jgroups.RemoteSolver;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@Service("studentSectioningSolverService")
public class StudentSectioningSolverService implements SolverService<StudentSolverProxy> {
	protected static Log sLog = LogFactory.getLog(StudentSectioningSolverService.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;

	@Override
	public DataProperties createConfig(Long settingsId, Map<Long, String> options) {
		DataProperties properties = new DataProperties();
		
		// Load properties
		for (SolverParameterDef def: (List<SolverParameterDef>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.type = :type").setInteger("type", SolverParameterGroup.SolverType.STUDENT.ordinal()).list()) {
			if (def.getDefault() != null) properties.put(def.getName(), def.getDefault());
			if (options != null && options.containsKey(def.getUniqueId()))
				properties.put(def.getName(), options.get(def.getUniqueId()));
		}
		
		SolverPredefinedSetting settings = SolverPredefinedSettingDAO.getInstance().get(settingsId);
		for (SolverParameter param: settings.getParameters()) {
			if (!param.getDefinition().isVisible() || param.getDefinition().getGroup().getSolverType() != SolverParameterGroup.SolverType.STUDENT) continue;
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
        if (properties.getPropertyBoolean("StudentSct.ScheduleQuality",true)) {
        	if (!ext.isEmpty()) ext += ";";
			ext += StudentQuality.class.getName();
        } else {
        	if (properties.getPropertyBoolean("StudentSct.StudentDist",true)) {
            	if (!ext.isEmpty()) ext += ";";
    			ext += DistanceConflict.class.getName();
            }
            if (properties.getPropertyBoolean("StudentSct.TimeOverlaps",true)) {
            	if (!ext.isEmpty()) ext += ";";
    			ext += TimeOverlapsCounter.class.getName();
            }
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
        
        if (properties.getProperty("Parallel.NrSolvers") == null) {
        	properties.setProperty("Parallel.NrSolvers", String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)));
        }
        
        properties.setProperty("General.UseAmPm", CONSTANTS.useAmPm() ? "true" : "false");

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
		    
		    StudentSolverProxy solver = solverServerService.createStudentSolver(host, sessionContext.getUser().getExternalUserId(), properties);
		    solver.load(properties);
		    
	    	return solver;
		} catch (Exception e) {
			sLog.error("Failed to start the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}
	
	public StudentSolverProxy publishSolver(Long id, DataProperties properties, byte[] backup) {
		try {
			if (!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAcademicSessionId() == null) return null;
			if (backup == null) return null;
			
			sessionContext.removeAttribute(SessionAttribute.StudentSectioningUser);
			StudentSolverProxy oldSolver = getSolver("PUBLISHED_" + properties.getProperty("General.SessionId"), null);
			if (oldSolver != null)
				oldSolver.dispose();
			
			DataProperties config = new DataProperties(properties.toMap());
			String host = config.getProperty("General.Host");
			config.setProperty("StudentSct.Published", String.valueOf((new Date()).getTime()));
			config.setProperty("StudentSct.PublishId", id == null ? null : id.toString());
			config.setProperty("General.OwnerPuid", "PUBLISHED_" + config.getProperty("General.SessionId"));
		    
		    StudentSolverProxy solver = solverServerService.createStudentSolver(host, "PUBLISHED_" + config.getProperty("General.SessionId"), config);
		    if (!solver.restoreXml(backup)) {
		    	solver.dispose();
		    	return null;
		    }
		    
	    	return solver;
		} catch (Exception e) {
			sLog.error("Failed to publish the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}
	
	public StudentSolverProxy createSolver(DataProperties properties, byte[] backup) {
		try {
			if (!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAcademicSessionId() == null) return null;
			if (backup == null) return null;
			
			removeSolver();
			
			DataProperties config = new DataProperties(properties.toMap());
			config.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			config.setProperty("General.OwnerPuid", sessionContext.getUser().getExternalUserId());
			config.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
			config.remove("StudentSct.Published");
			
			String host = config.getProperty("General.Host");
		    
		    String instructorFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
		    if (instructorFormat != null)
		    	config.setProperty("General.InstructorFormat",instructorFormat);
		    
		    StudentSolverProxy solver = solverServerService.createStudentSolver(host, sessionContext.getUser().getExternalUserId(), config);
		    if (!solver.restoreXml(backup)) {
		    	solver.dispose();
		    	return null;
		    }
		    
	    	return solver;
		} catch (Exception e) {
			sLog.error("Failed to clone the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}

	public StudentSolverProxy getSolver(String puid, Long sessionId) {
		try {
			StudentSolverProxy proxy = solverServerService.getStudentSolverContainer().getSolver(puid);
			if (proxy == null) return null;
			if (sessionId != null && !sessionId.equals(proxy.getProperties().getPropertyLong("General.SessionId",null))) 
	            return null;
	        return proxy;
		} catch (Exception e) {
			sLog.error("Unable to retrieve solver, reason:"+e.getMessage(),e);
		}
		return null;
	}

	@Override
	public StudentSolverProxy getSolver() {
		ProxyHolder<String, StudentSolverProxy> h = (ProxyHolder<String, StudentSolverProxy>)sessionContext.getAttribute(SessionAttribute.StudentSectioningSolver);
		StudentSolverProxy solver = (h != null && h.isValid() ? h.getProxy() : null);
		if (solver!=null) {
			try {
				if (solver instanceof RemoteSolver && ((RemoteSolver)solver).exists())
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
				sessionContext.setAttribute(SessionAttribute.StudentSectioningSolver, new ProxyHolder<String, StudentSolverProxy>(puid, solver));
				return solver;
			}
		}
		solver = getSolver(sessionContext.getUser().getExternalUserId(), sessionId);
		if (solver!=null)
			sessionContext.setAttribute(SessionAttribute.StudentSectioningSolver, new ProxyHolder<String, StudentSolverProxy>(sessionContext.getUser().getExternalUserId(), solver));
		if (solver == null) {
			solver = getSolver("PUBLISHED_" + sessionId, sessionId);
			if (solver!=null)
				sessionContext.setAttribute(SessionAttribute.StudentSectioningSolver, new ProxyHolder<String, StudentSolverProxy>("PUBLISHED_" + sessionId, solver));
		}
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
		Map<String, StudentSolverProxy> solvers = new HashMap<String, StudentSolverProxy>();
		SolverContainer<StudentSolverProxy> container = solverServerService.getStudentSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
	@Override
	public Map<String, StudentSolverProxy> getLocalSolvers() {
		Map<String, StudentSolverProxy> solvers = new HashMap<String, StudentSolverProxy>();
		SolverContainer<StudentSolverProxy> container = solverServerService.getLocalServer().getStudentSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
}
