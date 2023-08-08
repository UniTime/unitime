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
package org.unitime.timetable.solver;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public interface ClassAssignmentProxy {
	public AssignmentInfo getAssignment(Long classId);
	public AssignmentInfo getAssignment(Class_ clazz);
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId);
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz);
	
	public Map<Long, AssignmentInfo> getAssignmentTable(Collection classesOrClassIds);
	public Map<Long, AssignmentPreferenceInfo> getAssignmentInfoTable(Collection classesOrClassIds);
	
	public boolean hasConflicts(Long offeringId);
	public Set<AssignmentInfo> getConflicts(Long classId);
	public Set<TimeBlock> getConflictingTimeBlocks(Long classId);
	
	public static interface AssignmentInfo extends Serializable {
		public Long getUniqueId();
		
		public Long getClassId();
		public String getClassName();
		public Class_ getClazz();
		
		public Integer getDays();
		public Integer getStartSlot();
		public int getSlotPerMtg();
		public TimePattern getTimePattern();
		public DatePattern getDatePattern();
		public TimeLocation getTimeLocation();
		public List<RoomLocation> getRoomLocations();

		public Set<Location> getRooms();
		public Placement getPlacement();

		public boolean isCommitted();
		public boolean overlaps(AssignmentInfo a);
	}
}
