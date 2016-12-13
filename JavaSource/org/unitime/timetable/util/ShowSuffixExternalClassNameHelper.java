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
package org.unitime.timetable.util;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

/**
 * @author Tomas Muller
 *
 */
public class ShowSuffixExternalClassNameHelper extends DefaultExternalClassNameHelper {
	
	@Override
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		String suffix = getClassSuffix(clazz, courseOffering);
		if (suffix != null && !suffix.isEmpty())
			return courseOffering.getCourseName() + " " + clazz.getItypeDesc().trim() + " " + suffix;
		else
			return courseOffering.getCourseName() + " " + clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString();
	}
	
	@Override
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
		String suffix = getClassSuffix(clazz, courseOffering);
		if (suffix != null && !suffix.isEmpty())
			return courseOffering.getCourseNameWithTitle() + " " + clazz.getItypeDesc().trim() + " " + suffix;
		else
			return courseOffering.getCourseNameWithTitle() + " " + clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString();
	}

}
