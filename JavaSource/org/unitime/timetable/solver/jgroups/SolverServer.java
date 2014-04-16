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

import org.jgroups.Address;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
public interface SolverServer {
	public static final short SCOPE_SERVER = 0, SCOPE_COURSE = 1, SCOPE_EXAM = 2, SCOPE_STUDENT = 3, SCOPE_AVAILABILITY = 4, SCOPE_ONLINE = 5;

	public boolean isLocal();
	
	public boolean isCoordinator();
	
	public Address getAddress();
	
	public Address getLocalAddress();
	
	public String getHost();
	
	public Date getStartTime();
	
	public int getUsage();
	
	public String getVersion();
	
	public void setUsageBase(int usage);
	
	public long getAvailableMemory();
	
	public int getAvailableProcessors();
	
	public long getMemoryLimit();
	
	public boolean isActive();
	
	public boolean isAvailable();
	
	public void shutdown();
	
	public SolverContainer<SolverProxy> getCourseSolverContainer();
	
	public SolverContainer<ExamSolverProxy> getExamSolverContainer();
	
	public SolverContainer<StudentSolverProxy> getStudentSolverContainer();
	
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer();
	
	public RoomAvailabilityInterface getRoomAvailability();
	
	public void refreshCourseSolution(Long... solutionId);
	
	public void refreshExamSolution(Long sessionId, Long examTypeId);
	
	public void setApplicationProperty(Long sessionId, String key, String value);
	
	public void setLoggingLevel(String name, Integer level);
	
	public void reset();
}
