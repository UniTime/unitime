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
package org.unitime.timetable.solver.jgroups;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.jgroups.Address;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public abstract class AbstractSolverServer implements SolverServer {
	protected static Log sLog = LogFactory.getLog(AbstractSolverServer.class);
	
	protected int iUsageBase = 0;
	protected Date iStartTime = new Date();
	protected boolean iActive = false;

	public AbstractSolverServer() {
	}
	
	@Override
	public void start() {
		iActive = true;
		sLog.info("Solver server is up and running.");
	}
	
	@Override
	public void stop() {
		sLog.info("Solver server is going down...");
		iActive = false;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public boolean isCoordinator() {
		return true;
	}

	@Override
	public Address getAddress() {
		return null;
	}

	@Override
	public Address getLocalAddress() {
		return getAddress();
	}

	@Override
	public String getHost() {
		return "local";
	}

	@Override
	public int getUsage() {
		int ret = iUsageBase;
		if (isLocal()) ret += 500;
		return ret;
	}
	
	@Override
	public void setUsageBase(int base) {
		iUsageBase = base;
	}

	@Override
	public long getAvailableMemory() {
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
	}
	
	@Override
	public int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	@Override
	public long getMemoryLimit() {
		return 1024l * 1024l * Long.parseLong(ApplicationProperties.getProperty(ApplicationProperty.SolverMemoryLimit));
	}
	
	@Override
	public String getVersion() {
		return Constants.getVersion();
	}
	
	public Date getStartTime() {
		return iStartTime;
	}
	
	@Override
	public boolean isActive() {
		return iActive;
	}
	
	@Override
	public boolean isAvailable() {
		if (!isActive()) return false;
		if (getMemoryLimit() > getAvailableMemory()) System.gc();
		return getMemoryLimit() <= getAvailableMemory();
	}
	
	@Override
	public RoomAvailabilityInterface getRoomAvailability() {
		return RoomAvailability.getInstance();
	}
	
	@Override
	public void refreshCourseSolution(Long... solutionIds) {
		try {
			for (Long solutionId: solutionIds)
				Solution.refreshSolution(solutionId);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public void refreshExamSolution(Long sessionId, Long examTypeId) {
		try {
			ExamType.refreshSolution(sessionId, examTypeId);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public void refreshInstructorSolution(Collection<Long> solverGroupIds) {
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		try {
			SessionFactory hibSessionFactory = hibSession.getSessionFactory();
	    	List<Long> classIds = (List<Long>)hibSession.createQuery(
	    			"select distinct c.uniqueId from Class_ c inner join c.teachingRequests r where c.controllingDept.solverGroup.uniqueId in :solverGroupId and c.cancelled = false")
	    			.setParameterList("solverGroupId", solverGroupIds).list();
	    	for (Long classId: classIds) {
	            hibSessionFactory.getCache().evictEntity(Class_.class, classId);
	            hibSessionFactory.getCache().evictCollection(Class_.class.getName()+".classInstructors", classId);
	    	}
	    	List<Long> instructorIds = (List<Long>)hibSession.createQuery(
	    			"select i.uniqueId from DepartmentalInstructor i, SolverGroup g inner join g.departments d where " +
	    			"g.uniqueId in :solverGroupId and i.department = d"
	    			).setParameterList("solverGroupId", solverGroupIds).list();
	    	for (Long instructorId: instructorIds) {
	            hibSessionFactory.getCache().evictEntity(DepartmentalInstructor.class, instructorId);
	            hibSessionFactory.getCache().evictCollection(DepartmentalInstructor.class.getName()+".classes", instructorId);
	    	}
		} finally {
			hibSession.close();
		}
	}


	@Override
	public void setApplicationProperty(Long sessionId, String key, String value) {
		sLog.info("Set " + key + " to " + value + (sessionId == null ? "" : " (for session " + sessionId + ")"));
		Properties properties = (sessionId == null ? ApplicationProperties.getConfigProperties() : ApplicationProperties.getSessionProperties(sessionId));
		if (properties == null) return;
		if (value == null)
			properties.remove(key);
		else
			properties.setProperty(key, value);
	}

	@Override
	public void setLoggingLevel(String name, Integer level) {
		sLog.info("Set logging level for " + (name == null ? "root" : name) + " to " + (level == null ? "null" : Level.toLevel(level)));
		Logger logger = (name == null ? Logger.getRootLogger() : Logger.getLogger(name));
		if (level == null)
			logger.setLevel(null);
		else
			logger.setLevel(Level.toLevel(level));
	}

	@Override
	public void reset() {
	}
}