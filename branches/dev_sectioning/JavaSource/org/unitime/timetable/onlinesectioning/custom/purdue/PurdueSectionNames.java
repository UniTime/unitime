/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CustomSectionNames;

/**
 * @author Tomas Muller
 */
public class PurdueSectionNames implements CustomSectionNames {

	public String getClassSuffix(Long sessionId, Long courseId, Long classId) {
		Class_ clazz = Class_DAO.getInstance().get(classId);
		return clazz.getClassSuffix(CourseOfferingDAO.getInstance().get(courseId));
	}
	
	public void update(AcademicSessionInfo session) {	
	}
}
