/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.test;

import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

/**
 * Example implementation of the {@link ExternalClassNameHelperInterface} that hides subpart suffixes.
 * 
 * @author Tomas Muller
 */
public class ClassNamesNoSubpartSuffix implements ExternalClassNameHelperInterface {

	@Override
	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
		return clazz.getSectionNumber().toString();
	}

	@Override
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		return courseOffering.getCourseName() + " "
				+ clazz.getItypeDesc().trim() + " "
				+ clazz.getSectionNumber();
	}

	@Override
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
		return courseOffering.getCourseNameWithTitle() + " "
				+ clazz.getItypeDesc().trim() + " "
				+ clazz.getSectionNumber();
	}

	@Override
	public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
		return clazz.getExternalUniqueId();
	}
}
