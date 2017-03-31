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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.criteria.additional.InstructorLunchBreak;
import org.cpsolver.coursett.criteria.additional.InstructorStudentConflict;
import org.cpsolver.coursett.criteria.additional.InstructorStudentHardConflict;
import org.cpsolver.coursett.criteria.additional.RoomSizePenalty;
import org.cpsolver.coursett.custom.DeterministicStudentSectioning;
import org.cpsolver.coursett.heuristics.FixCompleteSolutionNeighbourSelection;
import org.cpsolver.coursett.model.DefaultStudentSectioning;
import org.cpsolver.coursett.sectioning.SctSectioning;
import org.cpsolver.coursett.sectioning.StudentSwapSectioning;
import org.cpsolver.ifs.algorithms.SimpleSearch;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.extension.SearchIntensification;
import org.cpsolver.ifs.extension.ViolatedInitials;
import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.server.solver.SolverPageBackend;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.jgroups.RemoteSolver;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * @author Tomas Muller
 */
@Service("courseTimetablingSolverService")
public class CourseTimetablingSolverService implements SolverService<SolverProxy> {
	protected static Log sLog = LogFactory.getLog(CourseTimetablingSolverService.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;
	
	public SolverProxy getSolver(String puid, Long sessionId) {
		try {
			SolverProxy proxy = solverServerService.getCourseSolverContainer().getSolver(puid);
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
	public SolverProxy getSolver() {
		ProxyHolder<String, SolverProxy> h = (ProxyHolder<String, SolverProxy>)sessionContext.getAttribute(SessionAttribute.CourseTimetablingSolver);
		SolverProxy solver = (h != null && h.isValid() ? h.getProxy() : null);
		if (solver != null) {
			try {
				if (solver instanceof RemoteSolver && ((RemoteSolver)solver).exists())
					return solver;
				else
					sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			} catch (Exception e) {
				sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			};
		}
		if (!sessionContext.isAuthenticated()) return null;
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		if (sessionId == null) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.CourseTimetablingUser);
		if (puid != null) {
			solver = getSolver(puid, sessionId);
			if (solver!=null) {
				sessionContext.setAttribute(SessionAttribute.CourseTimetablingSolver, new ProxyHolder<String, SolverProxy>(puid, solver));
				return solver;
			}
		}
		solver = getSolver(sessionContext.getUser().getExternalUserId(), sessionId);
		if (solver!=null)
			sessionContext.setAttribute(SessionAttribute.CourseTimetablingSolver, new ProxyHolder<String, SolverProxy>(sessionContext.getUser().getExternalUserId(), solver));
		return solver;
	}
	
	@Override
	public SolverProxy getSolverNoSessionCheck() {
		if (!sessionContext.isAuthenticated()) return null;
		String puid = (String)sessionContext.getAttribute(SessionAttribute.CourseTimetablingUser);
		if (puid!=null) {
			SolverProxy solver = getSolver(puid, null);
			if (solver!=null) return solver;
		}
		return getSolver(sessionContext.getUser().getExternalUserId(), null);
	}
	
	@Override
	public void removeSolver() {
		try {
			sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			sessionContext.removeAttribute("Suggestions.model");
			SolverProxy solver = getSolverNoSessionCheck();
			if (solver != null) {
				solver.interrupt();
				solver.dispose();
			}
			sessionContext.removeAttribute(SessionAttribute.CourseTimetablingUser);
		} catch (Exception e) {
			sLog.warn("Failed to remove a solver: " + e.getMessage(), e);
		}
	}

	@Override
	public DataProperties createConfig(Long settingsId, Map<Long, String> options) {
		DataProperties properties = new DataProperties();
		
		// Load properties
		for (SolverParameterDef def: (List<SolverParameterDef>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.type = :type").setInteger("type", SolverParameterGroup.SolverType.COURSE.ordinal()).list()) {
			if (def.getDefault() != null) properties.put(def.getName(), def.getDefault());
			if (options != null && options.containsKey(def.getUniqueId()))
				properties.put(def.getName(), options.get(def.getUniqueId()));
		}
		
		SolverPredefinedSetting settings = SolverPredefinedSettingDAO.getInstance().get(settingsId);
		for (SolverParameter param: settings.getParameters()) {
			if (!param.getDefinition().isVisible() || param.getDefinition().getGroup().getSolverType() != SolverParameterGroup.SolverType.COURSE) continue;
			properties.put(param.getDefinition().getName(),param.getValue());
			if (options != null && options.containsKey(param.getDefinition().getUniqueId()))
				properties.put(param.getDefinition().getName(), options.get(param.getDefinition().getUniqueId()));
		}
		properties.setProperty("General.SettingsId", settings.getUniqueId().toString());
		
		String algorithm = properties.getProperty("General.SearchAlgorithm", "Default");
        if ("Experimental".equalsIgnoreCase(algorithm) || "Deluge".equalsIgnoreCase(algorithm) || "GD".equalsIgnoreCase(algorithm) || "SA".equalsIgnoreCase(algorithm) || "Annealing".equalsIgnoreCase(algorithm)) {
        	properties.setProperty("Neighbour.Class", SimpleSearch.class.getName());
        	properties.setProperty("General.SearchIntensification", "false");
        	properties.setProperty("General.CompleteSolutionFixInterval", "-1");
        	properties.setProperty("General.IncompleteSolutionFixInterval", "-1");
        	if ("Deluge".equalsIgnoreCase(algorithm) || "GD".equalsIgnoreCase(algorithm))
        		properties.setProperty("Search.GreatDeluge", "true");
        	else if ("SA".equalsIgnoreCase(algorithm) || "Annealing".equalsIgnoreCase(algorithm))
        		properties.setProperty("Search.GreatDeluge", "false");
        } else if ("Default".equals(algorithm) || "Legacy".equals(algorithm) || "IFS".equals(algorithm)) {
        	properties.setProperty("Neighbour.Class", FixCompleteSolutionNeighbourSelection.class.getName());
        }
        
		// Generate extensions
		String ext = properties.getProperty("Extensions.Classes", "");
		if (properties.getPropertyBoolean("General.SearchIntensification", true)) {
			if (!ext.isEmpty()) ext += ";";
			ext += SearchIntensification.class.getName();
        }
        if (properties.getPropertyBoolean("General.CBS", true)) {
        	if (!ext.isEmpty()) ext += ";";
			ext += ConflictStatistics.class.getName();
        }
        String mode = properties.getProperty("Basic.Mode","Initial");
        if ("MPP".equals(mode)) {
            properties.setProperty("General.MPP","true");
            if (!ext.isEmpty()) ext += ";";
            ext += ViolatedInitials.class.getName();
        }
        properties.setProperty("Extensions.Classes", ext);
        
        // Interactive mode?
        if (properties.getPropertyBoolean("Basic.DisobeyHard",false))
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
        } else if ("Save as New".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","true");
            properties.setProperty("General.Unload","false");
        } else if ("Save and Unload".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","false");
            properties.setProperty("General.Unload","true");
        } else if ("Save as New and Unload".equals(properties.getProperty("Basic.WhenFinished"))) {
            properties.setProperty("General.Save","true");
            properties.setProperty("General.CreateNewSolution","true");
            properties.setProperty("General.Unload","true");
        }
        
        // XML save/load properties
        properties.setProperty("Xml.ShowNames", "true");
        properties.setProperty("Xml.ExportStudentSectioning", "true");
        
        // Distances Matrics
        if (properties.getProperty("Distances.Ellipsoid") == null || properties.getProperty("Distances.Ellipsoid").equals("DEFAULT"))
            properties.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty(ApplicationProperty.DistanceEllipsoid));
        
        if (properties.getPropertyBoolean("Global.LoadStudentInstructorConflicts", false))
        	properties.setProperty("General.AdditionalCriteria", properties.getProperty("General.AdditionalCriteria") + ";" + InstructorStudentConflict.class.getName() + ";" + InstructorStudentHardConflict.class.getName());
        
        if (properties.getPropertyBoolean("InstructorLunch.Enabled", false))
        	properties.setProperty("General.AdditionalCriteria", properties.getProperty("General.AdditionalCriteria") + ";" + InstructorLunchBreak.class.getName());
        
        if (properties.getPropertyDouble("Comparator.RoomSizeWeight", 0.0) != 0.0)
        	properties.setProperty("General.AdditionalCriteria", properties.getProperty("General.AdditionalCriteria") + ";" + RoomSizePenalty.class.getName());
        
        if (properties.getPropertyBoolean("General.DeterministicStudentSectioning", false)) {
        	properties.setProperty("StudentSectioning.Class", DeterministicStudentSectioning.class.getName());
        } else if ("Deterministic".equalsIgnoreCase(properties.getProperty("General.StudentSectioning"))) {
        	properties.setProperty("StudentSectioning.Class", DeterministicStudentSectioning.class.getName());
        } else if ("Branch & Bound".equalsIgnoreCase(properties.getProperty("General.StudentSectioning"))) {
        	properties.setProperty("StudentSectioning.Class", SctSectioning.class.getName());
        	properties.setProperty("SctSectioning.GroupFirst", "false");
        } else if ("Local-Search".equalsIgnoreCase(properties.getProperty("General.StudentSectioning")) || "Local Search".equalsIgnoreCase(properties.getProperty("General.StudentSectioning"))) {
        	properties.setProperty("StudentSectioning.Class", StudentSwapSectioning.class.getName());
        } else if ("B&B Groups".equalsIgnoreCase(properties.getProperty("General.StudentSectioning"))) {
        	properties.setProperty("StudentSectioning.Class", SctSectioning.class.getName());
        	properties.setProperty("SctSectioning.GroupFirst", "true");
        } else if ("Legacy".equalsIgnoreCase(properties.getProperty("General.StudentSectioning")) || "Default".equalsIgnoreCase(properties.getProperty("General.StudentSectioning"))) {
        	properties.setProperty("StudentSectioning.Class", DefaultStudentSectioning.class.getName());
        }
        
        if (properties.getProperty("Parallel.NrSolvers") == null) {
        	properties.setProperty("Parallel.NrSolvers", String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)));
        }
        if (properties.getPropertyBoolean("OnFlySectioning.Enabled", false)) {
        	properties.setProperty("Parallel.NrSolvers", "1");
        }
        
        properties.setProperty("General.UseAmPm", CONSTANTS.useAmPm() ? "true" : "false");

        properties.expand();
                
        return properties;
	}
	
	private String getSolverWarning(DataProperties config) {
		String warn = "";
		int maxDistPriority = Integer.MIN_VALUE;
		int nrWarns = 0;
		Long sessionId = config.getPropertyLong("General.SessionId",null); 
		Long[] solverGroupIds = config.getPropertyLongArry("General.SolverGroupId", null);
		Set<SolverGroup> solverGroups = new HashSet<SolverGroup>();
		for (Long solverGroupId: solverGroupIds) {
			SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId);
			if (!sg.getSession().getUniqueId().equals(sessionId)) continue;
			maxDistPriority = Math.max(maxDistPriority, sg.getMaxDistributionPriority());
			solverGroups.add(sg);
		}
		
		for (SolverGroup sg: SolverGroup.findBySessionId(sessionId)) {
			if (solverGroups.contains(sg)) continue;
			if (sg.getMinDistributionPriority() < maxDistPriority && sg.getCommittedSolution() == null) {
				if (nrWarns>0) warn += "<BR>";
				List<String> subjects = new ArrayList<String>();
				boolean dept = false;
				for (Department d: sg.getDepartments()) {
					if (d.isExternalManager().booleanValue()) {
						subjects.add(d.getExternalMgrAbbv());
					} else {
						dept = true;
						for (SubjectArea sa: d.getSubjectAreas()) {
							subjects.add(sa.getSubjectAreaAbbreviation());
						}
					}
				}
				if (dept)
					warn += MESSAGES.warnSolverNoCommittedSolutionDepartmental(sg.getAbbv(), SolverPageBackend.toString(subjects));
				else
					warn += MESSAGES.warnSolverNoCommittedSolutionExternal(sg.getAbbv(), SolverPageBackend.toString(subjects));
				nrWarns++;
			}
			if (nrWarns >= 3) {
				warn += "<BR>...";
				break;
			}
		}
		
		return (warn.isEmpty() ? null : warn);
	}
	
	@Override
	public SolverProxy createSolver(DataProperties properties) {
		try {
			if (!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAcademicSessionId() == null) return null;
			
			removeSolver();
				
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			// properties.setProperty("General.SolverGroupId", ownerIds);
			properties.setProperty("General.OwnerPuid", sessionContext.getUser().getExternalUserId());
			// if (solutionIds!=null) properties.setProperty("General.SolutionId",solutionIds);
			properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));
		    // properties.setProperty("General.StartSolver", Boolean.toString(start));
			
			String host = properties.getProperty("General.Host");
		    
	        String warn = getSolverWarning(properties);
	        if (warn!=null) 
	        	properties.setProperty("General.SolverWarnings",warn);
	        else
	        	properties.remove("General.SolverWarnings");
		    
		    String instructorFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
		    if (instructorFormat != null)
		    	properties.setProperty("General.InstructorFormat",instructorFormat);
		    
		    SolverProxy solver = solverServerService.createCourseSolver(host, sessionContext.getUser().getExternalUserId(), properties);
		    solver.load(properties);
		    
			return solver;
		} catch (Exception e) {
			sLog.error("Failed to start the solver: " + e.getMessage(), e);
			throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
		}
	}
	
	@Override
	public SolverProxy reload(DataProperties properties) {
		try {
			SolverProxy solver = getSolver();
			if (solver == null) return createSolver(properties);

			DataProperties oldProperties = solver.getProperties();
			
			properties.setProperty("General.SessionId", sessionContext.getUser().getCurrentAcademicSessionId().toString());
			properties.setProperty("General.SolverGroupId",oldProperties.getProperty("General.SolverGroupId"));
			properties.setProperty("General.OwnerPuid", oldProperties.getProperty("General.OwnerPuid"));
			properties.setProperty("General.StartTime", String.valueOf((new Date()).getTime()));

	        String warn = getSolverWarning(properties);
	        if (warn!=null) 
	        	properties.setProperty("General.SolverWarnings",warn);
	        else
	        	properties.remove("General.SolverWarnings");

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
	public Map<String, SolverProxy> getSolvers() {
		Map<String,SolverProxy> solvers = new HashMap<String, SolverProxy>();
		SolverContainer<SolverProxy> container = solverServerService.getCourseSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}
	
	public Map<String, SolverProxy> getLocalSolvers() {
		Map<String,SolverProxy> solvers = new HashMap<String, SolverProxy>();
		SolverContainer<SolverProxy> container = solverServerService.getLocalServer().getCourseSolverContainer(); 
		for (String user: container.getSolvers())
			solvers.put(user, container.getSolver(user));
		return solvers; 
	}

}
