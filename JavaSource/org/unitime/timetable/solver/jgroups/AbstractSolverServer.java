/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.jgroups;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
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