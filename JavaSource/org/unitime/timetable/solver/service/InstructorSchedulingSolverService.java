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

import java.io.IOException;
import java.io.InputStream;
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
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.jgroups.RemoteSolver;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * @author Tomas Muller
 */
@Service("instructorSchedulingSolverService")
public class InstructorSchedulingSolverService implements SolverService<InstructorSchedulingProxy> {
	protected static Log sLog = LogFactory.getLog(InstructorSchedulingSolverService.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;
	
	@Override
	public DataProperties createConfig(Long settingsId, Map<Long, String> options) {
		DataProperties properties = new DataProperties();
		
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("org/cpsolver/instructor/default.properties");
			if (is != null) properties.load(is);
		} catch (IOException e) {
			sLog.warn("Failed to load configuration defaults:" + e.getMessage());
		}
		
		// Load properties
		for (SolverParameterDef def: (List<SolverParameterDef>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.type = :type").setInteger("type", SolverParameterGroup.SolverType.INSTRUCTOR.ordinal()).list()) {
			if (def.getDefault() != null) properties.put(def.getName(), def.getDefault());
			if (options != null && options.containsKey(def.getUniqueId()))
				properties.put(def.getName(), options.get(def.getUniqueId()));
		}
		
		SolverPredefinedSetting settings = SolverPredefinedSettingDAO.getInstance().get(settingsId);
		for (SolverParameter param: settings.getParameters()) {
			if (!param.getDefinition().isVisible() || param.getDefinition().getGroup().getSolverType() != SolverParameterGroup.SolverType.INSTRUCTOR) continue;
			properties.put(param.getDefinition().getName(),param.getValue());
			if (options != null && options.containsKey(param.getDefinition().getUniqueId()))
				properties.put(param.getDefinition().getName(), options.get(param.getDefinition().getUniqueId()));
		}
		properties.setProperty("General.SettingsId", settings.getUniqueId().toString());
		
		// Generate extensions
		String ext = properties.getProperty("Extensions.Classes", "");
		if (properties.getPropertyBoolean("General.CBS", true)) {
			if (!ext.isEmpty()) ext += ";";
			ext += ConflictStatistics.class.getName();
			properties.setProperty("ConflictStatistics.Print","true");
		}
		
		String mode = properties.getProperty("Basic.Mode","Initial");
        if ("MPP".equals(mode)) 
            properties.setProperty("General.MPP","true");

        properties.setProperty("Extensions.Classes", ext);
        
        // Interactive mode?
        if (properties.getPropertyBoolean("Basic.DisobeyHard", false))
        	properties.setProperty("General.InteractiveMode", "true");
        
        // When finished?
        if ("No Action".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","false");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","false");
        } else if ("Save and Unload".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","true");
        }
        
        // XML save/load properties
        properties.setProperty("Xml.ShowNames", "true");
        
        properties.setProperty("Search.GreatDeluge", ("Great Deluge".equals(properties.getProperty("General.Algorithm","Great Deluge"))?"true":"false"));
        
        // Distances Matrics
        if (properties.getProperty("Distances.Ellipsoid") == null || properties.getProperty("Distances.Ellipsoid").equals("DEFAULT"))
            properties.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty(ApplicationProperty.DistanceEllipsoid));
        
        if (properties.getProperty("Parallel.NrSolvers") == null) {
        	properties.setProperty("Parallel.NrSolvers", "1"); // String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() / 2))
        }
        
        properties.setProperty("General.UseAmPm", CONSTANTS.useAmPm() ? "true" : "false");

        properties.expand();
        
        return properties;
	}

	@Override
	public InstructorSchedulingProxy createSolver(DataProperties properties) {
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
		    
		    InstructorSchedulingProxy solver = solverServerService.createInstructorScheduling(host, sessionContext.getUser().getExternalUserId(), properties);
		    solver.load(properties);
		    return solver;
		} catch (Exception e) {
			sLog.error("Failed to start the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}

	public InstructorSchedulingProxy getSolver(String puid, Long sessionId) {
		try {
			InstructorSchedulingProxy proxy = solverServerService.getInstructorSchedulingContainer().getSolver(puid);
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
	public InstructorSchedulingProxy getSolver() {
		ProxyHolder<String, InstructorSchedulingProxy> h = (ProxyHolder<String, InstructorSchedulingProxy>)sessionContext.getAttribute(SessionAttribute.InstructorSchedulingSolver);
		InstructorSchedulingProxy solver = (h != null && h.isValid() ? h.getProxy() : null);
		if (solver!=null) {
			try {
				if (solver instanceof RemoteSolver && ((RemoteSolver)solver).exists())
					return (InstructorSchedulingProxy)solver;
				else
					sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			} catch (Exception e) {
				sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			};
		}
		if (!sessionContext.isAuthenticated()) return null;
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		if (sessionId == null) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.InstructorSchedulingUser);
		if (puid != null) {
			solver = getSolver(puid, sessionId);
			if (solver!=null) {
				sessionContext.setAttribute(SessionAttribute.InstructorSchedulingSolver, new ProxyHolder<String, InstructorSchedulingProxy>(puid, solver));
				return solver;
			}
		}
		solver = getSolver(sessionContext.getUser().getExternalUserId(), sessionId);
		if (solver!=null)
			sessionContext.setAttribute(SessionAttribute.InstructorSchedulingSolver, new ProxyHolder<String, InstructorSchedulingProxy>(sessionContext.getUser().getExternalUserId(), solver));
		return solver;
	}
	
	@Override
	public InstructorSchedulingProxy getSolverNoSessionCheck() {
		if (!sessionContext.isAuthenticated()) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.InstructorSchedulingUser);
		if (puid!=null) {
			InstructorSchedulingProxy solver = getSolver(puid, null);
			if (solver!=null) return solver;
		}
		return getSolver(sessionContext.getUser().getExternalUserId(), null);
	}

	@Override
	public void removeSolver() {
		try {
			sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			InstructorSchedulingProxy solver = getSolverNoSessionCheck();
			if (solver != null) {
				solver.interrupt();
				solver.dispose();
			}
			sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingUser);
		} catch (Exception e) {
			sLog.warn("Failed to remove a solver: " + e.getMessage(), e);
		}
	}

	@Override
	public InstructorSchedulingProxy reload(DataProperties properties) {
		try {
			InstructorSchedulingProxy solver = getSolver();
			if (solver == null) return createSolver(properties);

			DataProperties oldProperties = solver.getProperties();
			
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			properties.setProperty("General.OwnerPuid", oldProperties.getProperty("General.OwnerPuid"));
			properties.setProperty("General.SolverGroupId",oldProperties.getProperty("General.SolverGroupId"));
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
	public Map<String, InstructorSchedulingProxy> getSolvers() {
		Map<String, InstructorSchedulingProxy> solvers = new HashMap<String, InstructorSchedulingProxy>();
		SolverContainer<InstructorSchedulingProxy> container = solverServerService.getInstructorSchedulingContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
	@Override
	public  Map<String, InstructorSchedulingProxy> getLocalSolvers() {
		Map<String, InstructorSchedulingProxy> solvers = new HashMap<String, InstructorSchedulingProxy>();
		SolverContainer<InstructorSchedulingProxy> container = solverServerService.getLocalServer().getInstructorSchedulingContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
}
