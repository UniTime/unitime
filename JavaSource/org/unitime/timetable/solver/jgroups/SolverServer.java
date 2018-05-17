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

import org.jgroups.Address;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.queue.QueueProcessor;

/**
 * @author Tomas Muller
 */
public interface SolverServer {
	public static final short SCOPE_SERVER = 0, SCOPE_COURSE = 1, SCOPE_EXAM = 2, SCOPE_STUDENT = 3, SCOPE_AVAILABILITY = 4, SCOPE_ONLINE = 5, SCOPE_INSTRUCTOR = 6, SCOPE_QUEUE_PROCESSOR = 7;
	
	public void start();
	
	public void stop();

	public boolean isLocal();
	
	public boolean isCoordinator();
	
	public boolean isLocalCoordinator();
	
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
	
	public SolverContainer<InstructorSchedulingProxy> getInstructorSchedulingContainer();
	
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer();
	
	public RoomAvailabilityInterface getRoomAvailability();
	
	public void refreshCourseSolution(Long... solutionId);
	
	public void refreshExamSolution(Long sessionId, Long examTypeId);
	
	public void refreshInstructorSolution(Collection<Long> solverGroupIds);
	
	public void setApplicationProperty(Long sessionId, String key, String value);
	
	public void setLoggingLevel(String name, Integer level);
	
	public void reset();
	
	public List<SolverServer> getServers(boolean onlyAvailable);
	
	public SolverServer crateServerProxy(Address address);
	
	public QueueProcessor getQueueProcessor();
}
