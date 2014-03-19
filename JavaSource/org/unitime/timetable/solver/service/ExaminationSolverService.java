/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.util.DataProperties;
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
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.jgroups.RemoteSolver;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * @author Tomas Muller
 */
@Service("examinationSolverService")
public class ExaminationSolverService implements SolverService<ExamSolverProxy> {
	protected static Log sLog = LogFactory.getLog(ExaminationSolverService.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;
	
	@Override
	public DataProperties createConfig(Long settingsId, Map<Long, String> options) {
		DataProperties properties = new DataProperties();
		
		// Load properties
		for (SolverParameterDef def: (List<SolverParameterDef>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.type = :type").setInteger("type", SolverParameterGroup.sTypeExam).list()) {
			if (def.getDefault() != null) properties.put(def.getName(), def.getDefault());
			if (options != null && options.containsKey(def.getUniqueId()))
				properties.put(def.getName(), options.get(def.getUniqueId()));
		}
		
		SolverPredefinedSetting settings = SolverPredefinedSettingDAO.getInstance().get(settingsId);
		for (SolverParameter param: settings.getParameters()) {
			if (!param.getDefinition().isVisible() || param.getDefinition().getGroup().getType() != SolverParameterGroup.sTypeExam) continue;
			properties.put(param.getDefinition().getName(),param.getValue());
			if (options != null && options.containsKey(param.getDefinition().getUniqueId()))
				properties.put(param.getDefinition().getName(), options.get(param.getDefinition().getUniqueId()));
		}
		properties.setProperty("General.SettingsId", settings.getUniqueId().toString());
		
		// Generate extensions
		String ext = properties.getProperty("Extensions.Classes", "");
		if (properties.getPropertyBoolean("ExamGeneral.CBS", true)) {
			if (!ext.isEmpty()) ext += ";";
			ext += ConflictStatistics.class.getName();
			properties.setProperty("ConflictStatistics.Print","true");
		}
		
		String mode = properties.getProperty("ExamBasic.Mode","Initial");
        if ("MPP".equals(mode)) 
            properties.setProperty("General.MPP","true");

        properties.setProperty("Extensions.Classes", ext);
        
        // Interactive mode?
        if (properties.getPropertyBoolean("Basic.DisobeyHard",false))
        	properties.setProperty("General.InteractiveMode", "true");
        
        // When finished?
        if ("No Action".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","false");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save and Unload".equals(properties.getProperty("ExamBasic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","true");
        }
        
        // XML save/load properties
        properties.setProperty("Xml.ShowNames", "true");
        
        properties.setProperty("Exam.GreatDeluge", ("Great Deluge".equals(properties.getProperty("Exam.Algorithm","Great Deluge"))?"true":"false"));
        properties.setProperty("Search.GreatDeluge", ("Great Deluge".equals(properties.getProperty("Exam.Algorithm","Great Deluge"))?"true":"false"));
        
        // Distances Matrics
        if (properties.getProperty("Distances.Ellipsoid") == null || properties.getProperty("Distances.Ellipsoid").equals("DEFAULT"))
            properties.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty(ApplicationProperty.DistanceEllipsoid));
        
        if (properties.getProperty("Parallel.NrSolvers") == null) {
        	properties.setProperty("Parallel.NrSolvers", String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)));
        }

        properties.expand();
        
        return properties;
	}

	@Override
	public ExamSolverProxy createSolver(DataProperties properties) {
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
		    
		    ExamSolverProxy solver = solverServerService.createExamSolver(host, sessionContext.getUser().getExternalUserId(), properties);
		    solver.load(properties);
		    return solver;
		} catch (Exception e) {
			sLog.error("Failed to start the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}

	public ExamSolverProxy getSolver(String puid, Long sessionId) {
		try {
			ExamSolverProxy proxy = solverServerService.getExamSolverContainer().getSolver(puid);
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
	public ExamSolverProxy getSolver() {
		ExamSolverProxy solver = (ExamSolverProxy)sessionContext.getAttribute(SessionAttribute.ExaminationSolver);
		if (solver!=null) {
			try {
				if (solver instanceof RemoteSolver && ((RemoteSolver)solver).exists())
					return (ExamSolverProxy)solver;
				else
					sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
			} catch (Exception e) {
				sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
			};
		}
		if (!sessionContext.isAuthenticated()) return null;
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		if (sessionId == null) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.ExaminationUser);
		if (puid != null) {
			solver = getSolver(puid, sessionId);
			if (solver!=null) {
				sessionContext.setAttribute(SessionAttribute.ExaminationSolver, solver);
				return solver;
			}
		}
		solver = getSolver(sessionContext.getUser().getExternalUserId(), sessionId);
		if (solver!=null)
			sessionContext.setAttribute(SessionAttribute.ExaminationSolver, solver);
		return solver;
	}
	
	@Override
	public ExamSolverProxy getSolverNoSessionCheck() {
		if (!sessionContext.isAuthenticated()) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.ExaminationUser);
		if (puid!=null) {
			ExamSolverProxy solver = getSolver(puid, null);
			if (solver!=null) return solver;
		}
		return getSolver(sessionContext.getUser().getExternalUserId(), null);
	}

	@Override
	public void removeSolver() {
		try {
			sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
			ExamSolverProxy solver = getSolverNoSessionCheck();
			if (solver != null) {
				solver.interrupt();
				solver.dispose();
			}
			sessionContext.removeAttribute(SessionAttribute.ExaminationUser);
		} catch (Exception e) {
			sLog.warn("Failed to remove a solver: " + e.getMessage(), e);
		}
	}

	@Override
	public ExamSolverProxy reload(DataProperties properties) {
		try {
			ExamSolverProxy solver = getSolver();
			if (solver == null) return createSolver(properties);

			DataProperties oldProperties = solver.getProperties();
			
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			properties.setProperty("Exam.Type",oldProperties.getProperty("Exam.Type"));
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
	public Map<String, ExamSolverProxy> getSolvers() {
		Map<String, ExamSolverProxy> solvers = new HashMap<String, ExamSolverProxy>();
		SolverContainer<ExamSolverProxy> container = solverServerService.getExamSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
	@Override
	public  Map<String, ExamSolverProxy> getLocalSolvers() {
		Map<String, ExamSolverProxy> solvers = new HashMap<String, ExamSolverProxy>();
		SolverContainer<ExamSolverProxy> container = solverServerService.getLocalServer().getExamSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
}
