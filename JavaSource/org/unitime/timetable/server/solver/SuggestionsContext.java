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
package org.unitime.timetable.server.solver;

import java.io.Serializable;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class SuggestionsContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String iInstructorNameFormat = NameFormat.SHORT.reference();
	private transient Map<String, String> iCourseObjectives = null; 
	
	public String getInstructorNameFormat() { return iInstructorNameFormat; }
	public void setInstructorNameFormat(String format) { iInstructorNameFormat = format; }
	
	public Map<String, String> courseObjectives() {
		if (iCourseObjectives == null)
			iCourseObjectives = Localization.create(CPSolverMessages.class).courseObjectives();
		return iCourseObjectives;
	}
	
}
