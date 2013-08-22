/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver;

import java.util.Collection;
import java.util.Hashtable;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public interface ClassAssignmentProxy {
	public Assignment getAssignment(Long classId) throws Exception;
	public Assignment getAssignment(Class_ clazz) throws Exception;
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) throws Exception;
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception;
	
	public Hashtable getAssignmentTable(Collection classesOrClassIds) throws Exception;
	public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) throws Exception;
}
